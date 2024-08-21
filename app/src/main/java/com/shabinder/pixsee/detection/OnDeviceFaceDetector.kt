@file:OptIn(ExperimentalCoroutinesApi::class)

package com.shabinder.pixsee.detection

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.BaseOptions.DelegateOptions.CpuOptions
import com.google.mediapipe.tasks.core.BaseOptions.DelegateOptions.GpuOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.shabinder.pixsee.detection.DetectionDelegateConfig.Companion.CPU_CONFIG
import com.shabinder.pixsee.detection.FaceDetectionModels.FaceDetectionShortRange
import com.shabinder.pixsee.detection.FaceDetector.DetectionResult
import com.shabinder.pixsee.detection.OnDeviceFaceDetectorHelper.Companion.THRESHOLD_DEFAULT
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext


sealed class FaceDetectionModels(val modelName: String) {
    data object FaceDetectionShortRange : FaceDetectionModels("face_detection_short_range.tflite")
}

data class FaceDetectionConfig(
    val model: FaceDetectionModels = FaceDetectionShortRange,
    val threshold: Float = THRESHOLD_DEFAULT,
    val delegateConfig: DetectionDelegateConfig = CPU_CONFIG,
)

data class DetectionDelegateConfig(
    val currentDelegate: Delegate,
    val delegateOptions: BaseOptions.DelegateOptions,
) {
    companion object {
        val CPU_CONFIG = DetectionDelegateConfig(
            Delegate.CPU,
            CpuOptions.builder().build()
        )
        val GPU_CONFIG = DetectionDelegateConfig(
            Delegate.GPU,
            GpuOptions.builder().build()
        )
    }
}

class OnDeviceFaceDetectorHelper(
    private val context: Context,
    private val config: FaceDetectionConfig,
    private val dispatcher: CoroutineDispatcher // This can configure and limit parallelism
) : FaceDetector {

    private val faceDetectorState: State by lazy {
        buildDetector().fold(
            onSuccess = { State.Initialized(it) },
            onFailure = { State.Error(it.message ?: "Unknown error") }
        )
    }

    @Volatile
    private var isClosed = false

    private fun buildDetector() = runCatching {
        val baseOptions = BaseOptions.builder().run {
            setModelAssetPath(config.model.modelName)
            with(config.delegateConfig) {
                setDelegate(currentDelegate)
                setDelegateOptions(delegateOptions)
            }
            build()
        }

        val options =
            com.google.mediapipe.tasks.vision.facedetector.FaceDetector.FaceDetectorOptions.builder()
                .run {
                    setBaseOptions(baseOptions)
                    setMinDetectionConfidence(config.threshold)
                    setRunningMode(RunningMode.IMAGE)
                    build()
                }

        com.google.mediapipe.tasks.vision.facedetector.FaceDetector.createFromOptions(
            context,
            options
        )
    }


    /* Caller Should bear responsibility of checking if there is any error in the result */
    override suspend fun detectImage(image: Bitmap): DetectionResult = withContext(dispatcher) {
        val state = faceDetectorState
        if (state !is State.Initialized)
            return@withContext DetectionResult.Error("Face detector init failed.")

        val startTime = SystemClock.uptimeMillis()

        val mpImage = BitmapImageBuilder(image).build()

        val detectionResult = state.faceDetector.detect(mpImage)
            ?: return@withContext DetectionResult.Error("Face detection failed.")

        DetectionResult.Success(
            results = detectionResult.detections().map {
                val box = it.boundingBox()
                FaceDetector.FaceMatch(
                    positionLeft = box.left,
                    positionTop = box.top,
                    positionRight = box.right,
                    positionBottom = box.bottom,
                )
            },
            inferenceTime = SystemClock.uptimeMillis() - startTime,
            inputImageHeight = image.height,
            inputImageWidth = image.width
        )
    }

    sealed interface State {
        data class Initialized(val faceDetector: com.google.mediapipe.tasks.vision.facedetector.FaceDetector) :
            State

        data class Error(val message: String) : State
    }

    override fun close() {
        (faceDetectorState as? State.Initialized)
            ?.faceDetector?.close()

        isClosed = true
    }

    fun isClosed(): Boolean {
        return isClosed
    }

    companion object {
        const val THRESHOLD_DEFAULT = 0.5F
        const val OTHER_ERROR = 0
        const val GPU_ERROR = 1

        const val TAG = "FaceDetectorHelper"
    }
}
package com.shabinder.pixsee.detection

import android.graphics.Bitmap

interface FaceDetector : AutoCloseable {
    suspend fun detectImage(image: Bitmap): DetectionResult

    sealed interface DetectionResult {
        data class Success(
            val results: List<FaceMatch>,
            val inferenceTime: Long,
            val inputImageHeight: Int,
            val inputImageWidth: Int,
        ) : DetectionResult

        data class Error(val message: String) : DetectionResult
    }

    data class FaceMatch(
        val positionLeft: Float,
        val positionTop: Float,
        val positionRight: Float,
        val positionBottom: Float,
    )
}
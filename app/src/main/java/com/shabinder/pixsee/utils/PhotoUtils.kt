package com.shabinder.pixsee.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.shabinder.pixsee.db.entity.FaceInfo
import com.shabinder.pixsee.db.entity.PhotoData
import com.shabinder.pixsee.detection.FaceDetector
import com.shabinder.pixsee.detection.FaceDetector.DetectionResult
import java.io.InputStream


fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    return BitmapFactory.decodeStream(inputStream)
}

suspend fun FaceDetector.extractFaceInfos(photo: PhotoData, context: Context): List<FaceInfo> {
    Log.i("FaceDetection", "Extracting: ${photo.uri}")
    val bitmap = loadBitmapFromUri(context, Uri.parse(photo.uri))

    return when (val detectionResult = detectImage(bitmap)) {
        is DetectionResult.Error -> {
            Log.e("FaceDetection", "Error: ${detectionResult.message} ${photo.uri}")
            emptyList()
        }

        is DetectionResult.Success -> {
            Log.i("FaceDetection", "Success: ${detectionResult.results.size} ${photo.uri}")
            detectionResult.results.map {
                FaceInfo(
                    tag = "Unknown",
                    positionLeft = it.positionLeft,
                    positionTop = it.positionTop,
                    positionRight = it.positionRight,
                    positionBottom = it.positionBottom,
                    photoUri = photo.uri
                )
            }
        }
    }
}
package com.shabinder.pixsee.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.shabinder.pixsee.MainViewModel
import com.shabinder.pixsee.PhotoResult
import com.shabinder.pixsee.db.entity.FaceInfo
import com.shabinder.pixsee.db.entity.PhotoWithFaces
import com.shabinder.pixsee.db.entity.updatedWithFaceTag
import com.shabinder.pixsee.ui.screens.components.FaceBoundingBox
import com.shabinder.pixsee.ui.screens.components.TagPopup

@Composable
fun PhotoTagScreenUI(
    uri: String,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val photoData by produceState<PhotoResult>(PhotoResult.Loading, uri) {
        value = viewModel.getPhotoDataWithFaces(uri)
    }

    Box(modifier = modifier) {
        IconButton(
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
            onClick = onBack
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        PhotoTagScreen(
            result = photoData,
            onTagUpdate = { updatedPhoto ->
                viewModel.updatePhoto(updatedPhoto)
            }
        )
    }
}

@Composable
fun PhotoTagScreen(
    result: PhotoResult,
    onTagUpdate: (PhotoWithFaces) -> Unit
) {
    when (result) {
        is PhotoResult.Success -> {
           PhotoScreenWithBoxes(result.photo, onTagUpdate)
        }

        is PhotoResult.Loading -> {
            CircularProgressIndicator()
        }

        is PhotoResult.Error -> {
            Text(text = result.message)
        }
    }
}

@Composable
fun PhotoScreenWithBoxes(photo: PhotoWithFaces, onTagUpdate: (PhotoWithFaces) -> Unit) {
    var data by remember(photo) { mutableStateOf(photo) }

    Box(modifier = Modifier) {
        Image(
            painter = rememberAsyncImagePainter(data.photoData.uri),
            contentDescription = null,
            contentScale = ContentScale.Fit
        )


        var faceBeingTagged by remember { mutableStateOf<FaceInfo?>(null) }
        // Overlay bounding boxes for each detected face
        data.faceInfos.forEach { face ->
            FaceBoundingBox(
                face = face,
                imageWidth = data.photoData.width,
                imageHeight = data.photoData.height,
                onClick = {
                    faceBeingTagged = face
                }
            )
        }

        if (faceBeingTagged != null) {
            val face = faceBeingTagged!!
            TagPopup(
                face = face,
                onDismiss = { faceBeingTagged = null },
                onTag = { tag ->
                    val updatedData = data.updatedWithFaceTag(face, tag)
                    data = updatedData
                    onTagUpdate(updatedData)
                }
            )
        }
    }
}

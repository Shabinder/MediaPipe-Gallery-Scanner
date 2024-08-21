package com.shabinder.pixsee.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.rememberAsyncImagePainter
import com.shabinder.pixsee.MainViewModel
import com.shabinder.pixsee.db.entity.PhotoWithFaces

@Composable
fun PhotoGridScreenUI(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    goToImage: (PhotoWithFaces) -> Unit = {}
) {
    val photos = viewModel.photosFlow.collectAsLazyPagingItems()
    val isLoading by viewModel.isLoading.collectAsState()
    val photosProcessedCount by viewModel.photosProcessedCount.collectAsState()

    val noPhotos by remember {
        derivedStateOf {
            !isLoading && photos.itemCount == 0
        }
    }

    Column(modifier = modifier) {
        AnimatedVisibility(isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )
        }

        AnimatedVisibility(photosProcessedCount != 0) {
            Text(
                text = "Processed $photosProcessedCount photos.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            )
        }

        AnimatedContent(noPhotos, label = "PhotosContent") { noPhotosAvlbl ->
            if (noPhotosAvlbl) {
                NoPhotosBanner()
            } else {
                PhotosGrid(photos, goToImage)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.processFromGallery()
    }
}

@Composable
fun PhotosGrid(photos: LazyPagingItems<PhotoWithFaces>, goToImage: (PhotoWithFaces) -> Unit) {
    val updatedGoToImage by rememberUpdatedState(goToImage)

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(
            count = photos.itemCount,
            key = photos.itemKey { it.photoData.run { uri + dateAdded } }
        ) { photoInd ->
            val photo = photos[photoInd] ?: return@items

            Image(
                painter = rememberAsyncImagePainter(photo.photoData.uri),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = remember {
                    Modifier
                        .aspectRatio(1f)
                        .padding(4.dp)
                        .clickable {
                            updatedGoToImage(photo)
                        }
                }
            )
        }
    }
}

@Composable
fun NoPhotosBanner() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No photos found.",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

package com.shabinder.pixsee.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.shabinder.pixsee.MainViewModel

@Composable
fun PhotoGridScreenUI(viewModel: MainViewModel) {
    val photos by viewModel.photos.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(photos) { photo ->
            Image(
                painter = rememberAsyncImagePainter(photo.uri),
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(4.dp)
            )
        }
    }
}
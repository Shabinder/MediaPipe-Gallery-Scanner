package com.shabinder.pixsee

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.shabinder.pixsee.db.entity.PhotoWithFaces
import com.shabinder.pixsee.repo.PhotoRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@Stable
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repo: PhotoRepo
) : ViewModel() {

    val photosFlow: Flow<PagingData<PhotoWithFaces>> = repo.photoPager.flow
        .cachedIn(viewModelScope)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _photosProcessedCount = MutableStateFlow(0)
    val photosProcessedCount: StateFlow<Int> = _photosProcessedCount.asStateFlow()

    fun processFromGallery(batchSize: Int = DEFAULT_BATCH_SIZE) {
        assert(isPermissionGranted()) { "Permission not granted to read from gallery." }

        viewModelScope.launch(Dispatchers.IO) {
            repo.batchGalleryPhotosAsFlow(batchSize)
                .onStart { _isLoading.value = true }
                .onCompletion { _isLoading.value = false }
                .collect { photoBatch ->
                    Log.i("FaceDetection", "Batch Processing ${photoBatch.size} photos.")
                    // Process photos in parallel in a batch
                    supervisorScope {
                        photoBatch
                            .map { async { repo.addPhotoWithFaces(it) } }
                            .awaitAll()
                    }
                    _photosProcessedCount.update { it + photoBatch.size }
                }
        }
    }

    suspend fun getPhotoDataWithFaces(uri: String): PhotoResult {
        return runCatching {
            repo.getPhotoDataWithFaces(uri)
        }.fold(
            onSuccess = { PhotoResult.Success(it) },
            onFailure = { PhotoResult.Error(it.message ?: "Unknown error") }
        )
    }

    fun updatePhoto(photo: PhotoWithFaces) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updatePhoto(photo)
        }
    }

    fun isPermissionGranted(context: Context = appContext): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val DEFAULT_BATCH_SIZE = 10
    }
}

@Immutable
sealed interface PhotoResult {
    @Immutable
    data object Loading : PhotoResult

    @Immutable
    data class Error(val message: String) : PhotoResult

    @Immutable
    data class Success(val photo: PhotoWithFaces) : PhotoResult
}
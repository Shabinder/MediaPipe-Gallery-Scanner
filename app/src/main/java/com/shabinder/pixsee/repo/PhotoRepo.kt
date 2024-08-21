package com.shabinder.pixsee.repo

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.shabinder.pixsee.db.dao.PhotoWithFacesDao
import com.shabinder.pixsee.db.entity.PhotoData
import com.shabinder.pixsee.db.entity.PhotoWithFaces
import com.shabinder.pixsee.detection.FaceDetector
import com.shabinder.pixsee.utils.extractFaceInfos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class PhotoRepo(
    private val appContext: Context,
    private val photoDao: PhotoWithFacesDao,
    private val faceDetector: FaceDetector
) {

    val photoPager: Pager<Int, PhotoWithFaces> = Pager(
        config = PagingConfig(
            pageSize = 20, // Number of items per page
            enablePlaceholders = false
        ),
        pagingSourceFactory = photoDao::getPhotosWithFacesPagingSource
    )

    suspend fun addPhotoWithFaces(photo: PhotoData) {
        withContext(Dispatchers.IO) {
            val faceInfos = faceDetector.extractFaceInfos(photo, appContext)

            if (faceInfos.isEmpty()) {
                return@withContext // Skip processing photo with no Faces
            }

            // Add to Dao, Shall emit to UI reactively when observing.
            photoDao.insertPhotoWithFaces(photo, faceInfos)
        }
    }

    fun batchGalleryPhotosAsFlow(batchSize: Int): Flow<List<PhotoData>> = flow {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
        )
        val selection = MediaStore.Images.Media.DATA + " LIKE ?"
        val sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC"
        val selectionArgs = arrayOf("%DCIM/Camera%")

        val cursor = appContext.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val widthColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val batch = mutableListOf<PhotoData>()

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val dateAdded = it.getLong(dateAddedColumn)
                val width = it.getInt(widthColumn)
                val height = it.getInt(heightColumn)
                val contentUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                val photoUri = contentUri.toString()

                // Check if the photo is already processed, if so skip it
                if (!photoDao.isPhotoProcessed(photoUri)) {
                    batch.add(
                        PhotoData(
                            uri = photoUri,
                            dateAdded = dateAdded,
                            width = width,
                            height = height
                        )
                    )
                }

                // Emit the batch when it's full
                if (batch.size == batchSize) {
                    emit(batch.toList()) // Emit a copy of the batch
                    batch.clear() // Clear the batch for the next set
                }
            }

            // Emit any remaining items in the last batch
            if (batch.isNotEmpty()) {
                emit(batch.toList())
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getPhotoDataWithFaces(uri: String) = withContext(Dispatchers.IO) {
        photoDao.getPhotoWithFacesByUri(uri)
    }

    suspend fun updatePhoto(photo: PhotoWithFaces) = withContext(Dispatchers.IO) {
        photoDao.insertPhotoWithFaces(photo.photoData, photo.faceInfos)
    }
}
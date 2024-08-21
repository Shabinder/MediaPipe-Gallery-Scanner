package com.shabinder.pixsee.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shabinder.pixsee.db.entity.FaceInfo
import com.shabinder.pixsee.db.entity.PhotoData
import com.shabinder.pixsee.db.entity.PhotoWithFaces
import kotlinx.coroutines.flow.Flow
@Dao
interface PhotoWithFacesDao {

    @Transaction
    @Query("SELECT DISTINCT * FROM PhotoData")
    fun getAllPhotosWithFaces(): Flow<List<PhotoWithFaces>>

    @Transaction
    @Query("""
        SELECT * FROM PhotoData
        WHERE PhotoData.uri = :uri 
        LIMIT 1
    """)
    fun getPhotoWithFacesByUri(uri: String): PhotoWithFaces

    @Transaction
    @Query("""
        SELECT * FROM PhotoData
        WHERE uri IN (
            SELECT DISTINCT photoUri FROM face_info
            UNION SELECT uri FROM PhotoData
        )
        ORDER BY dateAdded DESC
    """)
    fun getPhotosWithFacesPagingSource(): PagingSource<Int, PhotoWithFaces>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaceInfos(faceInfos: List<FaceInfo>)

    @Transaction
    suspend fun insertPhotoWithFaces(photoData: PhotoData, faceInfos: List<FaceInfo>) {
        insertPhoto(photoData)
        insertFaceInfos(faceInfos)
    }

    @Query("SELECT EXISTS(SELECT 1 FROM PhotoData WHERE uri = :uri)")
    suspend fun isPhotoProcessed(uri: String): Boolean
    
    @Transaction
    @Query("DELETE FROM PhotoData WHERE uri = :photoUri")
    suspend fun deletePhotoByUri(photoUri: String)

    @Transaction
    @Query("DELETE FROM face_info WHERE photoUri = :photoUri")
    suspend fun deleteFaceInfosByPhotoUri(photoUri: String)

    @Transaction
    suspend fun deletePhotoAndFaces(photoUri: String) {
        deleteFaceInfosByPhotoUri(photoUri)
        deletePhotoByUri(photoUri)
    }
}
package com.shabinder.pixsee.di

import android.content.Context
import androidx.room.Room
import com.shabinder.pixsee.db.PixseeDatabase
import com.shabinder.pixsee.db.dao.PhotoWithFacesDao
import com.shabinder.pixsee.detection.FaceDetectionConfig
import com.shabinder.pixsee.detection.FaceDetector
import com.shabinder.pixsee.detection.OnDeviceFaceDetectorHelper
import com.shabinder.pixsee.repo.PhotoRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideFaceDetectorHelper(
        @ApplicationContext context: Context
    ): FaceDetector {
        // Default dispatcher, limited Thread Pool for CPU Intensive tasks
        // To Limit Parallelism more, use Dispatcher.limitedParallelism(n)
        val dispatcher = Dispatchers.Default
        val config = FaceDetectionConfig()
        return OnDeviceFaceDetectorHelper(
            context = context,
            config = config,
            dispatcher = dispatcher
        )
    }

    @Provides
    @Singleton
    fun providePhotoRepo(
        @ApplicationContext context: Context,
        photoDao: PhotoWithFacesDao,
        faceDetector: FaceDetector
    ): PhotoRepo {
        return PhotoRepo(context, photoDao, faceDetector)
    }

    @Provides
    @Singleton
    fun providePixseeDB(
        @ApplicationContext context: Context,
    ): PixseeDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = PixseeDatabase::class.java,
            name = "pixsee_db"
        ).build()
    }

    @Provides
    @Singleton
    fun providePhotoWithFacesDao(
        db: PixseeDatabase
    ): PhotoWithFacesDao {
        return db.photoDao()
    }
}
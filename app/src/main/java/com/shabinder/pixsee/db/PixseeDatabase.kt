package com.shabinder.pixsee.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shabinder.pixsee.db.dao.PhotoWithFacesDao
import com.shabinder.pixsee.db.entity.FaceInfo
import com.shabinder.pixsee.db.entity.PhotoData

@Database(entities = [PhotoData::class, FaceInfo::class], version = 1, exportSchema = false)
abstract class PixseeDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoWithFacesDao
}
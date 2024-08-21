package com.shabinder.pixsee.db.entity

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.Serializable

@Entity
@Immutable
@Serializable
data class PhotoData(
    @PrimaryKey val uri: String, // The URI of the image
    val dateAdded: Long,
    val width: Int,
    val height: Int
)

@Entity(
    tableName = "face_info",
    foreignKeys = [
        ForeignKey(
            entity = PhotoData::class,
            parentColumns = ["uri"],
            childColumns = ["photoUri"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

@Immutable
@Serializable
data class FaceInfo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val photoUri: String,
    val positionLeft: Float,
    val positionTop: Float,
    val positionRight: Float,
    val positionBottom: Float,
    val tag: String
)

@Immutable
@Serializable
data class PhotoWithFaces(
    @Embedded val photoData: PhotoData,
    @Relation(
        parentColumn = "uri",
        entityColumn = "photoUri"
    )
    val faceInfos: List<FaceInfo>
)


fun PhotoWithFaces.updatedWithFaceTag(faceInfo: FaceInfo, tag: String): PhotoWithFaces {
    return copy(
        faceInfos = faceInfos.map {
            if (it == faceInfo) {
                it.copy(tag = tag)
            } else {
                it
            }
        }
    )
}

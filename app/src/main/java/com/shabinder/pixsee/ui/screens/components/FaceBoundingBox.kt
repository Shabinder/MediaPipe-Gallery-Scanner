package com.shabinder.pixsee.ui.screens.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shabinder.pixsee.db.entity.FaceInfo
import kotlin.math.min

@Composable
fun FaceBoundingBox(
    face: FaceInfo,
    imageWidth: Int,
    imageHeight: Int,
    onClick: () -> Unit
) {
    BoxWithConstraints {
        // Calculate the scale factor based on the composable size and image size
        val scaleFactor = min(
            constraints.maxWidth.toFloat() / imageWidth,
            constraints.maxHeight.toFloat() / imageHeight
        )

        val leftDp = with(LocalDensity.current) { (face.positionLeft * scaleFactor).toDp() }
        val topDp = with(LocalDensity.current) { (face.positionTop * scaleFactor).toDp() }
        val widthDp = with(LocalDensity.current) { ((face.positionRight - face.positionLeft) * scaleFactor).toDp() }
        val heightDp = with(LocalDensity.current) { ((face.positionBottom - face.positionTop) * scaleFactor).toDp() }

        Box(
            modifier = Modifier
                .offset(x = leftDp, y = topDp)
                .size(width = widthDp, height = heightDp)
                .border(2.dp, Color.Green, shape = RectangleShape)
                .clickable { onClick() }
        ) {
            Text(
                text = face.tag,
                color = Color.Green,
                softWrap = true,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.offset(x = 4.dp, y = 4.dp)
            )
        }
    }
}

package com.shabinder.pixsee.ui.screens.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.shabinder.pixsee.db.entity.FaceInfo

@Composable
fun TagPopup(
    face: FaceInfo,
    onDismiss: () -> Unit,
    onTag: (String) -> Unit,
) {
    var tagText by rememberSaveable { mutableStateOf(face.tag) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tag Face") },
        text = {
            Column {
                Text("Enter a tag for this face:")
                TextField(value = tagText, onValueChange = { tagText = it })
            }
        },
        confirmButton = {
            Button(onClick = {
                onTag(tagText)
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

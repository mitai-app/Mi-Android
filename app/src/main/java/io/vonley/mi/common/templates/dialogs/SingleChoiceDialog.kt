package io.vonley.mi.common.templates.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.vonley.mi.ui.compose.ComposableFun


@Composable
fun CustomDialog(
    title: String,
    accept: String,
    cancel: String,
    content: ComposableFun,
    onAccept: () -> Unit,
    onCancel: () -> Unit
) {
    val openDialog = remember { mutableStateOf(true) }
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            title = {
                Text(text = title)
            },
            text = {
                content()
            },
            buttons = {
                Row(
                    modifier = Modifier.padding(all = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            openDialog.value = false
                            onAccept()
                        }
                    ) {
                        Text(text = accept)
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            openDialog.value = false
                            onCancel()
                        }
                    ) {
                        Text(text = cancel)
                    }
                }
            }
        )
    }
}
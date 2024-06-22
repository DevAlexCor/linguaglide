package ru.softstone.linguaglide.presentation.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun DialogOverlay(
    controller: HasDialog,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        val state: DialogState by controller.stateFlow.collectAsState()

        if (state.isShowing) {
            AlertDialog(
                onDismissRequest = controller::onDismiss,
                title = { Text(text = state.title) },
                text = { Text(text = state.message) },
                confirmButton = {
                    state.positiveButton?.let { positiveButton ->
                        Button(onClick = controller::onPositiveButtonClick) {
                            Text(text = positiveButton)
                        }
                    }
                },
                dismissButton = {
                    state.negativeButton?.let { negativeButton ->
                        Button(onClick = controller::onNegativeButtonClick) {
                            Text(text = negativeButton)
                        }
                    }
                }
            )
        }
    }
}
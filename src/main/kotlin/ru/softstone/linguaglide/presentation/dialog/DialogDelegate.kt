package ru.softstone.linguaglide.presentation.dialog

import ru.softstone.linguaglide.presentation.core.HasState
import ru.softstone.linguaglide.presentation.core.StateDelegate

class DialogDelegate : HasDialog, HasState<DialogState> by StateDelegate(DialogState()) {

    private val dialogsQueue: MutableList<Dialog> = mutableListOf()

    override fun showDialog(
        title: String,
        message: String,
        positiveButton: String?,
        negativeButton: String?,
        onPositiveButtonClick: () -> Unit,
        onNegativeButtonClick: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        dialogsQueue.add(
            Dialog(
                title = title,
                message = message,
                positiveButton = positiveButton,
                negativeButton = negativeButton,
                onPositiveButtonClick = onPositiveButtonClick,
                onNegativeButtonClick = onNegativeButtonClick,
                onDismiss = onDismiss
            )
        )
        state = dialogsQueue.first().toState()
    }


    override fun onPositiveButtonClick() {
        dialogsQueue.first().onPositiveButtonClick()
        dialogsQueue.removeFirst()
        state = dialogsQueue.firstOrNull()?.toState() ?: DialogState(isShowing = false)
    }

    override fun onNegativeButtonClick() {
        dialogsQueue.first().onNegativeButtonClick()
        dialogsQueue.removeFirst()
        state = dialogsQueue.firstOrNull()?.toState() ?: DialogState(isShowing = false)
    }

    override fun onDismiss() {
        dialogsQueue.first().onDismiss()
        dialogsQueue.removeFirst()
        state = dialogsQueue.firstOrNull()?.toState() ?: DialogState(isShowing = false)
    }

    private data class Dialog(
        val title: String = "",
        val message: String = "",
        val positiveButton: String? = null,
        val negativeButton: String? = null,
        val onPositiveButtonClick: () -> Unit = {},
        val onNegativeButtonClick: () -> Unit = {},
        val onDismiss: () -> Unit = {}
    )

    private fun Dialog.toState() = DialogState(
        title = title,
        message = message,
        positiveButton = positiveButton,
        negativeButton = negativeButton,
        isShowing = true
    )
}
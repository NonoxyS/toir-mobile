package ru.mirea.toir.feature.checklist.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore.State
import ru.mirea.toir.feature.checklist.impl.domain.ChecklistStoreFactory.Message

internal class ChecklistReducer : Reducer<State, Message> {

    override fun State.reduce(msg: Message): State = when (msg) {
        Message.SetLoading -> copy(
            isLoading = true,
            isError = false,
            isValidationError = false,
            isPhotoValidationError = false,
        )

        Message.SetError -> copy(
            isLoading = false,
            isError = true,
        )

        is Message.SetItems -> copy(
            isLoading = false,
            isError = false,
            items = msg.items,
        )

        is Message.UpdateItem -> copy(
            items = items.map { item -> if (item.id == msg.item.id) msg.item else item },
        )

        is Message.SetNumberInvalid -> copy(
            invalidNumberInputs = invalidNumberInputs + (msg.itemId to msg.raw),
        )

        is Message.ClearNumberInvalid -> copy(
            invalidNumberInputs = invalidNumberInputs - msg.itemId,
        )

        Message.SetValidationRequiredError -> copy(
            isValidationError = true,
            isPhotoValidationError = false,
            isOutOfRangeError = false,
            isInvalidNumberError = false,
        )

        Message.SetValidationPhotoError -> copy(
            isValidationError = false,
            isPhotoValidationError = true,
            isOutOfRangeError = false,
            isInvalidNumberError = false,
        )

        Message.SetValidationOutOfRangeError -> copy(
            isValidationError = false,
            isPhotoValidationError = false,
            isOutOfRangeError = true,
            isInvalidNumberError = false,
        )

        Message.SetValidationInvalidNumberError -> copy(
            isValidationError = false,
            isPhotoValidationError = false,
            isOutOfRangeError = false,
            isInvalidNumberError = true,
        )

        Message.ClearValidationError -> copy(
            isValidationError = false,
            isPhotoValidationError = false,
            isOutOfRangeError = false,
            isInvalidNumberError = false,
        )

        Message.SetCompleted -> copy(
            isCompleted = true,
            isValidationError = false,
            isPhotoValidationError = false,
            isOutOfRangeError = false,
            isInvalidNumberError = false,
        )
    }
}

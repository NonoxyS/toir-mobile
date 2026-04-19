package ru.mirea.toir.feature.photo.capture.presentation.models

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class UiPhotoCaptureState(
    val photos: ImmutableList<String> = persistentListOf(),
    val isLoading: Boolean = false,
)

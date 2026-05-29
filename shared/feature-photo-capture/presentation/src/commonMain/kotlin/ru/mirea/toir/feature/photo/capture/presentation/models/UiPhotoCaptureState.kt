package ru.mirea.toir.feature.photo.capture.presentation.models

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class UiPhotoCaptureState(
    val photos: ImmutableList<UiPhotoEntry> = persistentListOf(),
    val maxPhotos: Int? = null,
    val isLoading: Boolean = false,
    val isLimitReached: Boolean = false,
    val canTakePhoto: Boolean = false,
)

/**
 * One entry in the photo row. `fileUri == null` means "metadata restored from server,
 * file not yet downloaded" — the UI renders a placeholder tile (see DS:
 * `docs/design-system/pages/photo-capture.md`). `id` is the stable identifier
 * (photo UUID from DB, or, for freshly captured photos, the URI itself).
 */
@Immutable
data class UiPhotoEntry(
    val id: String,
    val fileUri: String?,
)

package ru.mirea.toir.feature.photo.capture.impl.domain

import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.PhotoEntry
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.State
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhotoCaptureReducerTest {
    private val reducer = PhotoCaptureReducer()
    private val initial = State()

    private fun entry(uri: String) = PhotoEntry(id = uri, fileUri = uri)

    @Test
    fun `SetLoading sets isLoading`() {
        val result = with(reducer) { initial.reduce(PhotoCaptureStoreFactory.Message.SetLoading(true)) }
        assertTrue(result.isLoading)
    }

    @Test
    fun `SetLoading false clears isLoading`() {
        val loading = initial.copy(isLoading = true)
        val result = with(reducer) { loading.reduce(PhotoCaptureStoreFactory.Message.SetLoading(false)) }
        assertFalse(result.isLoading)
    }

    @Test
    fun `SetPhotos replaces photos list`() {
        val withPhotos = initial.copy(photos = listOf(entry("old.jpg")))
        val result = with(reducer) {
            withPhotos.reduce(PhotoCaptureStoreFactory.Message.SetPhotos(listOf(entry("new.jpg"))))
        }
        assertEquals(1, result.photos.size)
        assertEquals(entry("new.jpg"), result.photos.first())
    }

    @Test
    fun `SetPhotos with mixed placeholder and real entries preserves order`() {
        // Reactive flow contract: SQLDelight emits the full list on every change, so the
        // reducer trusts it as the source of truth — no merging with prior state.
        val placeholder = PhotoEntry(id = "restored-1", fileUri = null)
        val real = entry("file:///a.jpg")
        val result = with(reducer) {
            initial.reduce(PhotoCaptureStoreFactory.Message.SetPhotos(listOf(placeholder, real)))
        }
        assertEquals(listOf(placeholder, real), result.photos)
    }

    @Test
    fun `SetPhotos with empty list clears photos`() {
        val withPhotos = initial.copy(photos = listOf(entry("a"), entry("b")))
        val result = with(reducer) {
            withPhotos.reduce(PhotoCaptureStoreFactory.Message.SetPhotos(emptyList()))
        }
        assertTrue(result.photos.isEmpty())
    }

    @Test
    fun `default State has DEFAULT_MAX_PHOTOS`() {
        assertEquals(PhotoCaptureStore.DEFAULT_MAX_PHOTOS, initial.maxPhotos)
    }
}

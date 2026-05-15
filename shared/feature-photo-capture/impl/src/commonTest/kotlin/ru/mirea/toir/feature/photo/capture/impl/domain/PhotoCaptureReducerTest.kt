package ru.mirea.toir.feature.photo.capture.impl.domain

import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.PhotoEntry
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.State
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PhotoCaptureReducerTest {
    private val reducer = PhotoCaptureReducer()
    private val initial = State()

    private fun entry(uri: String) = PhotoEntry(id = uri, fileUri = uri)

    @Test
    fun `AddPhoto appends to photos list`() {
        val result = with(reducer) {
            initial.reduce(PhotoCaptureStoreFactory.Message.AddPhoto(entry("file:///test.jpg")))
        }
        assertEquals(1, result.photos.size)
        assertEquals(entry("file:///test.jpg"), result.photos.first())
    }

    @Test
    fun `SetLoading sets isLoading`() {
        val result = with(reducer) { initial.reduce(PhotoCaptureStoreFactory.Message.SetLoading(true)) }
        assertTrue(result.isLoading)
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
    fun `PhotoRemoved drops the matching uri`() {
        val withPhotos = initial.copy(photos = listOf(entry("a"), entry("b"), entry("c")))
        val result = with(reducer) {
            withPhotos.reduce(PhotoCaptureStoreFactory.Message.PhotoRemoved("b"))
        }
        assertEquals(listOf(entry("a"), entry("c")), result.photos)
    }

    @Test
    fun `PhotoRemoved with non-existing uri leaves photos unchanged`() {
        val withPhotos = initial.copy(photos = listOf(entry("a"), entry("b")))
        val result = with(reducer) {
            withPhotos.reduce(PhotoCaptureStoreFactory.Message.PhotoRemoved("nope"))
        }
        assertEquals(listOf(entry("a"), entry("b")), result.photos)
    }

    @Test
    fun `PhotoRemoved keeps placeholder entries with null fileUri`() {
        // Restored-but-not-yet-downloaded photo: fileUri == null, id is the photo UUID.
        // Tap-to-delete only goes through tiles that have a fileUri, so PhotoRemoved
        // should not touch placeholder rows.
        val placeholder = PhotoEntry(id = "restored-1", fileUri = null)
        val withPhotos = initial.copy(photos = listOf(placeholder, entry("a")))
        val result = with(reducer) {
            withPhotos.reduce(PhotoCaptureStoreFactory.Message.PhotoRemoved("a"))
        }
        assertEquals(listOf(placeholder), result.photos)
    }

    @Test
    fun `default State has DEFAULT_MAX_PHOTOS`() {
        assertEquals(PhotoCaptureStore.DEFAULT_MAX_PHOTOS, initial.maxPhotos)
    }
}

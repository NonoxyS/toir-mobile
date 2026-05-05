package ru.mirea.toir.feature.photo.capture.impl.domain

import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore.State
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PhotoCaptureReducerTest {
    private val reducer = PhotoCaptureReducer()
    private val initial = State()

    @Test
    fun `AddPhoto appends to photos list`() {
        val result = with(reducer) {
            initial.reduce(PhotoCaptureStoreFactory.Message.AddPhoto("file:///test.jpg"))
        }
        assertEquals(1, result.photos.size)
        assertEquals("file:///test.jpg", result.photos.first())
    }

    @Test
    fun `SetLoading sets isLoading`() {
        val result = with(reducer) { initial.reduce(PhotoCaptureStoreFactory.Message.SetLoading(true)) }
        assertTrue(result.isLoading)
    }

    @Test
    fun `SetPhotos replaces photos list`() {
        val withPhotos = initial.copy(photos = listOf("old.jpg"))
        val result = with(reducer) {
            withPhotos.reduce(PhotoCaptureStoreFactory.Message.SetPhotos(listOf("new.jpg")))
        }
        assertEquals(1, result.photos.size)
        assertEquals("new.jpg", result.photos.first())
    }

    @Test
    fun `PhotoRemoved drops the matching uri`() {
        val withPhotos = initial.copy(photos = listOf("a", "b", "c"))
        val result = with(reducer) {
            withPhotos.reduce(PhotoCaptureStoreFactory.Message.PhotoRemoved("b"))
        }
        assertEquals(listOf("a", "c"), result.photos)
    }

    @Test
    fun `PhotoRemoved with non-existing uri leaves photos unchanged`() {
        val withPhotos = initial.copy(photos = listOf("a", "b"))
        val result = with(reducer) {
            withPhotos.reduce(PhotoCaptureStoreFactory.Message.PhotoRemoved("nope"))
        }
        assertEquals(listOf("a", "b"), result.photos)
    }

    @Test
    fun `default State has DEFAULT_MAX_PHOTOS`() {
        assertEquals(PhotoCaptureStore.DEFAULT_MAX_PHOTOS, initial.maxPhotos)
    }
}

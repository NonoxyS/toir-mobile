package ru.mirea.toir.feature.photo.capture.impl.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.mirea.toir.feature.photo.capture.impl.data.files.AndroidPhotoFileDeleter
import ru.mirea.toir.feature.photo.capture.impl.data.files.PhotoFileDeleter

internal actual val platformFeaturePhotoCaptureImplModule = module {
    factory<PhotoFileDeleter> { AndroidPhotoFileDeleter(context = androidContext()) }
}

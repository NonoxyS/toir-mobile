package ru.mirea.toir.feature.photo.capture.impl.di

import org.koin.dsl.module
import ru.mirea.toir.feature.photo.capture.impl.data.files.IosPhotoFileDeleter
import ru.mirea.toir.feature.photo.capture.impl.data.files.PhotoFileDeleter

internal actual val platformFeaturePhotoCaptureImplModule = module {
    factory<PhotoFileDeleter> { IosPhotoFileDeleter() }
}

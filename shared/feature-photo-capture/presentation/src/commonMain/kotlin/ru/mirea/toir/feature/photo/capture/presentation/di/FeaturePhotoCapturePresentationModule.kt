package ru.mirea.toir.feature.photo.capture.presentation.di

import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.mirea.toir.feature.photo.capture.presentation.PhotoCaptureViewModel
import ru.mirea.toir.feature.photo.capture.presentation.mappers.UiPhotoCaptureLabelMapper
import ru.mirea.toir.feature.photo.capture.presentation.mappers.UiPhotoCaptureLabelMapperImpl
import ru.mirea.toir.feature.photo.capture.presentation.mappers.UiPhotoCaptureStateMapper
import ru.mirea.toir.feature.photo.capture.presentation.mappers.UiPhotoCaptureStateMapperImpl

val featurePhotoCapturePresentationModule = module {
    factory<UiPhotoCaptureStateMapper> { new(::UiPhotoCaptureStateMapperImpl) }
    factory<UiPhotoCaptureLabelMapper> { new(::UiPhotoCaptureLabelMapperImpl) }
    viewModelOf(::PhotoCaptureViewModel)
}

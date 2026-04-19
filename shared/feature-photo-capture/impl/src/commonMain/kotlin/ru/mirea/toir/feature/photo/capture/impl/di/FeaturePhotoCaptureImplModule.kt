package ru.mirea.toir.feature.photo.capture.impl.di

import org.koin.core.module.dsl.new
import org.koin.dsl.module
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.feature.photo.capture.api.store.PhotoCaptureStore
import ru.mirea.toir.feature.photo.capture.impl.data.repository.PhotoCaptureRepositoryImpl
import ru.mirea.toir.feature.photo.capture.impl.domain.PhotoCaptureStoreFactory
import ru.mirea.toir.feature.photo.capture.impl.domain.repository.PhotoCaptureRepository

val featurePhotoCaptureImplModule = module {
    factory<PhotoCaptureRepository> { new(::PhotoCaptureRepositoryImpl) }

    factory<PhotoCaptureStore> {
        PhotoCaptureStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            repository = get(),
        ).create()
    }
}

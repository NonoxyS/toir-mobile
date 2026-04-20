package ru.mirea.toir.di

import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import ru.mirea.toir.common.di.commonModule
import ru.mirea.toir.common.resources.di.commonResourcesModule
import ru.mirea.toir.core.auth.di.coreAuthModule
import ru.mirea.toir.core.database.di.coreDatabaseModule
import ru.mirea.toir.core.domain.di.coreDomainModule
import ru.mirea.toir.core.mvikotlin.di.coreMVIKotlinModule
import ru.mirea.toir.core.network.di.coreNetworkModule
import ru.mirea.toir.core.network.ktor.di.coreNetworkKtorModule
import ru.mirea.toir.core.storage.di.coreStorageModule
import ru.mirea.toir.feature.auth.impl.di.featureAuthImplModule
import ru.mirea.toir.feature.auth.presentation.di.featureAuthPresentationModule
import ru.mirea.toir.feature.bootstrap.impl.di.featureBootstrapImplModule
import ru.mirea.toir.feature.bootstrap.presentation.di.featureBootstrapPresentationModule
import ru.mirea.toir.feature.equipment.card.impl.di.featureEquipmentCardImplModule
import ru.mirea.toir.feature.equipment.card.presentation.di.featureEquipmentCardPresentationModule
import ru.mirea.toir.feature.photo.capture.impl.di.featurePhotoCaptureImplModule
import ru.mirea.toir.feature.photo.capture.presentation.di.featurePhotoCapturePresentationModule
import ru.mirea.toir.feature.route.points.impl.di.featureRoutePointsImplModule
import ru.mirea.toir.feature.route.points.presentation.di.featureRoutePointsPresentationModule
import ru.mirea.toir.feature.routes.list.impl.di.featureRoutesListImplModule
import ru.mirea.toir.feature.routes.list.presentation.di.featureRoutesListPresentationModule
import ru.mirea.toir.sync.di.syncManagerModule

fun initKoin(appDeclaration: KoinAppDeclaration) {
    Napier.d(message = "initKoin")
    startKoin {
        appDeclaration()
        modules(
            commonModule,
            commonResourcesModule,

            coreDomainModule,
            coreMVIKotlinModule,
            coreNetworkModule,
            coreNetworkKtorModule,
            coreStorageModule,
            coreDatabaseModule,
            coreAuthModule,

            featureAuthImplModule,
            featureAuthPresentationModule,

            featureBootstrapImplModule,
            featureBootstrapPresentationModule,

            featureRoutesListImplModule,
            featureRoutesListPresentationModule,

            featureRoutePointsImplModule,
            featureRoutePointsPresentationModule,

            featureEquipmentCardImplModule,
            featureEquipmentCardPresentationModule,

            featurePhotoCaptureImplModule,
            featurePhotoCapturePresentationModule,

            syncManagerModule,
        )
    }
}

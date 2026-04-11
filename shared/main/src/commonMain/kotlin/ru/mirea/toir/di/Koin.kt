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
        )
    }
}

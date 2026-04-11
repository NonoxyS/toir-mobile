package ru.mirea.toir.di

import ru.mirea.toir.feature.auth.impl.di.featureAuthImplModule
import ru.mirea.toir.feature.auth.presentation.di.featureAuthPresentationModule
import ru.mirea.toir.feature.demo.first.impl.di.featureDemoFirstImplModule
import ru.mirea.toir.feature.demo.first.presentation.di.featureDemoFirstPresentationModule
import ru.mirea.toir.feature.demo.second.presentation.di.featureDemoSecondPresentationModule
import ru.mirea.toir.common.di.commonModule
import ru.mirea.toir.common.resources.di.commonResourcesModule
import ru.mirea.toir.core.domain.di.coreDomainModule
import ru.mirea.toir.core.mvikotlin.di.coreMVIKotlinModule
import ru.mirea.toir.core.network.di.coreNetworkModule
import ru.mirea.toir.core.network.ktor.di.coreNetworkKtorModule
import ru.mirea.toir.core.auth.di.coreAuthModule
import ru.mirea.toir.core.database.di.coreDatabaseModule
import ru.mirea.toir.core.storage.di.coreStorageModule
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

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

            featureDemoFirstImplModule,
            featureDemoFirstPresentationModule,

            _root_ide_package_.ru.mirea.toir.feature.demo.second.impl.di.featureDemoSecondImplModule,
            featureDemoSecondPresentationModule,
        )
    }
}

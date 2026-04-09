package dev.nonoxy.kmmtemplate.di

import dev.nonoxy.kmmtemplate.feature.demo.first.impl.di.featureDemoFirstImplModule
import dev.nonoxy.kmmtemplate.feature.demo.first.presentation.di.featureDemoFirstPresentationModule
import dev.nonoxy.kmmtemplate.feature.demo.second.presentation.di.featureDemoSecondPresentationModule
import dev.nonoxy.kmmtemplate.common.di.commonModule
import dev.nonoxy.kmmtemplate.common.resources.di.commonResourcesModule
import dev.nonoxy.kmmtemplate.core.domain.di.coreDomainModule
import dev.nonoxy.kmmtemplate.core.mvikotlin.di.coreMVIKotlinModule
import dev.nonoxy.kmmtemplate.core.network.di.coreNetworkModule
import dev.nonoxy.kmmtemplate.core.network.ktor.di.coreNetworkKtorModule
import dev.nonoxy.kmmtemplate.core.storage.di.coreStorageModule
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

            featureDemoFirstImplModule,
            featureDemoFirstPresentationModule,

            _root_ide_package_.dev.nonoxy.kmmtemplate.feature.demo.second.impl.di.featureDemoSecondImplModule,
            featureDemoSecondPresentationModule,
        )
    }
}

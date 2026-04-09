package dev.nonoxy.kmmtemplate.common.di

import dev.nonoxy.kmmtemplate.common.coroutines.CoroutineDispatchers
import dev.nonoxy.kmmtemplate.common.coroutines.CoroutineDispatchersImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val commonModule = module {

    singleOf<CoroutineDispatchers>(::CoroutineDispatchersImpl)
}

package ru.mirea.toir.common.di

import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.common.coroutines.CoroutineDispatchersImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val commonModule = module {

    singleOf<CoroutineDispatchers>(::CoroutineDispatchersImpl)
}

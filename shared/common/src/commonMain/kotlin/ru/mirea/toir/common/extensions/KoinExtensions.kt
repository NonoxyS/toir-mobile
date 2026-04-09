package ru.mirea.toir.common.extensions

import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.getScopeId
import org.koin.core.component.getScopeName
import org.koin.core.scope.Scope

inline fun <reified T : Any> Scope.getScope(): Scope {
    return getKoin().get<T>().getOrCreateScope(getKoin())
}

inline fun <reified T : Any> KoinComponent.getScope(): Scope {
    return getKoin().get<T>().getOrCreateScope(getKoin())
}

fun <T : Any> T.getOrCreateScope(koin: Koin): Scope {
    return koin.getOrCreateScope(scopeId = getScopeId(), qualifier = getScopeName(), source = null)
}
package ru.mirea.toir.sync.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import ru.mirea.toir.common.coroutines.CoroutineDispatchers
import ru.mirea.toir.sync.domain.network.NetworkMonitor

internal class AndroidNetworkMonitor(
    context: Context,
    coroutineDispatchers: CoroutineDispatchers,
) : NetworkMonitor {

    private val connectivityManager = context.getSystemService<ConnectivityManager>()
    private val scope = CoroutineScope(coroutineDispatchers.io + SupervisorJob())

    override val isOnline: StateFlow<Boolean> = callbackFlow {
        val manager = connectivityManager
        if (manager == null) {
            trySend(false)
            awaitClose { }
            return@callbackFlow
        }

        val validatedNetworks = mutableSetOf<Network>()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                if (isValidated) {
                    validatedNetworks += network
                } else {
                    validatedNetworks -= network
                }
                trySend(validatedNetworks.isNotEmpty())
            }

            override fun onLost(network: Network) {
                validatedNetworks -= network
                trySend(validatedNetworks.isNotEmpty())
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        manager.registerNetworkCallback(request, callback)
        trySend(false)

        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = false,
    )
}

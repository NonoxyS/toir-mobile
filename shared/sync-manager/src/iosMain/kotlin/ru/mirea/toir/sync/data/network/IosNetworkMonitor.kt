package ru.mirea.toir.sync.data.network

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_t
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_queue_create
import ru.mirea.toir.sync.domain.network.NetworkMonitor

@OptIn(ExperimentalForeignApi::class)
internal class IosNetworkMonitor : NetworkMonitor {

    private val _isOnline = MutableStateFlow(false)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val queue = dispatch_queue_create("ru.mirea.toir.sync.network-monitor", null)
    private val monitor: nw_path_monitor_t = nw_path_monitor_create()

    init {
        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_set_update_handler(monitor) { path ->
            val status = nw_path_get_status(path)
            _isOnline.value = status == nw_path_status_satisfied
        }
        nw_path_monitor_start(monitor)
    }

    fun stop() {
        nw_path_monitor_cancel(monitor)
    }
}

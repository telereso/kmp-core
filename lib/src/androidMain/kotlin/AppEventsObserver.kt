package io.telereso.kmp.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

open class AppEventsObserver(
    context: Context,
    capabilities: List<Int>,
    transportTypes: List<Int>,
) : DefaultLifecycleObserver {

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        // network is available for use
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            onNetworkAvailable(network)
        }

        // Network capabilities have changed for the network
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
//            val unmetered =
//                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            onNetworkCapabilitiesChanged(network, networkCapabilities)
        }

        // lost network connection
        override fun onLost(network: Network) {
            super.onLost(network)
            onNetworkLost(network)
        }
    }

    init {
        if (capabilities.isNotEmpty() || transportTypes.isNotEmpty()) {
            val networkRequest = NetworkRequest.Builder().apply {
                capabilities.forEach {
                    addCapability(it)
                }
                transportTypes.forEach {
                    addTransportType(it)
                }
            }.build()


            val connectivityManager =
                getSystemService(context, ConnectivityManager::class.java) as ConnectivityManager
            connectivityManager.requestNetwork(networkRequest, networkCallback)
        }

    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        onEnterForeground?.invoke()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        onEnterBackground?.invoke()
    }

    // network is available for use
    open fun onNetworkAvailable(network: Network) {
        onNetworkAvailable?.invoke(network)
    }

    // Network capabilities have changed for the network
    open fun onNetworkCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities
    ) {
        onNetworkCapabilitiesChanged?.invoke(network, networkCapabilities)
    }

    // lost network connection
    open fun onNetworkLost(network: Network) {
        onNetworkLost?.invoke(network)
    }

    companion object {
        /**
         * If your app have multiple process , make sure to call this one only on the main one only (UI)
         */
        fun observe(
            context: Context,
            capabilities: List<Int> = listOf(NetworkCapabilities.NET_CAPABILITY_INTERNET),
            transportTypes: List<Int> = listOf(
                NetworkCapabilities.TRANSPORT_WIFI,
                NetworkCapabilities.TRANSPORT_CELLULAR
            )
        ) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(
                AppEventsObserver(
                    context,
                    capabilities,
                    transportTypes
                )
            )
        }

        private var onEnterForeground: (() -> Unit)? = {}
        private var onEnterBackground: (() -> Unit)? = {}
        private var onNetworkAvailable: ((network: Network) -> Unit)? = {}
        private var onNetworkCapabilitiesChanged: ((network: Network, networkCapabilities: NetworkCapabilities) -> Unit)? =
            { _, _ -> }
        private var onNetworkLost: ((network: Network) -> Unit)? = {}

        fun onEnterForeground(callback: () -> Unit): Companion {
            onEnterForeground = callback
            return this
        }

        fun onEnterBackground(callback: () -> Unit): Companion {
            onEnterBackground = callback
            return this
        }

        fun onNetworkAvailable(callback: (network: Network) -> Unit): Companion {
            onNetworkAvailable = callback
            return this
        }

        fun onNetworkCapabilitiesChanged(callback: (network: Network, networkCapabilities: NetworkCapabilities) -> Unit): Companion {
            onNetworkCapabilitiesChanged = callback
            return this
        }

        fun onNetworkLost(callback: (network: Network) -> Unit): Companion {
            onNetworkLost = callback
            return this
        }
    }
}
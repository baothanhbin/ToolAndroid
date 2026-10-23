package com.b9utils.util;

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.b9utils.util.ShortNameEventUtils.makeActivityShortName

object CheckInternet {

    private const val TAG = "CheckInternetTag"
    private const val Event_TAG = "NetChk"

    private fun haveNetworkConnection2(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //FirebaseLogEventUtils.logEventCommonWithName(context, "net2_begin")

        //FirebaseLogEventUtils.logEventCommonWithName(context, "net2_c1Begin")
        // Returns a Network object corresponding to
        // the currently active default data network.
        //val network = connectivityManager.activeNetwork ?: return false
        val network = connectivityManager.activeNetwork ?: return false

        // Representation of the capabilities of an active network.
        //val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            // Indicates this network uses a Wi-Fi transport,
            // or WiFi has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                //FirebaseLogEventUtils.logEventCommonWithName(context, "net2_c1Wifi_true")
                true
            }
            // Indicates this network uses a Cellular transport. or
            // Cellular has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                //FirebaseLogEventUtils.logEventCommonWithName(context, "net2_c1Cell_true")
                true
            }

            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                //FirebaseLogEventUtils.logEventCommonWithName(context, "net2_c1Eth_true")
                true
            }

            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                //FirebaseLogEventUtils.logEventCommonWithName(context, "net2_c1Vpn_true")
                true
            }
            // else return false
            else -> {
                //FirebaseLogEventUtils.logEventCommonWithName(context, "net2_c1Other_false")
                false
            }
        }

    }

    private fun haveNetworkConnection1(context: Context): Boolean {
        //FirebaseLogEventUtils.logEventCommonWithName(context, "net1_begin")

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        //FirebaseLogEventUtils.logEventCommonWithName(context, "net1_C1Begin")

        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val hasCapabilityInternet =
            (networkCapabilities != null && networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            ))
        //FirebaseLogEventUtils.logEventCommonWithName(context, "net1_C1${hasCapabilityInternet}")

        return hasCapabilityInternet
    }

    fun haveNetworkConnection(context: Context): Boolean {
        //FirebaseLogEventUtils.logEventCommonWithName(context, "net_final_begin")

        val isInternet1 = haveNetworkConnection1(context)
        val isInternet2 = haveNetworkConnection2(context)
        val isInternet = isInternet1 || isInternet2

        //log with screen name
        val screen = makeActivityShortName(context.javaClass.simpleName)
        if (!isInternet) {
            FirebaseLogEventUtils.logEventCommonWithName(context, "${Event_TAG}_Scr${screen}False")
        }

        //log without screen name
        FirebaseLogEventUtils.logEventCommonWithName(context, "${Event_TAG}_all$isInternet")
        FirebaseLogEventUtils.logEventCommonWithName(context, "${Event_TAG}_fun1$isInternet1")
        FirebaseLogEventUtils.logEventCommonWithName(context, "${Event_TAG}_fun2$isInternet2")
        return isInternet
    }
}

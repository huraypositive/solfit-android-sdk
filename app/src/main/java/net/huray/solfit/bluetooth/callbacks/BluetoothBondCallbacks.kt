package net.huray.solfit.bluetooth.callbacks

import aicare.net.cn.iweightlibrary.wby.WBYService

interface BluetoothBondCallbacks {
    fun onServiceBinded(wbyBinder: WBYService.WBYBinder?)
    fun onServiceUnbinded()
}
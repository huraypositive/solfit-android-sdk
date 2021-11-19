package net.huray.solfit.bluetooth.callbacks

import aicare.net.cn.iweightlibrary.wby.WBYService

interface BluetoothStateCallbacks {
    fun onStateChanged(deviceAddress: String?, state: Int)
}
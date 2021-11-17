package net.huray.solfit.bluetooth.callbacks

import aicare.net.cn.iweightlibrary.entity.BroadData

interface BluetoothDeviceCallbacks {
    fun getAicareDevice(broadData: BroadData)
}
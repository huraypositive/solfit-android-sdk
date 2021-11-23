package net.huray.solfit.bluetooth.callbacks

import aicare.net.cn.iweightlibrary.entity.BroadData

interface BluetoothScanCallbacks {
    fun onScan(state: Int, errorMsg: String?, broadData: BroadData?)
}
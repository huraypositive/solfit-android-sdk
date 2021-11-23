package net.huray.solfit.bluetooth.callbacks

import aicare.net.cn.iweightlibrary.entity.BroadData

interface BluetoothScanCallbacks {
    // STATE : STATE_FAIL(실패)
    //         STATE_SCANNING(성공)
    //         STATE_STOPPED(종료)
    fun onScan(state: Int, errorMsg: String?, deviceList: List<BroadData>?)
}
package net.huray.solfit.bluetooth.callbacks

import aicare.net.cn.iweightlibrary.entity.BroadData
import net.huray.solfit.bluetooth.data.enums.ScanState

interface BluetoothScanCallbacks {
    /**
     *  STATE : ScanState_FAIL
     *          ScanState_SCANNING
     *          ScanState_STOPPED
     */
    fun onScan(state: ScanState, errorMsg: String?, deviceList: List<BroadData>?)
}
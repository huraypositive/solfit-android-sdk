package net.huray.solfit.bluetooth.callbacks

import aicare.net.cn.iweightlibrary.entity.BroadData
import net.huray.solfit.bluetooth.data.enums.ScanState

interface BluetoothScanCallbacks {
    // STATE : ScanState_FAIL(실패)
    //         ScanState_SCANNING(성공)
    //         ScanState_STOPPED(종료)
    fun onScan(state: ScanState, errorMsg: String?, deviceList: List<BroadData>?)
}
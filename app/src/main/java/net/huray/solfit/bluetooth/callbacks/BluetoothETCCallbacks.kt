package net.huray.solfit.bluetooth.callbacks

import aicare.net.cn.iweightlibrary.entity.AlgorithmInfo
import aicare.net.cn.iweightlibrary.entity.DecimalInfo

interface BluetoothETCCallbacks {
    fun onGetResult(did: Int, result: String)
    fun onGetDecimalInfo(decimalInfo: DecimalInfo?)
    fun onGetAlgorithmInfo(algorithmInfo: AlgorithmInfo?)
    fun onGetCMD(cmd: String)
    fun onGetDID(did: Int)
    fun onGetAuthData(
        sources: ByteArray?,
        bleReturn: ByteArray?,
        encrypt: ByteArray?,
        isEquals: Boolean
    )
    fun onGetMode(status: Boolean)
}
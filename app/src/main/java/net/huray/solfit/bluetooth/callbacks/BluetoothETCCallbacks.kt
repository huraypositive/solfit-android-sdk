package net.huray.solfit.bluetooth.callbacks

import aicare.net.cn.iweightlibrary.entity.AlgorithmInfo
import aicare.net.cn.iweightlibrary.entity.BodyFatData
import aicare.net.cn.iweightlibrary.entity.DecimalInfo
import aicare.net.cn.iweightlibrary.wby.WBYService

interface BluetoothETCCallbacks {
    fun onServiceBinded(wbyBinder: WBYService.WBYBinder?)
    fun onServiceUnbinded()
    fun onGetResult(did: Int, result: String)
    fun onGetFatData(b: Boolean, bodyFatData: BodyFatData?)
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
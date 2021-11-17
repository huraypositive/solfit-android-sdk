package net.huray.solfit.bluetooth.callbacks

import aicare.net.cn.iweightlibrary.entity.AlgorithmInfo
import aicare.net.cn.iweightlibrary.entity.BodyFatData
import aicare.net.cn.iweightlibrary.entity.DecimalInfo
import aicare.net.cn.iweightlibrary.entity.WeightData

interface BluetoothDataCallbacks {
    fun onGetWeightData(weightData: WeightData)

    fun onGetSettingStatus(status: Int)

    fun onGetResult(index: Int, result: String)

    fun onGetFatData(b: Boolean, bodyFatData: BodyFatData)

    fun onGetDecimalInfo(decimalInfo: DecimalInfo)

    fun onGetAlgorithmInfo(algorithmInfo: AlgorithmInfo)
}
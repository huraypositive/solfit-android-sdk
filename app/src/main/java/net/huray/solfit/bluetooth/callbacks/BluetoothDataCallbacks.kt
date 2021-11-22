package net.huray.solfit.bluetooth.callbacks

import aicare.net.cn.iweightlibrary.entity.AlgorithmInfo
import aicare.net.cn.iweightlibrary.entity.BodyFatData
import aicare.net.cn.iweightlibrary.entity.DecimalInfo
import aicare.net.cn.iweightlibrary.entity.WeightData

interface BluetoothDataCallbacks {
    fun onGetWeightData(weightData: Double?)

    fun onGetFatData(b: Boolean, bodyFatData: BodyFatData?)

    fun onGetMuscleMass(muscleMass: Float?)

    fun onGetFatRate(fatRate: Float?)

    fun onGetMeasureStatus(status: Int)
}
package net.huray.solfit.bluetooth.callbacks

import aicare.net.cn.iweightlibrary.entity.AlgorithmInfo
import aicare.net.cn.iweightlibrary.entity.BodyFatData
import aicare.net.cn.iweightlibrary.entity.DecimalInfo
import aicare.net.cn.iweightlibrary.entity.WeightData

interface BluetoothDataCallbacks {
    fun onGetMeasureStatus(status: Int)

    fun onGetWeightData(weightData: Double?)

    fun onGetMuscleMass(muscleMass: Float?)

    fun onGetFatRate(fatRate: Float?)
}
package net.huray.solfit.bluetooth.callbacks

import aicare.net.cn.iweightlibrary.entity.AlgorithmInfo
import aicare.net.cn.iweightlibrary.entity.BodyFatData
import aicare.net.cn.iweightlibrary.entity.DecimalInfo
import aicare.net.cn.iweightlibrary.entity.WeightData

interface BluetoothDataCallbacks {

    // state: 측정 시작(STATE_GET_WEIGHT_START)
    //        대기 상태(STATE_GET_WEIGHT_WAITING)
    //        측정 성공(STATE_GET_WEIGHT_SUCCESS)
    fun onGetWeight(state: Int, weightData: Double?)

    // state: 측정 시작(STATE_GET_BODY_COMPOSITION_START)
    //        측정 실패(STATE_GET_BODY_COMPOSITION_FAIL)
    //        측정 성공(STATE_GET_BODY_COMPOSITION_SUCCESS)
    fun onGetBodyComposition(state: Int, fatRate: Float?, muscleMass: Float?)
}


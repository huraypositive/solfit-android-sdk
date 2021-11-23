package net.huray.solfit.bluetooth.callbacks

import aicare.net.cn.iweightlibrary.entity.AlgorithmInfo
import aicare.net.cn.iweightlibrary.entity.BodyFatData
import aicare.net.cn.iweightlibrary.entity.DecimalInfo
import aicare.net.cn.iweightlibrary.entity.WeightData

interface BluetoothDataCallbacks {

    // STATE: 측정 시작(STATE_GET_WEIGHT_START = 0)
    //        대기 상태(STATE_GET_WEIGHT_WAITING = 1)
    //        측정 성공(STATE_GET_WEIGHT_SUCCESS = 2)
    fun onGetWeight(state: Int, weightData: Double?)

    // STATE: 측정 시작(STATE_GET_BODY_COMPOSITION_START = 20)
    //        측정 실패(STATE_GET_BODY_COMPOSITION_FAIL = 21)
    //        측정 성공(STATE_GET_BODY_COMPOSITION_SUCCESS =22)
    fun onGetBodyComposition(state: Int, fatRate: Float?, muscleMass: Float?)
}


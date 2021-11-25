package net.huray.solfit.bluetooth.callbacks

import cn.net.aicare.algorithmutil.BodyFatData
import cn.net.aicare.MoreFatData
import net.huray.solfit.bluetooth.data.enums.BodyCompositionState
import net.huray.solfit.bluetooth.data.enums.WeightState

interface BluetoothDataCallbacks {

    /**
     * STATE: 측정 시작(WeightState_START = 0)
     *        대기 상태(WeightState_WAITING = 1)
     *        측정 성공(WeightState_SUCCESS = 2)
     */
    fun onGetWeight(state: WeightState, weightData: Double?)

    /**
     * STATE: 측정 시작(BodyCompositionState_START = 20)
     *        측정 실패(BodyCompositionState_FAIL = 21)
     *       측정 성공(BodyCompositionState_SUCCESS =22)
     */
    fun onGetBodyComposition(state: BodyCompositionState, bodyFatData: BodyFatData?, moreFatData: MoreFatData?)
}

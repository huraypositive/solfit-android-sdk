package net.huray.solfit.bluetooth.callbacks

import cn.net.aicare.MoreFatData
import cn.net.aicare.algorithmutil.BodyFatData
import net.huray.solfit.bluetooth.data.enums.BodyCompositionState
import net.huray.solfit.bluetooth.data.enums.WeightState

interface BluetoothDataCallbacks {

    /**
     * STATE: WeightState_START
     *        WeightState_WAITING
     *        WeightState_SUCCESS
     */
    fun onGetWeight(state: WeightState, weightData: Double?)

    /**
     * STATE: BodyCompositionState_START
     *        BodyCompositionState_FAIL
     *        BodyCompositionState_FAIL_USER_INFO_NOT_INITIALIZED
     *        BodyCompositionState_SUCCESS
     */
    fun onGetBodyComposition(
        state: BodyCompositionState,
        bodyFatData: BodyFatData?,
        moreFatData: MoreFatData?
    )
}

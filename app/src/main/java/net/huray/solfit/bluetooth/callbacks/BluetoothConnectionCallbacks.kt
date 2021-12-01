package net.huray.solfit.bluetooth.callbacks

import net.huray.solfit.bluetooth.data.enums.ConnectState

interface BluetoothConnectionCallbacks {
    /**
     *  STATE: ConnectState_DISCONNECTED
     *         ConnectState_CONNECTED
     *         ConnectState_SERVICE_DISCOVERED
     *         ConnectState_INDICATION_SUCCESS
     *         ConnectState_CONNECTING
     *         ConnectState_TIME_OUT
     *         ConnectState_ERROR
     */
    fun onStateChanged(deviceAddress: String?, state: ConnectState, errMsg: String?, errCode: Int?)
}
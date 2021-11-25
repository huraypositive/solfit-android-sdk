package net.huray.solfit.bluetooth.callbacks

import net.huray.solfit.bluetooth.data.enums.ConnectState

interface BluetoothConnectionCallbacks {
    /**
     *  STATE: ConnectState_DISCONNECTED = 0
     *         ConnectState_CONNECTED = 1
     *         ConnectState_SERVICE_DISCOVERED = 2
     *         ConnectState_INDICATION_SUCCESS = 3
     *         ConnectState_CONNECTING = 4
     *         ConnectState_TIME_OUT = 5
     *         ConnectState_ERROR = 6
     */
    fun onStateChanged(deviceAddress: String?, state: ConnectState, errMsg: String?, errCode: Int?)
}
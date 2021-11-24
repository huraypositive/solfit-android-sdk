package net.huray.solfit.bluetooth.callbacks

import net.huray.solfit.bluetooth.data.enums.ConnectState

interface BluetoothConnectionCallbacks {
    // STATE: STATE_DISCONNECTED = 0
    //        STATE_CONNECTED = 1
    //        STATE_SERVICE_DISCOVERED = 2
    //        STATE_INDICATION_SUCCESS = 3
    //        STATE_CONNECTING = 4
    //        STATE_TIME_OUT = 5
    //        STATE_ERROR = 6
    fun onStateChanged(deviceAddress: String?, state: ConnectState, errMsg: String?, errCode: Int?)
}
package net.huray.solfit.bluetooth.callbacks

interface BluetoothConnectionCallbacks {
    // state: STATE_DISCONNECTED
    fun onStateChanged(deviceAddress: String?, state: Int, errMsg: String?, errCode: Int?)
}
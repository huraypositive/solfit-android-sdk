package net.huray.solfit.bluetooth.callbacks

interface BluetoothConnectionCallbacks {
    fun onStateChanged(deviceAddress: String?, state: Int)
    fun onError(s: String?, i: Int)
}
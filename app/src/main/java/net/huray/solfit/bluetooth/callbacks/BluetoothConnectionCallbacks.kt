package net.huray.solfit.bluetooth.callbacks

interface BluetoothConnectionCallbacks {
    fun onStateChanged(isEnabled: Boolean)
    fun onError(s: String?, i: Int)
}
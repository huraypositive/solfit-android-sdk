package net.huray.solfit.bluetooth

import aicare.net.cn.iweightlibrary.AiFitSDK
import aicare.net.cn.iweightlibrary.bleprofile.BleProfileServiceReadyActivity
import aicare.net.cn.iweightlibrary.entity.*
import aicare.net.cn.iweightlibrary.wby.WBYService
import aicare.net.cn.iweightlibrary.wby.WBYService.WBYBinder
import android.content.Context
import android.content.Intent
import net.huray.solfit.bluetooth.callbacks.*


open class SolfitBluetooth: BleProfileServiceReadyActivity<WBYService.WBYBinder>() {
    protected var mSolfitBinder: WBYBinder? = null
    protected val permissionDeniedMessage: String = "1"

    protected var bluetoothStateCallback = object : BluetoothStateCallbacks {
        override fun onStateChanged(deviceAddress: String, state: Int) {
            TODO("디폴트 로직")
        }
    }

    protected var bluetoothBondCallbacks = object : BluetoothBondCallbacks {
        override fun onServiceBinded(wbyBinder: WBYBinder?) {
            TODO("디폴트 로직")
        }

        override fun onServiceUnbinded() {
            TODO("디폴트 로직")
        }
    }
    protected var bluetoothConnectionCallbacks = object : BluetoothConnectionCallbacks {
        override fun onError(s: String?, i: Int) {
            TODO("디폴트 로직")
        }
    }
    protected var bluetoothDataCallbacks = object : BluetoothDataCallbacks {
        override fun onGetWeightData(weightData: WeightData) {
            TODO("디폴트 로직")
        }

        override fun onGetSettingStatus(status: Int) {
            TODO("디폴트 로직")
        }

        override fun onGetResult(index: Int, result: String) {
            TODO("디폴트 로직")
        }

        override fun onGetFatData(b: Boolean, bodyFatData: BodyFatData) {
            TODO("디폴트 로직")
        }

        override fun onGetDecimalInfo(decimalInfo: DecimalInfo) {
            TODO("디폴트 로직")
        }

        override fun onGetAlgorithmInfo(algorithmInfo: AlgorithmInfo) {
            TODO("디폴트 로직")
        }
    }
    protected var bluetoothErrorCallbacks = object : BluetoothErrorCallbacks {
        override fun onError(s: String?, i: Int) {
            TODO("디폴트 로직")
        }
    }

    protected var bluetoothDeviceCallbacks = object: BluetoothDeviceCallbacks {
        override fun getAicareDevice(broadData: BroadData) {
            TODO("디폴트 로직")
        }

    }

    fun scan(){
        startScan()
    }

    fun endScan(){
        stopScan()
    }

    fun initSolfitSDK() {
        // solfit setting
        AiFitSDK.getInstance().init(this)
    }

    override fun onServiceBinded(wbyBinder: WBYService.WBYBinder?) {
        bluetoothBondCallbacks.onServiceBinded(wbyBinder)
    }

    override fun onServiceUnbinded() {
        bluetoothBondCallbacks.onServiceUnbinded()
    }

    override fun getAicareDevice(broadData: BroadData) {
        bluetoothDeviceCallbacks.getAicareDevice(broadData)
    }

    override fun onError(s: String?, i: Int) {
        bluetoothErrorCallbacks.onError(s,i)
    }

    // Manage BlueTooth Data
    override fun onGetWeightData(weightData: WeightData) {
        bluetoothDataCallbacks.onGetWeightData(weightData)
    }

    override fun onGetSettingStatus(status: Int) {
        bluetoothDataCallbacks.onGetSettingStatus(status)
    }

    override fun onGetResult(index: Int, result: String) {
        bluetoothDataCallbacks.onGetResult(index, result)
    }

    override fun onGetFatData(b: Boolean, bodyFatData: BodyFatData) {
        bluetoothDataCallbacks.onGetFatData(b, bodyFatData)
    }

    override fun onGetDecimalInfo(demicalInfo: DecimalInfo) {
        bluetoothDataCallbacks.onGetDecimalInfo(demicalInfo)
    }

    override fun onGetAlgorithmInfo(algorithmInfo: AlgorithmInfo) {
        bluetoothDataCallbacks.onGetAlgorithmInfo(algorithmInfo)
    }
}
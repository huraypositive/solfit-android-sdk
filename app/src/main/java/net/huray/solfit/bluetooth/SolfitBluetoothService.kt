package net.huray.solfit.bluetooth

import aicare.net.cn.iweightlibrary.AiFitSDK
import aicare.net.cn.iweightlibrary.entity.*
import aicare.net.cn.iweightlibrary.utils.AicareBleConfig
import aicare.net.cn.iweightlibrary.utils.ParseData
import aicare.net.cn.iweightlibrary.wby.WBYService
import aicare.net.cn.iweightlibrary.wby.WBYService.*
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.*
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import net.huray.solfit.bluetooth.callbacks.*
import net.huray.solfit.bluetooth.data.UserInfo
import java.lang.Exception

//BleProfileServiceReadyActivity 역할
class SolfitBluetoothService: Service() {
    private val TAG = this::class.java.simpleName
    private val binder = ServiceBinder()
    private var mService: WBYBinder? = null
    private var mIsScanning = false
    private var adapter: BluetoothAdapter? = null

    private lateinit var user: UserInfo
    private lateinit var algorithmInfo: AlgorithmInfo

    // Interface
    private var bluetoothBondCallbacks: BluetoothBondCallbacks? = null
    private var bluetoothConnectionCallbacks: BluetoothConnectionCallbacks? = null
    private var bluetoothDataCallbacks: BluetoothDataCallbacks?= null
    private var bluetoothDeviceCallbacks: BluetoothDeviceCallbacks?= null
    private var bluetoothErrorCallbacks: BluetoothErrorCallbacks?= null
    private var bluetoothStateCallbacks: BluetoothStateCallbacks?= null

    private val handler = Handler()
    private val startScanRunnable = Runnable { startScan() }
    private val stopScanRunnable = Runnable {
        stopScan()
        handler.post(startScanRunnable)
    }

    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as WBYService.WBYBinder
            mService = binder.service.WBYBinder()
            val bleService: WBYBinder = mService as WBYBinder

            onServiceBinded(bleService)
            if (bleService.isConnected) {
                onStateChanged(bleService.deviceAddress, 1)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
            onServiceUnbinded()
        }
    }

    private val mLEScanCallback: LeScanCallback =
        LeScanCallback { device, rssi, scanRecord ->
            Log.e(TAG, "onLeScan")
            if (device != null) {
                Log.e(
                    TAG,
                    "address: " + device.address + "; name: " + device.name
                )
                Log.e(TAG, ParseData.byteArr2Str(scanRecord))
                val broadData = AicareBleConfig.getBroadData(device, rssi, scanRecord)
                if (broadData != null) {
                    handler.post {
                        bluetoothDeviceCallbacks?.getAicareDevice(broadData)
                    }
                }
            }
        }

    private val commonBroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val did: Int
            val action = intent?.action
            if (ACTION_STATE_CHANGED == action) {
                did = intent.getIntExtra(EXTRA_STATE, -1)
                bluetoothStateChanged(did)
            } else {
                val result: String?
                if (ACTION_CONNECT_STATE_CHANGED == action) {
                    did = intent.getIntExtra(EXTRA_CONNECT_STATE, -1)
                    result = intent.getStringExtra(EXTRA_DEVICE_ADDRESS)
                    bluetoothStateCallbacks?.onStateChanged(result, did)
                } else {
                    val cmd: String?
                    if (ACTION_CONNECT_ERROR == action) {
                        cmd = intent.getStringExtra(EXTRA_ERROR_MSG)
                        val errCode =
                            intent.getIntExtra(EXTRA_ERROR_CODE, -1)
                        bluetoothErrorCallbacks?.onError(cmd, errCode)
                    } else if (ACTION_WEIGHT_DATA == action) {
                        val weightData =
                            intent.getSerializableExtra(EXTRA_WEIGHT_DATA) as WeightData
                        bluetoothDataCallbacks?.onGetWeightData(weightData)
                    } else if (ACTION_SETTING_STATUS_CHANGED == action) {
                        did = intent.getIntExtra(EXTRA_SETTING_STATUS, -1)
                        bluetoothDataCallbacks?.onGetSettingStatus(did)
                    } else if (ACTION_RESULT_CHANGED == action) {
                        did = intent.getIntExtra(EXTRA_RESULT_INDEX, -1)
                        result = intent.getStringExtra(EXTRA_RESULT) ?: ""
                        bluetoothDataCallbacks?.onGetResult(did, result)
                    } else {
                        val status: Boolean
                        if (ACTION_FAT_DATA == action) {
                            status = intent.getBooleanExtra(
                                EXTRA_IS_HISTORY,
                                false
                            )
                            val bodyFatData =
                                intent.getSerializableExtra(EXTRA_FAT_DATA) as BodyFatData
                            bluetoothDataCallbacks?.onGetFatData(status, bodyFatData)
                        } else if (ACTION_AUTH_DATA == action) {
                            val sources =
                                intent.getByteArrayExtra(EXTRA_SOURCE_DATA)
                            val bleReturn =
                                intent.getByteArrayExtra(EXTRA_BLE_DATA)
                            val encrypt =
                                intent.getByteArrayExtra(EXTRA_ENCRYPT_DATA)
                            val isEquals = intent.getBooleanExtra(
                                EXTRA_IS_EQUALS,
                                false
                            )
                            onGetAuthData(
                                sources,
                                bleReturn,
                                encrypt,
                                isEquals
                            )
                        } else if (ACTION_DID == action) {
                            did = intent.getIntExtra(EXTRA_DID, -1)
                            onGetDID(did)
                        } else if (ACTION_DECIMAL_INFO == action) {
                            val decimalInfo =
                                intent.getSerializableExtra(EXTRA_DECIMAL_INFO) as DecimalInfo?
                            bluetoothDataCallbacks?.onGetDecimalInfo(decimalInfo)
                        } else if (ACTION_CMD == action) {
                            cmd = intent.getStringExtra(EXTRA_CMD) ?: ""
                            onGetCMD(cmd)
                        } else if (ACTION_ALGORITHM_INFO == action) {
                            algorithmInfo =
                                intent.getSerializableExtra(EXTRA_ALGORITHM_INFO) as AlgorithmInfo
                            bluetoothDataCallbacks?.onGetAlgorithmInfo(algorithmInfo)
                        } else if (ACTION_SET_MODE == action) {
                            status = intent.getBooleanExtra(
                                EXTRA_SET_MODE,
                                false
                            )
                            onGetMode(status)
                        }
                    }
                }
            }
        }
    }

    fun initilize(
        bluetoothBondCallbacks: BluetoothBondCallbacks? = null,
        bluetoothConnectionCallbacks: BluetoothConnectionCallbacks?= null,
        bluetoothDataCallbacks: BluetoothDataCallbacks?= null,
        bluetoothDeviceCallbacks: BluetoothDeviceCallbacks?= null,
        bluetoothErrorCallbacks: BluetoothErrorCallbacks?= null,
        bluetoothStateCallbacks: BluetoothStateCallbacks?= null
    ): Boolean{
        this.bluetoothBondCallbacks = bluetoothBondCallbacks
        this.bluetoothConnectionCallbacks = bluetoothConnectionCallbacks
        this.bluetoothDataCallbacks = bluetoothDataCallbacks
        this.bluetoothDeviceCallbacks = bluetoothDeviceCallbacks
        this.bluetoothErrorCallbacks = bluetoothErrorCallbacks
        this.bluetoothStateCallbacks = bluetoothStateCallbacks

        return isBLEEnabled()
    }

    private fun onInitialize() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = bluetoothManager.adapter
    }

    override fun onBind(intent: Intent?): IBinder? {
        onInitialize()
        bindService(null as String?)
        application.registerReceiver(
            commonBroadcastReceiver,
            makeIntentFilter()
        )
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        unbindService()
        return super.onUnbind(intent)
    }

    private fun bindService(address: String?) {
        val service = Intent(this, WBYService::class.java)
        if (!TextUtils.isEmpty(address)) {
            service.putExtra(EXTRA_DEVICE_ADDRESS, address)
            startService(service)
        }
        bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindService() {
        try {
            unbindService(mServiceConnection)
            mService = null
            onServiceUnbinded()
        } catch (var2: IllegalArgumentException) {}
    }

    private fun connect() {}
    private fun disconnect() {
        stopSelf()
    }

    private fun onStateChanged(deviceAddress: String?, state: Int) {
        when (state) {
            0 -> unbindService()
            else -> {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            application.unregisterReceiver(commonBroadcastReceiver)
            unbindService()
        } catch (var2: Exception) {
            var2.printStackTrace()
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_ADMIN")
    private fun bluetoothStateChanged(state: Int) {
        when (state) {
            13 -> {
                if (mService != null) {
                    mService!!.disconnect()
                }
                stopScan()
            }
            else -> {
            }
        }
    }
    private fun makeIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_CONNECT_STATE_CHANGED)
        intentFilter.addAction(ACTION_CONNECT_STATE_CHANGED)
        intentFilter.addAction(ACTION_CONNECT_ERROR)
        intentFilter.addAction(ACTION_WEIGHT_DATA)
        intentFilter.addAction(ACTION_SETTING_STATUS_CHANGED)
        intentFilter.addAction(ACTION_RESULT_CHANGED)
        intentFilter.addAction(ACTION_FAT_DATA)
        intentFilter.addAction(ACTION_AUTH_DATA)
        intentFilter.addAction(ACTION_DID)
        intentFilter.addAction(ACTION_DECIMAL_INFO)
        intentFilter.addAction(ACTION_CMD)
        intentFilter.addAction(ACTION_ALGORITHM_INFO)
        intentFilter.addAction(ACTION_SET_MODE)
        return intentFilter
    }

    fun isBLEEnabled(): Boolean {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter
        return adapter != null && adapter.isEnabled
    }

    fun startScan() {
        if (!AiFitSDK.getInstance().isInitOk) {
            Log.e("AiFitSDK", "请先调用AiFitSDK.getInstance().init()")
            throw SecurityException("请先调用AiFitSDK.getInstance().init().(Please call AiFitSDK.getInstance().init() first.)")
        } else {
            if (isBLEEnabled()) {
                if (!mIsScanning) {
                    adapter!!.startLeScan(mLEScanCallback)
                    mIsScanning = true
                    handler.postDelayed(stopScanRunnable, 60000L)
                }
            } else {
                showBLEDialog()
            }
        }
    }

    fun stopScan() {
        handler.removeCallbacks(startScanRunnable)
        handler.removeCallbacks(stopScanRunnable)
        if (mIsScanning) {
            if (adapter != null) {
                adapter!!.stopLeScan(mLEScanCallback)
            }
            mIsScanning = false
        }
    }

    fun setUserData(sex: Int, age: Int, height: Int, weight: Float){
        user = UserInfo(sex,age,height,weight)
    }

    private fun getBodyFat() =  AicareBleConfig.getBodyFatData(
        algorithmInfo.algorithmId,
        user.sex,
        user.age,
        ParseData.getKgWeight((user.weight * 10).toDouble(), algorithmInfo.decimalInfo).toDouble(),
        user.height,algorithmInfo.adc
    )

    fun getBodyFatRate() = getBodyFat().bfr.toFloat()

    fun getMuscleMass() = AicareBleConfig.getMoreFatData(
        user.sex,
        user.height,
        user.weight.toDouble(),
        getBodyFatRate().toDouble(),
        getBodyFat().rom,
        getBodyFat().pp
    ).muscleMass.toFloat()

    fun onGetWeightData(weightData: WeightData){
        bluetoothDataCallbacks?.onGetWeightData(weightData)
    }

    fun setWeightData(broad: BroadData){

    }

    fun getBodyFatData() {
        val bodyFatData =
        AicareBleConfig.getBodyFatData(
            algorithmInfo.algorithmId,
            user.sex,
            user.age,
            ParseData.getKgWeight((user.weight * 10).toDouble(), algorithmInfo!!.decimalInfo)
                .toDouble(),
            user.height,
            algorithmInfo.adc
        )
    }

    fun getMuscleMassData() {

    }

    fun onGetDID(did: Int){}

    fun onGetCMD(cmd: String){}

    fun onGetMode(status:Boolean){}

    fun onGetAuthData(
        sources: ByteArray?,
        bleReturn: ByteArray?,
        encrypt: ByteArray?,
        isEquals: Boolean
    ) {}

    fun onServiceBinded(var1: WBYBinder?){}
    fun onServiceUnbinded(){}

    protected fun showBLEDialog() {
        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        //블루투스 Enable
        //기존에 StartActivityForResult 메소드 사용했엇는데 변경.
        //TODO('requires android.permission.BLUETOOTH_CONNECT 이슈 확인')
        //startActivity(enableIntent)
    }

    inner class ServiceBinder: Binder() {
        fun getService(): SolfitBluetoothService {
            return this@SolfitBluetoothService
        }
    }
}
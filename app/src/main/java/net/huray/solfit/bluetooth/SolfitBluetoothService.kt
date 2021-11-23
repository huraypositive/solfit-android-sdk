package net.huray.solfit.bluetooth

import aicare.net.cn.iweightlibrary.AiFitSDK
import aicare.net.cn.iweightlibrary.entity.*
import aicare.net.cn.iweightlibrary.entity.BroadData.AddressComparator
import aicare.net.cn.iweightlibrary.utils.AicareBleConfig
import aicare.net.cn.iweightlibrary.utils.AicareBleConfig.SettingStatus.*
import aicare.net.cn.iweightlibrary.utils.ParseData
import aicare.net.cn.iweightlibrary.wby.WBYService
import aicare.net.cn.iweightlibrary.wby.WBYService.*
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.*
import android.bluetooth.BluetoothManager
import android.content.*
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresPermission
import net.huray.solfit.bluetooth.callbacks.*
import net.huray.solfit.bluetooth.data.UserInfo
import net.huray.solfit.bluetooth.util.*
import java.lang.Exception

class SolfitBluetoothService : Service() {
    private val TAG = this::class.java.simpleName
    private val binder = ServiceBinder()
    private var mService: WBYBinder? = null
    private var mIsScanning = false
    private var adapter: BluetoothAdapter? = null

    private lateinit var userInfo: UserInfo
    private lateinit var algorithmInfo: AlgorithmInfo
    private var mWeight: Double? = 0.0
    private val mDeviceList = ArrayList<BroadData>()

    // Interface
    private var bluetoothConnectionCallbacks: BluetoothConnectionCallbacks? = null
    private var bluetoothDataCallbacks: BluetoothDataCallbacks? = null
    private var bluetoothScanCallbacks: BluetoothScanCallbacks? = null
    private var bluetoothETCCallback: BluetoothETCCallbacks? = null

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
                bluetoothConnectionCallbacks?.onStateChanged(
                    bleService.deviceAddress,
                    net.huray.solfit.bluetooth.util.STATE_CONNECTED,
                    null, null
                )
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
            mDeviceList.clear()
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
                    addDevice(broadData)
                    handler.post {
                        bluetoothScanCallbacks?.onScan(STATE_SCANNING, null, mDeviceList)
                    }
                }
            }
        }

    private val commonBroadcastReceiver = object : BroadcastReceiver() {
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
                    bluetoothConnectionCallbacks?.onStateChanged(result, did, null, null)
                } else {
                    val cmd: String?
                    if (ACTION_CONNECT_ERROR == action) {
                        cmd = intent.getStringExtra(EXTRA_ERROR_MSG)
                        val errCode =
                            intent.getIntExtra(EXTRA_ERROR_CODE, -1)
                        bluetoothConnectionCallbacks?.onStateChanged(
                            null,
                            STATE_ERROR,
                            cmd,
                            errCode
                        )
                    } else if (ACTION_WEIGHT_DATA == action) {
                        val weightData =
                            intent.getSerializableExtra(EXTRA_WEIGHT_DATA) as WeightData
                        onGetWeightData(weightData)
                    } else if (ACTION_SETTING_STATUS_CHANGED == action) {
                        did = intent.getIntExtra(EXTRA_SETTING_STATUS, -1)
                        when (did) {
                            NORMAL, LOW_POWER
                            -> bluetoothDataCallbacks?.onGetWeight(did, null)
                            ADC_MEASURED_ING, ADC_ERROR
                            -> bluetoothDataCallbacks?.onGetBodyComposition(did, null, null)
                        }
                    } else if (ACTION_RESULT_CHANGED == action) {
                        did = intent.getIntExtra(EXTRA_RESULT_INDEX, -1)
                        result = intent.getStringExtra(EXTRA_RESULT) ?: ""
                        bluetoothETCCallback?.onGetResult(did, result)
                    } else {
                        val status: Boolean
                        if (ACTION_FAT_DATA == action) {
                            status = intent.getBooleanExtra(
                                EXTRA_IS_HISTORY,
                                false
                            )
                            val bodyFatData =
                                intent.getSerializableExtra(EXTRA_FAT_DATA) as BodyFatData
                            bluetoothETCCallback?.onGetFatData(status, bodyFatData)
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
                            bluetoothETCCallback?.onGetDecimalInfo(decimalInfo)
                        } else if (ACTION_CMD == action) {
                            cmd = intent.getStringExtra(EXTRA_CMD) ?: ""
                            onGetCMD(cmd)
                        } else if (ACTION_ALGORITHM_INFO == action) {
                            algorithmInfo =
                                intent.getSerializableExtra(EXTRA_ALGORITHM_INFO) as AlgorithmInfo
                            bluetoothDataCallbacks?.onGetBodyComposition(
                                STATE_GET_BODY_COMPOSITION_SUCCESS,
                                getBodyFatRate(),
                                getMuscleMass()
                            )
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
        bluetoothScanCallbacks: BluetoothScanCallbacks? = null,
        bluetoothConnectionCallbacks: BluetoothConnectionCallbacks? = null,
        bluetoothDataCallbacks: BluetoothDataCallbacks? = null,
        bluetoothETCCallback: BluetoothETCCallbacks? = null
    ) {
        this.bluetoothScanCallbacks = bluetoothScanCallbacks
        this.bluetoothConnectionCallbacks = bluetoothConnectionCallbacks
        this.bluetoothDataCallbacks = bluetoothDataCallbacks
        this.bluetoothETCCallback = bluetoothETCCallback
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
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    fun startConnect(address: String?) {
        bindService(address)
    }

    fun disconnect() {
        if (mIsScanning) {
            stopScan()
        }
        mService?.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            application.unregisterReceiver(commonBroadcastReceiver)
            unbindService()
        } catch (e: Exception) {
            e.printStackTrace()
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
                bluetoothScanCallbacks?.onScan(
                    STATE_FAIL,
                    resources.getString(R.string.error_bluetooth_not_enabled), null
                )
            }
        }
    }

    fun stopScan() {
        handler.removeCallbacks(startScanRunnable)
        handler.removeCallbacks(stopScanRunnable)
        if (mIsScanning) {
            if (adapter != null) {
                bluetoothScanCallbacks?.onScan(STATE_STOPPED, null, null)
                adapter!!.stopLeScan(mLEScanCallback)
            }
            mIsScanning = false
        }
    }

    private fun addDevice(device: BroadData){
        if(!mDeviceList.contains(device)){
            mDeviceList.add(device)
        }
    }

    fun setUserInfo(sex: Int = 1, age: Int = 25, height: Int = 174) {
        userInfo = UserInfo(sex, age, height)
    }

    private fun getBodyFatData() = AicareBleConfig.getBodyFatData(
        algorithmInfo.algorithmId,
        userInfo.sex,
        userInfo.age,
        ParseData.getKgWeight(mWeight?.times(10.0)!!, algorithmInfo.decimalInfo).toDouble(),
        userInfo.height, algorithmInfo.adc
    )

    fun getBodyFatRate() = getBodyFatData().bfr.toFloat()

    fun getMuscleMass() = AicareBleConfig.getMoreFatData(
        userInfo.sex,
        userInfo.height,
        mWeight!!,
        getBodyFatRate().toDouble(),
        getBodyFatData().rom,
        getBodyFatData().pp
    ).muscleMass.toFloat()

    fun onGetWeightData(weightData: WeightData?) {
        mWeight = (weightData?.weight?.div(10f))
        bluetoothDataCallbacks?.onGetWeight(STATE_GET_WEIGHT_SUCCESS, mWeight)
    }

    fun onGetDID(did: Int) {
        bluetoothETCCallback?.onGetDID(did)
    }

    fun onGetCMD(cmd: String) {
        bluetoothETCCallback?.onGetCMD(cmd)
    }

    fun onGetMode(status: Boolean) {
        bluetoothETCCallback?.onGetMode(status)
    }

    fun onGetAuthData(
        sources: ByteArray?,
        bleReturn: ByteArray?,
        encrypt: ByteArray?,
        isEquals: Boolean
    ) {
        bluetoothETCCallback?.onGetAuthData(sources, bleReturn, encrypt, isEquals)
    }

    fun onServiceBinded(wbyBinder: WBYBinder?) {
        bluetoothETCCallback?.onServiceBinded(wbyBinder)
    }

    fun onServiceUnbinded() {
        bluetoothETCCallback?.onServiceUnbinded()
    }

    protected fun showBLEDialog() {
        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        //블루투스 Enable
        //기존에 StartActivityForResult 메소드 사용했엇는데 변경.
        //TODO('requires android.permission.BLUETOOTH_CONNECT 이슈 확인')
        //startActivity(enableIntent)
    }

    inner class ServiceBinder : Binder() {
        fun getService(): SolfitBluetoothService {
            return this@SolfitBluetoothService
        }
    }
}
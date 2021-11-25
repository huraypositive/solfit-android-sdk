package net.huray.solfit.bluetooth

import aicare.net.cn.iweightlibrary.AiFitSDK
import aicare.net.cn.iweightlibrary.entity.*
import aicare.net.cn.iweightlibrary.utils.AicareBleConfig
import aicare.net.cn.iweightlibrary.utils.AicareBleConfig.SettingStatus.*
import aicare.net.cn.iweightlibrary.utils.ParseData
import aicare.net.cn.iweightlibrary.wby.WBYService
import aicare.net.cn.iweightlibrary.wby.WBYService.*
import android.Manifest
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
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.huray.solfit.bluetooth.callbacks.*
import net.huray.solfit.bluetooth.data.datastore.UserInfoDataStore
import net.huray.solfit.bluetooth.data.UserInfo
import net.huray.solfit.bluetooth.data.enums.BodyCompositionState
import net.huray.solfit.bluetooth.data.enums.ConnectState
import net.huray.solfit.bluetooth.data.enums.ScanState
import net.huray.solfit.bluetooth.data.enums.WeightState
import java.lang.Exception

open class SolfitBluetoothService : Service() {
    private val TAG = this::class.java.simpleName
    private lateinit var context: Context
    private val binder = ServiceBinder()
    private var mService: WBYBinder? = null
    private var mIsScanning = false
    private var adapter: BluetoothAdapter? = null

    private lateinit var userInfo: UserInfo
    private var previousUserInfo: UserInfo? = null

    private lateinit var userInfoDataStore: UserInfoDataStore
    private lateinit var algorithmInfo: AlgorithmInfo
    private var mWeight: Double? = 0.0
    private val mDeviceList = ArrayList<BroadData>()

    // Interface
    private var bluetoothConnectionCallbacks: BluetoothConnectionCallbacks? = null
    private var bluetoothDataCallbacks: BluetoothDataCallbacks? = null
    private var bluetoothScanCallbacks: BluetoothScanCallbacks? = null

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

            if (bleService.isConnected) {
                bluetoothConnectionCallbacks?.onStateChanged(
                    bleService.deviceAddress,
                    ConnectState.CONNECTED,
                    null, null
                )
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
            mDeviceList.clear()
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
                        bluetoothScanCallbacks?.onScan(ScanState.SCANNING, null, mDeviceList)
                    }
                }
            }
        }

    private val commonBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val did: Int
            val action = intent?.action
            val result: String?
            val cmd: String?
            when(action) {
                ACTION_STATE_CHANGED -> {
                    did = intent.getIntExtra(EXTRA_STATE, -1)
                    bluetoothStateChanged(did)
                }
                ACTION_CONNECT_STATE_CHANGED -> {
                    did = intent.getIntExtra(EXTRA_CONNECT_STATE, -1)
                    result = intent.getStringExtra(EXTRA_DEVICE_ADDRESS)
                    bluetoothConnectionCallbacks?.onStateChanged(result, ConnectState.getConnectState(did), null, null)
                }
                ACTION_CONNECT_ERROR -> {
                    cmd = intent.getStringExtra(EXTRA_ERROR_MSG)
                    val errCode =
                        intent.getIntExtra(EXTRA_ERROR_CODE, -1)
                    bluetoothConnectionCallbacks?.onStateChanged(
                        null,
                        ConnectState.ERROR,
                        cmd,
                        errCode
                    )
                }
                ACTION_WEIGHT_DATA -> {
                    val weightData =
                        intent.getSerializableExtra(EXTRA_WEIGHT_DATA) as WeightData
                    onGetWeightData(weightData)
                }
                ACTION_SETTING_STATUS_CHANGED -> {
                    did = intent.getIntExtra(EXTRA_SETTING_STATUS, -1)
                    when (did) {
                        NORMAL, LOW_POWER
                        -> bluetoothDataCallbacks?.onGetWeight(WeightState.getWeightState(did),
                            null)
                        ADC_MEASURED_ING, ADC_ERROR
                        -> bluetoothDataCallbacks?.onGetBodyComposition(BodyCompositionState.getBodyCompositionState(did),
                            null, null)
                    }
                }
                ACTION_ALGORITHM_INFO -> {
                    algorithmInfo =
                        intent.getSerializableExtra(EXTRA_ALGORITHM_INFO) as AlgorithmInfo
                    bluetoothDataCallbacks?.onGetBodyComposition(
                        BodyCompositionState.SUCCESS,
                        getBodyFatData(),
                        getMoreFatData()
                    )
                }
            }
        }
    }

    fun initilize(
        context: Context,
        bluetoothScanCallbacks: BluetoothScanCallbacks? = null,
        bluetoothConnectionCallbacks: BluetoothConnectionCallbacks? = null,
        bluetoothDataCallbacks: BluetoothDataCallbacks? = null,
    ) {
        this.context = context
        this.bluetoothScanCallbacks = bluetoothScanCallbacks
        this.bluetoothConnectionCallbacks = bluetoothConnectionCallbacks
        this.bluetoothDataCallbacks = bluetoothDataCallbacks
        userInfoDataStore = UserInfoDataStore(context)
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

    private fun isBlEAvailable() = adapter != null

    fun isBLEEnabled(): Boolean {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter
        return adapter != null && adapter.isEnabled
    }

    fun startScan() {
        if (!AiFitSDK.getInstance().isInitOk) {
            Log.e("AiFitSDK", "请先调用AiFitSDK.getInstance().init()")
            throw SecurityException("请先调用AiFitSDK.getInstance().init().(Please call AiFitSDK.getInstance().init() first.)")
        }

        if(!isBlEAvailable()) {
            bluetoothScanCallbacks?.onScan(
                ScanState.FAIL,
                resources.getString(R.string.error_feature_not_supported), null
            )
            return
        }

        if(!isBLEEnabled()) {
            bluetoothScanCallbacks?.onScan(
                ScanState.FAIL,
                resources.getString(R.string.error_bluetooth_not_enabled), null
            )
            showBLEDialog()
            return
        }

        if(!hasPermissions()) {
            requestPermissions()
            return
        }

        if (!mIsScanning) {
            adapter!!.startLeScan(mLEScanCallback)
            mIsScanning = true
            handler.postDelayed(stopScanRunnable, 60000L)
        }
    }

    fun stopScan() {
        handler.removeCallbacks(startScanRunnable)
        handler.removeCallbacks(stopScanRunnable)
        if (mIsScanning) {
            if (adapter != null) {
                bluetoothScanCallbacks?.onScan(ScanState.STOPPED, null, null)
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

    fun setUserInfo(userInfo: UserInfo) {
        this.userInfo = userInfo
        CoroutineScope(Dispatchers.IO).launch {
            saveUserInfo()
            getUserInfo()
        }
    }

    private suspend fun saveUserInfo() {
        userInfoDataStore.apply {
            setSex(userInfo.sex)
            setAge(userInfo.age)
            setheight(userInfo.height)
        }
    }

    private suspend fun getUserInfo() {
        previousUserInfo = UserInfo(userInfoDataStore.sex.first(),userInfoDataStore.age.first(),userInfoDataStore.height.first())
    }

    fun getPreviousUserInfo()= previousUserInfo

    private fun getBodyFatData() = AicareBleConfig.getBodyFatData(
        algorithmInfo.algorithmId,
        userInfo.sex,
        userInfo.age,
        ParseData.getKgWeight(mWeight?.times(10.0)!!, algorithmInfo.decimalInfo).toDouble(),
        userInfo.height, algorithmInfo.adc
    )

    private fun getBodyFatRate() = getBodyFatData().bfr.toFloat()

    private fun getMoreFatData() = AicareBleConfig.getMoreFatData(
        userInfo.sex,
        userInfo.height,
        mWeight!!,
        getBodyFatRate().toDouble(),
        getBodyFatData().rom,
        getBodyFatData().pp
    )

    private fun onGetWeightData(weightData: WeightData?) {
        mWeight = (weightData?.weight?.div(10f))
        bluetoothDataCallbacks?.onGetWeight(WeightState.SUCCESS, mWeight)
    }

    private fun hasPermissions(): Boolean = TedPermission.isGranted(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private fun requestPermissions() {
        TedPermission.with(context)
            .setPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .setDeniedMessage(context.getString(R.string.error_denied_permission))
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    if (!mIsScanning) {
                        adapter!!.startLeScan(mLEScanCallback)
                        mIsScanning = true
                        handler.postDelayed(stopScanRunnable, 60000L)
                    }
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    bluetoothScanCallbacks?.onScan(ScanState.FAIL,context.getString(R.string.error_denied_permission),
                                                    null)
                }
            })
            .check()
    }

    private fun showBLEDialog() {
        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(enableIntent)
    }

    inner class ServiceBinder : Binder() {
        fun getService(): SolfitBluetoothService {
            return this@SolfitBluetoothService
        }
    }
}
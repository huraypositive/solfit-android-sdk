package net.huray.solfit.bluetooth

import aicare.net.cn.iweightlibrary.AiFitSDK
import aicare.net.cn.iweightlibrary.entity.*
import aicare.net.cn.iweightlibrary.utils.AicareBleConfig
import aicare.net.cn.iweightlibrary.utils.AicareBleConfig.SettingStatus.LOW_POWER
import aicare.net.cn.iweightlibrary.utils.AicareBleConfig.SettingStatus.NORMAL
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.net.aicare.MoreFatData
import cn.net.aicare.algorithmutil.BodyFatData
import net.huray.solfit.bluetooth.callbacks.BluetoothConnectionCallbacks
import net.huray.solfit.bluetooth.callbacks.BluetoothDataCallbacks
import net.huray.solfit.bluetooth.callbacks.BluetoothScanCallbacks
import net.huray.solfit.bluetooth.data.enums.BodyCompositionState
import net.huray.solfit.bluetooth.data.enums.ConnectState
import net.huray.solfit.bluetooth.data.enums.ScanState
import net.huray.solfit.bluetooth.data.enums.WeightState


class UserActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName
    private var solfitBluetoothService: SolfitBluetoothService? = null
    private var isServiceConnected = false
    private var serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isServiceConnected = true
            val serviceBinder = service as SolfitBluetoothService.ServiceBinder
            solfitBluetoothService = serviceBinder.getService().apply {
                setUserInfo(1, 33, 175)
                initilize(
                    this@UserActivity,
                    bluetoothScanCallbacks = object : BluetoothScanCallbacks {
                        override fun onScan(state: ScanState, errorMsg: String?, deviceList: List<BroadData>?) {
                            val textVScanResult = findViewById<TextView>(R.id.textV_scan_result)
                            when (state) {
                                ScanState.FAIL -> textVScanResult.text = errorMsg
                                ScanState.SCANNING -> {
                                    var deviceListString = ""
                                    for(index in deviceList!!){
                                        deviceListString += index.address.toString() + "\n"
                                    }
                                    textVScanResult.text = deviceListString
                                }
                                ScanState.STOPPED -> {}
                            }
                        }
                    },
                    bluetoothConnectionCallbacks = object : BluetoothConnectionCallbacks {
                        override fun onStateChanged(
                            deviceAddress: String?,
                            state: ConnectState,
                            errMsg: String?,
                            errCode: Int?
                        ) {
                            findViewById<TextView>(R.id.textV_get_connect_state_chagne).apply {
                                text = when (state) {
                                    ConnectState.DISCONNECTED -> "DISCONNECTED"
                                    ConnectState.CONNECTED -> "CONNECTED"
                                    ConnectState.INDICATION_SUCCESS -> "INDICATION_SUCCESS"
                                    ConnectState.SERVICE_DISCOVERED -> "SERVICE_DISCOVERED"
                                    ConnectState.CONNECTING -> "CONNECTING"
                                    ConnectState.TIME_OUT -> "TIMEOUT"
                                    ConnectState.ERROR -> "ERROR"
                                    else -> "Exception"
                                }
                            }
                        }

                    },
                    bluetoothDataCallbacks = object : BluetoothDataCallbacks {
                        override fun onGetWeight(state: WeightState, weightData: Double?) {
                            when (state) {
                                WeightState.START -> {
                                }
                                WeightState.WAITING -> {
                                }
                                WeightState.SUCCESS -> {
                                    findViewById<TextView>(R.id.textV_weight).text =
                                        weightData.toString()
                                }
                                WeightState.UNKNOWN -> {}
                            }
                        }

                        override fun onGetBodyComposition(
                            state: BodyCompositionState,
                            bodyFatData: BodyFatData?,
                            moreFatData: MoreFatData?
                        ) {
                            val textVBodyFat = findViewById<TextView>(R.id.textV_body_fat)
                            val textVMusclemass = findViewById<TextView>(R.id.textV_muscle)
                            when (state) {
                                BodyCompositionState.START -> {
                                    textVBodyFat.text = "측정중"
                                    textVMusclemass.text = "측정중"
                                }
                                BodyCompositionState.FAIL -> {
                                    textVBodyFat.text = "계산 실패"
                                    textVMusclemass.text = "계산 실패"
                                }
                                BodyCompositionState.SUCCESS -> {
                                    textVBodyFat.text = bodyFatData?.bfr.toString()
                                    textVMusclemass.text = moreFatData?.muscleMass.toString()
                                }
                                BodyCompositionState.UNKNOWN -> {}
                            }
                        }
                    }
                )
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceConnected = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Solfitbluetooth Setting
        AiFitSDK.getInstance().init(this)
        serviceBind()


        val buttonStartScan = findViewById<Button>(R.id.button_start_scan)
        buttonStartScan.setOnClickListener {
            if (isServiceConnected) {
                solfitBluetoothService?.startScan()
            }
        }

        val buttonStopScan = findViewById<Button>(R.id.button_stop_scan)
        buttonStopScan.setOnClickListener {
            if (isServiceConnected) {
                solfitBluetoothService?.stopScan()
            }
        }

        findViewById<Button>(R.id.button_connect).let {
            it.setOnClickListener {
                if (isServiceConnected) {
                    solfitBluetoothService?.startConnect("01:B6:EC:B8:0B:A6")
                }
            }
        }

        findViewById<Button>(R.id.button_disconnect).let {
            it.setOnClickListener {
                if (isServiceConnected) {
                    solfitBluetoothService?.disconnect()
                }
            }
        }
    }

    fun serviceBind() {
        val intent = Intent(this, SolfitBluetoothService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun serviceUnbind() {
        if (isServiceConnected) {
            solfitBluetoothService?.unbindService(serviceConnection)
            isServiceConnected = false
        }
    }

    override fun onDestroy() {
        serviceUnbind()
        super.onDestroy()
    }
}
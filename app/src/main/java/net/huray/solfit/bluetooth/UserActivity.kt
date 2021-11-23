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
import net.huray.solfit.bluetooth.callbacks.BluetoothConnectionCallbacks
import net.huray.solfit.bluetooth.callbacks.BluetoothDataCallbacks
import net.huray.solfit.bluetooth.callbacks.BluetoothScanCallbacks
import net.huray.solfit.bluetooth.util.*


class UserActivity: AppCompatActivity() {
    private val TAG = javaClass.simpleName
    private var solfitBluetoothService: SolfitBluetoothService? = null
    private var isServiceConnected = false
    private var serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isServiceConnected = true
            val serviceBinder = service as SolfitBluetoothService.ServiceBinder
            solfitBluetoothService = serviceBinder.getService().apply {
                setUserInfo(1,33,175)
                initilize(
                    bluetoothConnectionCallbacks = object: BluetoothConnectionCallbacks {
                        override fun onStateChanged(deviceAddress: String?, state: Int) {
                            findViewById<TextView>(R.id.textV_get_connect_state_chagne).apply {
                                text = when(state) {
                                    STATE_DISCONNECTED -> "DISCONNECTED"
                                    STATE_CONNECTED -> "CONNECTED"
                                    STATE_INDICATION_SUCCESS -> "INDICATION_SUCCESS"
                                    STATE_SERVICE_DISCOVERED -> "SERVICE_DISCOVERED"
                                    STATE_CONNECTING -> "CONNECTING"
                                    STATE_TIME_OUT -> "TIMEOUT"
                                    else -> "Exception"
                                }
                            }
                        }

                        override fun onError(s: String?, i: Int) {
                            findViewById<TextView>(R.id.textV_get_connect_state_chagne).apply{
                                text = "On Error Called:$s"
                            }
                        }
                    },
                    bluetoothScanCallbacks = object: BluetoothScanCallbacks{
                        override fun onScan(state: Int, errorMsg: String?, broadData: BroadData?) {
                            val textVScanResult = findViewById<TextView>(R.id.textV_scan_result)
                            when(state) {
                                STATE_FAIL -> textVScanResult.text = errorMsg
                                STATE_SCANNING ->
                                    textVScanResult.text = broadData?.address.toString()

                                STATE_STOPPED -> textVScanResult.text = "STOPPED"
                            }
                        }
                    },
                    bluetoothDataCallbacks = object: BluetoothDataCallbacks {
                        override fun onGetWeight(state: Int, weightData: Double?) {
                            when(state) {
                                STATE_GET_WEIGHT_START -> {}
                                STATE_GET_WEIGHT_WAITING -> {}
                                STATE_GET_WEIGHT_SUCCESS -> {
                                    findViewById<TextView>(R.id.textV_weight).text = weightData.toString()
                                }
                            }
                        }

                        override fun onGetBodyComposition(
                            state: Int,
                            fatRate: Float?,
                            muscleMass: Float?
                        ) {
                            val textVBodyFat = findViewById<TextView>(R.id.textV_body_fat)
                            val textVMusclemass = findViewById<TextView>(R.id.textV_muscle)
                            when(state) {
                                STATE_GET_BODY_COMPOSITION_START -> {
                                    textVBodyFat.text="측정중"
                                    textVMusclemass.text="측정중"
                                }
                                STATE_GET_BODY_COMPOSITION_FAIL -> {
                                    textVBodyFat.text="계산 실패"
                                    textVMusclemass.text="계산 실패"
                                }
                                STATE_GET_BODY_COMPOSITION_SUCCESS -> {
                                    textVBodyFat.text = fatRate.toString()
                                    textVMusclemass.text = muscleMass.toString()
                                }
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
            if(isServiceConnected) {
                solfitBluetoothService?.startScan()
            }
        }

        val buttonStopScan = findViewById<Button>(R.id.button_stop_scan)
        buttonStopScan.setOnClickListener {
            if(isServiceConnected) {
                solfitBluetoothService?.stopScan()
            }
        }

        findViewById<Button>(R.id.button_connect).let{
            it.setOnClickListener {
                if(isServiceConnected) {
                    solfitBluetoothService?.startConnect("01:B6:EC:B8:0B:A6")
                }
            }
        }

        findViewById<Button>(R.id.button_disconnect).let{
            it.setOnClickListener {
                if(isServiceConnected) {
                    solfitBluetoothService?.disconnect()
                }
            }
        }
    }

    fun serviceBind() {
        val intent = Intent(this, SolfitBluetoothService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun serviceUnbind(){
        if(isServiceConnected) {
            solfitBluetoothService?.unbindService(serviceConnection)
            isServiceConnected = false
        }
    }

    override fun onDestroy() {
        serviceUnbind()
        super.onDestroy()
    }
}
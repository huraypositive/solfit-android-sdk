package net.huray.solfit.bluetooth

import aicare.net.cn.iweightlibrary.AiFitSDK
import aicare.net.cn.iweightlibrary.entity.*
import aicare.net.cn.iweightlibrary.wby.WBYService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.huray.solfit.bluetooth.callbacks.BluetoothBondCallbacks
import net.huray.solfit.bluetooth.callbacks.BluetoothConnectionCallbacks
import net.huray.solfit.bluetooth.callbacks.BluetoothDataCallbacks
import net.huray.solfit.bluetooth.callbacks.BluetoothDeviceCallbacks
import net.huray.solfit.bluetooth.util.*
import org.w3c.dom.Text


class UserActivity: AppCompatActivity() {
    private val TAG = javaClass.simpleName
    private var solfitBluetoothService: SolfitBluetoothService? = null
    private var isServiceConnected = false
    private var serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val serviceBinder = service as SolfitBluetoothService.ServiceBinder
            solfitBluetoothService = serviceBinder.getService()
            isServiceConnected = true
            //TODO: 유저 인포
            solfitBluetoothService?.setUserInfo(1,33,175)
            val serviceInitialize = solfitBluetoothService?.initilize(
                bluetoothBondCallbacks = object: BluetoothBondCallbacks{
                    override fun onServiceBinded(wbyBinder: WBYService.WBYBinder?) {
                        Log.d(TAG,"onServiceBinded")
                    }

                    override fun onServiceUnbinded() {
                        Log.d(TAG,"onServiceUnbinded")
                    }

                },
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
                            text = "On Error Called"
                        }
                    }
                },
                bluetoothDeviceCallbacks = object: BluetoothDeviceCallbacks{
                    override fun getAicareDevice(broadData: BroadData) {
                        val textViewGetDeviceList = findViewById<TextView>(R.id.textV_get_device)
                        textViewGetDeviceList.text = broadData.address.toString()
                    }
                },
                bluetoothDataCallbacks = object: BluetoothDataCallbacks {
                    override fun onGetWeightData(weightData: Double?) {
                        findViewById<TextView>(R.id.textV_weight).text =
                            "몸무게: "+ weightData.toString() +"kg"
                    }

                    override fun onGetMeasureStatus(status: Int) {
                    }

                    override fun onGetFatData(b: Boolean, bodyFatData: BodyFatData?) {
                        //findViewById<TextView>(R.id.textV_body_fat).text = bodyFatData?.toString()
                    }

                    override fun onGetMuscleMass(muscleMass: Float?) {
                        findViewById<TextView>(R.id.textV_muscle).text =
                            "근육량: " +muscleMass.toString()+"kg"
                    }

                    override fun onGetFatRate(fatRatio: Float?) {
                        findViewById<TextView>(R.id.textV_body_fat).text =
                            "체지방률:" + fatRatio.toString()+"%"
                    }
                }
            )
            if(!serviceInitialize!!) {
                //disconnect()
                return
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
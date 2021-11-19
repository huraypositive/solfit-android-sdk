package net.huray.solfit.bluetooth

import aicare.net.cn.iweightlibrary.AiFitSDK
import aicare.net.cn.iweightlibrary.entity.BroadData
import aicare.net.cn.iweightlibrary.wby.WBYService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.huray.solfit.bluetooth.callbacks.BluetoothBondCallbacks
import net.huray.solfit.bluetooth.callbacks.BluetoothDeviceCallbacks

class UserActivity: AppCompatActivity() {
    private var solfitBluetoothService: SolfitBluetoothService? = null
    private var isServiceConnected = false
    private var serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val serviceBinder = service as SolfitBluetoothService.ServiceBinder
            solfitBluetoothService = serviceBinder.getService()
            isServiceConnected = true
            val serviceInitialize = solfitBluetoothService?.initilize(
                bluetoothBondCallbacks = object: BluetoothBondCallbacks{
                    override fun onServiceBinded(wbyBinder: WBYService.WBYBinder?) {
                        TODO("Not yet implemented")
                    }

                    override fun onServiceUnbinded() {
                        TODO("Not yet implemented")
                    }

                },
                bluetoothDeviceCallbacks = object: BluetoothDeviceCallbacks{
                    override fun getAicareDevice(broadData: BroadData) {
                        Log.d("UserActivity","getAicareDevice called")
                    }
                })
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
        setSolfitBluetooth()


        val textView = findViewById<TextView>(R.id.textV_hello_world)
        textView.setOnClickListener {
            callBindServiceTest()
        }
    }

    fun setSolfitBluetooth(){
        serviceBind()
        callBindServiceTest()
    }

    fun serviceBind() {
        val intent = Intent(this, SolfitBluetoothService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun serviceUnbind(){
        if(isServiceConnected) {
            unbindService(serviceConnection)
            isServiceConnected = false
        }
    }

    fun callBindServiceTest(){
        if(isServiceConnected) {
            solfitBluetoothService?.startScan()
        }
    }
}
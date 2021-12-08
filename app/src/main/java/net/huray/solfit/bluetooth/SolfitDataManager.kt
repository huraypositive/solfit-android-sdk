package net.huray.solfit.bluetooth

import aicare.net.cn.iweightlibrary.entity.BroadData
import android.annotation.SuppressLint
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.huray.solfit.bluetooth.data.UserInfo

class SolfitDataManager(private val context: Context) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile private var instance: SolfitDataManager? = null

        @JvmStatic fun getInstance(context: Context): SolfitDataManager =
            instance ?: synchronized(this) {
                instance ?: SolfitDataManager(context).also {
                    instance = it
                }
            }
    }

    private val masterKey by lazy {
        MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val userInfoSharedPreferences by lazy {
        EncryptedSharedPreferences.create(context, "user_info"
               ,masterKey,EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV
               ,EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    }

    private val deviceInfoSharedPreferences by lazy {
        EncryptedSharedPreferences.create(context, "device_info"
            ,masterKey,EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV
            ,EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    }

    fun saveUserInfoData(userInfo: UserInfo){
        userInfoSharedPreferences.edit().apply{
            putInt("sex",userInfo.sex)
            putInt("age",userInfo.age)
            putInt("height",userInfo.height)
        }.apply()
    }

    fun readUserInfoData() =
        UserInfo(userInfoSharedPreferences.getInt("sex",1),
                 userInfoSharedPreferences.getInt("age",1),
                 userInfoSharedPreferences.getInt("height",1))

    fun saveDeviceInfo(deviceInfo: BroadData){
        if(deviceInfo.address == null) return
        val gson = GsonBuilder().create()
        val jsonDeviceInfo = gson.toJson(deviceInfo,BroadData::class.java)
        deviceInfoSharedPreferences.edit().putString(deviceInfo.address,jsonDeviceInfo).apply()
    }

    fun readDeviceInfoList(): List<BroadData> {
        val iterator = deviceInfoSharedPreferences.all.values.iterator()
        val gson = GsonBuilder().create()
        val deviceInfoList = ArrayList<BroadData>()
        while(iterator.hasNext()){
            deviceInfoList.add(gson.fromJson(iterator.next() as String, BroadData::class.java))
        }
        return deviceInfoList
    }

    fun updateDeviceInfo(deviceInfo: BroadData){
        saveDeviceInfo(deviceInfo)
    }

    fun deleteDeviceInfo(deviceAddress: String){
        deviceInfoSharedPreferences.edit().remove(deviceAddress).apply()
    }

    fun cleanDeviceInfo(){
        deviceInfoSharedPreferences.edit().clear().apply()
    }
}
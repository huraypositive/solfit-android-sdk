package net.huray.solfit.bluetooth

import aicare.net.cn.iweightlibrary.entity.BroadData
import android.annotation.SuppressLint
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
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

    val deviceInfoSharedPreferences by lazy {
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

    fun readUserInfoData() = UserInfo(
                                userInfoSharedPreferences.getInt("sex",1),
                                userInfoSharedPreferences.getInt("age",1),
                                userInfoSharedPreferences.getInt("height",1))

    fun saveDeviceInfo(deviceInfo: BroadData){
        //TODO: GSON 활용해서 객체 정보 저장
        //중복 체크 로직
    }

    fun readDeviceInfo(): List<BroadData>? = null

    fun updateDeviceInfo(broadData: BroadData){}

    fun deleteDeviceInfo(broadData: BroadData){}
}
# SolfitBluetooth
-----------------
블루투스 기기 통신을 위한 라이브러리
>> 샘플 앱 보러 가기 (https://github.com/huraypositive/solfit-android-sdk-sample)

# Language
* Kotlin

# IDE
* Arctic Fox | 2020.3.1 Patch 3

# Kotlin & SDK Version
* Kotlin Version: 1.5.20
* minSdkVersion: 21
* targetSdkVersion: 30
* compileSdkVersion: 30

# Gradle
* Gradle Version: 4.2.2

# Compatibility
---------------
* Minimum version android 4.4 (API 19)
* The Bluetooth version used by the device requires 4.0 and above
* Dependent environment androidx
* Supported cpu architecture: arm64-v8a; armeabi-v7a; x86; x86_64

# installation
* Step 1. Add the JitPack repository to your build file
```groovy
build.gradle(Project)
allprojects {
  repositories {
    ....
    maven { url 'https://jitpack.io' }
  }
}  
```
* Step 2. Add the dependency
```groovy
 build.gradle(app)
dependencies {
  implementation 'com.github.hurayPositive:solfit-android-sdk:$latestVersion'
}
```
* Step 3. Allow Project Repository
```groovy
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    maven { url "https://jitpack.io" }  // Add this line
    jcenter() // Warning: this repository is going to shut down soon
  }
}
```
# Required Dependencies
-----------------------
* [BodyFatScaleSDKRepositoryAndroid]
```groovy
   implementation 'com.github.elinkthings:BodyFatScaleSDKRepositoryAndroid:$latestVersion'
```

# Required Permissions
* AndroidManifest.xml
```xml
  <uses-permission android:name="android.permission.BLUETOOTH" />
  <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
  <uses-permission android:name="android.permission.BLUETOOTH_SCAN"/>
  <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

# Usage
* 서비스 등록 (AndroidManifest.xml)
```xml
   <service android:name="net.huray.solfit.bluetooth.SolfitBluetoothService" android:enabled="true"/>
   <service android:name="aicare.net.cn.iweightlibrary.wby.WBYService" android:enabled="true"/>
```

* 서비스 시작
  >> 서비스 연결 되면 서비스 Initialize. 유저 정보 및 상태값 콜백 메서드를 여기서 선언해줄 수 있다. 여기서 만약에 상태값 콜백 메서드를 선언하지 않아도
  >> 하단에 설명되어있는 set Interface 메서드를 활용해서도 상태값 콜백 메서드를 선언 가능합니다
~~~kotlin
  private var serviceConnection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName?, service: IBinder?){
        val serviceBinder = service as SolfitBluetoothService.ServiceBinder
        solfitBluetoothService = serviceBinder.getService().apply {
          initialize(
              context: Context, UserInfo(sex: Int, age: Int, height: Int),
              bluetoothScanCallbacks: BluetoothScanCallbacks, 
              bluetoothConnectionCallbacks: BluetoothConnectionCallbacks,
              bluetoothDataCallbacks: BluetoothDataCallbacks
          )
        } 
    }
    override fun onServiceDisconnected(name: ComponentName?){}
  }
  
  fun serviceBind() {
      val intent = Intent(this, SolfitBluetoothService::class.java)
      bindService(intent: Intent, serviceConnection: ServiceConnection, Context.BIND_AUTO_CREATE)
  }
~~~

* 서비스 종료
~~~kotlin
  solfitBluetoothService?.unbindService(serviceConnection: ServiceConnection)
~~~

* 스캔 시작
~~~kotlin
  solfitBluetoothService?.startScan()
~~~

* 스캔 종료
~~~kotlin
  solfitBluetoothService?.stopScan()
~~~

* 스캔 상태 콜백 메서드 설정
~~~kotlin
  sofltiBluetoothService?.setBluetoothScanCallbacks(bluetoothScanCallbacks: BluetoothScanCallbacks)
~~~

* 블루투스 기기와 연결
~~~kotlin
  solfitBluetoothService?.connect(address: String?)
~~~

* 블루투스 기기와 연결 종료
~~~kotlin
  solfitBluetoothService?.disconnect()
~~~

* 블루투스 기기 연결 상태 콜백 메서드 설정
~~~kotlin
  solfitBluetoothService?.setBluetoothConnectionCallbacks(bluetoothConnectionCallbacks: BluetoothConnectionCallbacks)
~~~

* 체중/체성분 데이터 상태 콜백 메서드 설정
~~~kotlin
  solfitBluetoothService?.setBluetoothDataCallbacks(bluetoothDataCallbacks: BluetoothDataCallbacks)
~~~

* 데이터 활용
  * 마지막 접속 유저 정보 가져오기
~~~kotlin
  SolfitDataManger().getInstance(context: Context).readUserInfoData()
~~~
* 블루투스 연결 했던 기기 목록 가져오기
~~~kotlin
  SolfitDataManger().getInstance(context: Context).readDeviceInfoList()

~~~
* 기타 Function들도 위와 같은 방식으로 사용하면됩니다
~~~kotlin
  SolfitDataManger().getInstance(context: Context).updateDeviceInfo(deviceInfo: BroadData)
  SolfitDataManger().getInstance(context: Context).deleteDeviceInfo(deviceAddress: String)
  SolfitDataManger().getInstance(context: Context).clearDeviceInfo()
~~~

# Core Classes
* SolfitBluetoothService
> 블루투스 통신을 사용하기 위한 코어 서비스

# Entity Classes
* UserInfo
  > 고객 정보
  * sex: 1(남성), 2(여성) (Int) 
  * age: 나이(Int)
  * height: 키(Int)
  
* BroadData
  > 디바이스 스캔 결과
  * name: 기기(String)
  * address: 기기 Mac Address 주소(String)
  * rssi: 기기 수신신호 강도(Int)
  * isBoolean: (Boolean)
  * specificData: (Byte[])
  * deviceType: (Int)
* BodyFatData
  > 체지방 데이터
  * bmi: 체질량 지수(Double)
  * bfr: 체지방률(%)(Double)
  * sfr: 피하 지방율(%)(Double)
  * uvi: 내장 지방 지수(Int)
  * rom: 근육율(%)(Double)
  * bmr: 기초 대사량(kcal)(Int)
  * bm: 골량(kg)(Double)
  * vwc: 수분함유율(%)(Double)
  * bodyAge: 신체나이(Int)
  * pp: 단백질 함유율(%)(Double)
* MoreFatData
  > 표준 체중, 지방, 근육량 등
  * standardWeight: 표준 체중(kg)(Double)
  * controlWeight: 실제 체중과 표준값의 차이(kg)(Double)
  * fat: 지방량(kg)(Double)
  * removeFatWeight: 지방을 제외한 체중(kg)(Double)
  * muscleMass: 근육량(kg)(Double)
  * protein: 단백질 함유량(kg)(Double)
  * fatLevel: 비만 등급(MoreFatData.FatLevel(Enum))
ㅍ
# Type Classes
* ScanState
  > 블루투스 스캔 연결 상태 유형
  * FAIL: 실패
  * SCANNING: 연결성공
  * DISCONNECTED: 연결해제
* ConnectState
  > 블루투스 기기 연결 상태 유형
  * DISCONNECTED
  * CONNECTED
  * SERVICE_DISCOVERED
  * INDICATION_SUCCESS
  * CONNECTING
  * TIME_OUT
  * ERROR
* WeightState
  > 체중 데이터 상태 유형
  * START
  * WAITING
  * SUCCESS
* BodyCompositionState
  > 체성분 데이터 상태 유형
  * START
  * FAIL
  * SUCCESS

# Interfaces
* BluetoothScanCallbacks
  > 블루투스 스캔 상태 변화 감지 관련 콜백
```kotlin
    /**
     *  STATE : ScanState_FAIL
     *          ScanState_SCANNING
     *          ScanState_STOPPED
     */
    fun onScan(state: ScanState, errorMsg: String?, deviceList: List<BroadData>?)
```
* BluetoothConnectionCallbacks
  > 블루투스 연결 상태 변화 감지 관련 콜백
```kotlin
    /**
     *  STATE: ConnectState_DISCONNECTED
     *         ConnectState_CONNECTED
     *         ConnectState_SERVICE_DISCOVERED
     *         ConnectState_INDICATION_SUCCESS
     *         ConnectState_CONNECTING
     *         ConnectState_TIME_OUT
     *         ConnectState_ERROR
     */
    fun onStateChanged(deviceAddress: String?, state: ConnectState, errMsg: String?, errCode: Int?)
```
* BluetoothDataCallbacks
  > 체중/체성분 상태 변화 감지 관련 콜백
```kotlin
    /**
     * STATE: WeightState_START
     *        WeightState_WAITING
     *        WeightState_SUCCESS
     */
    fun onGetWeight(state: WeightState, weightData: Double?)
    /**
     * STATE: BodyCompositionState_START
     *        BodyCompositionState_FAIL
     *        BodyCompositionState_SUCCESS
     */
    fun onGetBodyComposition(state: BodyCompositionState, bodyFatData: BodyFatData?, moreFatData: MoreFatData?)
```

# Functions
* SolfitBluetoothService
  * startScan():Void 기기의 블루투스 스캔을 시작한다
  * stopScan():Void 기기의 블루투스 스캔을 멈춘다
  * startConnect(address: String?):Void 전달받은 Mac Address에 해당하는 기기에 연결을 요청한다
  * disconnect():Void 연결된 기기에 연결 해제를 요청한다

* SolfitDataManager
  * readUserInfoData():UserInfo 마지막으로 접속했던 유저 정보를 가져옴
  * readDeviceInfoList():List<BroadData> 블루투스 연결했던 기기들의 목록을 가져옴
  * updateDeviceInfo(deviceInfo: BroadData): Void 블루투스 연결했던 기기들 중 특정 디바이스 정보를 업데이트함
  * deleteDeviceInfo(deviceAddress: String): Void 블루투스 연결했던 기기들 중 특정 디바이스 정보를 삭제함
  * clearDeviceInfo():Void 블루투스 연결했던 기기들의 목록을 전부 지움

# Library Publish
* Step 1. GitHub에서 TAG 생성
* Step 2. 생성한 TAG Release
* Step 3. https://jitpack.io/ 홈페이지 접속 후, GitHub repo url 입력('hurayPositive/solfit-android-sdk')
* Step 4. 로그 확인을 통해 정상적으로 Publish 되었는지 확인

[releases]: https://github.com/huraypositive/solfit-android-sdk/releases
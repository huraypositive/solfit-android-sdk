# SolfitBluetooth
블루투스 기기 통신을 위한 라이브러리

# Compatibility
 * Minimum version android4.4 (API 19)
 * The Bluetooth version used by the device requires 4.0 and above
 * Dependent environment androidx
 * Supported cpu architecture: arm64-v8a; armeabi-v7a; x86; x86_64

# installation
 * Step 1. Add the JitPack repository to your build file  
 build.gradle(Project)  
     allprojects {  
         repositories {  
             ....  
             maven { url 'https://jitpack.io' }  
         }  
     }  
 * Step 2. Add the dependency  
 build.gradle(app)  
     dependencies {  
         implementation 'com.github.hurayPositive:solfit-android-sdk:$latestVersion'  
     }
     
# Required Dependencies

* [TedPermission][tedpermission]
  ```groovy
  implementation "gun0912.ted:tedpermission:$latestVersion"
  ```

# Required Permissions
* AndroidManifest.xml
  ```xml
  <uses-permission android:name="android.permission.BLUETOOTH" />
  <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
  ```

# Usage
* 서비스 등록 (AndroidManifest.xml)
  ```xml
  <service android:name="net.huray.solfit.bluetooth.SolfitBluetoothService" />
  ```

* 서비스 시작(추후 업데이트 예정)
  * 

# Core Classes
  * SolfitBluetoothService
  > 블루투스 통신을 사용하기 위한 코어 서비스

# Entity Classes
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
  * bmi: (Double)
  * bfr: (Double)
  * sfr: (Double)
  * uvi: (Int)
  * rom: (Double)
  * bmr: (Int)
  * bm: (Double)
  * vwc: (Double)
  * bodyAge: (Int)
  * pp: (Double)
* MoreFatData
  > 표준 체중, 지방, 근육량 등
  * standardWeight: (Double)
  * controlWeight: (Double)
  * fat: (Double)
  * removeFatWeight: (Double)
  * muscleMass: (Double)
  * protein: (Double)
  * fatLevel: (MoreFatData.FatLevel(Enum))
# Type Classes
* ScanState
  > 블루투스 스캔 연결 상태 유형
    * FAIL: 실패
    * SCANNING: 연결성공
    * DISCONNECTED: 연결해제
* ConnectState
  > 블루투스 기기 연결 상태 유형
  * DISCONNECTED: 
  * CONNECTED:
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
     *  STATE : ScanState_FAIL(실패)
     *          ScanState_SCANNING(성공)
     *          ScanState_STOPPED(종료)
     */
    fun onScan(state: ScanState, errorMsg: String?, deviceList: List<BroadData>?)
  ```
* BluetoothConnectionCallbacks
  > 블루투스 연결 상태 변화 감지 관련 콜백
  ```kotlin
    fun onStateChanged(deviceAddress: String?, state: ConnectState, errMsg: String?, errCode: Int?)
  ```
* BluetoothDataCallbacks
  > 체중/체성분 상태 변화 감지 관련 콜백
  ```kotlin
    fun onGetWeight(state: WeightState, weightData: Double?)
    fun onGetBodyComposition(state: BodyCompositionState, bodyFatData: BodyFatData?, moreFatData: MoreFatData?)
  ```

# Functions
* SolfitBluetoothService
    * startScan(): 기기의 블루투스 스캔을 시작한다
    * stopScan(): 기기의 블루투스 스캔을 멈춘다
    * connect(address: String?): 전달받은 Mac Address에 해당하는 기기에 연결을 요청한다
    * disconnect(): 연결된 기기에 연결 해제를 요청한다
    
# Release
  * Step 1. GitHub에서 TAG 생성
  * Step 2. 생성한 TAG Release
  * Step 3. https://jitpack.io/ 홈페이지 접속 후, GitHub repo url 입력('hurayPositive/solfit-android-sdk')
  * Step 4. 로그 확인을 통해 정상적으로 Publish 되었는지 확인
    
[releases]: https://github.com/huraypositive/solfit-android-sdk/releases
[sample]:
[TedPermission]: https://github.com/ParkSangGwon/TedPermission

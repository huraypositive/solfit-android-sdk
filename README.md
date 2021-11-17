# SolfitBluetooth
-----------------
블루투스 기기 통신을 위한 라이브러리

# Compatibility
---------------
 * Minimum version android4.4 (API 19)
 * The Bluetooth version used by the device requires 4.0 and above
 * Dependent environment androidx
 * Supported cpu architecture: arm64-v8a; armeabi-v7a; x86; x86_64

# installation
---------------
 * Step 1. Add the JitPack repository to your build file
 build.gradle(Project)  
~~~groovy
 allprojects {  
     repositories {  
         maven { url 'https://jitpack.io' }
     }  
 }  
~~~
 * Step 2. Add the dependency 
build.gradle(App)
~~~groovy
 dependencies {  
     implementation 'com.github.hurayPositive:solfit-android-sdk:1.0.0' 
 }
~~~  
# Required Dependencies
-----------------------
~~~groovy
 dependencies {  
     implementation 'com.github.elinkthings:BodyFatScaleSDKRepositoryAndroid:1.3.2'
 }
~~~  


# Required Permissions
----------------------
~~~xml
<!--In most cases, you need to ensure that the device supports BLE .-->
<uses-feature
android:name="android.hardware.bluetooth_le"
android:required="true"/>
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
<!-- Android 6.0 and above. Bluetooth scanning requires one of the following two permissions. You need to apply at run time .-->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<!-- Optional. If your app need dfu function .-->
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
~~~

# Usage
-------

# Core Classes
--------------

# Entity
--------

# Type Classes
--------------

# Interfaces
------------

# Functions
-----------

# Release
-----------
 * Step 1. GitHub에서 TAG 생성
 * Step 2. 생성한 TAG Release
 * Step 3. https://jitpack.io/ 홈페이지 접속 후, GitHub repo url 입력('hurayPositive/solfit-android-sdk)
 * Step 4. 로그 확인을 통해 정상적으로 Publish 되었는지 확인
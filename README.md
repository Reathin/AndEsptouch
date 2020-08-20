### AndEsptouch
基于最新的乐鑫8266的配网项目进行封装。
### 使用

#### 集成
```
implementation 'com.rairmmd:andesptouch:1.0.5'
```
#### 需要的权限
需要一下权限，库文件中已添加好了。
```
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
```
** ⚠️请注意适配高版本安卓，获取WiFi信息需要动态请求定位权限。

#### AndEsptouch
```java
AndEsptouch andEsptouch = new AndEsptouch.Builder(this)
    .setSSID(ssid)//WiFi名字 可通过AndEsptouchHelper获得
    .setBSSID(bssid)//路由器mac地址 可通过AndEsptouchHelper获得
    .setPassWord(password)//WiFi密码
    .build();
andEsptouch.startConfig();

//停止配置
andEsptouch.stopConfig();
```
设置回调监听
```java
andEsptouch.setOnEsptouchTaskListener(new AndEsptouch.OnEsptouchTaskListener() {
    @Override
    public void onEsptouchTaskCallback(int code, String message) {
        Log.d(TAG, "code:" + code + "\nmessage:" + message);
        dismissProgressDialog();
        if (code == AndEsptouch.RESULT_CONFIG_SUCCESS) {
            Toast.makeText(MainActivity.this, "配网成功", Toast.LENGTH_SHORT).show();
        } else if (code == AndEsptouch.RESULT_CONFIG_TIMEOUT) {
            Toast.makeText(MainActivity.this, "配网超时", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "配网失败", Toast.LENGTH_SHORT).show();
        }
    }
});
```
**code**有一下几个值：
>RESULT_CONFIG_SUCCESS; 表示成功配网，接着看message的信息 ；

>RESULT_CONFIG_MULTI_SUCCESS ; 为多个配网信息，刚刚配对成功的设备 ;

>RESULT_CONFIG_FAILURE; 表示配网失败;

>RESULT_CONFIG_RECEIVE_SUCCESS; 表示成功接受到设备的信息

>RESULT_CONFIG_TIMEOUT; 表示超时

#### AndEsptouchHelper
WiFi操作工具类
```java
//获取当前WiFi
String ssid = AndEsptouchHelper.getInstance(this).getWifiSsid();
String ssid = AndEsptouchHelper.getSSID(this);
//获取mac地址
String bssid = AndEsptouchHelper.getInstance(this).getBSSID();
String bssid = AndEsptouchHelper.getBSSID(this);
```
还有其他例如：打开 关闭wifi  判断是否可用。。。

### Apk体验
[apk下载](https://github.com/Rairmmd/AndEsptouch/raw/master/app/release/app-release.apk)

![1]
### 说明
基于EsptouchForAndroid: https://github.com/EspressifApp/EsptouchForAndroid

参考XSmartConfig: https://github.com/xuhongv/XSmartConfig

IOS友好封装（一句代码调用）：https://github.com/obama901/HDEspTouch

[1]:https://s1.ax1x.com/2018/06/27/PPbQfJ.png

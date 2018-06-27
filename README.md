### AndEsptouch
基于最新的乐鑫8266的配网项目进行封装。
### 使用

#### 集成
```
implementation 'com.rairmmd:andesptouch:1.0.0'
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
#### AndEsptouch
```
AndEsptouch andEsptouch = new AndEsptouch.Builder(this)
    .setSsid(currentWifiSsid)//WiFi名字 可通过AndEsptouchHelper获得
    .setBssid(bssid)//路由器mac地址 可通过AndEsptouchHelper获得
    .setPassWord(password)//WiFi密码
    .build();
andEsptouch.startEsptouchConfig();
showProgressDialog("努力配网中...");
```
设置回调监听
```
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
```
//获取当前WiFi
String currentWifiSsid = AndEsptouchHelper.getInstance(this).getCurrentWifiSsid();
//获取mac地址
String bssid = AndEsptouchHelper.getInstance(this).getBSSID();
```
还有其他例如：打开 关闭wifi  判断是否可用。。。

### Apk体验
[apk下载](https://github.com/Rairmmd/AndEsptouch/raw/master/app/release/app-release.apk)

![1]
### 参考
[EsptouchForAndroid](https://github.com/EspressifApp/EsptouchForAndroid) 
https://github.com/EspressifApp/EsptouchForAndroid

[XSmartConfig](https://github.com/xuhongv/XSmartConfig) https://github.com/xuhongv/XSmartConfig

[1]:https://s1.ax1x.com/2018/06/27/PPbQfJ.png

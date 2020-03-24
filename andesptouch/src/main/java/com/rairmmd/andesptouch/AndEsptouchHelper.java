package com.rairmmd.andesptouch;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * @author Rair
 * @date 2018/6/27
 * <p>
 * desc:
 */
public class AndEsptouchHelper {

    private final ConnectivityManager connectManager;
    /**
     * 声明Wifi管理对象
     */
    private WifiManager wifiManager;
    /**
     * Wifi信息
     */
    private WifiInfo wifiInfo;
    /**
     * 扫描出来的网络连接列表
     */
    private List<ScanResult> scanResultList;
    /**
     * 网络配置列表
     */
    private List<WifiConfiguration> wifiConfigList;
    /**
     * Wifi锁
     */
    private WifiManager.WifiLock wifiLock;

    private static AndEsptouchHelper andEsptouchHelper;

    public static AndEsptouchHelper getInstance(Context context) {
        if (andEsptouchHelper == null) {
            andEsptouchHelper = new AndEsptouchHelper(context);
        }
        return andEsptouchHelper;
    }


    /**
     * 构造函数
     *
     * @param context 上下文
     */
    private AndEsptouchHelper(Context context) {
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
    }

    /**
     * 获取WifiManager
     */
    public WifiManager getWifiManager() {
        return wifiManager;
    }

    /**
     * Wifi状态.
     *
     * @return wifi是否可用
     */
    public boolean isEnabled() {
        return wifiManager.isWifiEnabled();
    }

    /**
     * 打开 wifi
     *
     * @return 是否打开
     */
    public boolean openWifi() {
        if (!isEnabled()) {
            return wifiManager.setWifiEnabled(true);
        } else {
            return false;
        }
    }

    /**
     * 关闭Wifi
     *
     * @return 是否关闭
     */
    public boolean closeWifi() {
        if (!isEnabled()) {
            return true;
        } else {
            return wifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 锁定wifi
     * 锁定WiFI就是判断wifi是否建立成功，在这里使用的是held(握手) acquire
     */
    public void lockWifi() {
        wifiLock.acquire();
    }


    /**
     * 解锁wifi
     */
    public void unLockWifi() {
        if (!wifiLock.isHeld()) {
            // 释放资源
            wifiLock.release();
        }
    }

    /**
     * 创建一个Wifi锁，需要时调用
     */
    public void createWifiLock() {
        // 创建一个锁的标志
        wifiLock = wifiManager.createWifiLock("flyfly");
    }

    /**
     * 获取扫描WIFI列表的信息
     */
    public String lookupScanInfo() {
        StringBuilder scanBuilder = new StringBuilder();
        if (scanResultList == null) {
            return "";
        }
        for (int i = 0; i < scanResultList.size(); i++) {
            ScanResult sResult = scanResultList.get(i);
            scanBuilder.append("编号：" + (i + 1));
            scanBuilder.append(" ");
            //所有信息
            scanBuilder.append(sResult.toString());
            scanBuilder.append("\n");
        }
        scanBuilder.append("--------------华丽分割线--------------------");
        for (int i = 0; i < wifiConfigList.size(); i++) {
            scanBuilder.append(wifiConfigList.get(i).toString());
            scanBuilder.append("\n");
        }
        return scanBuilder.toString();
    }

    /**
     * 获取指定Wifi的信号强度
     */
    public int getLevel(int netId) {
        return scanResultList.get(netId).level;
    }


    /**
     * 获取本机Mac地址
     *
     * @return mac地址
     */
    public String getMac() {
        return (wifiInfo == null) ? "" : wifiInfo.getMacAddress();
    }

    public String getBSSID() {
        wifiInfo = wifiManager.getConnectionInfo();
        return (wifiInfo == null) ? null : wifiInfo.getBSSID();
    }

    public String getSSID() {
        return (wifiInfo == null) ? null : wifiInfo.getSSID();
    }

    /**
     * 返回当前连接的网络的ID
     */
    public int getCurrentNetId() {
        return (wifiInfo == null) ? null : wifiInfo.getNetworkId();
    }

    /**
     * 返回所有信息
     */
    public WifiInfo getWifiInfo() {
        // 得到连接信息
        wifiInfo = wifiManager.getConnectionInfo();
        return (wifiInfo == null) ? null : wifiInfo;
    }

    /**
     * 获取IP地址
     */
    public int getIP() {
        return (wifiInfo == null) ? null : wifiInfo.getIpAddress();
    }

    /**
     * 添加一个连接
     *
     * @param config wifi配置
     */
    public boolean addNetWordLink(WifiConfiguration config) {
        int netId = wifiManager.addNetwork(config);
        return wifiManager.enableNetwork(netId, true);
    }

    /**
     * 禁用一个链接
     *
     * @param netId
     */
    public boolean disableNetWordLink(int netId) {
        wifiManager.disableNetwork(netId);
        return wifiManager.disconnect();
    }

    /**
     * 移除一个链接
     *
     * @param netId
     */
    public boolean removeNetworkLink(int netId) {
        return wifiManager.removeNetwork(netId);
    }

    /**
     * 不显示SSID
     *
     * @param netId
     */
    public void hiddenSSID(int netId) {
        wifiConfigList.get(netId).hiddenSSID = true;
    }

    /**
     * 显示SSID
     *
     * @param netId
     */
    public void displaySSID(int netId) {
        wifiConfigList.get(netId).hiddenSSID = false;
    }

    /**
     * 获取当前已连接的wifi名称
     */
    public String getCurrentWifiSsid() {
        //得到连接信息
        wifiInfo = wifiManager.getConnectionInfo();
        String info = wifiInfo.toString();
        String ssid = wifiInfo.getSSID();
        if (info.contains(ssid)) {
            return ssid;
        } else if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            return ssid.substring(1, ssid.length() - 1);
        } else {
            return ssid;
        }
    }
}

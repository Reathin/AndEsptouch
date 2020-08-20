package com.rairmmd.andesptouch;

import android.content.Context;
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

    /**
     * 声明Wifi管理对象
     */
    private WifiManager mWifiManager;
    /**
     * Wifi信息
     */
    private WifiInfo mWifiInfo;
    /**
     * 扫描出来的网络连接列表
     */
    private List<ScanResult> mScanResultList;
    /**
     * 网络配置列表
     */
    private List<WifiConfiguration> mWifiConfigurationList;
    /**
     * Wifi锁
     */
    private WifiManager.WifiLock mWifiLock;

    private static AndEsptouchHelper mAndEsptouchHelper;

    public static AndEsptouchHelper getInstance(Context context) {
        if (mAndEsptouchHelper == null) {
            mAndEsptouchHelper = new AndEsptouchHelper(context);
        }
        return mAndEsptouchHelper;
    }

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    private AndEsptouchHelper(Context context) {
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    /**
     * 获取WifiManager
     */
    public WifiManager getWifiManager() {
        return mWifiManager;
    }

    /**
     * Wifi状态.
     *
     * @return wifi是否可用
     */
    public boolean isEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    /**
     * 打开 wifi
     *
     * @return 是否打开
     */
    public boolean isOpen() {
        if (!isEnabled()) {
            return mWifiManager.setWifiEnabled(true);
        } else {
            return false;
        }
    }

    /**
     * 关闭Wifi
     *
     * @return 是否关闭
     */
    public boolean isClose() {
        if (!isEnabled()) {
            return true;
        } else {
            return mWifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 锁定wifi
     * 锁定WiFI就是判断wifi是否建立成功，在这里使用的是held(握手) acquire
     */
    public void lockWifi() {
        mWifiLock.acquire();
    }


    /**
     * 解锁wifi
     */
    public void unLockWifi() {
        if (!mWifiLock.isHeld()) {
            // 释放资源
            mWifiLock.release();
        }
    }

    /**
     * 创建一个Wifi锁，需要时调用
     */
    public void createWifiLock() {
        // 创建一个锁的标志
        mWifiLock = mWifiManager.createWifiLock("flyfly");
    }

    /**
     * 创建一个Wifi锁，需要时调用
     */
    public void createWifiLock(String tag) {
        // 创建一个锁的标志
        mWifiLock = mWifiManager.createWifiLock(tag);
    }

    /**
     * 获取扫描WIFI列表的信息
     */
    public String lookupScanInfo() {
        StringBuilder scanBuilder = new StringBuilder();
        if (mScanResultList == null) {
            return "";
        }
        for (ScanResult scanResult : mScanResultList) {
            scanBuilder.append(scanResult.toString());
            scanBuilder.append("\n");
        }
        scanBuilder.append("----------------------------------");
        for (WifiConfiguration wifiConfiguration : mWifiConfigurationList) {
            scanBuilder.append(wifiConfiguration.toString());
            scanBuilder.append("\n");
        }
        return scanBuilder.toString();
    }

    /**
     * 获取指定Wifi的信号强度
     */
    public int getLevel(int position) {
        return mScanResultList.get(position).level;
    }


    /**
     * 获取本机Mac地址
     *
     * @return mac地址
     */
    public String getMac() {
        return (mWifiInfo == null) ? "" : mWifiInfo.getMacAddress();
    }

    public String getBSSID() {
        return (mWifiInfo == null) ? null : mWifiInfo.getBSSID();
    }

    public String getSSID() {
        return (mWifiInfo == null) ? null : mWifiInfo.getSSID();
    }

    /**
     * 返回当前连接的网络的ID
     */
    public int getNetWorkId() {
        return (mWifiInfo == null) ? null : mWifiInfo.getNetworkId();
    }

    /**
     * 返回所有信息
     */
    public WifiInfo getWifiInfo() {
        // 得到连接信息
        return mWifiInfo;
    }

    /**
     * 获取IP地址
     */
    public int getIP() {
        return (mWifiInfo == null) ? null : mWifiInfo.getIpAddress();
    }

    /**
     * 添加一个连接
     *
     * @param wifiConfiguration wifi配置
     */
    public boolean addNetWordLink(WifiConfiguration wifiConfiguration) {
        int netId = mWifiManager.addNetwork(wifiConfiguration);
        return mWifiManager.enableNetwork(netId, true);
    }

    /**
     * 禁用一个链接
     *
     * @param netId
     */
    public boolean disableNetWordLink(int netId) {
        mWifiManager.disableNetwork(netId);
        return mWifiManager.disconnect();
    }

    /**
     * 移除一个链接
     *
     * @param netId
     */
    public boolean removeNetWorkLink(int netId) {
        return mWifiManager.removeNetwork(netId);
    }

    /**
     * 不显示SSID
     *
     * @param netId
     */
    public void hiddenSSID(int netId) {
        mWifiConfigurationList.get(netId).hiddenSSID = true;
    }

    /**
     * 显示SSID
     *
     * @param netId
     */
    public void displaySSID(int netId) {
        mWifiConfigurationList.get(netId).hiddenSSID = false;
    }

    /**
     * 获取当前已连接的wifi名称
     */
    public String getWifiSSID() {
        //得到连接信息
        String info = mWifiInfo.toString();
        String ssid = mWifiInfo.getSSID();
        if (info.contains(ssid)) {
            return ssid;
        } else if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            return ssid.substring(1, ssid.length() - 1);
        } else {
            return ssid;
        }
    }
}

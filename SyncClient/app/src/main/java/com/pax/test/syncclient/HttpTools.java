package com.pax.test.syncclient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import java.lang.reflect.Method;

/**
 * 网络请求相关辅助类
 * @author jason.zhan
 *
 */
public class HttpTools {

    /**
     * 判断是否有网络
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {  
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {  
                return mNetworkInfo.isAvailable();  
            }  
        }  
        return false;  
    }

    public static boolean setWifiApEnabled(WifiManager wifiManager, WifiConfiguration wifiConfiguration, boolean enable) {
        boolean invokeStatus = true;
        try {
            Method setupHotSpot = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            setupHotSpot.invoke(wifiManager, wifiConfiguration, enable);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            invokeStatus = false;
        }

        return invokeStatus;
    }


    /**
     * 判断WiFi是否连接上了
     * @param context
     * @return
     */
    public static boolean isWiFiConnected(Context context) {
        if (context != null) {  
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return (mNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI);
            }  
        }  
        return false;  
    }

    /**
     * 获取所连接的WiFi名称
     * @param wifiManager
     * @return
     */
    public static String getConnectedWifiSsid(WifiManager wifiManager) {
        return wifiManager.getConnectionInfo().getSSID();  
    }  

    /**
     * @return (发送端)服务器IP地址，转换IP输出格式
     */
    public static String getSenderIP(WifiManager wifiManager) {
        //WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        //int ipAddress = wifiInfo.getIpAddress();  
        int ipAddress = wifiManager.getDhcpInfo().serverAddress;

        return Formatter.formatIpAddress(ipAddress);
    }

    /*****************************供接收者使用***************************************/
    // 加密类型，分为三种情况：1.没有密码   2.用WEP加密    3.用WPA加密，我们这里只用到了第3种
    public static final int TYPE_NO_PASSWD = 1;
    public static final int TYPE_WEP = 2;
    public static final int TYPE_WPA = 3;
    /**
     * 连接信息生成配置对象
     */
    public static WifiConfiguration createWifiInfo(String SSID, String password, int type) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = SSID;
        // 清除热点记录clearAll(SSID);
        if (type == TYPE_NO_PASSWD) {
            config.hiddenSSID = false;
            config.status = WifiConfiguration.Status.ENABLED;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.preSharedKey = null;
        } else if (type == TYPE_WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == TYPE_WPA) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = false;
            config.priority = 10000;
            config.status = WifiConfiguration.Status.ENABLED;

            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        }

        return config;
    }
    /*****************************供接收者使用***************************************/
}
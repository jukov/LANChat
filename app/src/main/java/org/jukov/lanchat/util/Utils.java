package org.jukov.lanchat.util;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by jukov on 05.02.2016.
 */
public class Utils {

    public static InetAddress getBroadcastAddress(Context context) throws IOException {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if(dhcp == null)
            return InetAddress.getByName("255.255.255.255");
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    public static InetAddress getIpAddress() {
        InetAddress inetAddress;
        InetAddress myAddr = null;
        try {
            for (Enumeration<NetworkInterface> networkInterface = NetworkInterface
                    .getNetworkInterfaces(); networkInterface.hasMoreElements();) {

                NetworkInterface singleInterface = networkInterface.nextElement();

                for (Enumeration <InetAddress> IpAddresses = singleInterface.getInetAddresses(); IpAddresses
                        .hasMoreElements();) {
                    inetAddress = IpAddresses.nextElement();

                    if (!inetAddress.isLoopbackAddress() && (singleInterface.getDisplayName()
                            .contains("wlan0") ||
                            singleInterface.getDisplayName().contains("eth0") ||
                            singleInterface.getDisplayName().contains("ap0"))) {

                        myAddr = inetAddress;
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return myAddr;
    }

    public static boolean isConnectedToWiFi(Context context) {
//        final int AP_STATE_DISABLING = 10;
//        final int AP_STATE_DISABLED = 11;
//        final int AP_STATE_ENABLING = 12;
        final int AP_STATE_ENABLED = 13;
//        final int AP_STATE_FAILED = 14;

        SupplicantState supplicantState;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        supplicantState = wifiInfo.getSupplicantState();
        String ssid = wifiInfo.getSSID();

        int actualState = 0;

        try {
            Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
            method.setAccessible(true);
            actualState = (Integer) method.invoke(wifiManager, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ((supplicantState == SupplicantState.COMPLETED && !ssid.equals("<unknown ssid>") && !ssid.equals("0x")) || actualState == AP_STATE_ENABLED);
    }

    public static String getAndroidID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String newRoomUID(Context context) {
        return getAndroidID(context) + Long.toString(new Date().getTime());
    }

    public static String getSendMessageDate(long millis) {
        long now = System.currentTimeMillis();
        long diff = now - millis;
        String format = "HH:mm";
        if (diff > TimeUnit.DAYS.toMillis(365)) {
            format = "MMM d yyyy, HH:mm";
        } else if (diff > TimeUnit.DAYS.toMillis(1)) {
            format = "MMM d, HH:mm";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return simpleDateFormat.format(new Date(millis));
    }
}

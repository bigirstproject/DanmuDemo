package utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by xujiexing on 14-6-10.
 */
public class NetworkUtils {
    private static WifiManager.WifiLock sWifiLocker;
    private static final String TAG = NetworkUtils.class.toString();

    static synchronized WifiManager.WifiLock wifiLocker(Context c) {
        if (sWifiLocker == null) {
            Log.d(TAG, "Create WifiManager for " + (Build.VERSION.SDK_INT >= 9 ? "WIFI_MODE_HIPREF" : "WIFI_MODE_FULL"));
            sWifiLocker = ((WifiManager) c.getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(Build.VERSION.SDK_INT >= 9 ? 3 : WifiManager.WIFI_MODE_FULL, "YY");
        }
        return sWifiLocker;
    }

    public static void lockWifi(Context c) {
        Log.d(TAG, "lock wifi");
        if (!wifiLocker(c).isHeld())
            wifiLocker(c).acquire();
    }

    public static void unlockWifi(Context c) {
        Log.d(TAG, "unlock wifi");
        if (wifiLocker(c).isHeld())
            wifiLocker(c).release();
    }

    public static boolean isWifiActive(Context c) {
        if(c == null){
            return false;
        }
        ConnectivityManager mgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = mgr.getActiveNetworkInfo();
        return networkInfo != null
                && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isTrafficActive(Context c) {
        return isNetworkStrictlyAvailable(c) && !isWifiActive(c);
    }

    public static boolean isNetworkStrictlyAvailable(Context c) {
        if(c == null){
            return false;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
        if (ni != null && ni.isAvailable() && ni.isConnected()) {
            return true;
        } else {
            String info = null;
            if (ni != null) {
                info = "network type = " + ni.getType() + ", "
                        + (ni.isAvailable() ? "available" : "inavailable")
                        + ", " + (ni.isConnected() ? "" : "not") + " connected";
            } else {
                info = "no active network";
            }
            Log.i("network", info);
            return false;
        }
    }

    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        }
        return ni.isConnected()
                || (ni.isAvailable() && ni.isConnectedOrConnecting());
    }

//    public static void showNetworkConfigDialog(final Context c, int msgId, int posStrId, int negStrId) {
//        final AlertDialog dialog = new AlertDialog.Builder(c).create();
//
//        dialog.show();
//        Window window = dialog.getWindow();
//        window.setContentView(R.layout.layout_network_error_dialog);
//
//        TextView tip = (TextView)window.findViewById(R.id.message);
//        tip.setText(c.getString(msgId));
//
//        TextView ok = (TextView) window.findViewById(R.id.btn_ok);
//        ok.setText(c.getString(posStrId));
//        ok.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//                openNetworkConfig(c);
//            }
//        });
//
//        TextView cancel = (TextView) window.findViewById(R.id.btn_cancel);
//        cancel.setText(c.getString(negStrId));
//        cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//    }
//
//    public static void showNetworkConfigDialog(Context c) {
//        NetworkUtils.showNetworkConfigDialog(c,
//            R.string.network_error, R.string.set_network,
//            R.string.cancel);
//    }

    public static void openNetworkConfig(Context c) {
        Intent i = null;
        if (android.os.Build.VERSION.SDK_INT > 10) {
            i = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
        } else {
            i = new Intent();
            i.setClassName("com.android.settings", "com.android.settings.WirelessSettings");
            i.setAction(Intent.ACTION_MAIN);
        }
        try {
            c.startActivity(i);
        } catch (Exception e) {
        }
    }

    public static String getSubscriberId(Context c) {
        TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        String id = null;
        if (tm != null) {
            id = tm.getSubscriberId();
        }
        return id != null ? id : "";
    }

    private static final int MIN_PORT = 0;
    private static final int MAX_PORT = 65535;
    private static final int DEFAULT_PROXY_PORT = 80;

    public static InetSocketAddress getTunnelProxy(Context c) {
        if (c.checkCallingOrSelfPermission("android.permission.WRITE_APN_SETTINGS") ==
                PackageManager.PERMISSION_DENIED) {
            return null;
        }
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null) {
            if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return null;
            }
        }
        String proxy = "";
        String portStr = "";
        Uri uri = Uri.parse("content://telephony/carriers/preferapn");
        Cursor cr = c.getContentResolver().query(uri, null, null, null, null);
        if (cr != null && cr.moveToNext()) {
            proxy = cr.getString(cr.getColumnIndex("proxy"));
            portStr = cr.getString(cr.getColumnIndex("port"));
            if (proxy != null && proxy.length() > 0) {
                cr.close();
                cr = null;
                int port;
                try {
                    port = Integer.parseInt(portStr);
                    if (port < MIN_PORT || port > MAX_PORT) {
                        port = DEFAULT_PROXY_PORT;
                    }
                } catch (Exception e) {
                    Log.i("getTunnelProxy", "port is invalid, e = " + e);
                    port = DEFAULT_PROXY_PORT;
                }
                InetSocketAddress addr = null;
                try {
                    addr = new InetSocketAddress(proxy, port);
                } catch (Exception e) {
                    Log.i("getTunnelProxy", "create address failed, e = " + e);
                }
                return addr;
            }
        }
        if (cr != null) {
            cr.close();
            cr = null;
        }
        return null;
    }

    public static byte[] getIPArray(int ip) {
        byte[] ipAddr = new byte[4];
        ipAddr[0] = (byte) ip;
        ipAddr[1] = (byte) (ip >>> 8);
        ipAddr[2] = (byte) (ip >>> 16);
        ipAddr[3] = (byte) (ip >>> 24);
        return ipAddr;
    }

    public static String getIpString(byte[] ip) {
        StringBuilder sb = new StringBuilder();
        sb.append(ip[0] & 0xff);
        sb.append(".");
        sb.append(ip[1] & 0xff);
        sb.append(".");
        sb.append(ip[2] & 0xff);
        sb.append(".");
        sb.append(ip[3] & 0xff);
        return sb.toString();
    }

    public static String getIpString(int ip) {
        StringBuilder sb = new StringBuilder();
        sb.append(ip & 0xff);
        sb.append(".");
        sb.append(ip >>> 8 & 0xff);
        sb.append(".");
        sb.append(ip >>> 16 & 0xff);
        sb.append(".");
        sb.append(ip >>> 24 & 0xff);
        return sb.toString();
    }

    public static int getPort(List<Integer> ports) {
        java.util.Random random = new java.util.Random(
                System.currentTimeMillis());
        return ports.get(random.nextInt(ports.size()));
    }

    public static int getLittleEndianInt(byte[] buffer, int start) {
        int i = buffer[start + 0] & 0xff;
        i |= (buffer[start + 1] << 8) & 0xff00;
        i |= (buffer[start + 2] << 16) & 0xff0000;
        i |= (buffer[start + 3] << 24) & 0xff000000;
        return i;
    }

    public static byte[] toBytes(ByteBuffer buffer) {
        if (buffer == null) {
            return new byte[0];
        }
        int savedPos = buffer.position();
        int savedLimit = buffer.limit();
        try {
            byte[] array = new byte[buffer.limit() - buffer.position()];
            if (buffer.hasArray()) {
                int offset = buffer.arrayOffset() + savedPos;
                byte[] bufferArray = buffer.array();
                System.arraycopy(bufferArray, offset, array, 0, array.length);
                return array;
            } else {
                buffer.get(array);
                return array;
            }
        } finally {
            buffer.position(savedPos);
            buffer.limit(savedLimit);
        }
    }

    public static String getNetwrokNameByType(Context c, int type) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] infos = cm.getAllNetworkInfo();
        for (NetworkInfo network : infos) {
            if (network.getType() == type) {
                return network.getTypeName();
            }
        }
        return "Unknown";
    }

    public static final int NET_INVALID = 0 ;   //  无网络
    public static final int NET_WIFI = 1;
    public static final int NET_2G = 2;
    public static final int NET_3G = 3;
    public static final int NET_4G = 4; //
    public static final int UNKNOW_NETWORK_TYPE = 5;

    public static NetworkInfo getActiveNetwork(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            return cm.getActiveNetworkInfo();
        } catch (Exception e) {
            Log.e("NetworkUtils", "error on getActiveNetworkInfo " + e.toString());
        }
        return null;
    }

    /**
     * get the machine address of wifi
     *
     * @param c
     * @return
     */
    public static String getWifiMacAddr(Context c) {
        try {
        if (c != null) {
            WifiManager wifiMan = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
            if (wifiMan != null) {
                WifiInfo wifiInf = wifiMan.getConnectionInfo();
                if (wifiInf != null) {
                    return wifiInf.getMacAddress();
                }
            }
        }
        }catch (Throwable e){
        }
        return "";
    }

    /**
     * get the type of network
     *
     * @param c
     * @return
     */
    public static int getNetworkType(Context c) {
        int networkType = UNKNOW_NETWORK_TYPE;
        NetworkInfo netInfo = getActiveNetwork(c);
        if (netInfo != null) {
            int type = netInfo.getType();
            if (type == ConnectivityManager.TYPE_WIFI
                    || type == ConnectivityManager.TYPE_WIMAX) {
                networkType = NET_WIFI;
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                int subType = netInfo.getSubtype();
                if (subType == TelephonyManager.NETWORK_TYPE_1xRTT
                        || subType == TelephonyManager.NETWORK_TYPE_UMTS
                        || subType == TelephonyManager.NETWORK_TYPE_EHRPD
                        || subType == TelephonyManager.NETWORK_TYPE_EVDO_0
                        || subType == TelephonyManager.NETWORK_TYPE_EVDO_A
                        || subType == TelephonyManager.NETWORK_TYPE_EVDO_B
                        || subType == TelephonyManager.NETWORK_TYPE_HSDPA
                        || subType == TelephonyManager.NETWORK_TYPE_HSPA
                        || subType == TelephonyManager.NETWORK_TYPE_HSPAP
                        || subType == TelephonyManager.NETWORK_TYPE_HSUPA ){
                    networkType = NET_3G;
                } else if (subType == TelephonyManager.NETWORK_TYPE_GPRS
                        || subType == TelephonyManager.NETWORK_TYPE_CDMA
                        || subType == TelephonyManager.NETWORK_TYPE_EDGE
                        || subType == TelephonyManager.NETWORK_TYPE_IDEN) {
                    networkType = NET_2G;
                }else if(subType == TelephonyManager.NETWORK_TYPE_LTE){
                    networkType = NET_4G;
                }
            }
        }
        return networkType;
    }

    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            int val = b & 0xff;
            if (val < 0x10) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(val));
        }
        return sb.toString();
    }


    public static String getSimOperator(Context c) {
        TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimOperator();
    }

    public static String getOperator(Context c) {
        String sim = getSimOperator(c);
        if(FP.empty(sim))
            return ChinaOperator.UNKNOWN;
        if (sim.startsWith("46003") || sim.startsWith("46005")) {
            return ChinaOperator.CTL;
        } else if (sim.startsWith("46001") || sim.startsWith("46006")) {
            return ChinaOperator.UNICOM;
        } else if (sim.startsWith("46000") || sim.startsWith("46002")
                || sim.startsWith("46007") || sim.startsWith("46020")){
            return ChinaOperator.CMCC;
        }
        else {
            return ChinaOperator.UNKNOWN;
        }
    }

    public static class ChinaOperator {
        public static final String CMCC = "CMCC";
        public static final String CTL = "CTL";
        public static final String UNICOM = "UNICOM";
        public static final String UNKNOWN = "Unknown";
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return null;
    }
}

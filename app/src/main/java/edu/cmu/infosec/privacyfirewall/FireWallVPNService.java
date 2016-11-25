package edu.cmu.infosec.privacyfirewall;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

public class FireWallVPNService extends VpnService {
    private static final String TAG = FireWallVPNService.class.getSimpleName();
    public static final String VPN_ADDRESS = "10.0.0.2"; // Only IPv4 support for now
    private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything

    private static boolean isRunning = false;

    private ParcelFileDescriptor vpnInterface = null;

    private PendingIntent pendingIntent;

    // TODO: Scan, Record the IPPackets in this Queue
    public static ConcurrentLinkedQueue<IPPacket> packetQueue = new ConcurrentLinkedQueue<>();

    // TODO: Add IPs into this Map, VPN will block those IPs in this List
    public static ConcurrentHashMap<String, Boolean> blockingIPList = new ConcurrentHashMap<>();

    private native void jni_init();

    private native void jni_start(int tun, boolean fwd53, int loglevel);

    private native void jni_stop(int tun, boolean clr);

    private native int jni_get_mtu();

    private native int[] jni_get_stats();

    private static native void jni_pcap(String name, int record_size, int file_size);

    private native void jni_socks5(String addr, int port, String username, String password);

    private native void jni_done();

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;

        jni_init();

        setupVPN();

        startNative(vpnInterface);
    }

    /**
     * Called from native code
     *
     * @param buffer
     * @param length
     * @return
     */
    public static boolean filter(byte[] buffer, int length) {
        if (length == 0) return true;

        byte[] b = new byte[length];
        System.arraycopy(buffer, 0, b, 0, length);

        ByteBuffer bf = ByteBuffer.wrap(b);

        try {

            IPPacket packet = new IPPacket(bf);

            Log.d(TAG, packet.ip4Header.sourceAddress + " --> " + packet.ip4Header.destinationAddress);
//            Log.d(TAG, packet.toString());

            FireWallVPNService.packetQueue.offer(packet);

        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void setupVPN() {
        if (vpnInterface == null) {
            Builder builder = new Builder();
            builder.addAddress(VPN_ADDRESS, 32);
            builder.addRoute(VPN_ROUTE, 0);
            vpnInterface = builder.setSession(getString(R.string.app_name)).setConfigureIntent(pendingIntent).establish();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    private void startNative(ParcelFileDescriptor vpn) {
        boolean log = false, log_app = false, filter = true;
        Log.i(TAG, "Start native log=" + log + "/" + log_app + " filter=" + filter);
        jni_socks5("", 0, "", "");
        jni_start(vpn.getFd(), true, 0);
    }

    private void stopNative(ParcelFileDescriptor vpn, boolean clear) {
        Log.i(TAG, "Stop native clear=" + clear);
        try {
            jni_stop(vpn.getFd(), clear);
        } catch (Throwable ex) {
            // File descriptor might be closed
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            jni_stop(-1, clear);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        cleanup();
        Log.i(TAG, "Stopped");
    }

    private void cleanup() {
        closeResources(vpnInterface);
    }

    private static void closeResources(Closeable... resources) {
        for (Closeable resource : resources) {
            try {
                resource.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }


    /**
     * Called from native code
     *
     * @param packet
     * @return
     */
    private Allowed isAddressAllowed(Packet packet) {

        if (blockingIPList.containsKey(packet.daddr)) return null;

        Log.d(TAG, packet.daddr);

        return new Allowed();
    }

}

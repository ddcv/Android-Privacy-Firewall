package edu.cmu.infosec.privacyfirewall;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.support.v4.util.Pair;

import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class FireWallVPNService extends VpnService {
    private static final String TAG = FireWallVPNService.class.getSimpleName();
    public static final String VPN_ADDRESS = "10.0.0.2"; // Only IPv4 support for now
    private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything

    private static boolean isRunning = false; // VPN status

    private ParcelFileDescriptor vpnInterface = null; // vpn ParcelFileDescriptor

    private PendingIntent pendingIntent; // used for establish vpn service

    private final IBinder mBinder = new LocalBinder(); // binder

    // A thread-safe queue used for :
    // 1. VPN network thread(implemented in C code) keep putting all output packets into it
    // 2. VPN filter thread keep polling packets and do the scanning jobs
    public static ConcurrentLinkedQueue<IPPacket> packetQueue = new ConcurrentLinkedQueue<>();

    // A thread-safe queue store banned IP list,
    // Pair:String is the IP address
    // Pair:Integer is the port
    public static ConcurrentLinkedQueue<Pair<String, Integer>>
            blockingIPMap = new ConcurrentLinkedQueue<>();

    // Native function: do the init work of network
    private native void jni_init();
    // Native function: start network part
    private native void jni_start(int tun, boolean fwd53, int loglevel);
    // Native function: stop network part
    private native void jni_stop(int tun, boolean clr);
    // Native function: configure the socket
    private native void jni_socks5(String addr, int port, String username, String password);

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;

        jni_init();

        setupVPN();

        startNative(vpnInterface);

        Thread consumer = new Thread(new VPNRunnable());
        consumer.start();
    }

    /**
     * Called from native code
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

    public void stopVPN() {
        Log.i(TAG, "Stopping");
        try {
            vpnInterface.close();
        } catch (IOException ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    /**
     * Start native network part
     * @param vpn
     */
    private void startNative(ParcelFileDescriptor vpn) {
        boolean log = false, log_app = false, filter = true;
        Log.i(TAG, "Start native log=" + log + "/" + log_app + " filter=" + filter);
        jni_socks5("", 0, "", "");
        jni_start(vpn.getFd(), true, 0);
    }

    /**
     * Stop native network part
     * @param vpn
     * @param clear
     */
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
        stopNative(vpnInterface, true);
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
            }
        }
    }


    /**
     * Called from native code
     * @param packet
     * @return
     */
    private Allowed isAddressAllowed(Packet packet) {

        Iterator<Pair<String, Integer>> it = blockingIPMap.iterator();
        while (it.hasNext()) {
            Pair<String, Integer> p = it.next();
            if (p.first.equals(packet.daddr)) {
                if (p.second == NetUtils.readProcFile(packet.sport)) {
                    return null;
                }
            }
        }

        Log.d(TAG, packet.daddr);

        return new Allowed();
    }

    /**
     * Runnable for vpn thread,
     * do scanning works
     */
    private class VPNRunnable implements Runnable {

        @Override
        public void run() {
            try {
                Thread currentThread = Thread.currentThread();
                while (true) {
                    IPPacket currentPacket;
                    do {
                        currentPacket = FireWallVPNService.packetQueue.poll();
                        if (currentPacket != null)
                            break;
                        Thread.sleep(10);
                    } while (!currentThread.isInterrupted());

                    if (currentThread.isInterrupted())
                        break;

                    // TODO: do scan and record on currentPacket
                    // TODO: Add IPs into Block Map, VPN will block those IPs
                    Monitor monitorTask = new Monitor(currentPacket);
                    monitorTask.execute();

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        FireWallVPNService getService() {
            return FireWallVPNService.this;
        }
    }

    /**
     * Delete rule from block list.
     */
    public static void deleteRulePair(String ipaddr, int appId) {
        Iterator<Pair<String, Integer>> it = FireWallVPNService.blockingIPMap.iterator();
        while (it.hasNext()) {
            Pair<String, Integer> rulePair = it.next();
            if (rulePair.first.equals(ipaddr) && rulePair.second == appId) {
                FireWallVPNService.blockingIPMap.remove(rulePair);
                break;
            }
        }
    }
}

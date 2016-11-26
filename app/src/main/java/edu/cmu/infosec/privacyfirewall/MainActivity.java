package edu.cmu.infosec.privacyfirewall;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    /**
     * VPN Part Variables Start
     */
    private static final int VPN_REQUEST_CODE = 0x0F;    /* VPN Request code, used when start VPN */
    private Intent serviceIntent;    /* The VPN Service Intent */

//    boolean isBindWithService = false;
//    FireWallVPNService myService;
//    public ServiceConnection mServiceConn = new ServiceConnection() {
//        public void onServiceConnected(ComponentName className, IBinder binder) {
//            Log.d("ServiceConnection","connected");
//            myService = ((FireWallVPNService.LocalBinder) binder).getService();
//            isBindWithService = true;
//        }
//
//        public void onServiceDisconnected(ComponentName className) {
//            Log.d("ServiceConnection","disconnected");
//            myService = null;
//            isBindWithService = false;
//        }
//    };

    /**
     * VPN Part Variables End
     */


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startVPN();
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        doBindService();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        doUnbindService();
//    }

    /**
     * Start the VPNService
     */
    public void startVPN() {
        serviceIntent = FireWallVPNService.prepare(getApplicationContext());
        if (serviceIntent != null) {
            startActivityForResult(serviceIntent, VPN_REQUEST_CODE);
        } else {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
        }
    }

//    public void stopVPN(){
//        if (isBindWithService) {
//            myService.stopVPN();
//        }
//    }

//    private void doUnbindService() {
//        unbindService(mServiceConn);
//        isBindWithService = false;
//    }
//
//    private void doBindService() {
//        if (isBindWithService) {
//            Intent bindIntent = new Intent(this, FireWallVPNService.class);
//            isBindWithService = bindService(bindIntent, mServiceConn, Context.BIND_AUTO_CREATE);
//        }
//    }
    /**
     * OnActivityResult
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
//            Intent intent = new Intent(this, FirewallVpnService.class);
            Intent intent = new Intent(this, FireWallVPNService.class);
            startService(intent);

//            bindService(new Intent("edu.cmu.infosec.privacy.FireWallVPNService.BIND"),
//                    mServiceConn, Context.BIND_AUTO_CREATE);
        }
    }
}

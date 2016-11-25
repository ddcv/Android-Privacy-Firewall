package edu.cmu.infosec.privacyfirewall;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    /**
     * VPN Part Variables Start
     */
    private static final int VPN_REQUEST_CODE = 0x0F;    /* VPN Request code, used when start VPN */
    private Intent serviceIntent;    /* The VPN Service Intent */

    /**
     * VPN Part Variables End
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startVPN();
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

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

    public static void add(int i) {
        System.out.print("asdasdas");
    }

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
        }
    }
}

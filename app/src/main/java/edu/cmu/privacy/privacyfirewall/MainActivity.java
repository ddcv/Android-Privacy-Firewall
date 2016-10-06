package edu.cmu.privacy.privacyfirewall;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    /** VPN Part Variables Start */
    private static final int VPN_REQUEST_CODE = 0x0;    /* VPN Request code, used when start VPN */
    private Intent serviceIntent;    /* The VPN Service Intent */
    /** VPN Part Variables End   */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        // Database Write and Read Demo
//        // init connection database
//        ConnectionDatabase cDb = new ConnectionDatabase(MainActivity.this);
//
//        // write data
//        SQLiteDatabase cDbWrite = cDb.getWritableDatabase();
//        cDb.insertConnection(cDbWrite, "App2", "192.168.0.2", "CMU", "", "ACCEPT");
//
//        // read data
//        SQLiteDatabase cDbRead = cDb.getReadableDatabase();
//        Cursor cur = cDb.getConnectionCursor(cDbRead, "app");
//        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
//            Log.i(ConnectionDatabase.DATABASE_TAG, "Output: " +
//                    "id = " + cur.getString(cur.getColumnIndex("id")) +
//                    ", app = " + cur.getString(cur.getColumnIndex("app")) +
//                    ", IP = " + cur.getString(cur.getColumnIndex("IP")) +
//                    ", org = " + cur.getString(cur.getColumnIndex("org")) +
//                    ", sensitive = " + cur.getString(cur.getColumnIndex("sensitive")) +
//                    ", action = " + cur.getString(cur.getColumnIndex("action")));
//        }


        /** VPN Part Demo Start */

        /* Start VPN */
//        serviceIntent = VpnTestService.prepare(getApplicationContext());
//        if (serviceIntent != null) {
//            startActivityForResult(serviceIntent, VPN_REQUEST_CODE);
//        } else {
//            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
//        }

        /** VPN Part Demo End   */
    }


    /** VPN Part Functions Start */

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, VpnTestService.class);
            startService(intent);
        }
    }

    /** VPN Part Functions End   */

}

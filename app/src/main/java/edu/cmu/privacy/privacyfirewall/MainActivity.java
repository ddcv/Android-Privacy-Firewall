package edu.cmu.privacy.privacyfirewall;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    /** VPN Part Variables Start */
    private static final int VPN_REQUEST_CODE = 0x0;    /* VPN Request code, used when start VPN */
    private Intent serviceIntent;    /* The VPN Service Intent */
    /** VPN Part Variables End   */

    private ListView listView;
    private TextView listappDisplay;
    private String listappName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /** UI Component Start */

        /**
         * Find Alternative way to use Actual App Names
         */
        listView = (ListView) findViewById(android.R.id.list);
        String[] values = new String[] { "My First App", "Game", "Malware", "Social Media", "Bloatware", "Phone" };
        //
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, values);
        listView.setAdapter(adapter);
        listappDisplay = (TextView) findViewById(R.id.text1);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listappName = (String) listView.getItemAtPosition(position);
                listappDisplay.setText(listappName);            }
        });



        /** UI Component End*/


        /** Database Demo Start */

//        // init database
//        DatabaseInterface db = new DataBaseController(MainActivity.this);
//
//        // write data
//        db.insertApplication("Amazon", "Online Shopping");
//        db.insertApplication("BestBuy", "Online Shopping");
//        db.insertRule("192.168.0.1", "Host", 1);
//        db.insertRule("4.4.4.4", "Evil", 0);
//        db.insertRule("8.8.8.8", "Good guy", 1);
//        db.insertConnection(1, 1, "Good data", 0);
//        db.insertConnection(1, 2, "Evil data", 1);
//        db.insertConnection(2, 1, "General data", 0);
//
//        // print all the application, its connection and action
//
//        // For each Application
//        Cursor appCur = db.getAllApplicationCursor();
//        for (appCur.moveToFirst(); !appCur.isAfterLast(); appCur.moveToNext()) {
//            ContentValues appVal = new ContentValues();
//            DatabaseUtils.cursorRowToContentValues(appCur, appVal);
//            Log.i(ApplicationDatabase.DATABASE_TAG, "Output-Application: " +
//                    "id = " + appVal.getAsString(ApplicationDatabase.FIELD_ID) +
//                    ", name = " + appVal.getAsString(ApplicationDatabase.FIELD_NAME) +
//                    ", description = " + appVal.getAsString(ApplicationDatabase.FIELD_DESC));
//
//            // For each connection of the application
//            Cursor cntCur = db.getConnectionCursorByAppId(
//                                                appVal.getAsInteger(ApplicationDatabase.FIELD_ID));
//            for (cntCur.moveToFirst(); !cntCur.isAfterLast(); cntCur.moveToNext()) {
//                ContentValues cntVal = new ContentValues();
//                DatabaseUtils.cursorRowToContentValues(cntCur, cntVal);
//                Log.i(ConnectionDatabase.DATABASE_TAG, "\tConnection: " +
//                        "id = " + cntVal.getAsInteger(ConnectionDatabase.FIELD_ID) +
//                        ", content = " + cntVal.getAsString(ConnectionDatabase.FIELD_CONTENT) +
//                        ", sensitive = " + cntVal.getAsInteger(ConnectionDatabase.FIELD_SENSITIVE));
//                Cursor ruleCur = db.getRuleCursorById(
//                                                cntVal.getAsInteger(ConnectionDatabase.FIELD_RULE));
//
//                // For each rule of the connection
//                for (ruleCur.moveToFirst(); !ruleCur.isAfterLast(); ruleCur.moveToNext()) {
//                    ContentValues ruleVal = new ContentValues();
//                    DatabaseUtils.cursorRowToContentValues(ruleCur, ruleVal);
//                    Log.i(RuleDatabase.DATABASE_TAG, "\t\t\t\tRule: " +
//                            "id = " + ruleVal.getAsInteger(RuleDatabase.FIELD_ID) +
//                            ", ipAdd = " + ruleVal.getAsString(RuleDatabase.FIELD_IP_ADD) +
//                            ", ipOwner = " + ruleVal.getAsString(RuleDatabase.FIELD_ID_OWNER) +
//                            ", action = " + ruleVal.getAsInteger(RuleDatabase.FIELD_ACTION));
//                }
//            }
//        }

        /** Database Demo End */

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

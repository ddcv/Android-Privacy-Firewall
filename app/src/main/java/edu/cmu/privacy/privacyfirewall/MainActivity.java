package edu.cmu.privacy.privacyfirewall;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

// Needed for AppList UI
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.widget.ListAdapter;

public class MainActivity extends ListActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create our own version of the list adapter
        List<Privacyapp> apps = getData();
        ListAdapter adapter = new AppListAdapter(this, apps, R.layout.applist, new String[] {
                Privacyapp.KEY_APPNAME, Privacyapp.KEY_DETAIL }, new int[] { android.R.id.text1, android.R.id.text2 });
        this.setListAdapter(adapter);
    }

    private List<Privacyapp> getData() {
        List<Privacyapp> apps = new ArrayList<Privacyapp>();
        apps.add(new Privacyapp("Dodge", "Viper"));
        apps.add(new Privacyapp("Chevrolet", "Corvette"));
        apps.add(new Privacyapp("Aston Martin", "Vanquish"));
        apps.add(new Privacyapp("Lamborghini", "Diablo"));
        apps.add(new Privacyapp("Ford", "Pinto"));
        return apps;
    }
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

}

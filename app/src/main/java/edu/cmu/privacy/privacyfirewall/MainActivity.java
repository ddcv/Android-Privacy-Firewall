package edu.cmu.privacy.privacyfirewall;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

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

    }
}

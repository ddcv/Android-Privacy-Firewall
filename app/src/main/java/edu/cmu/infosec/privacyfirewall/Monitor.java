package edu.cmu.infosec.privacyfirewall;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Patterns;

import java.nio.charset.StandardCharsets;

/**
 * Created by YunfanW on 9/30/2016.
 */

public class Monitor extends AsyncTask<Void, Void, Void> {
    public static DatabaseInterface db;
    public final static String MONITOR_TAG = "Monitor";
    private IPPacket p;

    public Monitor(IPPacket _p) {
        super();
        p = _p;
    }

    public void filter() {
        int rId;
        p.contentBuffer.position(0);
        p.contentBuffer.flip();
        byte[] bytes = new byte[p.contentBuffer.remaining()];
        String plaintext;
        Cursor c;

        p.contentBuffer.get(bytes);
        plaintext = new String(bytes, StandardCharsets.UTF_8);

        if (!plaintext.equals("")) {
            Log.d(MONITOR_TAG, "plaintext!");
        }

        /** Filter packet */
        c = db.getRuleCursorByAdd(p.ip4Header.destinationAddress.getHostAddress());
        if (c.getCount() < 1) {
            db.insertRule(p.ip4Header.destinationAddress.getHostAddress(), RuleDatabase.ORG_DEFAULT,
                    RuleDatabase.COUNTRY_DEFAULT);
            c = db.getRuleCursorByAdd(p.ip4Header.destinationAddress.getHostAddress());
        }

        /** Get action & rId */
        c.moveToFirst();
        rId = c.getInt(c.getColumnIndex(RuleDatabase.FIELD_ID));

        int port = -1;
        int uid = -1;
        if (p.isTCP()) {
            port = p.tcpHeader.sourcePort;
        } else if (p.isUDP()) {
            port = p.udpHeader.sourcePort;
        }

        if (port != -1) {
            uid = NetUtils.readProcFile(port);
        }

        if (uid != -1) {
            c = db.getApplicationCursorById(uid);
            c.moveToFirst();
            if (c.getCount() > 0) {
                c.moveToFirst();
                ContentValues appVal = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(c, appVal);
                String appName = appVal.getAsString(ApplicationDatabase.FIELD_NAME);



                /** Create Connection */
                String sensitiveContent = scanSensitive(plaintext);
                int sensitive = sensitiveContent.equals("") ? ConnectionDatabase.NON_SENSITIVE :
                        ConnectionDatabase.SENSITIVE;

                c = db.getConnectionCursorByAppId(uid);
                boolean exist = false;
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    DatabaseUtils.cursorRowToContentValues(c, appVal);
                    if (appVal.getAsInteger(ConnectionDatabase.FIELD_RULE) == rId) {
                        exist = true;
                        break;
                    }
                }

                Log.d(MONITOR_TAG, "exist = " + exist);
                if (!exist) {
                    db.insertConnection(uid, rId, ConnectionDatabase.ACTION_ALOW,
                            sensitiveContent, sensitive);
                    Log.d(MONITOR_TAG, "uid = " + uid);
                    Log.d(MONITOR_TAG, "rId = " + rId);
                    Log.d(MONITOR_TAG, "AppName = " + appName);
                    Log.d(MONITOR_TAG, "DestAddr = " +
                            p.ip4Header.destinationAddress.getHostAddress());
                    Log.d(MONITOR_TAG, "plaintext = " + plaintext);
                }
            }
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        filter();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }

    public static boolean checkIPAddr(String ipaddr) {
        return Patterns.IP_ADDRESS.matcher(ipaddr).matches();
    }

    private static String scanSensitive(String plaintext) {
        String result = "";
        if (Patterns.PHONE.matcher(plaintext).matches()) {
            result = result + "Phone ";
        }
        return result;
    }
}

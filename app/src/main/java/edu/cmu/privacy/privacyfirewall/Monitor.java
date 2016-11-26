package edu.cmu.privacy.privacyfirewall;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by YunfanW on 9/30/2016.
 */

public class Monitor {
    public static DatabaseInterface db;
    public final static String MONITOR_TAG = "Monitor";
    public static IPPacket filter(IPPacket p) {
        AuxiliaryInterface aux;
        int action = 1;
        int rId;
        byte[] bytes = new byte[p.contentBuffer.remaining()];
        String plaintext;
        Cursor c;

        p.contentBuffer.get(bytes);
        plaintext = new String(bytes, StandardCharsets.UTF_8);

        //Log.d(MONITOR_TAG, p.toString());
        //Log.d(MONITOR_TAG, p.ip4Header.destinationAddress.getHostAddress());
        //Log.d(MONITOR_TAG, plaintext);

        /** Filter packet */
        c = db.getRuleCursorByAdd(p.ip4Header.destinationAddress.getHostAddress());
//        if (c.isAfterLast()) {
//            // TODO: trace recipient
//            //String recipient = aux.traceRecipient(p.ip4Header.destinationAddress);
//            db.insertRule(p.ip4Header.destinationAddress.getHostAddress(), RuleDatabase.ORG_DEFAULT,
//                    RuleDatabase.COUNTRY_DEFAULT);
//            c = db.getRuleCursorByAdd(p.ip4Header.destinationAddress.getHostAddress());
//            Log.d(MONITOR_TAG, "Add new filter rule: " + p.ip4Header.destinationAddress.getHostAddress());
//        }

        /** Get action & rId */
        c.moveToFirst();
        ContentValues rVal = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(c, rVal);
        action = ConnectionDatabase.ACTION_ALOW;
        rId = rVal.getAsInteger(RuleDatabase.FIELD_ID);

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
            if (!c.isAfterLast()) {


                c.moveToFirst();
                ContentValues appVal = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(c, appVal);
                String appName = appVal.getAsString(ApplicationDatabase.FIELD_NAME);

                /** Create Connection */
                String sensitiveContent = plaintext;
                int sensitive = ConnectionDatabase.NON_SENSITIVE;

                // TODO: scan sensitive content
//                sensitiveContent = aux.scanSensitive(plaintext);
//                if (sensitiveContent == null) {
//                    sensitive = 0;
//                } else {
//                    sensitive = 1;
//                }

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
                if (exist == false) {
                    db.insertConnection(uid, rId, ConnectionDatabase.ACTION_ALOW,
                            sensitiveContent, sensitive);
                    Log.d(MONITOR_TAG, "uid = " + uid);
                    Log.d(MONITOR_TAG, "rId = " + rId);
                    Log.d(MONITOR_TAG, "AppName = " + appName);
                    Log.d(MONITOR_TAG, "IPPacket = " + p.toString());
                    Log.d(MONITOR_TAG, "DestAddr = " + p.ip4Header.destinationAddress.getHostAddress());
                    Log.d(MONITOR_TAG, "plaintext = " + plaintext);
                }


            }
        }

        if (action == ConnectionDatabase.ACTION_ALOW) {
            return p;
        } else {
            return null;
        }
    }

    public static boolean checkIPAddr(String ipaddr) {
        return Patterns.IP_ADDRESS.matcher(ipaddr).matches();
    }
}

package edu.cmu.privacy.privacyfirewall;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;

/**
 * Created by YunfanW on 9/30/2016.
 */

public class Monitor {
    public static DatabaseInterface db;
    public static IPPacket filter(IPPacket p) {
        AuxiliaryInterface aux;
        int action = 1;
        int rId;
        char[] plaintext;

//        // Filter packet
//        Cursor c = db.getRuleCursorByAdd(p.ip4Header.destinationAddress.toString());
//        if (c.isAfterLast()) {
//            String recipient = aux.traceRecipient(p.ip4Header.destinationAddress);
//            db.insertRule(p.ip4Header.destinationAddress.toString(), recipient, 1);
//            c = db.getRuleCursorByAdd(p.ip4Header.destinationAddress.toString());
//        }
//
//        // Get action & rId
//        ContentValues rVal = new ContentValues();
//        DatabaseUtils.cursorRowToContentValues(c, rVal);
//        action = rVal.getAsInteger(RuleDatabase.FIELD_ACTION);
//        rId = rVal.getAsInteger(RuleDatabase.FIELD_ID);
//
//        // Create connection
//        plaintext = p.contentBuffer.asCharBuffer().array();
//        String sensitiveContent = aux.scanSensitive(plaintext);
//        int sensitive;
//        int appId;
//        if (sensitiveContent == null) {
//            sensitive = 0;
//        } else {
//            sensitive = 1;
//        }
//        db.insertConnection(appId, rId, sensitiveContent, sensitive);

        if (action == 1) {
            return p;
        } else {
            return null;
        }
    }
}

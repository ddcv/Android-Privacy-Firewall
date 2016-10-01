package edu.cmu.privacy.privacyfirewall;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by YunfanW on 9/30/2016.
 */

public interface QueryConnection {
    public Cursor getConnectionCursor(SQLiteDatabase db, String app);
}

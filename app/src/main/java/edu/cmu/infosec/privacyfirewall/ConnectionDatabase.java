package edu.cmu.infosec.privacyfirewall;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by YunfanW on 9/30/2016.
 */

public class ConnectionDatabase extends SQLiteOpenHelper{
    public final static String TABLE_NAME = "Connection";
    public final static String FIELD_ID = "cid";
    public final static String FIELD_ACTION = "action";
    public final static String FIELD_CONTENT = "content";
    public final static String FIELD_SENSITIVE = "sensitive";
    public final static String FIELD_APP = "aid";
    public final static String FIELD_RULE = "rid";

    public final static String CONTENT_DEFAULT = "NULL";
    public final static int SENSITIVE = 1;
    public final static int NON_SENSITIVE = 0;
    public final static int ACTION_ALOW = 1;
    public final static int ACTION_DENY = 0;

    public final static String DATABASE_TAG = "ConnectionDB";

    public ConnectionDatabase(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ");
        sql.append(TABLE_NAME);
        sql.append(" (");
        sql.append(FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sql.append(FIELD_ACTION + " INTEGER NOT NULL, ");
        sql.append(FIELD_CONTENT + " TEXT, ");
        sql.append(FIELD_SENSITIVE + " INTEGER NOT NULL, ");
        sql.append(FIELD_APP + " INTEGER NOT NULL, ");
        sql.append(FIELD_RULE + " INTEGER NOT NULL");
        sql.append(");");
        db.execSQL(sql.toString());
    }

    public static boolean insertConnection(SQLiteDatabase db, int appId, int ruleId, int action,
                                           String content, int sensitive) {
        ContentValues v = new ContentValues();
        v.put(FIELD_APP, appId);
        v.put(FIELD_RULE, ruleId);
        v.put(FIELD_CONTENT, content);
        v.put(FIELD_ACTION, action);
        v.put(FIELD_SENSITIVE, sensitive);
        if (db.insert(TABLE_NAME, "null", v) != -1) {
            return true;
        } else {
            return false;
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static Cursor getConnectionCursorByAppId(SQLiteDatabase db, int appId) {
        /*
         * table
         * columns
         * selection
         * selectionArgs
         * groupBy
         * having
         * orderBy
         * */
        return db.query(TABLE_NAME, null, FIELD_APP + " = " + appId, null, null, null, null);
    }

    public static Cursor getConnectionCursorByAppIdRuleId(SQLiteDatabase db, int appId, int ruleId)
    {
        /*
         * table
         * columns
         * selection
         * selectionArgs
         * groupBy
         * having
         * orderBy
         * */
        return db.query(TABLE_NAME, null, FIELD_APP + " = " + appId + " AND " + FIELD_RULE + " = " +
                ruleId, null, null, null, null);
    }

    public static Cursor getAllConnectionCursor(SQLiteDatabase db) {
        return db.query(TABLE_NAME, null, null, null, null, null, null);
    }

    public static void deleteConnectionByAppIdRuleId(SQLiteDatabase db, int appId, int ruleId) {
        db.delete(TABLE_NAME, FIELD_RULE + "=? AND " + FIELD_APP + "=?",
                new String[] {String.valueOf(ruleId), String.valueOf(appId)});
    }

    public static boolean updateAction(SQLiteDatabase db, int appId, int ruleId, int action) {
        Cursor c = db.query(TABLE_NAME, null, FIELD_APP + " = " + appId + " AND " + FIELD_RULE +
                " = " + ruleId, null, null, null, null);
        ContentValues v = new ContentValues();
        c.moveToFirst();
        DatabaseUtils.cursorRowToContentValues(c, v);
        v.put(FIELD_ACTION, action);
        c.close();
        if (db.replace(TABLE_NAME, "null", v) != -1) {
            return true;
        } else {
            return false;
        }
    }
}

package edu.cmu.privacy.privacyfirewall;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by YunfanW on 9/30/2016.
 */

public class ConnectionDatabase extends SQLiteOpenHelper{
    final static String TABLE_NAME = "Connection";
    final static String FIELD_ID = "cid";
    final static String FIELD_CTIME = "cTime";
    final static String FIELD_CONTENT = "content";
    final static String FIELD_SENSITIVE = "sensitive";
    final static String FIELD_APP = "aid";
    final static String FIELD_RULE = "rid";

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
        sql.append(FIELD_CTIME + " DATETIME DEFAULT CURRENT_TIMESTAMP, ");
        sql.append(FIELD_CONTENT + " TEXT, ");
        sql.append(FIELD_SENSITIVE + " INTEGER NOT NULL, ");
        sql.append(FIELD_APP + " INTEGER NOT NULL, ");
        sql.append(FIELD_RULE + " INTEGER NOT NULL");
        sql.append(");");
        db.execSQL(sql.toString());
    }

    public static boolean insertConnection(SQLiteDatabase db, int appId, int ruleId,
                                           String content, int sensitive) {
        ContentValues v = new ContentValues();
        v.put(FIELD_APP, appId);
        v.put(FIELD_RULE, ruleId);
        v.put(FIELD_CONTENT, content);
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

    public static void deleteConnectionByRuleId(SQLiteDatabase db, int ruleId) {
        db.delete(TABLE_NAME, FIELD_RULE + "=?", new String[] {String.valueOf(ruleId)});
    }
}

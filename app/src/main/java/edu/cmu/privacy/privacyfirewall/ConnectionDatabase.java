package edu.cmu.privacy.privacyfirewall;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by YunfanW on 9/30/2016.
 */

public class ConnectionDatabase extends SQLiteOpenHelper implements QueryConnection{
    private final static String DATABASE_NAME = "ConnectionDB";
    private final static int DATABASE_VERSION = 1;
    private final static String TABLE_NAME = "ConnectionInfo";
    public final static String FIELD_ID = "id";
    public final static String FIELD_APP = "app";
    public final static String FIELD_IP = "IP";
    public final static String FIELD_ORG = "org";
    public final static String FIELD_SENSITIVE = "sensitive";
    public final static String FIELD_ACTION = "action";

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
        sql.append(FIELD_IP + " TEXT, ");
        sql.append(FIELD_APP + " TEXT, ");
        sql.append(FIELD_ORG + " TEXT, ");
        sql.append(FIELD_SENSITIVE + " TEXT, ");
        sql.append(FIELD_ACTION + " TEXT");
        sql.append(");");
        db.execSQL(sql.toString());
    }

    public static void insertConnection(SQLiteDatabase db, String app, String ip, String org,
                                        String sensitive, String action) {
        ContentValues v = new ContentValues();
        v.put("app", app);
        v.put("org", org);
        v.put("sensitive", sensitive);
        v.put("action", action);
        db.insert(TABLE_NAME, "null", v);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Cursor getConnectionCursor(SQLiteDatabase db, String app) {
        /*
         * table
         * columns
         * selection
         * selectionArgs
         * groupBy
         * having
         * orderBy
         * */
        return db.query(TABLE_NAME, null, FIELD_APP + " = " + app, null, null, null, null);
    }
}

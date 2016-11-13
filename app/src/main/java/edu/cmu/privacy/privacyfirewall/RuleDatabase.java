package edu.cmu.privacy.privacyfirewall;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import 	android.database.DatabaseUtils;

/**
 * Created by YunfanW on 10/9/2016.
 */

public class RuleDatabase extends SQLiteOpenHelper {
    final static String TABLE_NAME = "Rule";
    final static String FIELD_ID = "rid";
    final static String FIELD_IP_ADD = "ipAdd";
    final static String FIELD_ID_OWNER = "ipOwner";
    final static String FIELD_ACTION = "action";

    public final static String DATABASE_TAG = "RuleDB";

    public RuleDatabase(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ");
        sql.append(TABLE_NAME);
        sql.append(" (");
        sql.append(FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sql.append(FIELD_IP_ADD + " TEXT, ");
        sql.append(FIELD_ID_OWNER + " TEXT, ");
        sql.append(FIELD_ACTION + " BIT NOT NULL");
        sql.append(");");
        db.execSQL(sql.toString());
    }

    public static boolean insertRule(SQLiteDatabase db, String ipAdd, String ipOwner
                                            , int action) {
        ContentValues v = new ContentValues();
        v.put(FIELD_IP_ADD, ipAdd);
        v.put(FIELD_ID_OWNER, ipOwner);
        v.put(FIELD_ACTION, action);
        if (db.insert(TABLE_NAME, "null", v) != -1) {
            return true;
        } else {
            return false;
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static Cursor getRuleCursorByAdd(SQLiteDatabase db, String ipAdd) {
        /*
         * table
         * columns
         * selection
         * selectionArgs
         * groupBy
         * having
         * orderBy
         * */
        return db.query(TABLE_NAME, null, FIELD_IP_ADD + " = \"" + ipAdd + "\"", null, null, null, null);
    }

    public static Cursor getRuleCursorById(SQLiteDatabase db, int id) {
        /*
         * table
         * columns
         * selection
         * selectionArgs
         * groupBy
         * having
         * orderBy
         * */
        return db.query(TABLE_NAME, null, FIELD_ID + " = " + id, null, null, null, null);
    }

    public static int getNewRuleId(SQLiteDatabase db) {
        return db.query(TABLE_NAME, null, null, null, null, null, null).getCount() + 1;
    }

    public static boolean updateAction(SQLiteDatabase db, int id, int action) {
        Cursor c = db.query(TABLE_NAME, null, FIELD_ID + " = " + id, null, null, null, null);
        ContentValues v = new ContentValues();
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

package edu.cmu.infosec.privacyfirewall;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import 	android.database.DatabaseUtils;
import android.util.Log;

/**
 * Created by YunfanW on 10/9/2016.
 */

public class RuleDatabase extends SQLiteOpenHelper {
    final static String TABLE_NAME = "Rule";
    final static String FIELD_ID = "rid";
    final static String FIELD_IP_ADD = "ipAdd";
    final static String FIELD_ORG = "organization";
    final static String FIELD_COUNTRY = "country";

    final static String ORG_DEFAULT = "Not Known";
    final static String COUNTRY_DEFAULT = "Not Known";

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
        sql.append(FIELD_ORG + " TEXT, ");
        sql.append(FIELD_COUNTRY + " TEXT");
        sql.append(");");
        db.execSQL(sql.toString());
    }

    public static boolean insertRule(SQLiteDatabase db, String ipAdd, String ipOwner,
                                     String country) {
        ContentValues v = new ContentValues();
        v.put(FIELD_IP_ADD, ipAdd);
        v.put(FIELD_ORG, ipOwner);
        v.put(FIELD_COUNTRY, country);
        if (db.insert(TABLE_NAME, "null", v) != -1) {
            Log.i("Rule", "insert rule id = " + (getNewRuleId(db) - 1));
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



    public static boolean updateRegistrant(SQLiteDatabase db, int id, String organization,
                                           String country) {
        Cursor c = db.query(TABLE_NAME, null, FIELD_ID + " = " + id, null, null, null, null);
        Log.i("Rule", "update rule id = " + id);
        if (c.getCount() >= 1) {
            ContentValues v = new ContentValues();
            c.moveToFirst();

            DatabaseUtils.cursorRowToContentValues(c, v);
            v.put(FIELD_ORG, organization);
            v.put(FIELD_COUNTRY, country);
            c.close();
            if (db.replace(TABLE_NAME, "null", v) != -1) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    public static void deleteRuleById(SQLiteDatabase db, int id) {
        Log.i("Rule", "delete id = " + id);
        db.delete(TABLE_NAME, FIELD_ID + "=?", new String[] {String.valueOf(id)});
    }
}

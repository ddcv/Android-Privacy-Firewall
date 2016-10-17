package edu.cmu.privacy.privacyfirewall;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by YunfanW on 10/9/2016.
 */

public class ApplicationDatabase extends SQLiteOpenHelper{
    final static String TABLE_NAME = "Application";
    final static String FIELD_ID = "aid";
    final static String FIELD_NAME = "name";
    final static String FIELD_DESC = "description";

    public final static String DATABASE_TAG = "ApplicationDB";

    public ApplicationDatabase(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ");
        sql.append(TABLE_NAME);
        sql.append(" (");
        sql.append(FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sql.append(FIELD_NAME + " TEXT, ");
        sql.append(FIELD_DESC + " TEXT");
        sql.append(");");
        db.execSQL(sql.toString());
    }

    public static boolean insertApplication(SQLiteDatabase db, String name, String description) {
        ContentValues v = new ContentValues();
        v.put(FIELD_NAME, name);
        v.put(FIELD_DESC, description);
        if (db.insert(TABLE_NAME, "null", v) != -1) {
            return true;
        } else {
            return false;
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static Cursor getAllApplicationCursor(SQLiteDatabase db) {
        /*
         * table
         * columns
         * selection
         * selectionArgs
         * groupBy
         * having
         * orderBy
         * */
        return db.query(TABLE_NAME, null, null, null, null, null, null);
    }


    public static Cursor getApplicationCursorByAppName(SQLiteDatabase db, String appName) {
        /*
         * table
         * columns
         * selection
         * selectionArgs
         * groupBy
         * having
         * orderBy
         * */
        return db.query(TABLE_NAME, null, FIELD_NAME + " = " + appName, null, null, null, null);
    }
}

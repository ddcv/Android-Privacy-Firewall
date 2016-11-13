package edu.cmu.privacy.privacyfirewall;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.ByteArrayOutputStream;

/**
 * Created by YunfanW on 10/9/2016.
 */

public class DataBaseController implements DatabaseInterface {
    private ConnectionDatabase cDb;
    private ApplicationDatabase aDb;
    private RuleDatabase rDb;

    public DataBaseController(Context context) {
        aDb = new ApplicationDatabase(context);
        rDb = new RuleDatabase(context);
        cDb = new ConnectionDatabase(context);
    }

    /** Connection Database Interface */
    public Cursor getConnectionCursorByAppId(int appId) {
        return ConnectionDatabase.getConnectionCursorByAppId(cDb.getReadableDatabase(), appId);
    }

    public boolean insertConnection(int appId, int ruleId, String content, int sensitive) {
        return ConnectionDatabase.insertConnection(cDb.getWritableDatabase(), appId, ruleId,
                                                    content, sensitive);
    }

    public void deleteConnectionByRuleId(int ruleId) {
        ConnectionDatabase.deleteConnectionByRuleId(cDb.getWritableDatabase(), ruleId);
    }

    /** Application Database Interface */
    public Cursor getAllApplicationCursor() {
        return ApplicationDatabase.getAllApplicationCursor(aDb.getReadableDatabase());
    }

    public boolean insertApplication(String name, String description, int id) {
        return ApplicationDatabase.insertApplication(aDb.getWritableDatabase(), name, description,
                id);
    }

    public Cursor getApplicationCursorByName(String name) {
        return ApplicationDatabase.getApplicationCursorByName(aDb.getReadableDatabase(), name);
    }

    public Cursor getApplicationCursorById(int id) {
        return ApplicationDatabase.getApplicationCursorById(aDb.getReadableDatabase(), id);
    }

    public int getApplicationIdByPackagename(String packagename) {
        return ApplicationDatabase.getApplicationIdByPackagename(aDb.getReadableDatabase(), packagename);
    }

    /** Rule Database Interface */
    public Cursor getRuleCursorByAdd(String ipAdd) {
        return RuleDatabase.getRuleCursorByAdd(rDb.getReadableDatabase(), ipAdd);
    }

    public Cursor getRuleCursorById(int id) {
        return RuleDatabase.getRuleCursorById(rDb.getReadableDatabase(), id);
    }

    public boolean updateAction(int id, int action) {
        return RuleDatabase.updateAction(rDb.getReadableDatabase(), id, action);
    }

    public boolean insertRule(String ipAdd, String ipOwner, int action) {
        return RuleDatabase.insertRule(rDb.getWritableDatabase(), ipAdd, ipOwner, action);
    }

    public int getNewRuleId() {
        return RuleDatabase.getNewRuleId(rDb.getReadableDatabase());
    }

    public void deleteRuleById(int id) {
        RuleDatabase.deleteRuleById(rDb.getWritableDatabase(), id);
    }
}

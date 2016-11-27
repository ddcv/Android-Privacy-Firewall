package edu.cmu.infosec.privacyfirewall;

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

    public Cursor getConnectionCursorByAppIdRuleId(int appId, int ruleId) {
        return ConnectionDatabase.getConnectionCursorByAppIdRuleId(cDb.getReadableDatabase(),
                appId, ruleId);
    }

    public Cursor getAllConnectionCursor() {
        return ConnectionDatabase.getAllConnectionCursor(cDb.getReadableDatabase());
    }

    public boolean insertConnection(int appId, int ruleId, int action, String content,
                                    int sensitive) {
        return ConnectionDatabase.insertConnection(cDb.getWritableDatabase(), appId, ruleId, action,
                                                    content, sensitive);
    }

    public void deleteConnectionByAppIdRuleId(int appId, int ruleId) {
        ConnectionDatabase.deleteConnectionByAppIdRuleId(cDb.getWritableDatabase(), appId, ruleId);
    }

    public boolean updateSensitive(int appId, int ruleId, String sensitive) {
        return ConnectionDatabase.updateSensitive(cDb.getWritableDatabase(), appId, ruleId,
                sensitive);
    }

    public boolean updateAction(int appId, int ruleId, int action) {
        return ConnectionDatabase.updateAction(cDb.getWritableDatabase(), appId, ruleId, action);
    }

    /** Application Database Interface */
    public Cursor getAllApplicationCursor() {
        return ApplicationDatabase.getAllApplicationCursor(aDb.getReadableDatabase());
    }

    public boolean insertApplication(String name, String description, int id, int permission) {
        return ApplicationDatabase.insertApplication(aDb.getWritableDatabase(), name, description,
                id, permission);
    }

    public Cursor getApplicationCursorByName(String name) {
        return ApplicationDatabase.getApplicationCursorByName(aDb.getReadableDatabase(), name);
    }

    public Cursor getApplicationCursorById(int id) {
        return ApplicationDatabase.getApplicationCursorById(aDb.getReadableDatabase(), id);
    }

    public int getApplicationIdByPackagename(String packagename) {
        return ApplicationDatabase.getApplicationIdByPackagename(aDb.getReadableDatabase()
                , packagename);
    }

    public Cursor getApplicationCursorByPackagename(String packagename) {
        return ApplicationDatabase.getApplicationCursorByPackagename(aDb.getReadableDatabase()
                , packagename);
    }

    /** Rule Database Interface */
    public Cursor getRuleCursorByAdd(String ipAdd) {
        return RuleDatabase.getRuleCursorByAdd(rDb.getReadableDatabase(), ipAdd);
    }

    public Cursor getRuleCursorById(int id) {
        return RuleDatabase.getRuleCursorById(rDb.getReadableDatabase(), id);
    }

    public Cursor getAllRuleCursor() {
        return RuleDatabase.getAllRuleCursor(rDb.getReadableDatabase());
    }

    public boolean updateRegistrant(int id, String registrant, String country) {
        return RuleDatabase.updateRegistrant(rDb.getWritableDatabase(), id, registrant, country);
    }

    public boolean insertRule(String ipAdd, String organization, String country) {
        boolean res = RuleDatabase.insertRule(rDb.getWritableDatabase(), ipAdd, organization,
                country);
        return res;
    }

    public int getNewRuleId() {
        return RuleDatabase.getNewRuleId(rDb.getReadableDatabase());
    }

    public void deleteRuleById(int id) {
        RuleDatabase.deleteRuleById(rDb.getWritableDatabase(), id);
    }
}

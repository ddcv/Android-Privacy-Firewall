package edu.cmu.privacy.privacyfirewall;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

    // Connection Database Interface
    public Cursor getConnectionCursorByAppId(int appId) {
        return ConnectionDatabase.getConnectionCursorByAppId(cDb.getReadableDatabase(), appId);
    }

    public boolean insertConnection(int appId, int ruleId, String content, int sensitive) {
        return ConnectionDatabase.insertConnection(cDb.getWritableDatabase(), appId, ruleId,
                                                    content, sensitive);
    }

    // Application Database Interface
    public Cursor getAllApplicationCursor() {
        return ApplicationDatabase.getAllApplicationCursor(aDb.getReadableDatabase());
    }

    // Application Database Interface
    public Cursor getApplicationCursorByAppName(String AppName) {
        return ApplicationDatabase.getApplicationCursorByAppName(aDb.getReadableDatabase(), AppName);
    }

    public boolean insertApplication(String name, String description) {
        return ApplicationDatabase.insertApplication(aDb.getWritableDatabase(), name, description);
    }

    // Rule Database Interface
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
}

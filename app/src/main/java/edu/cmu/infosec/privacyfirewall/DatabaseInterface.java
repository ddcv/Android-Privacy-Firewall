package edu.cmu.infosec.privacyfirewall;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.ByteArrayOutputStream;

/**
 * Created by YunfanW on 9/30/2016.
 */

public interface DatabaseInterface {
    /** Connection */
    public Cursor getConnectionCursorByAppId(int appId);
    public Cursor getConnectionCursorByAppIdRuleId(int appId, int ruleId);
    public boolean insertConnection(int appId, int ruleId, int action, String content,
                                    int sensitive);
    public void deleteConnectionByAppIdRuleId(int appId, int ruleId);
    public boolean updateAction(int appId, int ruleId, int action);

    /** Application */
    public Cursor getAllApplicationCursor();
    public Cursor getApplicationCursorByName(String name);
    public Cursor getApplicationCursorById(int id);
    public boolean insertApplication(String name, String description, int id);
    public int getApplicationIdByPackagename(String packagename);

    /** Rule */
    public Cursor getRuleCursorByAdd(String ipAdd);
    public Cursor getRuleCursorById(int id);
    public boolean updateRegistrant(int id, String registrant, String country);
    public boolean insertRule(String ipAdd, String ipOwner, String country);
    public int getNewRuleId();
    public void deleteRuleById(int id);
}

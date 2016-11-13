package edu.cmu.privacy.privacyfirewall;


import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.net.InetAddress;

/**
 * Created by YunfanW on 11/13/2016.
 */

public class MainAddNewRuleListener implements View.OnClickListener{
    private Context context;

    public MainAddNewRuleListener(Context _context) {
        super();

        context = _context;
    }

    @Override
    public void onClick(View view) {
        android.support.v7.app.AlertDialog.Builder builder = new
                    android.support.v7.app.AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_main_title);
        builder.setMessage(R.string.dialog_addr);
        final EditText ipaddr = new EditText(context);
        builder.setView(ipaddr);
        builder.setNegativeButton(R.string.dialog_cancel, null);
        final View parentView = view;
        builder.setPositiveButton(R.string.dialog_add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String addrStr = ipaddr.getText().toString();
                Log.i("Dialog", "IP Addr: " + addrStr);

                /** check IP address validate */
                if (Monitor.checkIPAddr(addrStr)) {
                    /** get rule id */
                    Cursor ruleCur = Monitor.db.getRuleCursorByAdd(addrStr);
                    int ruleId;
                    if (ruleCur.getCount() >= 1) {
                        /** exist rule */
                        ruleCur.moveToFirst();
                        ruleId = ruleCur.getInt(ruleCur.getColumnIndex(RuleDatabase.FIELD_ID));
                    } else {
                        /** add new rule */
                        ruleId = Monitor.db.getNewRuleId();
                        Monitor.db.insertRule(addrStr, "New Rule", 0);
                    }

                    /** for all application */
                    Cursor appCur = Monitor.db.getAllApplicationCursor();
                    for (appCur.moveToFirst(); !appCur.isAfterLast(); appCur.moveToNext()) {
                        /** get application id */
                        int appId = appCur.getInt(appCur.getColumnIndex(
                                ApplicationDatabase.FIELD_ID));

                        /** add rule if not exist */
                        Cursor conCur = Monitor.db.getConnectionCursorByAppIdRuleId(appId, ruleId);
                        if (conCur.getCount() < 1) {
                            Monitor.db.insertConnection(appId, ruleId, "New Global Rule", 0);
                        }
                    }

                    Snackbar.make(parentView, "IP Address " + addrStr + " Add Successfully",
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(parentView, "Invalid IP Address: " + addrStr,
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }
}

package edu.cmu.privacy.privacyfirewall;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import edu.cmu.privacy.privacyfirewall.ConnectionDatabase;
import edu.cmu.privacy.privacyfirewall.Monitor;
import edu.cmu.privacy.privacyfirewall.R;
import edu.cmu.privacy.privacyfirewall.RuleDatabase;

/**
 * Created by YunfanW on 11/13/2016.
 */

public class DetailAddNewRuleListener implements View.OnClickListener {
    private String packagename;
    private DetailActivity activity;

    public DetailAddNewRuleListener(DetailActivity _activity, String _packagename) {
        super();
        activity = _activity;
        packagename = _packagename;
    }

    @Override
    public void onClick(View view) {
        android.support.v7.app.AlertDialog.Builder builder = new
                android.support.v7.app.AlertDialog.Builder(activity);
        builder.setTitle(R.string.dialog_title);
        builder.setMessage(R.string.dialog_addr);
        final EditText ipaddr = new EditText(activity);
        builder.setView(ipaddr);
        builder.setNegativeButton(R.string.dialog_cancel, null);
        final View parentView = view;
        builder.setPositiveButton(R.string.dialog_add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String addrStr = ipaddr.getText().toString();
                Log.i("Dialog", "IP Addr: " + addrStr);

                if (Monitor.checkIPAddr(addrStr)) {
                    int ruleId = -1;
                    Cursor ruleCur = Monitor.db.getRuleCursorByAdd(addrStr);
                    if (ruleCur.getCount() >= 1) {
                        ruleCur.moveToFirst();
                        ruleId = ruleCur.getInt(ruleCur.getColumnIndex(RuleDatabase.FIELD_ID));
                    }

                    int appId = -1;
                    if (packagename != null) {
                        appId = Monitor.db.getApplicationIdByPackagename(packagename);
                    }

                    if (appId != -1) {
                        if (ruleId != -1) {
                            Cursor conCur = Monitor.db.getConnectionCursorByAppId(appId);

                            boolean existFlag = false;
                            for (conCur.moveToFirst(); !conCur.isAfterLast(); conCur.moveToNext()) {
                                int existRuleId = conCur.getInt(conCur.getColumnIndex(
                                        ConnectionDatabase.FIELD_RULE));
                                if (existRuleId == ruleId) {
                                    existFlag = true;
                                    break;
                                }
                            }

                            if (existFlag) {
                                Snackbar.make(parentView, "Rule " + addrStr + " Duplicated in Database",
                                        Snackbar.LENGTH_SHORT).show();
                            } else {
                                Monitor.db.insertConnection(appId, ruleId, "New Rule", 0);
                                Snackbar.make(parentView, "Add " + addrStr + " Successfully",
                                        Snackbar.LENGTH_SHORT).show();
                                activity.clearConnection();
                                activity.loadConnection();
                            }
                        } else {
                            int newRuleId = Monitor.db.getNewRuleId();
                            Monitor.db.insertRule(addrStr, "New Recipient", 0);
                            Monitor.db.insertConnection(appId, newRuleId, "New Rule", 0);
                            Snackbar.make(parentView, "Add " + addrStr + " Successfully",
                                    Snackbar.LENGTH_SHORT).show();
                            activity.clearConnection();
                            activity.loadConnection();
                        }
                    } else {
                        Snackbar.make(parentView, "Application ID is not exist!",
                                Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(parentView, "Invalid IP Address: " + addrStr,
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }
}

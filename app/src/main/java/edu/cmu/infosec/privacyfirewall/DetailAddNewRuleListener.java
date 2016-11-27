package edu.cmu.infosec.privacyfirewall;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import edu.cmu.infosec.privacyfirewall.ConnectionDatabase;
import edu.cmu.infosec.privacyfirewall.Monitor;
import edu.cmu.infosec.privacyfirewall.R;
import edu.cmu.infosec.privacyfirewall.RuleDatabase;

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
        /** build dialog */
        android.support.v7.app.AlertDialog.Builder builder = new
                android.support.v7.app.AlertDialog.Builder(activity);
        builder.setTitle(R.string.dialog_title);
        builder.setMessage(R.string.dialog_addr);
        final EditText ipaddr = new EditText(activity);
        ipaddr.setHint(R.string.dialog_addr_hint);
        ipaddr.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(ipaddr);
        builder.setNegativeButton(R.string.dialog_cancel, null);
        final View parentView = view;

        /** set onClick event */
        builder.setPositiveButton(R.string.dialog_add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String addrStr = ipaddr.getText().toString();

                if (Monitor.checkIPAddr(addrStr)) {
                    int ruleId;
                    Cursor ruleCur = Monitor.db.getRuleCursorByAdd(addrStr);

                    /** find rule id */
                    if (ruleCur.getCount() >= 1) {
                        ruleCur.moveToFirst();
                        ruleId = ruleCur.getInt(ruleCur.getColumnIndex(RuleDatabase.FIELD_ID));
                    } else {
                        /** add rule if not exist */
                        ruleId = Monitor.db.getNewRuleId();
                        Monitor.db.insertRule(addrStr, RuleDatabase.ORG_DEFAULT,
                                RuleDatabase.COUNTRY_DEFAULT);
                    }

                    /** find app id */
                    int appId = -1;
                    if (packagename != null) {
                        appId = Monitor.db.getApplicationIdByPackagename(packagename);
                    }

                    if (appId != -1) {
                        Cursor conCur = Monitor.db.getConnectionCursorByAppIdRuleId(appId, ruleId);

                        /** update action */
                        if (conCur.getCount() >= 1) {
                            Monitor.db.updateAction(appId, ruleId, ConnectionDatabase.ACTION_DENY);
                            Snackbar.make(parentView, "Update " + addrStr + " Successfully",
                                    Snackbar.LENGTH_SHORT).show();
                        }

                        /** insert action */
                        else {
                            Monitor.db.insertConnection(appId, ruleId,
                                    ConnectionDatabase.ACTION_DENY,
                                    ConnectionDatabase.CONTENT_DEFAULT,
                                    ConnectionDatabase.NON_SENSITIVE);
                            Snackbar.make(parentView, "Add " + addrStr + " Successfully",
                                    Snackbar.LENGTH_SHORT).show();

                        }
                        activity.clearConnection();
                        activity.loadConnection();
                    } else {
                        Snackbar.make(parentView, "App ID is not exist!",
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

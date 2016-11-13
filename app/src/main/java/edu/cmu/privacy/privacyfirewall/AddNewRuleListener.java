package edu.cmu.privacy.privacyfirewall;


import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

/**
 * Created by YunfanW on 11/13/2016.
 */

public class AddNewRuleListener implements View.OnClickListener{
    private Context context;
    private View view;

    public AddNewRuleListener(Context _context, View _view) {
        super();

        context = _context;
        view = _view;
    }

    @Override
    public void onClick(View view) {
        android.support.v7.app.AlertDialog.Builder builder = new
                    android.support.v7.app.AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_title);
        builder.setMessage(R.string.dialog_addr);
        final EditText ipaddr = new EditText(context);
        builder.setView(ipaddr);
        builder.setNegativeButton(R.string.dialog_cancel, null);
        final View parentView = view;
        builder.setPositiveButton(R.string.dialog_add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("Dialog", "IP Addr: " + ipaddr.getText());

                Snackbar.make(parentView, "Add Successfully", Snackbar.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }
}

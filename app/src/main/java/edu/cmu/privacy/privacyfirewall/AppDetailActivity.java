package edu.cmu.privacy.privacyfirewall;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.content.ContentValues;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


public class AppDetailActivity extends ListActivity {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appdetail);


        Intent intent = getIntent();
        String value = intent.getStringExtra("listappName");

        TextView appnametextView = (TextView) findViewById(R.id.app_name);
        appnametextView.setText(value);

        TextView appdetailtextView = (TextView) findViewById(R.id.app_detail);
        appdetailtextView.setMovementMethod(new ScrollingMovementMethod());


        /**
         * Database access for Application by AppName
         */
//        DatabaseInterface db = new DataBaseController(AppDetailActivity.this);
//        Cursor CurappCur = db.getApplicationCursorByAppName(value);
//        ContentValues appVal = new ContentValues();
//        DatabaseUtils.cursorRowToContentValues(CurappCur, appVal);

        HashMap<String, String> RuleVal = new HashMap<String, String>();
        RuleVal.put("Evil Inc.", "4.4.4.4");
        RuleVal.put("Angel Inc.", "3.3.3.3");

        HashMap<String, String[]> ConnectionVal = new HashMap<String, String[]>();
        String[] Connection1Details = {"Evil Data", "1"};
        String[] Connection2Details = {"Good Data", "0"};
        ConnectionVal.put("Evil Inc.", Connection1Details);
        ConnectionVal.put("Angel Inc.", Connection2Details);


        String[] Orgvalues = RuleVal.keySet().toArray(new String[0] );
        ArrayList Connectionvalues = new ArrayList<HashMap<String, String>>();
        for (String OrgHistory: Orgvalues) {
            HashMap<String, String> Connectionvalue = new HashMap<String, String>();


            Connectionvalue.put("Org Name", "Organization: " + OrgHistory);
            Connectionvalue.put("IP Address", "IP Address: " + RuleVal.get(OrgHistory));
            Connectionvalue.put("Data Content", "Data Content: " + ConnectionVal.get(OrgHistory)[0]);
            if (ConnectionVal.get(OrgHistory)[1].equals("0") ) {
                Connectionvalue.put("Sensitive?", "Sensitive Data? No");
            }
            else {
                Connectionvalue.put("Sensitive?", "Sensitive Data? Yes");
            }


            Connectionvalues.add(Connectionvalue);
        }



        String[] from = {"Org Name", "IP Address", "Data Content", "Sensitive?"};
        int[] to = {R.id.connectname,R.id.connectIP, R.id.connectdetail, R.id.connectsensitive};

        listView = (ListView) findViewById(android.R.id.list);
        ListAdapter adapter = new SimpleAdapter(this,Connectionvalues, R.layout.list_items,
                from, to);
        listView.setAdapter(adapter);



    }
}

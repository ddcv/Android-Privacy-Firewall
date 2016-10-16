package edu.cmu.privacy.privacyfirewall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AppDetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appdetail);

        Intent intent = getIntent();
        String value = intent.getStringExtra("ITEM");

        TextView appnametextView = (TextView) findViewById(R.id.app_name);
        appnametextView.setText(value);

        TextView appdetailtextView = (TextView) findViewById(R.id.app_detail);
        appdetailtextView.setText("This is an app.");

      //  ListView listView = (ListView) findViewById(R.id.connection_history);


    }
}

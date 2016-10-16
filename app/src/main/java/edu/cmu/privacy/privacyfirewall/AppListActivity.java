package edu.cmu.privacy.privacyfirewall;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AppListActivity extends ListActivity {

    private TextView listappDisplay;
    private String listappName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        /** UI Component Start */

        /**
         * Find Alternative way to use Actual App Names
         */
        String[] values = new String[] { "Dr. Evil's App", "Game", "Malware", "Social Media", "Bloatware", "Phone" };
        //
        ArrayAdapter adapter =
                new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                        values);
        setListAdapter(adapter);
        setContentView(R.layout.activity_app_list);
        listappDisplay = (TextView) findViewById(R.id.text1);
        ListView list = getListView();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listappName = (String) getListAdapter().getItem(position);
                listappDisplay.setText(listappName);            }
        });


        /** UI Component End*/
    }
}

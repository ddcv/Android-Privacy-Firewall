package edu.cmu.privacy.privacyfirewall;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import edu.cmu.privacy.privacyfirewall.adapter.ApplicationAdapter;
import edu.cmu.privacy.privacyfirewall.entity.AppInfo;
import edu.cmu.privacy.privacyfirewall.itemanimator.CustomItemAnimator;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

public class MainActivity extends AppCompatActivity {

    /** VPN Part Variables Start */
    private static final int VPN_REQUEST_CODE = 0x0;    /* VPN Request code, used when start VPN */
    private Intent serviceIntent;    /* The VPN Service Intent */
    /** VPN Part Variables End   */

    /** UI Variables Start */
    private static final int DRAWER_ITEM_SWITCH = 1;
    private static final int DRAWER_ITEM_OPEN_SOURCE = 10;

    private List<AppInfo> applicationList = new ArrayList<AppInfo>();

    private Drawer drawer;

    private ApplicationAdapter mAdapter;
    private FloatingActionButton mFabButton;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;
    /** UI Variables End */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** init database */
        Monitor.db = new DataBaseController(MainActivity.this);

        /** VPN Part Demo Start */

        /** Start VPN */
        serviceIntent = VpnTestService.prepare(getApplicationContext());
        if (serviceIntent != null) {
            startActivityForResult(serviceIntent, VPN_REQUEST_CODE);
        } else {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
        }
        /** VPN Part Demo End   */

        /** Load Application info into Database */
        final PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> installedApplications =
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo appInfo : installedApplications) {
            Cursor c = Monitor.db.getApplicationCursorById(appInfo.uid);

            /** Not exist */
            if (c.isAfterLast()) {
                Monitor.db.insertApplication(appInfo.loadLabel(packageManager).toString(),
                        appInfo.packageName, appInfo.uid);
            }
        }

        /** UI Start */

        /** Handle Toolbar */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Fab Button
        mFabButton = (FloatingActionButton) findViewById(R.id.fab_normal);
        mFabButton.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_add).color(Color.WHITE).actionBar());
        mFabButton.setOnClickListener(new AddNewRuleListener(this, mRecyclerView));

        drawer = new DrawerBuilder(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        new SecondaryDrawerItem().withIcon(GoogleMaterial.Icon.gmd_settings).withName(R.string.drawer_title)
                )
                .addDrawerItems(
                        new SwitchDrawerItem().withOnCheckedChangeListener(new OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton compoundButton, boolean b) {
                                if (b) {
                                    Log.i("Switch", "click-on");
                                    Snackbar.make(mRecyclerView, "VPN turn on",
                                            Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Log.i("Switch", "click-off");
                                    Snackbar.make(mRecyclerView, "VPN turn off",
                                            Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        }).withName(R.string.drawer_switch)
                )
                .withSelectedItem(-1)
                .withSavedInstance(savedInstanceState)
                .build();

        // Handle ProgressBar
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new CustomItemAnimator());

        mAdapter = new ApplicationAdapter(new ArrayList<AppInfo>(), R.layout.row_application,
                                                                                MainActivity.this);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.theme_accent));
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new InitializeApplicationsTask().execute();
            }
        });

        new InitializeApplicationsTask().execute();

        //show progress
        mRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        /** UI End */

        /** Add Fake Data */
        int fakeAppId = Monitor.db.getApplicationIdByPackagename("com.android.browser");
        if (fakeAppId != -1) {
            Cursor FakeCur = Monitor.db.getConnectionCursorByAppId(fakeAppId);
            if (FakeCur.getCount() < 1) {
                Monitor.db.insertConnection(fakeAppId, 0, "FakeData", 0);
            }
        }
    }


    /** VPN Part Functions Start */

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, VpnTestService.class);
            startService(intent);
        }
    }

    /** VPN Part Functions End   */

    /** UI Part Functions Start */

    /**
     * helper class to start the new detailActivity animated
     *
     * @param appInfo
     * @param appIcon
     */
    public void animateActivity(AppInfo appInfo, View appIcon) {
        Intent i = new Intent(this, DetailActivity.class);
        i.putExtra("appInfo", appInfo.getComponentName());

        ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this, Pair.create(appIcon, "appIcon"));
        startActivity(i, transitionActivityOptions.toBundle());
    }

    /**
     * A simple AsyncTask to load the list of applications and display them
     */
    private class InitializeApplicationsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mAdapter.clearApplications();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            applicationList.clear();

            //Query the applications
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> ril = getPackageManager().queryIntentActivities(mainIntent, 0);
            for (ResolveInfo ri : ril) {
                applicationList.add(new AppInfo(MainActivity.this, ri));
            }
            Collections.sort(applicationList);

            for (AppInfo appInfo : applicationList) {
                //load icons before shown. so the list is smoother
                appInfo.getIcon();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //handle visibility
            mRecyclerView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);

            //set data for list
            mAdapter.addApplications(applicationList);
            mSwipeRefreshLayout.setRefreshing(false);

            super.onPostExecute(result);
        }
    }

    /** UI Part Functions End */

}

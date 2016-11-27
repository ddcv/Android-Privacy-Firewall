package edu.cmu.infosec.privacyfirewall;

import android.animation.Animator;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import edu.cmu.infosec.privacyfirewall.entity.AppInfo;

public class DetailActivity extends AppCompatActivity {

    private static final int SCALE_DELAY = 30;

    private LinearLayout mRowContainer;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Toolbar toolbar;

    private AppInfo mAppInfo = null;
    private String packagename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        View view;

        mRowContainer = (LinearLayout) findViewById(R.id.row_container);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Handle Back Navigation
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailActivity.this.onBackPressed();
            }
        });

        // Handle Refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.theme_accent));
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new InitializeConnection().execute();
                //recreate();
            }
        });

        ComponentName componentName = null;

        if (savedInstanceState != null) {
            componentName = savedInstanceState.getParcelable("appInfo");
        } else if (getIntent() != null && getIntent().getExtras() != null) {
            componentName = (ComponentName) getIntent().getExtras().get("appInfo");
        }

        if (componentName != null) {
            Intent intent = new Intent();
            intent.setComponent(componentName);
            ResolveInfo app = getPackageManager().resolveActivity(intent, 0);
            mAppInfo = new AppInfo(this, app);
        }

        toolbar.setTitle(mAppInfo.getName());

        view = mRowContainer.findViewById(R.id.row_name);
        fillRow(view, "Application Name", mAppInfo.getName());
        ((ImageView) view.findViewById(R.id.appIcon)).setImageDrawable(mAppInfo.getIcon());

        packagename = mAppInfo.getPackageName();
        view = mRowContainer.findViewById(R.id.row_package_name);
        fillRow(view, "Package Name", mAppInfo.getPackageName());

        view = mRowContainer.findViewById(R.id.row_version);
        fillRow(view, "Version", mAppInfo.getVersionName() + " (" +
                mAppInfo.getVersionCode() + ")");

        loadConnection();

        // Fab Button
        FloatingActionButton floatingActionButton =
                (FloatingActionButton) findViewById(R.id.fab_normal);
        floatingActionButton.setImageDrawable(new IconicsDrawable(
                this, GoogleMaterial.Icon.gmd_add).color(Color.WHITE).actionBar());
        floatingActionButton.setOnClickListener(
                new DetailAddNewRuleListener(this, packagename));




    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("appInfo", mAppInfo.getComponentName());
        super.onSaveInstanceState(outState);
    }

    /**
     * fill the rows with some information
     *
     * @param view
     * @param title
     * @param description
     */
    public void fillRow(View view, final String title, final String description) {
        TextView titleView = (TextView) view.findViewById(R.id.title);
        titleView.setText(title);

        TextView descriptionView = (TextView) view.findViewById(R.id.description);
        descriptionView.setText(description);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    /**
     * fill the rows with some information
     *
     * @param view
     * @param ip
     * @param recipient
     * @param sensitive
     */
    public void fillConnection(View view, final String ip, final String recipient, final String country,
                               final String sensitive, final int appId, final int action) {
        String line;

        TextView titleView = (TextView) view.findViewById(R.id.dest_ip_address);
        line = "IP Address: " + ip;
        titleView.setText(line);

        line = "Organization: " + recipient;
        TextView descriptionView = (TextView) view.findViewById(R.id.recipient);
        descriptionView.setText(line);

        line = "Country: " + country;
        TextView countryView = (TextView) view.findViewById(R.id.country);
        countryView.setText(line);

        line = "Sensitive Info: " + sensitive;
        TextView sensitiveView = (TextView) view.findViewById(R.id.sensitive);
        sensitiveView.setText(line);

        line = "Action: " + (action == ConnectionDatabase.ACTION_ALOW ? "Allow" : "Deny");
        TextView actionView = (TextView) view.findViewById(R.id.action);
        actionView.setText(line);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v7.app.AlertDialog.Builder builder = new
                        android.support.v7.app.AlertDialog.Builder(DetailActivity.this);

                /** Set Notification */
                if (action == ConnectionDatabase.ACTION_ALOW) {
                    builder.setTitle(R.string.dialog_title_allow);
                } else {
                    builder.setTitle(R.string.dialog_title_deny);
                }

                builder.setNegativeButton(R.string.dialog_no, null);
                builder.setPositiveButton(R.string.dialog_yes,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int ruleId;
                        Cursor ruleCur = Monitor.db.getRuleCursorByAdd(ip);
                        if (ruleCur.getCount() >= 1) {
                            ruleCur.moveToFirst();
                            ruleId = ruleCur.getInt(ruleCur.getColumnIndex(RuleDatabase.FIELD_ID));

                            /** set new action */
                            int new_action;
                            if (action == ConnectionDatabase.ACTION_ALOW) {
                                new_action = ConnectionDatabase.ACTION_DENY;
                            } else {
                                new_action = ConnectionDatabase.ACTION_ALOW;
                            }

                            Monitor.db.updateAction(appId, ruleId, new_action);

                            Snackbar.make(mRowContainer, "Update Action Successfully",
                                    Snackbar.LENGTH_SHORT).show();
                            new InitializeConnection().execute();
                        } else {
                            Snackbar.make(mRowContainer, "Rule Is Not Exist!",
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.show();
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                android.support.v7.app.AlertDialog.Builder builder = new
                        android.support.v7.app.AlertDialog.Builder(DetailActivity.this);

                builder.setTitle(R.string.dialog_title_delete);

                builder.setNegativeButton(R.string.dialog_no, null);
                builder.setPositiveButton(R.string.dialog_yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                int ruleId;
                                Cursor ruleCur = Monitor.db.getRuleCursorByAdd(ip);
                                if (ruleCur.getCount() >= 1) {
                                    ruleCur.moveToFirst();
                                    ruleId = ruleCur.getInt(ruleCur.getColumnIndex(RuleDatabase.FIELD_ID));

                                    Monitor.db.deleteConnectionByAppIdRuleId(appId, ruleId);

                                    Snackbar.make(mRowContainer, "Delete " + ip + " Successfully",
                                            Snackbar.LENGTH_SHORT).show();
                                    new InitializeConnection().execute();
                                } else {
                                    Snackbar.make(mRowContainer, "Rule Is Not Exist!",
                                            Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
                builder.show();
                return true;
            }
        });
    }

    /**
     * animate the views if we close the activity
     */
    @Override
    public void onBackPressed() {
        for (int i = mRowContainer.getChildCount() - 1; i > 0; i--) {
            View rowView = mRowContainer.getChildAt(i);
            ViewPropertyAnimator propertyAnimator = rowView.animate()
                    .setStartDelay((mRowContainer.getChildCount() - 1 - i) * SCALE_DELAY)
                    .scaleX(0).scaleY(0);

            propertyAnimator.setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAfterTransition();
                    } else {
                        finish();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
        }
    }

    public void loadConnection() {
        if (mAppInfo != null) {
            int appId = Monitor.db.getApplicationIdByPackagename(mAppInfo.getPackageName());
            LayoutInflater inflater = getLayoutInflater();
            if (appId != -1) {
                Cursor conCur = Monitor.db.getConnectionCursorByAppId(appId);
                for (conCur.moveToFirst(); !conCur.isAfterLast(); conCur.moveToNext()) {
                    String ip = "Not Known";
                    String recipient = RuleDatabase.ORG_DEFAULT;
                    String country = RuleDatabase.COUNTRY_DEFAULT;
                    String sensitive = conCur.getString(conCur.getColumnIndex(
                            ConnectionDatabase.FIELD_CONTENT));
                    int action = conCur.getInt(conCur.getColumnIndex(
                            ConnectionDatabase.FIELD_ACTION));
                    int ruleId =conCur.getInt(conCur.getColumnIndex(
                            ConnectionDatabase.FIELD_RULE));
                    Log.i("Fill Connection", "rule Id: " + ruleId);

                            Cursor ruleCur = Monitor.db.getRuleCursorById(ruleId);
                    if (ruleCur.getCount() >= 1) {
                        ruleCur.moveToFirst();
                        ip = ruleCur.getString(ruleCur.getColumnIndex(
                                RuleDatabase.FIELD_IP_ADD));
                        recipient = ruleCur.getString(ruleCur.getColumnIndex(
                                RuleDatabase.FIELD_ORG));
                        country = ruleCur.getString(ruleCur.getColumnIndex(
                                RuleDatabase.FIELD_COUNTRY));
                        Log.i("Fill Connection", "ip: " + ip + ", recipient: " + recipient +
                                ", country: " + country);
                    }
                    inflater.inflate(R.layout.row_detailconnection, mRowContainer, true);
                    fillConnection(mRowContainer.getChildAt(mRowContainer.getChildCount() - 1),
                            ip, recipient, country, sensitive, appId, action);
                }
            }
        }
        for (int i = 1; i < mRowContainer.getChildCount(); i++) {
            View rowView = mRowContainer.getChildAt(i);
            rowView.animate().setStartDelay(100 + i * SCALE_DELAY).scaleX(1).scaleY(1);
        }

        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void clearConnection() {
        mRowContainer.removeViews(4, mRowContainer.getChildCount() - 4);
    }

    /**
     * A simple AsyncTask to load the list of applications and display them
     */
    private class InitializeConnection extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            clearConnection();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            loadConnection();
            //handle visibility
            mRowContainer.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setRefreshing(false);
            super.onPostExecute(result);
        }
    }
}

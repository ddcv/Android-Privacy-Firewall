package edu.cmu.privacy.privacyfirewall;

import android.animation.Animator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.cmu.privacy.privacyfirewall.entity.AppInfo;

public class DetailActivity extends AppCompatActivity {

    private static final int SCALE_DELAY = 30;

    private LinearLayout mRowContainer;
//    private CoordinatorLayout mCoordinatorLayout;

    private AppInfo mAppInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

//        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.container);
        mRowContainer = (LinearLayout) findViewById(R.id.row_container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        View view;

        LayoutInflater inflater = getLayoutInflater();
        inflater.inflate(R.layout.row_detailconnection, mRowContainer, true);
        fillConnection(mRowContainer.getChildAt(mRowContainer.getChildCount() - 1),
                "IP Address: 192.168.0.1(Demo)", "Recipient: Amazon", "Sensitive Info: NULL");

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

        if (mAppInfo != null) {
            //toolbar.setLogo(mAppInfo.getIcon());
            toolbar.setTitle(mAppInfo.getName());

            view = mRowContainer.findViewById(R.id.row_name);
            fillRow(view, "Application Name", mAppInfo.getName());
            ((ImageView) view.findViewById(R.id.appIcon)).setImageDrawable(mAppInfo.getIcon());

            view = mRowContainer.findViewById(R.id.row_package_name);
            fillRow(view, "Package Name", mAppInfo.getPackageName());

            view = mRowContainer.findViewById(R.id.row_version);
            fillRow(view, "Version", mAppInfo.getVersionName() + " (" + mAppInfo.getVersionCode() + ")");

            int appId = Monitor.db.getApplicationIdByPackagename(mAppInfo.getPackageName());
            if (appId != -1) {
                Cursor appCur = Monitor.db.getConnectionCursorByAppId(appId);
                for (appCur.moveToFirst(); !appCur.isAfterLast(); appCur.moveToNext()) {
                    String sensitive = appCur.getString(appCur.getColumnIndex(
                            ConnectionDatabase.FIELD_CONTENT));
                    Cursor ruleCur = Monitor.db.getRuleCursorById(appCur.getInt(
                            appCur.getColumnIndex(ConnectionDatabase.FIELD_RULE)));
                    String ip = "Not Known";
                    String recipient = "Not Know";
                    if (ruleCur.getCount() >= 1) {
                        ruleCur.moveToFirst();
                        ip = ruleCur.getString(ruleCur.getColumnIndex(RuleDatabase.FIELD_IP_ADD));
                        recipient = ruleCur.getString(ruleCur.getColumnIndex(
                                RuleDatabase.FIELD_ID_OWNER));
                    }
                    inflater.inflate(R.layout.row_detailconnection, mRowContainer, true);
                    fillConnection(mRowContainer.getChildAt(mRowContainer.getChildCount() - 1),
                            "IP Address: " + ip, "Recipient: " + recipient,
                            "Sensitive Info: " + sensitive);
                }
            }
        }

        for (int i = 1; i < mRowContainer.getChildCount(); i++) {
            View rowView = mRowContainer.getChildAt(i);
            rowView.animate().setStartDelay(100 + i * SCALE_DELAY).scaleX(1).scaleY(1);
        }
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
//                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//                ClipData clip = ClipData.newPlainText("AppInfo", description);
//                clipboard.setPrimaryClip(clip);
//
//                Snackbar.make(mCoordinatorLayout, "Copied " + title, Snackbar.LENGTH_SHORT).show();
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
    public void fillConnection(View view, final String ip, final String recipient,
                               final String sensitive) {
        TextView titleView = (TextView) view.findViewById(R.id.dest_ip_address);
        titleView.setText(ip);

        TextView descriptionView = (TextView) view.findViewById(R.id.recipient);
        descriptionView.setText(recipient);

        TextView sensitiveView = (TextView) view.findViewById(R.id.sensitive);
        sensitiveView.setText(sensitive);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//                ClipData clip = ClipData.newPlainText("AppInfo", description);
//                clipboard.setPrimaryClip(clip);
//
//                Snackbar.make(mCoordinatorLayout, "Copied " + title, Snackbar.LENGTH_SHORT).show();
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
            ViewPropertyAnimator propertyAnimator = rowView.animate().setStartDelay((mRowContainer.getChildCount() - 1 - i) * SCALE_DELAY)
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
}

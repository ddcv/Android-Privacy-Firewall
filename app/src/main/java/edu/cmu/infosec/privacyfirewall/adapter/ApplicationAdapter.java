package edu.cmu.infosec.privacyfirewall.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.cmu.infosec.privacyfirewall.ApplicationDatabase;
import edu.cmu.infosec.privacyfirewall.MainActivity;
import edu.cmu.infosec.privacyfirewall.Monitor;
import edu.cmu.infosec.privacyfirewall.R;
import edu.cmu.infosec.privacyfirewall.entity.AppInfo;

import java.util.List;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private List<AppInfo> applications;
    private int rowLayout;
    private MainActivity mAct;

    public ApplicationAdapter(List<AppInfo> applications, int rowLayout, MainActivity act) {
        this.applications = applications;
        this.rowLayout = rowLayout;
        this.mAct = act;
    }


    public void clearApplications() {
        int size = this.applications.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                applications.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
    }

    public void addApplications(List<AppInfo> applications) {
        this.applications.addAll(applications);
        this.notifyItemRangeInserted(0, applications.size() - 1);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        final AppInfo appInfo = applications.get(i);
        viewHolder.name.setText(appInfo.getName());
        viewHolder.image.setImageDrawable(appInfo.getIcon());
        viewHolder.permissionCount.setText(String.valueOf(appInfo.getPermissionCount()));
        viewHolder.connectionCount.setText(String.valueOf(appInfo.getConnectionCount()));
        viewHolder.sensitiveCount.setText(String.valueOf(appInfo.getSensitiveCount()));

        if (appInfo.getSensitiveCount() != 0) {
            viewHolder.sensitiveLabel.setTextColor(mAct.getResources().getColor(R.color.theme_accent));
            viewHolder.sensitiveCount.setTextColor(mAct.getResources().getColor(R.color.theme_accent));
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAct.animateActivity(appInfo, viewHolder.image);
            }
        });
    }

    @Override
    public int getItemCount() {
        return applications == null ? 0 : applications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView image;
        public TextView permissionCount;
        public TextView connectionCount;
        public TextView sensitiveCount;
        public TextView sensitiveLabel;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.countryName);
            image = (ImageView) itemView.findViewById(R.id.countryImage);
            permissionCount = (TextView) itemView.findViewById(R.id.permissionCount);
            connectionCount = (TextView) itemView.findViewById(R.id.connectionCount);
            sensitiveCount = (TextView) itemView.findViewById(R.id.sensitiveCount);
            sensitiveLabel = (TextView) itemView.findViewById(R.id.sensitiveLabel);
        }

    }
}

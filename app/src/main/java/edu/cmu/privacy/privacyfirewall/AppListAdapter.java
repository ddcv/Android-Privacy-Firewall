package edu.cmu.privacy.privacyfirewall;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import java.util.List;
import java.util.Map;
/**
 * Created by ddcv on 10/5/16.
 */



public class AppListAdapter extends SimpleAdapter {
    private List<Privacyapp> apps;
    /*
     * Alternating color list -- you could initialize this from anywhere.
     * Note that the colors make use of the alpha here, otherwise they would be
     * opaque and wouldn't give good results!
     */
    private int[] colors = new int[] { 0x30ffffff, 0x30ff2020, 0x30808080 };

    @SuppressWarnings("unchecked")
    public AppListAdapter(Context context, List<? extends Map<String, String>> apps, int resource, String[] from,
                          int[] to) {
        super(context, apps, resource, from, to);
        this.apps = (List<Privacyapp>) apps;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        int colorPos = position % colors.length;
        view.setBackgroundColor(colors[colorPos]);

        return view;
    }

}
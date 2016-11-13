package edu.cmu.privacy.privacyfirewall;

import android.graphics.drawable.Drawable;

/**
 * Created by YunfanW on 11/12/2016.
 */

public class Application {
    private String packageName;
    private String name;
    private Drawable appIcon;
    public Application() {
    }
    public Application(String _name, String _packageName, Drawable _appIcon) {
        packageName = _packageName;
        name = _name;
        appIcon = _appIcon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }
}

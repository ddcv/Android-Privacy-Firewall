package edu.cmu.infosec.privacyfirewall;

import android.app.Application;

/**
 * Created by YunfanW on 12/4/2016.
 */

public class ContextUtil extends Application {
    private static ContextUtil instance;

    public static ContextUtil getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        instance = this;
    }
}

package com.rair.andesptouch;

import android.app.Application;

import com.rair.andesptouch.utils.AppUtils;

/**
 * @author Rair
 * @date 2018/10/29
 * <p>
 * desc:
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppUtils.init(this);
    }
}

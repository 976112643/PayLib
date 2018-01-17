package com.quanwe.library.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

/**
 * Created by WQ on 2018/1/17.
 */

public class Config {
    private static final Config ourInstance = new Config();
    public String APP_ID;

    public void setAPP_ID(String APP_ID) {
        this.APP_ID = APP_ID;
    }

    public String getAppID(Context context) {
        try {
            if (!TextUtils.isEmpty(APP_ID))
                return APP_ID;
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            String wx_appid = appInfo.metaData.getString("WX_APPID");
            if (!TextUtils.isEmpty(wx_appid)) {
                return wx_appid;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return APP_ID;
    }

    public static Config getInstance() {
        return ourInstance;
    }

    private Config() {
    }
}

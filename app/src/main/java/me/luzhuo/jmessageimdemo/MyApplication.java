package me.luzhuo.jmessageimdemo;

import android.app.Application;

import androidx.multidex.MultiDex;
import me.luzhuo.lib_core.app.base.CoreBaseApplication;
import me.luzhuo.lib_im.manager.IMManager;

/**
 * Description:
 *
 * @Author: Luzhuo
 * @Creation Date: 2021/1/11 9:52
 * @Copyright: Copyright 2021 Luzhuo. All rights reserved.
 **/
public class MyApplication extends CoreBaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        // multiDex
        MultiDex.install(this);
        // IM
        IMManager.init(this);
    }
}

package me.luzhuo.jmessageimdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.viewpager.widget.ViewPager;
import me.luzhuo.lib_core.app.base.AppManager;
import me.luzhuo.lib_core.app.base.CoreBaseActivity;
import me.luzhuo.lib_core.ui.adapter.ViewPagerAdapter;
import me.luzhuo.lib_core.ui.adapter.bean.ViewPagerBean;
import me.luzhuo.lib_core.ui.toast.ToastManager;
import me.luzhuo.lib_im.main.list.GroupConversationList;
import me.luzhuo.lib_im.main.list.SingleConversationList;
import me.luzhuo.lib_im.manager.IMConversationManager;
import me.luzhuo.lib_im.manager.IMRTCManager;
import me.luzhuo.lib_im.manager.IMUserManager;
import me.luzhuo.lib_im.manager.event.eventbus.MainEvent;
import me.luzhuo.lib_permission.Permission;
import me.luzhuo.lib_permission.PermissionCallback;

import static me.luzhuo.lib_im.manager.event.eventbus.MainEvent.TypeLogin;
import static me.luzhuo.lib_im.manager.event.eventbus.MainEvent.TypeUnreader;

/**
 * Description: IM 列表
 *
 * @Author: Luzhuo
 * @Creation Date: 2021/1/11 11:16
 * @Copyright: Copyright 2021 Luzhuo. All rights reserved.
 **/
public class IMListActivity extends CoreBaseActivity {
    private TextView unread;

    public static void start(Context context) {
        Intent intent = new Intent(context, IMListActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.im_list);
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this);


        unread = findViewById(R.id.unread);
        unread.setText(String.valueOf(IMConversationManager.getAllUnread()));


        TabLayout tabLayout = findViewById(R.id.table_layout);
        ViewPager viewPager = findViewById(R.id.view_pager);
        List<ViewPagerBean> lists = new ArrayList<>();
        lists.add(new ViewPagerBean(new SingleConversationList(), "单聊列表"));
        lists.add(new ViewPagerBean(new GroupConversationList(), "群聊列表"));
        viewPager.setAdapter(new ViewPagerAdapter(this, lists));
        tabLayout.setupWithViewPager(viewPager);


        // 2. 语音通话必须申请的权限
        Permission.request(this, new PermissionCallback() {
            @Override
            public void onGranted() {
                IMRTCManager.reInit();
            }

            @Override
            public void onDenieds(List<String> denieds) {
                ToastManager.show(IMListActivity.this, "请授予权限, 否则语音通话功能将无法使用");
            }
        }, Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.CAMERA, Manifest.permission.VIBRATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void XXX(MainEvent event) {
        if (event.type == TypeUnreader) { // 未读消息
            unread.setText(String.valueOf(IMConversationManager.getAllUnread()));

        } else if (event.type == TypeLogin) { // 账户在其他地方登录
            if (!TextUtils.isEmpty(event.username) && event.username.equals("user1")) {
                AppManager.loginOut();
                IMUserManager.logout();
                /*JunYueUserInfo.clear()
                LoginActivity.start(JunyueApplication.instance.applicationContext)*/
                Toast.makeText(this, "你的账号在其他地方登录", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

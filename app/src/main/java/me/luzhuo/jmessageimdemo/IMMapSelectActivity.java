package me.luzhuo.jmessageimdemo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.UUID;

import me.luzhuo.lib_common_ui.toolbar.OnToolBarCallback;
import me.luzhuo.lib_common_ui.toolbar.ToolBarView;
import me.luzhuo.lib_core.app.base.CoreBaseActivity;
import me.luzhuo.lib_core.data.file.FileManager;
import me.luzhuo.lib_core.ui.fragment.FragmentManager;
import me.luzhuo.lib_im.manager.IMMapStartUtils;
import me.luzhuo.lib_im.manager.enums.ConversationType;
import me.luzhuo.lib_im.manager.event.eventbus.MapSelectEvent;
import me.luzhuo.lib_map_gaode.OnScreentShotCallback;
import me.luzhuo.lib_map_gaode.bean.LocationBean;
import me.luzhuo.lib_map_gaode.fragment.IMMapPickerFragement;

/**
 * Description:
 *
 * @Author: Luzhuo
 * @Creation Date: 2021/1/11 11:40
 * @Copyright: Copyright 2021 Luzhuo. All rights reserved.
 **/
public class IMMapSelectActivity extends CoreBaseActivity {
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_single);

        ToolBarView toolBarView = findViewById(R.id.toolbar);
        toolBarView.setTitle("选择地图");
        toolBarView.setRText("确定");


        final IMMapStartUtils startUtils = new IMMapStartUtils(this);
        IMMapStartUtils.IMSelectMapData imSelectMapData = startUtils.parseSelectMap(getIntent().getData());
        if (imSelectMapData == null) return;

        final FileManager fileManager = new FileManager();
        final IMMapPickerFragement fragment = new IMMapPickerFragement();

        final FragmentManager fragmentManager = new FragmentManager(this, R.id.frame);
        fragmentManager.replaceFragment(fragment);

        toolBarView.setOnToolBarCallback(new OnToolBarCallback(){
            @Override
            public void onRightButton() {
                fragment.screentShot(new OnScreentShotCallback() {
                    @Override
                    public void onScreentShotCallback(Bitmap bitmap) {
                        try {
                            String filepath = fileManager.getFileDirectory(IMMapSelectActivity.this) + File.separator + UUID.randomUUID().toString().replace("-", "") + ".png";
                            fileManager.Bitmap2PNGFile(bitmap, filepath);

                            final LocationBean picker = fragment.getCurrentPicker();
                            final int mapType = imSelectMapData.type;
                            final String name = imSelectMapData.targetId;
                            if (mapType == ConversationType.Single.ordinal()) EventBus.getDefault().post(new MapSelectEvent(ConversationType.Single.ordinal(), name, filepath, "", picker.latitude, picker.longitude, picker.city, picker.title, picker.address));
                            else if (mapType == ConversationType.Group.ordinal()) EventBus.getDefault().post(new MapSelectEvent(ConversationType.Group.ordinal(), name, filepath, "", picker.latitude, picker.longitude, picker.city, picker.title, picker.address));
                            else if (mapType == ConversationType.ChatRoom.ordinal()) EventBus.getDefault().post(new MapSelectEvent(ConversationType.ChatRoom.ordinal(), name, filepath, "", picker.latitude, picker.longitude, picker.city, picker.title, picker.address));
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(IMMapSelectActivity.this, "返回截图失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}

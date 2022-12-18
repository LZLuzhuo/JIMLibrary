package me.luzhuo.jmessageimdemo;

import android.os.Bundle;

import me.luzhuo.lib_common_ui.toolbar.ToolBarView;
import me.luzhuo.lib_core.app.base.CoreBaseActivity;
import me.luzhuo.lib_core.ui.fragment.FragmentManager;
import me.luzhuo.lib_im.manager.IMMapStartUtils;
import me.luzhuo.lib_im.manager.event.eventbus.MapSelectEvent;
import me.luzhuo.lib_map_gaode.bean.LocationBean;
import me.luzhuo.lib_map_gaode.fragment.IMMapShowFragment;

/**
 * Description:
 *
 * @Author: Luzhuo
 * @Creation Date: 2021/1/11 11:40
 * @Copyright: Copyright 2021 Luzhuo. All rights reserved.
 **/
public class IMMapShowActivity extends CoreBaseActivity {
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_single);

        ToolBarView toolBarView = findViewById(R.id.toolbar);
        toolBarView.setTitle("显示地图");


        final IMMapStartUtils startUtils = new IMMapStartUtils(this);
        MapSelectEvent selectEvent = startUtils.parseShowMap(getIntent().getData());
        if (selectEvent == null) return;

        LocationBean location = new LocationBean(selectEvent.lat, selectEvent.lon, selectEvent.title, selectEvent.address);
        final FragmentManager fragmentManager = new FragmentManager(this, R.id.frame);
        fragmentManager.replaceFragment(IMMapShowFragment.instance(location));
    }
}

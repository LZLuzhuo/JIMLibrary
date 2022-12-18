package me.luzhuo.jmessageimdemo;

import android.os.Bundle;

import me.luzhuo.lib_common_ui.toolbar.ToolBarView;
import me.luzhuo.lib_core.app.base.CoreBaseActivity;
import me.luzhuo.lib_core.ui.fragment.FragmentManager;
import me.luzhuo.lib_im.main.detail.GroupDetailConversation;
import me.luzhuo.lib_im.manager.IMConversationManager;
import me.luzhuo.lib_im.manager.IMStartUtils;
import me.luzhuo.lib_im.manager.enums.ConversationType;

/**
 * Description:
 *
 * @Author: Luzhuo
 * @Creation Date: 2021/1/11 11:25
 * @Copyright: Copyright 2021 Luzhuo. All rights reserved.
 **/
public class GroupDetailActivity extends CoreBaseActivity {
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_single);

        ToolBarView toolBarView = findViewById(R.id.toolbar);

        IMStartUtils imStart = new IMStartUtils(this);
        long groupId = imStart.parseGroupDetail(getIntent().getData());
        if (groupId == 0L) return;

        toolBarView.setTitle(IMConversationManager.getConversationTitle(ConversationType.Group, String.valueOf(groupId), null));

        final FragmentManager fragmentManager = new FragmentManager(this, R.id.frame);
        fragmentManager.replaceFragment(GroupDetailConversation.instance(groupId));
    }
}

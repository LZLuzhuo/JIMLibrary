/* Copyright 2020 Luzhuo. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.luzhuo.lib_im.main.list;

import android.Manifest;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import me.luzhuo.lib_core.data.DataConvert;
import me.luzhuo.lib_core.ui.dialog.Dialog;
import me.luzhuo.lib_im.R;
import me.luzhuo.lib_im.main.detail.adapter.GroupListAdapter;
import me.luzhuo.lib_im.main.detail.callback.IMListCallback;
import me.luzhuo.lib_im.manager.IMConversationManager;
import me.luzhuo.lib_im.manager.IMStartUtils;
import me.luzhuo.lib_im.manager.enums.ConversationType;
import me.luzhuo.lib_im.manager.event.eventbus.MainEvent;
import me.luzhuo.lib_im.manager.event.eventbus.ReceivedMessageEvent;
import me.luzhuo.lib_permission.Permission;
import me.luzhuo.lib_permission.PermissionCallback;

/**
 * 群会话列表
 */
public class GroupConversationList extends Fragment {
    private RecyclerView listView;
    private GroupListAdapter adapter;
    private DataConvert dataConvert = new DataConvert();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.im_conversation_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView(getView());
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initView(View view) {
        initRecycerView(view);
    }

    private void initRecycerView(View view) {
        listView = view.findViewById(R.id.im_list_rec);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        listView.setLayoutManager(layoutManager);
        adapter = new GroupListAdapter();
        listView.setAdapter(adapter);

        adapter.setIMListCallback(new IMListCallback(){
            @Override
            public void onConversationOnClick(int type, final Conversation conversation) {
                Permission.request(GroupConversationList.this, new PermissionCallback() {
                    @Override
                    public void onGranted() {
                        conversation.resetUnreadCount();
                        EventBus.getDefault().post(new MainEvent(MainEvent.TypeUnreader, ""));
                        IMStartUtils.startGroupDetail(getContext(), ((GroupInfo) conversation.getTargetInfo()).getGroupID());
                    }
                }, Manifest.permission.RECORD_AUDIO);
            }

            @Override
            public void onConversationOnLongClick(int type, final Conversation conversation) {
                List<String> menus = new ArrayList<>();
                if (type != GroupListAdapter.Type_Forever) {
                    if (TextUtils.isEmpty(conversation.getExtra())) menus.add("会话置顶");
                    else menus.add("取消置顶");
                }
                menus.add("删除该会话");

                Dialog.instance().showMenu(getContext(), dataConvert.list2Array(menus), new Dialog.OnSingleChoice() {
                    @Override
                    public void onOk(int i, String s, String[] strings, Object o) {
                        if (s.equals("会话置顶")) adapter.setTop(conversation);
                        else if (s.equals("取消置顶")) adapter.cancelTop(conversation);
                        else if (s.equals("删除该会话")) {
                            JMessageClient.deleteGroupConversation(((GroupInfo) conversation.getTargetInfo()).getGroupID());
                            adapter.delete(conversation);
                        }
                    }
                    @Override
                    public void onCancel(int i, String[] strings, Object o) { }
                }, null);
            }
        });
    }

    // ================================= 事件接收 ↓ =================================

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void XXX(ReceivedMessageEvent event) {
        if (event.type == ConversationType.Group) {
            long groupId = ((GroupInfo) event.message.getTargetInfo()).getGroupID();
            Conversation conv = IMConversationManager.getConversation(ConversationType.Group, String.valueOf(groupId));
            adapter.updateConversation(conv);
        }
    }
}

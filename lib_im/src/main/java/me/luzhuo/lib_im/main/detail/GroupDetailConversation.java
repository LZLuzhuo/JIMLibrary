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
package me.luzhuo.lib_im.main.detail;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.liaoinstan.springview.widget.SpringView;
import com.sj.emoji.EmojiBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.LocationContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VideoContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.api.BasicCallback;
import me.luzhuo.lib_common_ui.emoji.filter.EmojiFilter;
import me.luzhuo.lib_core.app.base.CoreBaseApplication;
import me.luzhuo.lib_core.data.DataConvert;
import me.luzhuo.lib_core.data.clipboard.ClipboardManager;
import me.luzhuo.lib_core.data.file.FileManager;
import me.luzhuo.lib_core.ui.toast.ToastManager;
import me.luzhuo.lib_im.R;
import me.luzhuo.lib_im.main.detail.adapter.GroupDetailAdapter;
import me.luzhuo.lib_im.main.detail.callback.IMDetailCallback;
import me.luzhuo.lib_im.manager.IMCommonConfig;
import me.luzhuo.lib_im.manager.IMConversationManager;
import me.luzhuo.lib_im.manager.IMDetailMessageManager;
import me.luzhuo.lib_im.manager.IMMapStartUtils;
import me.luzhuo.lib_im.manager.IMSendMessager;
import me.luzhuo.lib_im.manager.enums.ConversationType;
import me.luzhuo.lib_im.manager.event.eventbus.MapSelectEvent;
import me.luzhuo.lib_im.manager.event.eventbus.RTCSendMessageEvent;
import me.luzhuo.lib_im.manager.event.eventbus.ReadedEvent;
import me.luzhuo.lib_im.manager.event.eventbus.ReceivedMessageEvent;
import me.luzhuo.lib_im.ui.layout.func.FuncLayout;
import me.luzhuo.lib_im.ui.layout.func.adapter.AppsAdapter;
import me.luzhuo.lib_im.ui.layout.func.adapter.EmoticonsAdapter;
import me.luzhuo.lib_im.ui.layout.func.adapter.PageSetAdapter;
import me.luzhuo.lib_im.ui.layout.func.adapter.SimpleAppsGridView;
import me.luzhuo.lib_im.ui.layout.func.bean.AppBean;
import me.luzhuo.lib_im.ui.layout.keyboard.EmoticonsKeyBoard;
import me.luzhuo.lib_im.ui.weight.EmoticonsEditText;
import me.luzhuo.lib_im.ui.weight.PopupList;
import me.luzhuo.lib_im.ui.weight.RecordVoiceButton;
import me.luzhuo.lib_im.utils.EmojiPageSetEntityUtils;
import me.luzhuo.lib_im.utils.KeyBoardUtils;
import me.luzhuo.lib_image_select.ImageSelectManager;
import me.luzhuo.lib_image_select.bean.FileBean;
import me.luzhuo.lib_image_select.callback.SelectCallBack;
import me.luzhuo.lib_image_select.utils.MediaUtil;
import me.luzhuo.lib_permission.Permission;
import me.luzhuo.lib_permission.PermissionCallback;

/**
 * 群聊会话详情
 */
public class GroupDetailConversation extends Fragment {
    private Conversation conv;
    private Long groupId;
    private EmoticonsKeyBoard keyBoard;
    private RecyclerView dropDownListView;
    private SpringView springView;
    private GroupDetailAdapter adapter;
    private KeyBoardUtils keyboard = new KeyBoardUtils();
    private ClipboardManager clipboard;
    private ImageSelectManager imageSelect;
    private FileManager fileManager = new FileManager();
    private DataConvert dataConvert = new DataConvert();

    public synchronized static GroupDetailConversation instance(long groupId) {
        final Bundle bundle = new Bundle();
        bundle.putLong(IMCommonConfig.GroupDetail_Name_Long, groupId);

        GroupDetailConversation groupDetail = new GroupDetailConversation();
        groupDetail.setArguments(bundle);

        return groupDetail;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this);
        clipboard = new ClipboardManager(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.im_conversation_detail, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        groupId = getArguments().getLong(IMCommonConfig.GroupDetail_Name_Long);
        conv = IMConversationManager.getConversation(ConversationType.Group, groupId.toString());
        imageSelect = new ImageSelectManager(this);
        initView(getView());
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        IMConversationManager.enterConversationTemp(ConversationType.Group, groupId.toString());
    }

    @Override
    public void onPause() {
        super.onPause();
        IMConversationManager.exitConversationTemp();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        IMConversationManager.exitConversation(ConversationType.Group, groupId.toString());
        EventBus.getDefault().unregister(this);
    }

    // ============================== 正片开始 ==============================

    // ============================== 初始化界面 ↓ ==============================

    private void initView(View view) {
        keyBoard = view.findViewById(R.id.im_keyboard);
        dropDownListView = view.findViewById(R.id.im_dropdown_listview);
        springView = view.findViewById(R.id.springview);

        initKeyBoard();
        initDropdownListView();
        initListView();
    }

    private void initKeyBoard() {
        // TODO @ 功能 367
        /*
        hasFocus 获取到焦点
        当输入的内容 >= 1, 并且获取到焦点时, 才发送对方正在输入的事件
        */
        keyBoard.getEtChat().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {

                String content;
                if (hasFocus && !keyBoard.getEtChat().toString().trim().isEmpty()) content = "对方正在输入...";
                else content = conv.getTitle();

                IMSendMessager.sendTransCommand(ConversationType.Group, groupId.toString(), content);
            }
        });

        // 输入框大小发生变化, 在换行的时候发生
        keyBoard.getEtChat().setOnSizeChangedListener(new EmoticonsEditText.OnSizeChangedListener() {
            @Override
            public void onSizeChanged(int w, int h, int oldw, int oldh) {
                scrollToBottom();
            }
        });

        // 发送文本消息
        keyBoard.getBtnSend().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollToBottom();

                String msgContent = keyBoard.getEtChat().getText().toString().trim();
                if(TextUtils.isEmpty(msgContent)) return;

                // TODO 发送@信息 暂未实现 453
                IMSendMessager.sendText(ConversationType.Group, groupId.toString(), msgContent, new IMSendMessager.IMessageCallback() {
                    @Override
                    public void onMessage(Message message) {
                        adapter.addMsgSendToList(message);
                        keyBoard.getEtChat().setText("");
                    }
                }, null);
            }
        });

        // 切换语音输入
        keyBoard.getVoiceOrText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = view.getId();
                if (id == R.id.btn_voice_or_text) keyBoard.setVoiceText();
            }
        });

        // 语音按钮
        keyBoard.getBtnVoice().setOnUserVoiceListener(new RecordVoiceButton.OnUserVoiceListener() {
            @Override
            public void onUserSpeaking() {
                IMSendMessager.sendTransCommand(ConversationType.Group, groupId.toString(), "对方正在说话...");
            }
            @Override
            public void onUserSpeaked() {
                IMSendMessager.sendTransCommand(ConversationType.Group, groupId.toString(), conv.getTitle());
            }
            @Override
            public void onVoiceContent(File myRecAudioFile, int duration) {
                try {
                    IMSendMessager.sendVoice(ConversationType.Group, groupId.toString(), myRecAudioFile.getAbsolutePath(), duration, new IMSendMessager.IMessageCallback() {
                        @Override
                        public void onMessage(Message message) {
                            adapter.addMsgSendToList(message);
                            setToBottom();
                        }
                    }, null);
                } catch (FileNotFoundException e) { e.printStackTrace(); }
            }
        });
        setToBottom();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initDropdownListView() {
        // 关闭软键盘
        dropDownListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                dropDownListView.setFocusable(true);
                dropDownListView.setFocusableInTouchMode(true);
                dropDownListView.requestFocus();
                keyboard.hide(getActivity());
                return false;
            }
        });

        // 下拉加载更多
        springView.setListener(new SpringView.OnFreshListener() {
            @Override
            public void onRefresh() {
                // 加载更多消息
                adapter.loadMoreMsg();
                springView.onFinishFreshAndLoad();
            }
            @Override
            public void onLoadmore() {  }
        });

        // 移至末尾
        setToBottom();
    }

    private final List<String> receivePopMenus = Arrays.asList("复制", "转发", "删除");
    private final List<String> sendPopMenus = Arrays.asList("复制", "转发", "撤回", "删除");
    private final List<String> receiveOtherPopMenus = Arrays.asList("转发", "删除");
    private final List<String> sendOtherPopMenus = Arrays.asList("转发", "撤回", "删除");
    private void initListView() {
        adapter = new GroupDetailAdapter(conv);
        dropDownListView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, true));
        dropDownListView.setAdapter(adapter);
        dropDownListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
             @Override
             public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                 /*
                 * ListView: SCROLL_STATE_FLING(开始滚动) / SCROLL_STATE_IDLE(停止滚动) / SCROLL_STATE_TOUCH_SCROLL(正在滚动)
                 * RecyclerView: SCROLL_STATE_DRAGGING(滑动状态) / SCROLL_STATE_IDLE(空闲状态) / SCROLL_STATE_SETTLING (滑动后自然沉降状态)
                 * */
                 if (newState == RecyclerView.SCROLL_STATE_SETTLING);
                 else if (newState == RecyclerView.SCROLL_STATE_IDLE);
                 else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) keyBoard.reset();
             }
             @Override
             public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) { }
        });

        adapter.setOnIMDetailCallbacklistener(new IMDetailCallback(){

            @Override
            public void onInBlackList(String targetId) {
                // 告知对方已被拉入黑名单
                Map<String, Boolean> boolValues = new HashMap<>();
                boolValues.put("blackList", true);
                IMSendMessager.sendCustom(ConversationType.Group, targetId, null, null, boolValues, null, null);
            }

            @Override
            public void onHeaderClick(boolean isMe, String targetId) {
                Toast.makeText(GroupDetailConversation.this.getContext(), "点击了头像" + targetId, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onContentLongClick(int position, View view) {
                final Message data = adapter.getMessage(position);
                if(data == null) return;

                int[] location = new int[2];
                view.getLocationOnScreen(location);

                ContentType type = data.getContentType();
                if (type == ContentType.text) {

                    if (data.getDirect() == MessageDirect.receive) {
                        new PopupList(view.getContext()).showPopupListWindow(view, position, location[0] + view.getWidth() / 2, location[1], receivePopMenus, new PopupList.PopupListListener() {
                            @Override
                            public boolean showPopupList(View adapterView, View contextView, int contextPosition) { return true; }
                            @Override
                            public void onPopupListClick(View contextView, int contextPosition, int position) {
                                final String content = ((TextContent) data.getContent()).getText();
                                final String name = receivePopMenus.get(position);

                                if (name.equals("复制")) {
                                    clipboard.copy(content);
                                    Toast.makeText(CoreBaseApplication.context, "已复制", Toast.LENGTH_SHORT).show();
                                } else if (name.equals("转发")) {
                                    // TODO 转发
                                } else if (name.equals("删除")) {
                                    conv.deleteMessage(data.getId());
                                    adapter.deleteMessage(data);
                                }
                            }
                        });
                    } else {

                        new PopupList(view.getContext()).showPopupListWindow(view, position, location[0] + view.getWidth() / 2, location[1], sendPopMenus, new PopupList.PopupListListener() {
                            @Override
                            public boolean showPopupList(View adapterView, View contextView, int contextPosition) { return true; }
                            @Override
                            public void onPopupListClick(View contextView, int contextPosition, int position) {
                                final String content = ((TextContent) data.getContent()).getText();
                                final String name = sendPopMenus.get(position);

                                if (name.equals("复制")) {
                                    clipboard.copy(content);
                                    Toast.makeText(CoreBaseApplication.context, "已复制", Toast.LENGTH_SHORT).show();
                                } else if (name.equals("转发")) {
                                    // TODO 转发
                                } else if (name.equals("删除")) {
                                    conv.deleteMessage(data.getId());
                                    adapter.deleteMessage(data);
                                } else if (name.equals("撤回")) {
                                    conv.retractMessage(data, new BasicCallback() {
                                        @Override
                                        public void gotResult(int i, String s) {
                                            if (i == 855001) Toast.makeText(CoreBaseApplication.context, "发送时间过长, 不能撤回", Toast.LENGTH_SHORT).show();
                                                // 撤回成功后, Message会被自动替换成自定义消息 "你撤回了一条消息"
                                            else if (i == 0) adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                        });
                    }
                } else {
                    // 除了文本消息, 其他消息的操作都一样
                    if (data.getDirect() == MessageDirect.receive) {

                        new PopupList(view.getContext()).showPopupListWindow(view, position, location[0] + view.getWidth() / 2, location[1], receiveOtherPopMenus, new PopupList.PopupListListener() {
                            @Override
                            public boolean showPopupList(View adapterView, View contextView, int contextPosition) { return true; }
                            @Override
                            public void onPopupListClick(View contextView, int contextPosition, int position) {
                                final String name = receivePopMenus.get(position);

                                if (name.equals("转发")) {
                                    // TODO 转发
                                } else if (name.equals("删除")) {
                                    conv.deleteMessage(data.getId());
                                    adapter.deleteMessage(data);
                                }
                            }
                        });

                    } else {

                        new PopupList(view.getContext()).showPopupListWindow(view, position, location[0] + view.getWidth() / 2, location[1], sendOtherPopMenus, new PopupList.PopupListListener() {
                            @Override
                            public boolean showPopupList(View adapterView, View contextView, int contextPosition) { return true; }
                            @Override
                            public void onPopupListClick(View contextView, int contextPosition, int position) {
                                final String name = sendOtherPopMenus.get(position);

                                if (name.equals("转发")) {
                                    // TODO 转发
                                } else if (name.equals("删除")) {
                                    conv.deleteMessage(data.getId());
                                    adapter.deleteMessage(data);
                                } else if (name.equals("撤回")) {
                                    conv.retractMessage(data, new BasicCallback() {
                                        @Override
                                        public void gotResult(int i, String s) {
                                            if (i == 855001) Toast.makeText(CoreBaseApplication.context, "发送时间过长, 不能撤回", Toast.LENGTH_SHORT).show();
                                                // 撤回成功后, Message会被自动替换成自定义消息 "你撤回了一条消息"
                                            else if (i == 0) adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onContentClick(int position, View v) {
                final Message data = adapter.getMessage(position);
                if(data == null) return;

                ContentType type = data.getContentType();
                if (type == ContentType.image) { // 打开图片, 查看详情
                    final ImageContent content = (ImageContent) data.getContent();
                    if (!TextUtils.isEmpty(content.getLocalPath())) MediaUtil.instance().showBigPhotoList(getActivity(), dataConvert.string2List(content.getLocalPath()));
                    else if (!TextUtils.isEmpty(content.getLocalThumbnailPath())) MediaUtil.instance().showBigPhotoList(getActivity(), dataConvert.string2List(content.getLocalThumbnailPath()));
                    else ToastManager.show(CoreBaseApplication.context, "未发现图片");
                } else if (type == ContentType.location) {
                    final LocationContent content = (LocationContent) data.getContent();
                    String title = content.getStringExtra("title");
                    String address = content.getAddress();

                    MapSelectEvent mapSelect = new MapSelectEvent(content.getLatitude().doubleValue(), content.getLongitude().doubleValue(), title, address);
                    IMMapStartUtils.startShowMap(getContext(), mapSelect);
                } else if (type == ContentType.video) {
                    final VideoContent content = (VideoContent) data.getContent();
                    String thumbLocalPath = content.getThumbLocalPath();
                    String videoLocalPath = content.getVideoLocalPath();
                    if (TextUtils.isEmpty(videoLocalPath)) {
                        ToastManager.show(CoreBaseApplication.context, "请等待视频文件下载完成");
                        return;
                    }
                    MediaUtil.instance().showVideo(getContext(), thumbLocalPath, videoLocalPath);
                }
            }

            private AnimationDrawable oldAnimation;
            private Message oldData;
            private ImageView old_im_detail_voice_icon;
            @Override
            public void onVoiceContentClick(int position, View v, ImageView im_detail_voice_icon) {
                // 播放语音
                final Message data = adapter.getMessage(position);
                if(data == null) return;
                // 如果之前有播放的动画, 则停止动画
                if (oldAnimation != null) {
                    oldAnimation.stop();
                    if (oldData != null && oldData.getDirect() == MessageDirect.send) old_im_detail_voice_icon.setImageResource(R.mipmap.im_detail_voice_right3);
                    else old_im_detail_voice_icon.setImageResource(R.mipmap.im_detail_voice_left3);
                }

                // 开始播放新的语音
                old_im_detail_voice_icon = im_detail_voice_icon;
                oldData = data;
                if (oldData != null && oldData.getDirect() == MessageDirect.send && old_im_detail_voice_icon != null) {
                    old_im_detail_voice_icon.setImageResource(R.drawable.im_detail_voice_right);
                    oldAnimation = (AnimationDrawable) old_im_detail_voice_icon.getDrawable();
                } else if (old_im_detail_voice_icon != null) {
                    old_im_detail_voice_icon.setImageResource(R.drawable.im_detail_voice_left);
                    oldAnimation = (AnimationDrawable) old_im_detail_voice_icon.getDrawable();
                }

                IMDetailMessageManager.playVoice(oldData, old_im_detail_voice_icon, oldAnimation, oldData != null && oldData.getDirect() == MessageDirect.send);
            }
        });
    }

    // ============================== 初始化界面 ↑ ==============================

    // ============================== 初始化数据 ↓ ==============================

    private void initData() {
        // 初始化表情符号
        initEmoticons();
        // 初始化功能页
        initFunc();
    }

    private void initEmoticons() {
        // 添加 EditText 表情过滤
        keyBoard.getEtChat().addEmoticonFilter(new EmojiFilter());

        // 添加表情功能页
        PageSetAdapter pageSetAdapter = new PageSetAdapter();
        EmojiPageSetEntityUtils.addEmojiPageSetEntity(getContext(), pageSetAdapter, new EmoticonsAdapter.EmoticonClickListener<EmojiBean>() {
            @Override
            public void onEmoticonClick(EmojiBean emojiBean, int actionType, boolean isDelBtn) {
                if (isDelBtn) { // 删除
                    keyBoard.getEtChat().onKeyDown(KeyEvent.KEYCODE_DEL, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                } else {
                    if (emojiBean == null) return;

                    if (actionType == EmoticonsAdapter.EMOTICON_CLICK_TEXT) {

                        String content = emojiBean.emoji;
                        if (TextUtils.isEmpty(content)) return;

                        int index = keyBoard.getEtChat().getSelectionStart();
                        Editable editable = keyBoard.getEtChat().getText();
                        editable.insert(index, content);

                    } else {
                        // TODO 暂不支持发送图片表情
                    }
                }
            }
        });
        keyBoard.setAdapter(pageSetAdapter);

        // 将表情页添加到功能页
        keyBoard.addOnFuncKeyBoardListener(new FuncLayout.OnFuncKeyBoardListener() {
            @Override
            public void OnFuncPop(int height) {
                scrollToBottom();
            }

            @Override
            public void OnFuncClose() { }
        });
    }

    private void initFunc() {
        List<AppBean> apps = new ArrayList<>();
        apps.add(new AppBean(R.mipmap.im_apps_icon_photo, "图片"));
        apps.add(new AppBean(R.mipmap.im_apps_icon_camera, "拍摄"));
        // apps.add(new AppBean(R.mipmap.im_apps_icon_file, "文件"));
        // apps.add(new AppBean(R.mipmap.im_apps_icon_voice_phone, "语音通话"));
        apps.add(new AppBean(R.mipmap.im_apps_icon_loaction, "位置"));
        SimpleAppsGridView gridView = new SimpleAppsGridView(requireContext(), apps);
        gridView.setOnAppListener(new AppsAdapter.OnAppListener() {
            @Override
            public void onClick(AppBean appBean) {
                if (appBean.funcName.equals("图片")) {
                    sendImageMessage();
                } else if (appBean.funcName.equals("拍摄")) {
                    sendVideoMessage();
                /*} else if (appBean.funcName.equals("语音通话")) {
                    VoicePhoneActivity.call(requireContext(), username);*/
                } else if (appBean.funcName.equals("位置")) {
                    getLocalMap();
                }
            }
        });
        keyBoard.addFuncView(gridView);
    }

    // ============================== 初始化数据 ↑ ==============================

    /**
     * 滚动到底部
     */
    private void scrollToBottom() {
        dropDownListView.requestLayout();
        dropDownListView.post(new Runnable() {
            @Override
            public void run() {
                dropDownListView.scrollToPosition(0);
            }
        });
    }

    /**
     * 直接到底部
     */
    private void setToBottom() {
        dropDownListView.clearFocus();
        dropDownListView.post(new Runnable() {
            @Override
            public void run() {
                dropDownListView.scrollToPosition(1);
            }
        });
    }

    // ================================= 事件接收 ↓ =================================

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void XXX(ReadedEvent event) {
        if (event.type == ConversationType.Group) adapter.updateReaded(event.serverMsgId, event.unReceiptCount);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void XXX(ReceivedMessageEvent event) {
        if (event.type == ConversationType.Group && event.targetId.equals(groupId.toString())) adapter.updateMessage(event.message);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void XXX(MapSelectEvent event) {
        if (event.type == ConversationType.Group.ordinal() && event.targetId.equals(groupId.toString())) {
            // 发送地图
            IMSendMessager.sendLocation(ConversationType.Group, groupId.toString(), event.lat, event.lon, 17, event.title, event.address, event.filePath, event.fileUrl, new IMSendMessager.IMessageCallback(){
                @Override
                public void onMessage(Message message) {
                    adapter.addMsgSendToList(message);
                    setToBottom();
                }
            }, null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void XXX(RTCSendMessageEvent event) {
        Message message = event.message;
        adapter.addMsgSendToList(message);
        setToBottom();
    }

    // ================================= 发送功能页的消息 ↓ =================================

    public void sendImageMessage() {
        Permission.request(requireActivity(), new PermissionCallback() {
            @Override
            public void onGranted() {
                imageSelect
                        .original(true)
                        .onSetCallbackListener(new SelectCallBack() {
                            @Override
                            public void onSelect(List<FileBean> list) {
                                String imagePath = fileManager.checkFilePath(getContext(), list.get(0).getPath());
                                IMSendMessager.sendImage(ConversationType.Group, groupId.toString(), imagePath, new IMSendMessager.IMessageCallback() {
                                    @Override
                                    public void onMessage(Message message) {
                                        adapter.addMsgSendToList(message);
                                        setToBottom();
                                    }
                                }, null);
                            }
                            @Override
                            public void onCancel() { }
                        }).openImage(1);
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private void sendVideoMessage() {
        Permission.request(requireActivity(), new PermissionCallback() {
            @Override
            public void onGranted() {
                imageSelect.onSetCallbackListener(new SelectCallBack() {
                    @Override
                    public void onSelect(List<FileBean> list) {
                        try {
                            FileBean fileBean = list.get(0);
                            final String videoPath = fileManager.checkFilePath(getContext(), fileBean.getPath());
                            IMSendMessager.sendVideo(ConversationType.Group, groupId.toString(), videoPath, (int) fileBean.duration, new IMSendMessager.IMessageCallback() {
                                @Override
                                public void onMessage(Message message) {
                                    adapter.addMsgSendToList(message);
                                    setToBottom();
                                }
                            }, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastManager.show(CoreBaseApplication.context, "读取视频文件发生错误");
                        }
                    }
                    @Override
                    public void onCancel() { }
                }).openVideo(1);
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public void getLocalMap() {
        Permission.request(requireActivity(), new PermissionCallback() {
            @Override
            public void onGranted() {
                IMMapStartUtils.startSelectMap(getContext(), ConversationType.Group, groupId.toString());
            }
        }, Manifest.permission.ACCESS_FINE_LOCATION);
    }
}

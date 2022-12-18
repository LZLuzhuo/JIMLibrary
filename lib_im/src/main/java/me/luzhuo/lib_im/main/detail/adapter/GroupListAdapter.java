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
package me.luzhuo.lib_im.main.detail.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.PromptContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageStatus;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import me.luzhuo.lib_common_ui.emoji.EmojiManager;
import me.luzhuo.lib_core.DateManager;
import me.luzhuo.lib_core.data.DataCheck;
import me.luzhuo.lib_im.R;
import me.luzhuo.lib_im.main.detail.callback.IMListCallback;
import me.luzhuo.lib_im.manager.IMConversationManager;
import me.luzhuo.lib_im.manager.IMDetailMessageManager;

public class GroupListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * 永久置顶区数据, 不可置顶 或 取消置顶
     */
    private List<Conversation> foreverDatas = new ArrayList<>();
    /**
     * 置顶区数据, 可取消置顶
     */
    private List<Conversation> topDatas = new ArrayList<>();
    /**
     * 普通会话区数据
     */
    private List<Conversation> mDatas = new ArrayList<>();
    public static final int Type_Forever = 1, Type_Top = 2, Type_Default = 3;
    private Context context;
    private IMListCallback callback;
    private DateManager dateU = new DateManager();
    private EmojiManager emoji = EmojiManager.getInstance();
    private StringBuffer contentStr;
    private GroupInfo groupInfo;

    public GroupListAdapter() {
        List<Conversation> conversations = IMConversationManager.getConversationList(me.luzhuo.lib_im.manager.enums.ConversationType.Group);
        if (conversations == null || conversations.size() <= 0) return;

        for (Conversation conversation : conversations) {
            // 只取单聊会话
            if(conversation.getType() != ConversationType.group) continue;

            if (IMDetailMessageManager.isForever(conversation)) foreverDatas.add(conversation);
            else if (!TextUtils.isEmpty(conversation.getExtra())) topDatas.add(conversation);
            else mDatas.add(conversation);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ItemHolder(LayoutInflater.from(context).inflate(R.layout.im_item_single_list, parent, false));
    }

    @Override
    public int getItemCount() {
        return foreverDatas.size() + topDatas.size() + mDatas.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < foreverDatas.size()) return Type_Forever;
        else {
            if (position < foreverDatas.size() + topDatas.size()) return Type_Top;
            else return Type_Default;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type == Type_Forever) ((ItemHolder) holder).bindData(foreverDatas.get(position));
        else if (type == Type_Top) ((ItemHolder) holder).bindData(topDatas.get(position - foreverDatas.size()));
        else ((ItemHolder) holder).bindData(mDatas.get(position - foreverDatas.size() - topDatas.size()));
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public View im_list_zone;
        public ImageView im_list_header;
        public TextView im_list_unread;
        public TextView im_list_nickname;
        public TextView im_list_last_content;
        public TextView im_list_last_time;

        public ItemHolder(View itemView) {
            super(itemView);
            im_list_zone = itemView.findViewById(R.id.im_list_zone);
            im_list_header = itemView.findViewById(R.id.im_list_header);
            im_list_unread = itemView.findViewById(R.id.im_list_unread);
            im_list_nickname = itemView.findViewById(R.id.im_list_nickname);
            im_list_last_content = itemView.findViewById(R.id.im_list_last_content);
            im_list_last_time = itemView.findViewById(R.id.im_list_last_time);

            im_list_zone.setOnClickListener(this);
            im_list_zone.setOnLongClickListener(this);
        }

        public void bindData(Conversation data) {
            // 头像
            groupInfo = (GroupInfo) data.getTargetInfo();
            if (groupInfo != null) {
                groupInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int status, String desc, Bitmap bitmap) {
                        if (status == 0) Glide.with(context).load(bitmap).error(R.drawable.im_default).into(im_list_header);
                        else im_list_header.setImageResource(R.drawable.im_default);
                    }
                });
            } else im_list_header.setImageResource(R.drawable.im_default);


            // 未读消息数
            if (data.getUnReadMsgCnt() > 0) {
                im_list_unread.setVisibility(View.VISIBLE);
                if (data.getUnReadMsgCnt() > 99) im_list_unread.setText("99+");
                else im_list_unread.setText(String.valueOf(data.getUnReadMsgCnt()));

            } else im_list_unread.setVisibility(View.GONE);


            // 昵称
            im_list_nickname.setText(data.getTitle());


            // 最后一条消息
            Message lastMsg = data.getLatestMessage();
            if (lastMsg == null){
                im_list_last_content.setText("");
                im_list_last_time.setText("");
            }
            else {
                // 时间
                long lastTime = lastMsg.getCreateTime();
                try { im_list_last_time.setText(dateU.conversationFormat(lastTime)); } catch (Exception e) { im_list_last_time.setText(""); }

                // 内容
                contentStr = new StringBuffer();
                ContentType type = lastMsg.getContentType();
                if (type == ContentType.image) contentStr.append("[图片]");
                else if (type == ContentType.voice) contentStr.append("[语音]");
                else if (type == ContentType.location) contentStr.append("[位置]");
                else if (type == ContentType.file) contentStr.append("[文件]");
                else if (type == ContentType.video) contentStr.append("[视频]");
                else if (type == ContentType.eventNotification) contentStr.append("[群组消息]");
                else if (type == ContentType.custom) {
                    // 是否被对方加入了黑名单
                    CustomContent customContent = (CustomContent) lastMsg.getContent();
                    boolean isBlackList = DataCheck.check(customContent.getBooleanValue("blackList"));
                    if (isBlackList) contentStr.append("消息已发出, 但被对方拒收了!");
                    else contentStr.append("[自定义消息]");
                }
                else if (type == ContentType.prompt) contentStr.append(((PromptContent) lastMsg.getContent()).getPromptText());
                else contentStr.append(((TextContent) lastMsg.getContent()).getText());

                // 发送失败
                if (lastMsg.getStatus() == MessageStatus.send_fail) {
                    SpannableStringBuilder builder = new SpannableStringBuilder("[发送失败]");
                    builder.setSpan(new ForegroundColorSpan(Color.RED), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    contentStr.insert(0, builder);
                }

                emoji.TextViewFilter(im_list_last_content, contentStr.toString());
            }
        }

        @Override
        public void onClick(View v) {
            if (callback != null) {
                if (getLayoutPosition() < foreverDatas.size()) callback.onConversationOnClick(Type_Forever, foreverDatas.get(getLayoutPosition()));
                else if (getLayoutPosition() < foreverDatas.size() + topDatas.size()) callback.onConversationOnClick(Type_Top, topDatas.get(getLayoutPosition() - foreverDatas.size()));
                else callback.onConversationOnClick(Type_Default, mDatas.get(getLayoutPosition() - foreverDatas.size() - topDatas.size()));
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (callback != null) {
                if (getLayoutPosition() < foreverDatas.size()) callback.onConversationOnLongClick(Type_Forever, foreverDatas.get(getLayoutPosition()));
                else if (getLayoutPosition() < foreverDatas.size() + topDatas.size()) callback.onConversationOnLongClick(Type_Top, topDatas.get(getLayoutPosition() - foreverDatas.size()));
                else callback.onConversationOnLongClick(Type_Default, mDatas.get(getLayoutPosition() - foreverDatas.size() - topDatas.size()));
                return true;
            }
            return false;
        }
    }

    /**
     * 设置回调监听
     */
    public void setIMListCallback(IMListCallback callback) {
        this.callback = callback;
    }

    // ========================================= 正片开始 ↓ =========================================

    /**
     * 会话置顶
     */
    public void setTop(Conversation conversation) {
        conversation.updateConversationExtra("isTop");
        mDatas.remove(conversation);
        topDatas.add(conversation);
        notifyDataSetChanged();
    }

    /**
     * 取消置顶
     */
    public void cancelTop(Conversation conversation) {
        conversation.updateConversationExtra("");
        topDatas.remove(conversation);
        mDatas.add(conversation);
        Collections.sort(mDatas, new Comparator<Conversation>(){
            @Override
            public int compare(Conversation o1, Conversation o2) {
                if (o1.getLastMsgDate() > o2.getLastMsgDate()) return -1;
                else if (o1.getLastMsgDate() < o2.getLastMsgDate()) return 1;
                return 0;
            }
        });
        notifyDataSetChanged();
    }

    /**
     * 删除会话
     */
    public void delete(Conversation conversation) {
        foreverDatas.remove(conversation);
        topDatas.remove(conversation);
        mDatas.remove(conversation);
        notifyDataSetChanged();
    }

    /**
     * 会话的刷新
     */
    public void updateConversation(Conversation conv) {
        // 如果是新会话
        if (!foreverDatas.contains(conv) && !topDatas.contains(conv) && !mDatas.contains(conv)) {
            if (IMDetailMessageManager.isForever(conv)) foreverDatas.add(conv); // 永久区的消息
            else mDatas.add(0, conv);
        }
        notifyDataSetChanged();
    }
}

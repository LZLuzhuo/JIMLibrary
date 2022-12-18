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
package me.luzhuo.lib_im.main.detail.callback;

import android.view.View;
import android.widget.ImageView;

import me.luzhuo.lib_im.main.detail.adapter.SingleDetailAdapter;

public class IMDetailCallback {
    /**
     * 头像被点击
     * @param isMe 是否是点击了自己的头像, 不是的话, 则点击的是对方的头像
     * @param targetId 目标id
     */
    public void onHeaderClick(boolean isMe, String targetId) {}

    /**
     * 发送消息失败，发送者已被接收者拉入黑名单，仅限单聊
     * @param targetId 对方的userid
     */
    public void onInBlackList(String targetId) {}

    /**
     * 内容页的长按点击
     * @param position 内容页的索引
     * @param v 内容页的View
     */
    public void onContentLongClick(int position, View v) {}

    /**
     * 内容页的点击
     * @param position 内容页的索引
     * @param v 内容页的View
     */
    public void onContentClick(int position, View v) {}

    /**
     * 语音内容的点击
     * @param postition 内容页的素银
     * @param v 内容页的View
     * @param im_detail_voice_icon 播放的图标
     */
    public void onVoiceContentClick(int position, View v, ImageView im_detail_voice_icon) {}
}

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
package me.luzhuo.lib_im.manager.event.eventbus;

import cn.jpush.im.android.api.model.Message;
import me.luzhuo.lib_im.manager.enums.ConversationType;

/**
 * 接收消息事件
 */
public class ReceivedMessageEvent {
    public ConversationType type;
    public String targetId;
    public Message message;

    public ReceivedMessageEvent(ConversationType type, String targetId, Message message) {
        this.type = type;
        this.targetId = targetId;
        this.message = message;
    }
}

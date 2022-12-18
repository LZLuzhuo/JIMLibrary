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

/**
 * RTC发送自定义消息
 */
public class RTCSendMessageEvent {
    public boolean callinger;
    public boolean isCalled;
    public long endTime;
    public Message message;

    public RTCSendMessageEvent(boolean callinger, boolean isCalled, long endTime, Message message) {
        this.callinger = callinger;
        this.isCalled = isCalled;
        this.endTime = endTime;
        this.message = message;
    }
}

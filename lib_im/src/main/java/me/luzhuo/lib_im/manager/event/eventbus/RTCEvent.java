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

public class RTCEvent {
    public int type;
    public long time; // s

    /**
     * 主动挂断电话
     */
    public static final int Calleding = 0x01;

    /**
     * 被对方挂断电话
     */
    public static final int Called = 0x03;

    /**
     * 通话已连接
     */
    public static final int Calling = 0x05;

    public RTCEvent(int type) {
        this(type, 0);
    }

    public RTCEvent(int type, long time) {
        this.type = type;
        this.time = time;
    }
}

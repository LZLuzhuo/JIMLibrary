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

import java.io.Serializable;

/**
 * 发送给框架使用者
 */
public class MainEvent implements Serializable {
    // 用户被挤下线
    public final static int TypeLogin = 0x01;
    public final static int TypeUnreader = 0x02;

    public int type;
    public String username;

    public MainEvent(int type, String username) {
        this.type = type;
        this.username = username;
    }
}

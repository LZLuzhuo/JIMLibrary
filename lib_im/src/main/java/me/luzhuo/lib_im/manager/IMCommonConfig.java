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
package me.luzhuo.lib_im.manager;

public interface IMCommonConfig {
    /**
     * 监听对方已阅未读的状态
     */
    public static final boolean NeedReadReceipt = true;

    public static final String scheme = "luzhuo";
    public static final String authority = "im";
    public static final String singlePath = "single";
    public static final String groupPath = "group";
    public static final String showMapPath = "map/show";
    public static final String selectMapPath = "map/select";

    public static final String SingleDetail_Name_String = "name";
    public static final String GroupDetail_Name_Long = "groupId";
}

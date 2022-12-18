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
package me.luzhuo.lib_im.manager.enums;

import cn.jpush.im.android.api.enums.ContentType;
import me.luzhuo.lib_im.R;

@Deprecated()
public enum DetailOtherMessageType {
    Notification(ContentType.eventNotification, 12/*0x111*/, R.layout.im_detail_default),
    Prompt(ContentType.prompt, 13/*0x211*/, R.layout.im_detail_prompt);

    public ContentType contentType;
    public int type;
    public int layout;
    private DetailOtherMessageType(ContentType contentType, int type, int layout) {
        this.contentType = contentType;
        this.type = type;
        this.layout = layout;
    }
}

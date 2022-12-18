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
public enum DetailMessageType {
    Text(ContentType.text, 0/*0x101*/, 1/*0x102*/, R.layout.im_detail_text_left, R.layout.im_detail_text_right),
    Image(ContentType.image, 2/*0x201*/, 3/*0x202*/, R.layout.im_detail_image_left, R.layout.im_detail_image_right),
    File(ContentType.file, 4/*0x301*/, 5/*0x302*/, R.layout.im_detail_file_left, R.layout.im_detail_file_right),
    Voice(ContentType.voice, 6/*0x401*/, 7/*0x402*/, R.layout.im_detail_voice_left, R.layout.im_detail_voice_right),
    Video(ContentType.video, 8/*0x501*/, 9/*0x502*/, R.layout.im_detail_video_left, R.layout.im_detail_video_right),
    Location(ContentType.location, 10/*0x601*/, 11/*0x602*/, R.layout.im_detail_location_left, R.layout.im_detail_location_right);

    public ContentType contentType;
    public int leftType, rightType;
    public int leftLayout, rightLayout;
    private DetailMessageType(ContentType type, int leftType, int rightType, int leftLayout, int rightLayout){
        this.contentType = type;
        this.leftType = leftType;
        this.rightType = rightType;
        this.leftLayout = leftLayout;
        this.rightLayout = rightLayout;
    }
}

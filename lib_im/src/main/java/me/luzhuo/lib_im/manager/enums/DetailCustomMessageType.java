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

import me.luzhuo.lib_im.R;

@Deprecated()
public enum DetailCustomMessageType {
    VoicePhont(14/*0x101*/, 15/*0x102*/, R.layout.im_detail_voice_phone_left, R.layout.im_detail_voice_phone_right);

    public int leftType, rightType;
    public int leftLayout, rightLayout;
    private DetailCustomMessageType(int leftType, int rightType, int leftLayout, int rightLayout) {
        this.leftType = leftType;
        this.rightType = rightType;
        this.leftLayout = leftLayout;
        this.rightLayout = rightLayout;
    }
}

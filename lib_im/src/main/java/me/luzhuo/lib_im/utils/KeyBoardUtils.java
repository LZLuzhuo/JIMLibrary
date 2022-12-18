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
package me.luzhuo.lib_im.utils;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

/**
 * 软键盘工具
 */
public class KeyBoardUtils {

    /**
     * 显示软键盘
     * 弹出软键盘, 需要界面上可以获取焦点的控件
     * TODO 没效果
     */
    @Deprecated
    public void show(Activity activity) {
        if(activity == null) return;

        try {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (!imm.isActive()) imm.showSoftInputFromInputMethod(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {e.printStackTrace();}
    }


    /**
     * 隐藏软键盘
     * 隐藏软键盘, 需要界面上可以获取焦点的控件
     */
    public void hide(Activity activity) {
        if (activity == null) return;

        try {
            InputMethodManager imm = (InputMethodManager) activity.getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {e.printStackTrace();}
    }
}

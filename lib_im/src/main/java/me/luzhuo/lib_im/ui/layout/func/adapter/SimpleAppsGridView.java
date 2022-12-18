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
package me.luzhuo.lib_im.ui.layout.func.adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import me.luzhuo.lib_im.R;
import me.luzhuo.lib_im.ui.layout.func.bean.AppBean;

/**
 * 扩展页适配器
 */
public class SimpleAppsGridView extends RelativeLayout {
    private AppsAdapter adapter;

    public SimpleAppsGridView(Context context, List<AppBean> apps) {
        this(context, null, apps);
    }

    public SimpleAppsGridView(Context context, AttributeSet attrs, List<AppBean> apps) {
        super(context, attrs);

        // <merge /> can be used only with a valid ViewGroup root and attachToRoot=true
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.im_func_apps, this);
        initData(view, apps);
    }

    private void initData(View view, List<AppBean> apps) {
        GridView gv_apps = view.findViewById(R.id.gv_apps);
        adapter = new AppsAdapter(apps);
        gv_apps.setAdapter(adapter);
    }

    public void setOnAppListener(AppsAdapter.OnAppListener listener) {
        if (adapter != null) adapter.setOnAppListener(listener);
    }
}

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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import me.luzhuo.lib_im.R;
import me.luzhuo.lib_im.ui.layout.func.bean.AppBean;

/**
 * 功能页的功能适配器
 */
public class AppsAdapter extends BaseAdapter {
    private List<AppBean> mData;
    private OnAppListener listener;

    public AppsAdapter(List<AppBean> apps) {
        this.mData = apps;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int positon, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {

            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.im_item_apps, parent, false);
            viewHolder.iv_con = convertView.findViewById(R.id.iv_icon);
            viewHolder.tv_name = convertView.findViewById(R.id.tv_name);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final AppBean data = mData.get(positon);
        viewHolder.iv_con.setBackgroundResource(data.icon);
        viewHolder.tv_name.setText(data.funcName);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClick(data);
            }
        });

        return convertView;
    }

    public interface OnAppListener {
        public void onClick(AppBean appBean);
    }

    public void setOnAppListener(OnAppListener appListener) {
        this.listener = appListener;
    }

    private class ViewHolder {
        public ImageView iv_con;
        public TextView tv_name;
    }
}

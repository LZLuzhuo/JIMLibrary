/* Copyright 2021 Luzhuo. All rights reserved.
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
package me.luzhuo.lib_map_gaode.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import me.luzhuo.lib_map_gaode.R;
import me.luzhuo.lib_map_gaode.bean.LocationBean;

public class IMMapPickerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<LocationBean> mDatas;
    private Context context;
    public int currentIndex = 0;
    private OnMapPickerCallback callback;

    public IMMapPickerAdapter(List<LocationBean> data) {
        this.mDatas = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        return new ItemHolder(View.inflate(context, R.layout.map_item_im_select, null));
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ItemHolder) holder).bindData(mDatas.get(position));
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View map_im_address_zone;
        public TextView map_im_address;
        public TextView map_im_address_detail;
        public ImageView map_im_address_point;

        public ItemHolder(View itemView) {
            super(itemView);
            map_im_address_zone = itemView.findViewById(R.id.map_im_address_zone);
            map_im_address = itemView.findViewById(R.id.map_im_address);
            map_im_address_detail = itemView.findViewById(R.id.map_im_address_detail);
            map_im_address_point = itemView.findViewById(R.id.map_im_address_point);

            map_im_address_zone.setOnClickListener(this);
        }

        public void bindData(LocationBean data) {
            if (currentIndex == getLayoutPosition()) map_im_address_point.setVisibility(View.VISIBLE);
            else map_im_address_point.setVisibility(View.INVISIBLE);

            map_im_address.setText(data.title);
            map_im_address_detail.setText(data.address);
        }

        @Override
        public void onClick(View v) {
            currentIndex = getLayoutPosition();
            notifyDataSetChanged();
            if (callback != null) callback.onMapPicked(currentIndex, mDatas.get(currentIndex));
        }
    }

    public interface OnMapPickerCallback {
        /**
         * 用户在地图上选择点的回调
         */
        public void onMapPicked(int position, LocationBean item);
    }
    public void setOnMapPickerCallback(OnMapPickerCallback listener){
        this.callback = listener;
    }
}

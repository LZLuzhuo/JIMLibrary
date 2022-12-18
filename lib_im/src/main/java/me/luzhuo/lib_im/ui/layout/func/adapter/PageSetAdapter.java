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

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.viewpager.widget.PagerAdapter;
import me.luzhuo.lib_im.ui.layout.func.bean.PageEntity;
import me.luzhuo.lib_im.ui.layout.func.bean.PageSetEntity;

public class PageSetAdapter extends PagerAdapter {

    private final ArrayList<PageSetEntity> mPageSetEntityList = new ArrayList<>();

    public ArrayList<PageSetEntity> getPageSetEntityList() {
        return mPageSetEntityList;
    }

    public int getPageSetStartPosition(PageSetEntity pageSetEntity) {
        if (pageSetEntity == null || TextUtils.isEmpty(pageSetEntity.getUuid())) {
            return 0;
        }

        int startPosition = 0;
        for (int i = 0; i < mPageSetEntityList.size(); i++) {
            if (i == mPageSetEntityList.size() - 1 && !pageSetEntity.getUuid().equals(mPageSetEntityList.get(i).getUuid())) {
                return 0;
            }
            if (pageSetEntity.getUuid().equals(mPageSetEntityList.get(i).getUuid())) {
                return startPosition;
            }
            startPosition += mPageSetEntityList.get(i).getPageCount();
        }
        return startPosition;
    }

    public void add(View view) {
        add(mPageSetEntityList.size(), view);
    }

    public void add(int index, View view) {
        PageSetEntity pageSetEntity = new PageSetEntity.Builder()
                .addPageEntity(new PageEntity(view))
                .setShowIndicator(false)
                .build();
        mPageSetEntityList.add(index, pageSetEntity);
    }

    public void add(PageSetEntity pageSetEntity) {
        add(mPageSetEntityList.size(), pageSetEntity);
    }

    public void add(int index, PageSetEntity pageSetEntity) {
        if (pageSetEntity == null) {
            return;
        }
        mPageSetEntityList.add(index, pageSetEntity);
    }

    public PageSetEntity get(int position) {
        return mPageSetEntityList.get(position);
    }

    public void remove(int position) {
        mPageSetEntityList.remove(position);
        notifyData();
    }

    public void notifyData() { }

    public PageEntity getPageEntity(int position) {
        for (PageSetEntity pageSetEntity : mPageSetEntityList) {
            if (pageSetEntity.getPageCount() > position) {
                return (PageEntity) pageSetEntity.getPageEntityList().get(position);
            } else {
                position -= pageSetEntity.getPageCount();
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        int count = 0;
        for (PageSetEntity pageSetEntity : mPageSetEntityList) {
            count += pageSetEntity.getPageCount();
        }
        return count;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = getPageEntity(position).instantiateItem(container, position, null);
        if(view == null){
            return null;
        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }
}

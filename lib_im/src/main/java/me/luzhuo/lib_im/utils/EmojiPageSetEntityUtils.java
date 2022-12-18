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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.sj.emoji.DefEmoticons;
import com.sj.emoji.EmojiBean;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;

import me.luzhuo.lib_im.R;
import me.luzhuo.lib_im.ui.layout.func.adapter.EmoticonsAdapter;
import me.luzhuo.lib_im.ui.layout.func.adapter.PageSetAdapter;
import me.luzhuo.lib_im.ui.layout.func.interfaces.PageViewInstantiateListener;
import me.luzhuo.lib_im.ui.layout.keyboard.EmoticonPageView;
import me.luzhuo.lib_im.ui.layout.keyboard.bean.EmoticonPageEntity;
import me.luzhuo.lib_im.ui.layout.keyboard.bean.EmoticonPageSetEntity;
import me.luzhuo.lib_im.ui.utils.ImageLoader;

/**
 * 填充表情功能页数据
 */
public class EmojiPageSetEntityUtils {

    /**
     * 插入emoji表情集
     */
    public static void addEmojiPageSetEntity(Context context, PageSetAdapter pageSetAdapter, final EmoticonsAdapter.EmoticonClickListener<EmojiBean> emoticonClickListener){
        final ArrayList<EmojiBean> emojiArray = new ArrayList<>();
        Collections.addAll(emojiArray, DefEmoticons.sEmojiArray);
        EmoticonPageSetEntity emojiPageSetEntity = new EmoticonPageSetEntity.Builder<EmojiBean>()
                .setLine(3)
                .setRow(7)
                .setEmoticonList(emojiArray)
                .setIPageViewInstantiateItem(getDefaultEmoticonPageViewInstantiateItem(new EmoticonsAdapter.EmoticonDisplayListener<EmojiBean>() {
                    @Override
                    public void onBindView(int position, ViewGroup parent, EmoticonsAdapter.ViewHolder viewHolder, final EmojiBean emojiBean, final boolean isDelBtn) {
                        if (emojiBean == null && !isDelBtn) return;

                        viewHolder.ly_root.setBackgroundResource(R.drawable.im_emoticon_bg);

                        if (isDelBtn) viewHolder.iv_emoticon.setImageResource(R.mipmap.icon_del);
                        else viewHolder.iv_emoticon.setImageResource(emojiBean.icon);

                        viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (emoticonClickListener != null) emoticonClickListener.onEmoticonClick(emojiBean, EmoticonsAdapter.EMOTICON_CLICK_TEXT, isDelBtn);
                            }
                        });
                    }
                }))
                .setShowDelBtn(EmoticonPageEntity.DelBtnStatus.LAST)
                .setIconUri(ImageLoader.Scheme.DRAWABLE.toUri(DefEmoticons.coverName))
                .build();
        pageSetAdapter.add(emojiPageSetEntity);
    }

    private static PageViewInstantiateListener<EmoticonPageEntity> getDefaultEmoticonPageViewInstantiateItem(EmoticonsAdapter.EmoticonDisplayListener<EmojiBean> emoticonDisplayListener) {
        return getEmoticonPageViewInstantiateItem(EmoticonsAdapter.class, null, emoticonDisplayListener);
    }

    private static PageViewInstantiateListener<EmoticonPageEntity> getEmoticonPageViewInstantiateItem(final Class _class, final EmoticonsAdapter.EmoticonClickListener<EmojiBean> onEmoticonClickListener, final EmoticonsAdapter.EmoticonDisplayListener<EmojiBean> emoticonDisplayListener) {
        return new PageViewInstantiateListener<EmoticonPageEntity>(){
            @Override
            public View instantiateItem(ViewGroup container, int position, EmoticonPageEntity pageEntity) {
                if (pageEntity.getRootView() == null) {
                    EmoticonPageView pageView = new EmoticonPageView(container.getContext());
                    pageView.setNumColumns(pageEntity.getRow());
                    pageEntity.setRootView(pageView);
                    try {
                        EmoticonsAdapter adapter = (EmoticonsAdapter) newInstance(_class, container.getContext(), pageEntity, onEmoticonClickListener);
                        if (emoticonDisplayListener != null) adapter.setOnDisPlayListener(emoticonDisplayListener);
                        pageView.getEmoticonsGridView().setAdapter(adapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return pageEntity.getRootView();
            }
        };
    }

    public static Object newInstance(Class _Class, Object... args) throws Exception {
        return newInstance(_Class, 0, args);
    }

    public static Object newInstance(Class _Class, int constructorIndex, Object... args) throws Exception {
        Constructor cons = _Class.getConstructors()[constructorIndex];
        return cons.newInstance(args);
    }
}

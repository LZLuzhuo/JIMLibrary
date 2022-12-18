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
package me.luzhuo.lib_im.main.detail.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.LocationContent;
import cn.jpush.im.android.api.content.PromptContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VideoContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.enums.MessageStatus;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import me.luzhuo.lib_common_ui.emoji.EmojiManager;
import me.luzhuo.lib_core.DateManager;
import me.luzhuo.lib_core.app.base.CoreBaseApplication;
import me.luzhuo.lib_core.ui.toast.ToastManager;
import me.luzhuo.lib_im.R;
import me.luzhuo.lib_im.main.detail.callback.IMDetailCallback;
import me.luzhuo.lib_im.manager.IMConversationManager;
import me.luzhuo.lib_im.manager.IMDetailMessageManager;
import me.luzhuo.lib_im.manager.IMSendMessager;
import me.luzhuo.lib_im.manager.IMUserManager;
import me.luzhuo.lib_im.manager.enums.ConversationType;

import static me.luzhuo.lib_im.manager.IMCommonConfig.NeedReadReceipt;

public class SingleDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // 分页, 每次的加载数量
    public static final int PAGE_MESSAGE_COUNT = 18;

    private Conversation conv;
    // 消息
    private List<Message> mData;
    public int mOffset = PAGE_MESSAGE_COUNT;
    public boolean mHasLastPage = false;
    private DateManager dateManager = new DateManager();
    private IMDetailCallback callback;
    private Context context;

    public SingleDetailAdapter(Conversation conv) {
        this.conv = conv;
        this.mData = conv.getMessagesFromNewest(0, mOffset);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private static final int Type_Text_Left         = 1,    Type_Text_Right         = 2;
    private static final int Type_Image_Left        = 3,    Type_Image_Right        = 4;
    private static final int Type_File_Left         = 5,    Type_File_Right         = 6;
    private static final int Type_Voice_Left        = 7,    Type_Voice_Right        = 8;
    private static final int Type_Video_Left        = 9,    Type_Video_Right        = 10;
    private static final int Type_Location_Left     = 11,   Type_Location_Right     = 12;
    private static final int Type_Notification      = 13,   Type_Prompt             = 14;
    private static final int Type_VoicePhone_Left   = 15,   Type_VoicePhone_Right   = 16;

    @Override
    public int getItemViewType(int position) {
        final Message msg = mData.get(position);
        final ContentType contentType = msg.getContentType();
        final MessageDirect direct = msg.getDirect();
        // ------ 常规 ------
        if (contentType == ContentType.text) {
            return direct == MessageDirect.receive ? Type_Text_Left : Type_Text_Right;
        } else if (contentType == ContentType.image) {
            return direct == MessageDirect.receive ? Type_Image_Left : Type_Image_Right;
        } else if (contentType == ContentType.file) {
            return direct == MessageDirect.receive ? Type_File_Left : Type_File_Right;
        } else if (contentType == ContentType.voice) {
            return direct == MessageDirect.receive ? Type_Voice_Left : Type_Voice_Right;
        } else if (contentType == ContentType.video) {
            return direct == MessageDirect.receive ? Type_Video_Left : Type_Video_Right;
        } else if (contentType == ContentType.location) {
            return direct == MessageDirect.receive ? Type_Location_Left : Type_Location_Right;
        // ------ 单个界面 ------
        } else if (contentType == ContentType.eventNotification) {
            return Type_Notification;
        } else if (contentType == ContentType.prompt) {
            return Type_Prompt;
        // ------ 自定义数据 ------
        } else if (contentType == ContentType.custom) {
            final CustomContent content = (CustomContent) msg.getContent();
            int type = content.getNumberValue("ws_type") == null ? -1 : content.getNumberValue("ws_type").intValue();
            if (type == 2) { // 语音通话
                return msg.getDirect() == MessageDirect.receive ? Type_VoicePhone_Left : Type_VoicePhone_Right;
            }
        }
        // ------ 其他不匹配的类型 ------
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        switch (viewType) {
            // ------ 常规 ------
            case Type_Text_Left:
                return new TextLeftHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.im_detail_text_left, parent, false));
            case Type_Text_Right:
                return new TextRightHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.im_detail_text_right, parent, false));
            case Type_Image_Left:
                return new ImageLeftHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.im_detail_image_left, parent, false));
            case Type_Image_Right:
                return new ImageRightHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.im_detail_image_right, parent, false));
            case Type_File_Left:
                return new FileLeftHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.im_detail_file_left, parent, false));
            case Type_File_Right:
                return new FileRightHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.im_detail_file_right, parent, false));
            case Type_Voice_Left:
                return new VoiceLeftHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.im_detail_voice_left, parent, false));
            case Type_Voice_Right:
                return new VoiceRightHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.im_detail_voice_right, parent, false));
            case Type_Video_Left:
                return new VideoLeftHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.im_detail_video_left, parent, false));
            case Type_Video_Right:
                return new VideoRightHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.im_detail_video_right, parent, false));
            case Type_Location_Left:
                return new LocationLeftHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.im_detail_location_left, parent, false));
            case Type_Location_Right:
                return new LocationRightHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.im_detail_location_right, parent, false));
            // ------ 单个界面 提示类消息 ------
            case Type_Notification:
                return new NotificationHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.im_detail_default, parent, false));
            case Type_Prompt:
                return new PromptHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.im_detail_prompt, parent, false));
            // ------ 自定义数据 ------
            case Type_VoicePhone_Left:
                return new VoicePhoneLeftHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.im_detail_voice_phone_left, parent, false));
            case Type_VoicePhone_Right:
                return new VoicePhoneRightHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.im_detail_voice_phone_right, parent, false));
            // ------ 其他不匹配的类型 ------
            default:
                return new DefalutHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.im_detail_default, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case Type_Text_Left:        ((TextLeftHolder) holder).bindData(mData.get(position));
                break;
            case Type_Text_Right:       ((TextRightHolder) holder).bindData(mData.get(position));
                break;
            case Type_Image_Left:       ((ImageLeftHolder) holder).bindData(mData.get(position));
                break;
            case Type_Image_Right:      ((ImageRightHolder) holder).bindData(mData.get(position));
                break;
            case Type_File_Left:        ((FileLeftHolder) holder).bindData(mData.get(position));
                break;
            case Type_File_Right:       ((FileRightHolder) holder).bindData(mData.get(position));
                break;
            case Type_Voice_Left:       ((VoiceLeftHolder) holder).bindData(mData.get(position));
                break;
            case Type_Voice_Right:      ((VoiceRightHolder) holder).bindData(mData.get(position));
                break;
            case Type_Video_Left:       ((VideoLeftHolder) holder).bindData(mData.get(position));
                break;
            case Type_Video_Right:      ((VideoRightHolder) holder).bindData(mData.get(position));
                break;
            case Type_Location_Left:    ((LocationLeftHolder) holder).bindData(mData.get(position));
                break;
            case Type_Location_Right:   ((LocationRightHolder) holder).bindData(mData.get(position));
                break;
            case Type_Notification:     ((NotificationHolder) holder).bindData(mData.get(position));
                break;
            case Type_Prompt:           ((PromptHolder) holder).bindData(mData.get(position));
                break;
            case Type_VoicePhone_Left:  ((VoicePhoneLeftHolder) holder).bindData(mData.get(position));
                break;
            case Type_VoicePhone_Right: ((VoicePhoneRightHolder) holder).bindData(mData.get(position));
                break;
            default:                    ((DefalutHolder) holder).bindData(mData.get(position));
                break;
        }
    }

    private int readedColor = 0xFFFFC000;
    private class BaseHolder extends RecyclerView.ViewHolder {
        // 共有的
        public TextView im_detail_time;
        public ImageView im_detail_header;
        public TextView im_detail_left_name;
        public TextView im_detail_right_readed;
        public ProgressBar im_detail_right_progress;
        public ImageButton im_detail_err;

        public BaseHolder(@NonNull View itemView) {
            super(itemView);
            im_detail_time = itemView.findViewById(R.id.im_detail_time);
            im_detail_header = itemView.findViewById(R.id.im_detail_header);
            im_detail_left_name = itemView.findViewById(R.id.im_detail_left_name);
            im_detail_right_progress = itemView.findViewById(R.id.im_detail_right_progress);
            im_detail_err = itemView.findViewById(R.id.im_detail_err);

            im_detail_right_readed = NeedReadReceipt ? (TextView) itemView.findViewById(R.id.im_detail_right_readed) : null;
            if (im_detail_right_readed != null) readedColor = im_detail_right_readed.getCurrentTextColor();
        }

        public void bindData(Message data) {
            IMSendMessager.readed(ConversationType.Single, data); // 已读回执
            time(data, getLayoutPosition(), im_detail_time);
            header(data, im_detail_header);
            isRead(data, im_detail_right_readed);
        }
    }

    public class TextLeftHolder extends BaseHolder {
        public TextView im_detail_content_text;

        public TextLeftHolder(View itemView) {
            super(itemView);
            im_detail_content_text = itemView.findViewById(R.id.im_detail_content_text);
            im_detail_content_text.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (callback != null) callback.onContentLongClick(getLayoutPosition(), v);
                    return true;
                }
            });
        }

        public void bindData(Message data) {
            super.bindData(data);
            final String content = ((TextContent) data.getContent()).getText();

            // 匹配表情
            EmojiManager.getInstance().TextViewFilter(im_detail_content_text, content);
        }
    }

    public class TextRightHolder extends BaseHolder {
        public TextView im_detail_content_text;

        public TextRightHolder(View itemView) {
            super(itemView);
            im_detail_content_text = itemView.findViewById(R.id.im_detail_content_text);
            im_detail_content_text.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (callback != null) callback.onContentLongClick(getLayoutPosition(), v);
                    return true;
                }
            });
            im_detail_err.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    IMDetailMessageManager.reSendTextMessage(context, mData.get(getLayoutPosition()), im_detail_err, im_detail_right_progress);
                }
            });
        }

        public void bindData(Message data) {
            super.bindData(data);
            final String content = ((TextContent) data.getContent()).getText();

            // 匹配表情
            EmojiManager.getInstance().TextViewFilter(im_detail_content_text, content);

            MessageStatus status = data.getStatus();
            if (status == MessageStatus.created) {
                im_detail_err.setVisibility(View.VISIBLE);

                im_detail_right_progress.setVisibility(View.GONE);
                im_detail_right_readed.setVisibility(View.GONE);
            } else if (status == MessageStatus.send_success) {
                im_detail_right_readed.setVisibility(View.VISIBLE);

                im_detail_err.setVisibility(View.GONE);
                im_detail_right_progress.setVisibility(View.GONE);
            } else if (status == MessageStatus.send_fail) {
                im_detail_err.setVisibility(View.VISIBLE);

                im_detail_right_progress.setVisibility(View.GONE);
                im_detail_right_readed.setVisibility(View.GONE);
            } else if (status == MessageStatus.send_going) {
                im_detail_right_progress.setVisibility(View.VISIBLE);

                im_detail_err.setVisibility(View.GONE);
                im_detail_right_readed.setVisibility(View.GONE);
                // 消息正在发送
                data.setOnSendCompleteCallback(new BasicCallback() {
                    @Override
                    public void gotResult(final int status, final String desc) {
                        im_detail_right_progress.setVisibility(View.GONE);
                        if (status == 803008) { // 被对方拉黑; 发送消息失败，发送者已被接收者拉入黑名单，仅限单聊
                            if (callback != null) callback.onInBlackList(IMConversationManager.getTargetId(conv));
                        } else if (status == 0) notifyDataSetChanged();
                        else im_detail_err.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }

    public class ImageLeftHolder extends BaseHolder {
        public ImageView im_detail_content_img;
        public ProgressBar im_detail_right_image_progress;

        public ImageLeftHolder(View itemView) {
            super(itemView);
            im_detail_content_img = itemView.findViewById(R.id.im_detail_content_img);
            im_detail_right_image_progress = itemView.findViewById(R.id.im_detail_right_image_progress);
        }

        public void bindData(final Message data) {
            super.bindData(data);
            final ImageContent content = (ImageContent) data.getContent();

            // 图片预览
            lookImage(im_detail_content_img, getLayoutPosition());
            // 缩略图
            thumbnail(data, im_detail_content_img);

            // 原图下载失败
            if (im_detail_err != null && data.getStatus() == MessageStatus.receive_fail) {
                im_detail_err.setVisibility(View.VISIBLE);
                im_detail_err.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        content.downloadOriginImage(data, new DownloadCompletionCallback() {
                            @Override
                            public void onComplete(int status, String desc, File file) {
                                if (status == 0) {
                                    ToastManager.show(CoreBaseApplication.context, "下载成功");
                                    im_detail_err.setVisibility(View.GONE);
                                    notifyDataSetChanged();
                                } else ToastManager.show(CoreBaseApplication.context, "下载失败");
                            }
                        });
                    }
                });
            }
        }
    }

    public class ImageRightHolder extends BaseHolder {
        public ImageView im_detail_content_img;
        public ProgressBar im_detail_right_image_progress;

        public ImageRightHolder(View itemView) {
            super(itemView);
            im_detail_content_img = itemView.findViewById(R.id.im_detail_content_img);
            im_detail_right_image_progress = itemView.findViewById(R.id.im_detail_right_image_progress);
            im_detail_err.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IMDetailMessageManager.reSendImageMessage(context, mData.get(getLayoutPosition()), im_detail_err, im_detail_right_progress, im_detail_right_image_progress);
                }
            });
        }

        public void bindData(Message data) {
            super.bindData(data);
            final ImageContent content = (ImageContent) data.getContent();

            // 图片预览
            lookImage(im_detail_content_img, getLayoutPosition());
            // 缩略图
            thumbnail(data, im_detail_content_img);

            // 图片下载状态
            MessageStatus status = data.getStatus();
            if (status == MessageStatus.created) { // 0%
                im_detail_content_img.setEnabled(false);
                im_detail_right_readed.setVisibility(View.GONE);
                im_detail_right_progress.setVisibility(View.VISIBLE);
                im_detail_err.setVisibility(View.GONE);
                im_detail_right_image_progress.setVisibility(View.VISIBLE);
            } else if (status == MessageStatus.send_success) { // 100%
                im_detail_content_img.setEnabled(true);
                im_detail_right_readed.setVisibility(View.VISIBLE);
                im_detail_right_progress.setVisibility(View.GONE);
                im_detail_err.setVisibility(View.GONE);
                im_detail_right_image_progress.setVisibility(View.GONE);
            } else if (status == MessageStatus.send_fail) {
                im_detail_content_img.setEnabled(true);
                im_detail_right_readed.setVisibility(View.GONE);
                im_detail_right_progress.setVisibility(View.GONE);
                im_detail_err.setVisibility(View.VISIBLE);
                im_detail_right_image_progress.setVisibility(View.GONE);
            } else if (status == MessageStatus.send_going) {
                im_detail_content_img.setEnabled(false);
                im_detail_right_readed.setVisibility(View.GONE);
                im_detail_right_progress.setVisibility(View.VISIBLE);
                im_detail_err.setVisibility(View.GONE);
                im_detail_right_image_progress.setVisibility(View.VISIBLE);

                // 图片还在发送...
                    /*data.setOnContentUploadProgressCallback(new ProgressUpdateCallback() {
                        @Override
                        public void onProgressUpdate(double v) {
                            int progress = (int)(v * 100);
                        }
                    });*/
                data.setOnSendCompleteCallback(new BasicCallback() {
                    @Override
                    public void gotResult(final int status, String desc) {
                        im_detail_right_progress.setVisibility(View.GONE);
                        im_detail_right_image_progress.setVisibility(View.GONE);
                        if (status == 803008) {
                            if (callback != null) callback.onInBlackList(IMConversationManager.getTargetId(conv));
                        } else if (status == 0) notifyDataSetChanged();
                        else im_detail_err.setVisibility(View.VISIBLE);
                    }
                });

            } else {
                im_detail_content_img.setEnabled(false);
                im_detail_right_readed.setVisibility(View.GONE);
                im_detail_right_progress.setVisibility(View.VISIBLE);
                im_detail_err.setVisibility(View.GONE);
                im_detail_right_image_progress.setVisibility(View.VISIBLE);
            }
        }
    }

    private void lookImage(ImageView im_detail_content_img, final int position) {
        if (im_detail_content_img == null) return;
        im_detail_content_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) callback.onContentClick(position, v);
            }
        });
        im_detail_content_img.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (callback != null) callback.onContentLongClick(position, v);
                return true;
            }
        });
    }

    private void thumbnail(Message data, final ImageView im_detail_content_img) {
        final ImageContent content = (ImageContent) data.getContent();
        final String thumbnailPath = content.getLocalThumbnailPath();
        if (thumbnailPath == null) {
            content.downloadThumbnailImage(data, new DownloadCompletionCallback() {
                @Override
                public void onComplete(int status, String desc, File file) {
                    if (status == 0) Glide.with(context).load(file).error(R.drawable.im_default).into(IMDetailMessageManager.scaleView(im_detail_content_img, file.getAbsolutePath()));
                    else Glide.with(context).load(R.drawable.im_default).into(im_detail_content_img);
                }
            });
        } else Glide.with(context).load(thumbnailPath).into(IMDetailMessageManager.scaleView(im_detail_content_img, thumbnailPath));

        final String localPath = content.getLocalPath();
        if (localPath == null) content.downloadOriginImage(data, null);
    }

    public class FileLeftHolder extends BaseHolder {
        public TextView textView;

        public FileLeftHolder(View itemView) {
            super(itemView);
        }

        public void bindData(Message data) {
            super.bindData(data);
        }
    }

    public class FileRightHolder extends BaseHolder {
        public TextView textView;

        public FileRightHolder(View itemView) {
            super(itemView);
        }

        public void bindData(Message data) {
            super.bindData(data);
        }
    }

    public class VoiceLeftHolder extends BaseHolder {
        public TextView im_detail_content_text;
        public ImageView im_detail_voice_icon;
        public TextView im_detail_voice_time;
        public ImageView im_detail_voice_left_unread;

        public VoiceLeftHolder(View itemView) {
            super(itemView);
            im_detail_content_text = itemView.findViewById(R.id.im_detail_content_text);
            im_detail_voice_icon = itemView.findViewById(R.id.im_detail_voice_icon);
            im_detail_voice_time = itemView.findViewById(R.id.im_detail_voice_time);
            im_detail_voice_left_unread = itemView.findViewById(R.id.im_detail_voice_left_unread);

            // 语音播放
            im_detail_content_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    conv.updateMessageExtra(mData.get(getLayoutPosition()), "isRead", true);
                    im_detail_voice_left_unread.setVisibility(View.GONE);

                    if (callback != null) callback.onVoiceContentClick(getLayoutPosition(), v, im_detail_voice_icon);
                }
            });
            im_detail_content_text.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (callback != null) callback.onContentLongClick(getLayoutPosition(), v);
                    return true;
                }
            });
        }

        public void bindData(Message data) {
            super.bindData(data);
            voiceTimeLength(data, im_detail_voice_time, im_detail_content_text);
            final VoiceContent content = (VoiceContent) data.getContent();

            MessageStatus status = data.getStatus();
            if (status == MessageStatus.receive_success) {
                im_detail_voice_icon.setImageResource(R.mipmap.im_detail_voice_left3);
                // 未读
                if (data.getContent().getBooleanExtra("isRead") == null || !data.getContent().getBooleanExtra("isRead")) {
                    conv.updateMessageExtra(data, "isRead", false);
                    im_detail_voice_left_unread.setVisibility(View.VISIBLE);
                } else if (data.getContent().getBooleanExtra("isRead") != null && data.getContent().getBooleanExtra("isRead")) {
                    im_detail_voice_left_unread.setVisibility(View.GONE);
                }
            } else if (status == MessageStatus.receive_fail) {
                im_detail_voice_icon.setImageResource(R.mipmap.im_detail_voice_left3);
                content.downloadVoiceFile(data, null);
            } else if (status == MessageStatus.receive_going) {}
        }
    }

    public class VoiceRightHolder extends BaseHolder {
        public TextView im_detail_content_text;
        public ImageView im_detail_voice_icon;
        public TextView im_detail_voice_time;
        public ImageView im_detail_voice_left_unread;


        public VoiceRightHolder(View itemView) {
            super(itemView);
            im_detail_content_text = itemView.findViewById(R.id.im_detail_content_text);
            im_detail_voice_icon = itemView.findViewById(R.id.im_detail_voice_icon);
            im_detail_voice_time = itemView.findViewById(R.id.im_detail_voice_time);
            im_detail_voice_left_unread = itemView.findViewById(R.id.im_detail_voice_left_unread);

            // 语音播放
            im_detail_content_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null) callback.onVoiceContentClick(getLayoutPosition(), v, im_detail_voice_icon);
                }
            });
            im_detail_content_text.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (callback != null) callback.onContentLongClick(getLayoutPosition(), v);
                    return true;
                }
            });
        }

        public void bindData(Message data) {
            super.bindData(data);
            voiceTimeLength(data, im_detail_voice_time, im_detail_content_text);


            im_detail_voice_icon.setImageResource(R.mipmap.im_detail_voice_right3);
            final MessageStatus status = data.getStatus();
            if (status == MessageStatus.created) {
                im_detail_right_progress.setVisibility(View.VISIBLE);
                im_detail_err.setVisibility(View.GONE);
                im_detail_right_readed.setVisibility(View.GONE);
            } else if (status == MessageStatus.send_success) {
                im_detail_right_progress.setVisibility(View.GONE);
                im_detail_err.setVisibility(View.GONE);
                im_detail_right_readed.setVisibility(View.VISIBLE);
            } else if (status == MessageStatus.send_fail) {
                im_detail_right_progress.setVisibility(View.GONE);
                im_detail_err.setVisibility(View.GONE);
                im_detail_right_readed.setVisibility(View.VISIBLE);
            } else if (status == MessageStatus.send_going) {
                im_detail_right_progress.setVisibility(View.VISIBLE);
                im_detail_err.setVisibility(View.GONE);
                im_detail_right_readed.setVisibility(View.GONE);

                data.setOnSendCompleteCallback(new BasicCallback() {
                    @Override
                    public void gotResult(int status, String desc) {
                        im_detail_right_progress.setVisibility(View.GONE);
                        if (status == 803008) {
                            if (callback != null) callback.onInBlackList(IMConversationManager.getTargetId(conv));
                        } else if (status == 0) notifyDataSetChanged();
                        else im_detail_err.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }

    // 时间长度
    private void voiceTimeLength(Message data, TextView im_detail_voice_time, TextView im_detail_content_text) {
        if (im_detail_voice_time == null) return;

        final VoiceContent content = (VoiceContent) data.getContent();
        int duration = content.getDuration();
        im_detail_voice_time.setText(String.valueOf(duration).concat("\""));

        // 修改控件长度
        int size = duration + 6;
        final StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < size; i++) { buffer.append(" "); }
        im_detail_content_text.setText(buffer.toString());
    }

    public class VideoLeftHolder extends BaseHolder {
        public ImageView im_detail_content_img;
        public ProgressBar im_detail_right_image_progress;

        public VideoLeftHolder(View itemView) {
            super(itemView);
            im_detail_content_img = itemView.findViewById(R.id.im_detail_content_img);
            im_detail_content_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null) callback.onContentClick(getLayoutPosition(), v);
                }
            });
            im_detail_content_img.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (callback != null) callback.onContentLongClick(getLayoutPosition(), v);
                    return true;
                }
            });
            im_detail_right_image_progress = itemView.findViewById(R.id.im_detail_right_image_progress);
        }

        public void bindData(final Message data) {
            super.bindData(data);
            thumbnailVideo(data, im_detail_content_img);
            final VideoContent content = (VideoContent) data.getContent();

            // 原图下载失败
            if (data.getStatus() == MessageStatus.receive_fail) {
                im_detail_err.setVisibility(View.VISIBLE);
                im_detail_err.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        content.downloadVideoFile(data, new DownloadCompletionCallback() {
                            @Override
                            public void onComplete(int status, String desc, File file) {
                                if (status == 0) {
                                    ToastManager.show(CoreBaseApplication.context, "下载成功");
                                    im_detail_err.setVisibility(View.GONE);
                                    notifyDataSetChanged();
                                } else ToastManager.show(CoreBaseApplication.context, "下载失败");
                            }
                        });
                    }
                });
            }
        }
    }

    public class VideoRightHolder extends BaseHolder {
        public ImageView im_detail_content_img;
        public ProgressBar im_detail_right_image_progress;

        public VideoRightHolder(View itemView) {
            super(itemView);
            im_detail_content_img = itemView.findViewById(R.id.im_detail_content_img);
            im_detail_content_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null) callback.onContentClick(getLayoutPosition(), v);
                }
            });
            im_detail_content_img.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (callback != null) callback.onContentLongClick(getLayoutPosition(), v);
                    return true;
                }
            });
            im_detail_right_image_progress = itemView.findViewById(R.id.im_detail_right_image_progress);
            im_detail_err.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IMDetailMessageManager.reSendTextMessage(context, mData.get(getLayoutPosition()), im_detail_err, im_detail_right_progress);
                }
            });
        }

        public void bindData(Message data) {
            super.bindData(data);
            thumbnailVideo(data, im_detail_content_img);

            MessageStatus status = data.getStatus();
            if (status == MessageStatus.created) { // 0%
                im_detail_content_img.setEnabled(false);
                im_detail_right_readed.setVisibility(View.GONE);
                im_detail_right_progress.setVisibility(View.VISIBLE);
                im_detail_err.setVisibility(View.GONE);
                im_detail_right_image_progress.setVisibility(View.VISIBLE);
            } else if (status == MessageStatus.send_success) { // 100%
                im_detail_content_img.setEnabled(true);
                im_detail_right_readed.setVisibility(View.VISIBLE);
                im_detail_right_progress.setVisibility(View.GONE);
                im_detail_err.setVisibility(View.GONE);
                im_detail_right_image_progress.setVisibility(View.GONE);
            } else if (status == MessageStatus.send_fail) {
                im_detail_content_img.setEnabled(true);
                im_detail_right_readed.setVisibility(View.GONE);
                im_detail_right_progress.setVisibility(View.GONE);
                im_detail_err.setVisibility(View.VISIBLE);
                im_detail_right_image_progress.setVisibility(View.GONE);
            } else if (status == MessageStatus.send_going) {
                im_detail_content_img.setEnabled(false);
                im_detail_right_readed.setVisibility(View.GONE);
                im_detail_right_progress.setVisibility(View.VISIBLE);
                im_detail_err.setVisibility(View.GONE);
                im_detail_right_image_progress.setVisibility(View.VISIBLE);

                // 图片还在发送...
                    /*data.setOnContentUploadProgressCallback(new ProgressUpdateCallback() {
                        @Override
                        public void onProgressUpdate(double v) {
                            int progress = (int)(v * 100);
                        }
                    });*/
                data.setOnSendCompleteCallback(new BasicCallback() {
                    @Override
                    public void gotResult(final int status, String desc) {
                        im_detail_right_progress.setVisibility(View.GONE);
                        im_detail_right_image_progress.setVisibility(View.GONE);
                        if (status == 803008) {
                            if (callback != null) callback.onInBlackList(IMConversationManager.getTargetId(conv));
                        } else if (status == 0) notifyDataSetChanged();
                        else im_detail_err.setVisibility(View.VISIBLE);
                    }
                });

            } else {
                im_detail_content_img.setEnabled(false);
                im_detail_right_readed.setVisibility(View.GONE);
                im_detail_right_progress.setVisibility(View.VISIBLE);
                im_detail_err.setVisibility(View.GONE);
                im_detail_right_image_progress.setVisibility(View.VISIBLE);
            }
        }
    }

    // 缩略图
    private void thumbnailVideo(Message data, final ImageView im_detail_content_img) {
        final VideoContent content = (VideoContent) data.getContent();
        final String thumbnailPath = content.getThumbLocalPath();
        if (thumbnailPath == null) {
            content.downloadThumbImage(data, new DownloadCompletionCallback() {
                @Override
                public void onComplete(int status, String desc, File file) {
                    if (status == 0) Glide.with(context).load(file).error(R.drawable.im_default).into(im_detail_content_img);
                    else Glide.with(context).load(R.drawable.im_default).into(im_detail_content_img);
                }
            });
        } else Glide.with(context).load(thumbnailPath).into(im_detail_content_img);

        final String localPath = content.getVideoLocalPath();
        if (localPath == null) content.downloadVideoFile(data, null);
    }

    public class LocationLeftHolder extends BaseHolder {
        public ImageView im_detail_map;
        public TextView im_detail_title;
        public TextView im_detail_street;;

        public LocationLeftHolder(View itemView) {
            super(itemView);
            im_detail_map = itemView.findViewById(R.id.im_detail_map);
            im_detail_title = itemView.findViewById(R.id.im_detail_title);
            im_detail_street = itemView.findViewById(R.id.im_detail_street);
        }

        public void bindData(Message data) {
            super.bindData(data);
            streetText(data, im_detail_map, im_detail_title, im_detail_street, getLayoutPosition());
            final LocationContent content = (LocationContent) data.getContent();

            String imageUrl = content.getStringExtra("url");
            MessageStatus status = data.getStatus();
            if (status == MessageStatus.receive_going);
            else if (status == MessageStatus.receive_success) {
                // 极光没有提供传输截图的方法
                if (TextUtils.isEmpty(imageUrl)) imageUrl = "http://api.map.baidu.com/staticimage?width=225&height=150&center=" + content.getLongitude() + "," + content.getLatitude() + "&zoom=17";
                Glide.with(context).load(imageUrl).error(R.drawable.im_default).into(im_detail_map);
            }
            else if (status == MessageStatus.receive_fail) ;
        }
    }

    public class LocationRightHolder extends BaseHolder {
        public ImageView im_detail_map;
        public TextView im_detail_title;
        public TextView im_detail_street;

        public LocationRightHolder(View itemView) {
            super(itemView);
            im_detail_map = itemView.findViewById(R.id.im_detail_map);
            im_detail_title = itemView.findViewById(R.id.im_detail_title);
            im_detail_street = itemView.findViewById(R.id.im_detail_street);
        }

        public void bindData(Message data) {
            super.bindData(data);
            streetText(data, im_detail_map, im_detail_title, im_detail_street, getLayoutPosition());
            final LocationContent content = (LocationContent) data.getContent();

            String imagePath = content.getStringExtra("path");
            Glide.with(context).load(imagePath).error(R.drawable.im_default).into(im_detail_map);
            MessageStatus status = data.getStatus();
            if (status == MessageStatus.created) {
                im_detail_right_readed.setVisibility(View.GONE);
                im_detail_right_progress.setVisibility(View.VISIBLE);
                im_detail_err.setVisibility(View.GONE);
            } else if (status == MessageStatus.send_going) {
                im_detail_right_readed.setVisibility(View.GONE);
                im_detail_right_progress.setVisibility(View.VISIBLE);
                im_detail_err.setVisibility(View.GONE);

                data.setOnSendCompleteCallback(new BasicCallback() {
                    @Override
                    public void gotResult(final int status, final String desc) {
                        im_detail_right_progress.setVisibility(View.GONE);
                        if (status == 803008) {
                            if (callback != null) callback.onInBlackList(IMConversationManager.getTargetId(conv));
                        } else if (status == 0) notifyDataSetChanged();
                        else im_detail_err.setVisibility(View.VISIBLE);
                    }
                });

            } else if (status == MessageStatus.send_success) {
                im_detail_right_readed.setVisibility(View.VISIBLE);
                im_detail_right_progress.setVisibility(View.GONE);
                im_detail_err.setVisibility(View.GONE);
            } else if (status == MessageStatus.send_fail) {
                im_detail_right_readed.setVisibility(View.GONE);
                im_detail_right_progress.setVisibility(View.GONE);
                im_detail_err.setVisibility(View.VISIBLE);
            }
        }
    }

    private void streetText(Message data, ImageView im_detail_map, TextView im_detail_title, TextView im_detail_street, final int position) {
        final LocationContent content = (LocationContent) data.getContent();
        String title = content.getStringExtra("title");
        String address = content.getAddress();

        if (im_detail_map != null) {
            im_detail_map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null) callback.onContentClick(position, v);
                }
            });
            im_detail_map.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (callback != null) callback.onContentLongClick(position, v);
                    return true;
                }
            });
        }

        if (!TextUtils.isEmpty(title)) {
            im_detail_title.setVisibility(View.VISIBLE);
            im_detail_title.setText(title);
        } else im_detail_title.setVisibility(View.GONE);
        im_detail_street.setText(address);
    }

    public class NotificationHolder extends BaseHolder {
        public TextView textView;

        public NotificationHolder(View itemView) {
            super(itemView);
        }

        public void bindData(Message data) {
            super.bindData(data);

        }
    }

    /**
     * 提示: 测回消息
     */
    public class PromptHolder extends BaseHolder {
        public TextView im_detail_prompt;

        public PromptHolder(View itemView) {
            super(itemView);
            im_detail_prompt = itemView.findViewById(R.id.im_detail_prompt);
        }

        public void bindData(Message data) {
            super.bindData(data);

            String promptText = ((PromptContent) data.getContent()).getPromptText();
            im_detail_prompt.setText(promptText);
            im_detail_time.setVisibility(View.GONE);
        }
    }

    public class VoicePhoneLeftHolder extends BaseHolder {
        public TextView im_detail_content_text;

        public VoicePhoneLeftHolder(View itemView) {
            super(itemView);
            im_detail_content_text = itemView.findViewById(R.id.im_detail_content_text);
        }

        public void bindData(Message data) {
            super.bindData(data);
            final CustomContent content = (CustomContent) data.getContent();

            String ws_voiceCallTime = content.getStringValue("ws_voiceCallTime");
            String content_text = content.getStringValue("content_text");
            int ws_voiceCallType = content.getNumberValue("ws_voiceCallType").intValue();

            if(ws_voiceCallType == 1) im_detail_content_text.setText("对方已拒绝");
            else if(ws_voiceCallType == 2) im_detail_content_text.setText("对方已取消");
            else if(ws_voiceCallType == 3) im_detail_content_text.setText("通话时长".concat(String.valueOf(ws_voiceCallTime)));
        }
    }

    public class VoicePhoneRightHolder extends BaseHolder {
        public TextView im_detail_content_text;

        public VoicePhoneRightHolder(View itemView) {
            super(itemView);
            im_detail_content_text = itemView.findViewById(R.id.im_detail_content_text);
        }

        public void bindData(Message data) {
            super.bindData(data);
            final CustomContent content = (CustomContent) data.getContent();

            String ws_voiceCallTime = content.getStringValue("ws_voiceCallTime");
            String content_text = content.getStringValue("content_text");
            int ws_voiceCallType = content.getNumberValue("ws_voiceCallType").intValue();

            if(ws_voiceCallType == 1) im_detail_content_text.setText("已拒绝");
            else if(ws_voiceCallType == 2) im_detail_content_text.setText("已取消");
            else if(ws_voiceCallType == 3) im_detail_content_text.setText("通话时长".concat(String.valueOf(ws_voiceCallTime)));
        }
    }

    public class DefalutHolder extends BaseHolder {
        public TextView textView;

        public DefalutHolder(View itemView) {
            super(itemView);
        }

        public void bindData(Message data) {
            super.bindData(data);

        }
    }

    // ================================= 通用的方法 ↓ =================================
    /**
     * 显示时间
     */
    private void time(Message data, int position, TextView im_detail_time) {
        final long nowDate = data.getCreateTime();
        if (mOffset == 18) {
            if (position % 18 == 0) {
                showAndHindTime(im_detail_time, nowDate);
            } else {
                long lastDate = mData.get(position - 1).getCreateTime();
                // 如果两条消息之间的间隔超过五分钟则显示时间
                if (nowDate - lastDate > 300 * 1000)
                    showAndHindTime(im_detail_time, nowDate);
                else showAndHindTime(im_detail_time, 0);
            }
        } else {
            if (position == 0 || position == mOffset || (position - mOffset) % 18 == 0) {
                showAndHindTime(im_detail_time, nowDate);
            } else {
                long lastDate = mData.get(position - 1).getCreateTime();
                if (nowDate - lastDate > 300 * 1000)
                    showAndHindTime(im_detail_time, nowDate);
                else showAndHindTime(im_detail_time, 0);
            }
        }
    }

    /**
     * 显示头像
     */
    private void header(final Message data, final ImageView im_detail_header) {
        final UserInfo userInfo = data.getFromUser(); // 发送者的个人信息
        if (im_detail_header != null) {
            if (userInfo != null && !TextUtils.isEmpty(userInfo.getAvatar())) {
                userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int status, String desc, Bitmap bitmap) {
                        if (status == 0) Glide.with(context).load(bitmap).error(R.drawable.im_default).into(im_detail_header);
                        else Glide.with(context).load(R.drawable.im_default).into(im_detail_header);
                    }
                });
            } else {
                Glide.with(context).load(R.drawable.im_default).into(im_detail_header);
            }

            // 头像的点击事件
            im_detail_header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (data.getDirect() == MessageDirect.send) {
                        if (callback != null) callback.onHeaderClick(true, JMessageClient.getMyInfo().getUserName());
                    } else {
                        if (callback != null && userInfo != null) callback.onHeaderClick(false, userInfo.getUserName());
                    }
                }
            });
        }
    }

    /**
     * 已读 与 未读
     */
    private void isRead(final Message data, TextView im_detail_right_readed){
        final UserInfo userInfo = data.getFromUser(); // 发送者的个人信息
        if (im_detail_right_readed != null && data.getDirect() == MessageDirect.send && !data.getContentType().equals(ContentType.prompt) && data.getContentType() != ContentType.custom && data.getContentType() != ContentType.video) {
            if (data.getUnreceiptCnt() == 0) {
                if(userInfo != null && !((UserInfo) data.getTargetInfo()).getUserName().equals(IMUserManager.getMyUserName())) im_detail_right_readed.setText("已读");
                im_detail_right_readed.setTextColor(0xFF999999);
            } else {
                if(userInfo != null && !((UserInfo) data.getTargetInfo()).getUserName().equals(IMUserManager.getMyUserName())) im_detail_right_readed.setText("未读");
                im_detail_right_readed.setTextColor(readedColor);
            }
        }
    }
    // ================================== 通用的方法 ↑ ==================================

    private void showAndHindTime(TextView textView, long time) {
        if (textView == null) return;

        if (time > 0) {
            String timeFormat = dateManager.conversationFormat(time);
            textView.setText(timeFormat);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    /**
     * 添加消息到末尾
     * @param message
     */
    public void addMsgSendToList(Message message) {
        mData.add(0, message);
        notifyDataSetChanged();
    }

    /**
     * 清空所有聊天信息
     */
    public void clearMsgList() {
        mData.clear();
        notifyDataSetChanged();
    }

    /**
     * 删除指定的消息
     */
    public void deleteMessage(Message message) {
        mData.remove(message);
        notifyDataSetChanged();
    }

    /**
     * 加载下一页的数据
     */
    public void loadMoreMsg() {
        List<Message> msgList = conv.getMessagesFromNewest(mData.size(), PAGE_MESSAGE_COUNT);
        if (msgList == null || msgList.size() <= 0) {
            mOffset = 0;
            mHasLastPage = false;
            return;
        }

        // 把数据到过来在添加到集合中
        mData.addAll(msgList);
        mOffset = msgList.size();
        mHasLastPage = true;

        notifyDataSetChanged();
    }

    public Message getMessage(int position) {
        try {
            return mData.get(position);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setOnIMDetailCallbacklistener(IMDetailCallback callbacklistener) {
        this.callback = callbacklistener;
    }

    /**
     * 更新已读状态
     */
    public void updateReaded(long serverMsgId, int unReceiptCount) {
        for (Message data : mData) {
            if (data.getServerMessageId() == serverMsgId) data.setUnreceiptCnt(unReceiptCount);
        }
        notifyDataSetChanged();
    }

    /**
     * 更新新收到的消息
     */
    public void updateMessage(Message message) {
        if (mData.size() <= 0) {
            mData.add(message);
        } else {
            Message lastMessage = mData.get(0);
            if (lastMessage.getId() != message.getId()) mData.add(0, message);
        }
        notifyDataSetChanged();
    }
}

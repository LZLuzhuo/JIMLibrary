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
package me.luzhuo.lib_im.manager;

import android.util.Log;

import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetBlacklistCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

public class IMUserManager {
    public interface IUserCallback {
        public void onSuccess();
        public void onError(String err);
    }

    public class IBackListCallback {
        public void onAddSucess(){}
        public void onAddError(String err){}

        public void onRemoveSucess(){}
        public void onRemoveError(String err){}

        public void onGetListSucess(List<UserInfo> list){}
        public void onGetListError(String err){}
    }

    public interface UserInfoCallback {
        public void onUserInfo(UserInfo userInfo);
        public void onError(String err);
    }

    /**
     * 用户登录, 如果用户未注册会自动去注册
     * @param username im username
     * @param password im password
     * @param userCallback IUserCallback
     */
    public static void login(final boolean isAutoRegister, final String username, final String password, final IUserCallback userCallback) {
        // 1. 去登录
        JMessageClient.login(username, password, new BasicCallback() {
            @Override
            public void gotResult(int responseCode, String message1) {
                Log.e("TAG", "" + responseCode + " : " + message1);
                if (responseCode == 0) {  // 登录成功
                    if(userCallback != null) userCallback.onSuccess();

                } else if (responseCode == 801003) { // 用户未注册
                    if (!isAutoRegister) {
                        if (userCallback != null) userCallback.onError(message1);
                        return;
                    }

                    // 2. 去注册
                    // 注册. 注册成功之后不会自动登录, 需要主动登录.
                    JMessageClient.register(username, password, new BasicCallback() {
                        @Override
                        public void gotResult(int responseCode, String message2) {
                            if (responseCode != 0) {
                                if(userCallback != null) userCallback.onError(message2);
                                return;
                            }

                            // 3. 注册成功, 去登录
                            JMessageClient.login(username, password, new BasicCallback() {
                                @Override
                                public void gotResult(int i, String message3) {
                                    if (i == 0) {
                                        if (userCallback != null) userCallback.onSuccess();
                                    } else {
                                        if (userCallback != null) userCallback.onError(message3);
                                    }
                                }
                            });
                        }
                    });
                } else {
                    if(userCallback != null) userCallback.onError(message1);
                }
            }
        });
    }

    /**
     * 退出登录
     */
    public static void logout() {
        JMessageClient.logout();
    }

    /**
     * 获取当前登录的username, 如果未登录, 可能返回null
     */
    public static String getMyUserName() {
        return JMessageClient.getMyInfo().getUserName();
    }

    public static void getUserInfo(String userName, final UserInfoCallback callback) {
        JMessageClient.getUserInfo(userName, new GetUserInfoCallback() {
            @Override
            public void gotResult(int status, String message, UserInfo userInfo) {
                if (status == 0) {
                    if(callback != null) callback.onUserInfo(userInfo);
                } else {
                    if(callback != null) callback.onError(message);
                }
            }
        });
    }

    // ============================================= 黑名单 ↓ =============================================
    /**
     * 将对方加入黑名单列表, 加入之后, 我方依然能给对方发消息, 但对方给我发消息时会返回指定错误码, 发送消息失败.
     * @param usernames im username list
     * @param callback IBackListCallback
     */
    public static void addBlackList(List<String> usernames, final IBackListCallback callback) {
        JMessageClient.addUsersToBlacklist(usernames, new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                if(i == 0) {
                    if (callback != null) callback.onAddSucess();
                } else {
                    if (callback != null) callback.onAddError(s);
                }
            }
        });
    }

    /**
     * 将用户移出黑名单
     */
    public static void removeBlackList(List<String> usernames, final IBackListCallback callback) {
        JMessageClient.delUsersFromBlacklist(usernames, new BasicCallback(){
            @Override
            public void gotResult(int i, String s) {
                if(i == 0) {
                    if (callback != null) callback.onRemoveSucess();
                } else {
                    if (callback != null) callback.onRemoveError(s);
                }
            }
        });
    }

    /**
     * 获取黑名单列表
     */
    public static void getBackList(final IBackListCallback callback) {
        JMessageClient.getBlacklist(new GetBlacklistCallback(){
            @Override
            public void gotResult(int i, String s, List<UserInfo> list) {
                if(i == 0) {
                    if (callback != null) callback.onGetListSucess(list);
                } else {
                    if (callback != null) callback.onGetListError(s);
                }
            }
        });
    }
    // ============================================= 黑名单 ↑ =============================================
}

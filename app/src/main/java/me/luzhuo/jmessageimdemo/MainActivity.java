package me.luzhuo.jmessageimdemo;

import androidx.appcompat.app.AppCompatActivity;
import me.luzhuo.lib_core.ui.toast.ToastManager;
import me.luzhuo.lib_im.manager.IMConversationManager;
import me.luzhuo.lib_im.manager.IMRTCManager;
import me.luzhuo.lib_im.manager.IMUserManager;
import me.luzhuo.lib_permission.Permission;
import me.luzhuo.lib_permission.PermissionCallback;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText account;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        account = findViewById(R.id.account);
        password = findViewById(R.id.password);
    }

    public void login(View view) {
        // 1. 登录
        final String accountStr = account.getText().toString().trim();
        final String passwordStr = password.getText().toString().trim();

        IMUserManager.login(true, accountStr, passwordStr, new IMUserManager.IUserCallback() {
            @Override
            public void onSuccess() {
                IMListActivity.start(MainActivity.this);
            }

            @Override
            public void onError(String err) {
                ToastManager.show(MainActivity.this, err);
            }
        });
    }

    public void logout(View view) {
        IMUserManager.logout();
    }
}
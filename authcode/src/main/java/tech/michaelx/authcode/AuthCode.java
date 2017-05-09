package tech.michaelx.authcode;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

/**
 * Author: michaelx
 * Create: 17-3-27.
 * <p>
 * Endcode: UTF-8
 * <p>
 * Blog:http://blog.csdn.net/xiong_it | https://xiong-it.github.io
 * github:https://github.com/xiong-it
 * <p>
 * Description: 自动填写短信中的验证码.
 */

public class AuthCode {
    private Context mContext;
    private CodeConfig mCodeConfig;
    private Intent mAuthcodeIntent;

    private Handler mHandler;

    static class AuthCodeHandler extends Handler {
        private Reference<TextView> mTextViewRef;

        public AuthCodeHandler(TextView codeView) {
            this.mTextViewRef = new SoftReference<>(codeView);
        }

        @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            TextView mAuthCode = mTextViewRef.get();
            if (mAuthCode == null) {
                return;
            }
            if (!mAuthCode.getText().toString().isEmpty()) return;

            switch (msg.what) {
                case ReadSmsService.OBSERVER_SMS_CODE_MSG:
                    mAuthCode.setText((String) msg.obj);
                    sInstance = null;
                    break;
                case ReadSmsService.RECEIVER_SMS_CODE_MSG:
                    mAuthCode.setText((String) msg.obj);
                    sInstance = null;
                    break;
                default:
                    break;
            }
        }
    }

    private static AuthCode sInstance;

    private AuthCode() {}

    public static synchronized AuthCode getInstance() {
        if (sInstance == null) {
            sInstance = new AuthCode();
        }
        return sInstance;
    }

    public AuthCode with(Context context) {
        mContext = context;
        return this;
    }

    public AuthCode config(CodeConfig config) {
        if (mContext == null) {
            throw new NullPointerException("mContext is null.Please call with(Context) first.");
        }
        mCodeConfig = config;
        return this;
    }

    public void into(TextView codeView) {
        if (mCodeConfig == null) {
            throw new NullPointerException("mCodeConfig is null.Please call config(CodeConfig) before this.");
        }
        mHandler = new AuthCodeHandler(codeView);
        if (checkPermission()) {
            startReadSmsService();
        } else {
            Log.e("AutoInputAuthCode", "Please allow app to read your sms for auto input auth code.");
        }
    }

    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 开启短信验证码获取服务
     */
    private void startReadSmsService() {
        mAuthcodeIntent = new Intent(mContext, ReadSmsService.class);
        mAuthcodeIntent.putExtra(ReadSmsService.EXTRAS_MESSAGER, new Messenger(mHandler));
        mAuthcodeIntent.putExtra(ReadSmsService.EXTRAS_COFIG, mCodeConfig);
        mContext.startService(mAuthcodeIntent);
    }
}

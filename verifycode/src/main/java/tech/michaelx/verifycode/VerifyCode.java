package tech.michaelx.verifycode;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.RequiresApi;
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

public class VerifyCode {
    private Context mContext;
    private CodeConfig mCodeConfig;
    private TextView mCodeView;
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
                    break;
                case ReadSmsService.RECEIVER_SMS_CODE_MSG:
                    mAuthCode.setText((String) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    private static VerifyCode sInstance;

    private VerifyCode() {}

    public static synchronized VerifyCode getInstance() {
        if (sInstance == null) {
            sInstance = new VerifyCode();
        }
        return sInstance;
    }

    public VerifyCode with(Context context) {
        mContext = context;
        return this;
    }

    public VerifyCode cofig(CodeConfig config) {
        mCodeConfig = config;
        return this;
    }

    public void into(TextView codeView) {
        mCodeView = codeView;

        mHandler = new AuthCodeHandler(mCodeView);
        startReadSmsService();
    }

    /**
     * 开启短信验证码获取服务
     */
    private void startReadSmsService() {
        mAuthcodeIntent = new Intent(mContext, ReadSmsService.class);
        mAuthcodeIntent.putExtra(ReadSmsService.EXTRAS_MESSAGER, new Messenger(mHandler));
        mAuthcodeIntent.putExtra(ReadSmsService.EXTRAS_MESSAGER, mCodeConfig);
        mContext.startService(mAuthcodeIntent);
    }
}

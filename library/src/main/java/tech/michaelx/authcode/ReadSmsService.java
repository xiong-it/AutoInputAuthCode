package tech.michaelx.authcode;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 短信验证码读取服务
 */
public class ReadSmsService extends Service {

    private static final String TAG = ReadSmsService.class.getSimpleName();
    private static final String SMS_RECEIVED_ACTION = Telephony.Sms.Intents.SMS_RECEIVED_ACTION;// 接收到短信时的action
    private static final String SMS_INBOX_URI = "content://sms/inbox";//API level>=23,可直接使用Telephony.Sms.Inbox.CONTENT_URI
    private static final String SMS_URI = "content://sms";//API level>=23,可直接使用Telephony.Sms.CONTENT_URI
    static final String[] PROJECTION = new String[]{
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
    };

    public static final String EXTRAS_MESSAGER = "tech.michaelx.verifycode.ReadSmsService.EXTRAS_MESSAGER";
    public static final String EXTRAS_COFIG = "tech.michaelx.verifycode.ReadSmsService.EXTRAS_COFIG";

    public static final int RECEIVER_SMS_CODE_MSG = 0;
    public static final int OBSERVER_SMS_CODE_MSG = 1;

    private Messenger mMessenger;
    private CodeConfig mCodeConfig;

    /**
     * 读取未读短信，用以填写验证码
     */
    private ContentObserver mReadSmsObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Cursor cursor = getContentResolver().query(Uri.parse(SMS_INBOX_URI), PROJECTION,
                    Telephony.Sms.READ + "=?", new String[]{"0"}, Telephony.Sms.Inbox.DEFAULT_SORT_ORDER);
            getSmsCodeFromObserver(cursor);
        }
    };

    /**
     * 短信广播接收者
     */
    private BroadcastReceiver mReadSmsCodeReceiver = new ReadSmsCodeReceiver();

    private class ReadSmsCodeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SMS_RECEIVED_ACTION)) {
                getSmsCodeFromReceiver(intent);
            }
        }
    }

    public ReadSmsService() {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                Log.e(TAG, "bundle = null");
            } else {
                register();

                mMessenger = (Messenger) bundle.get(EXTRAS_MESSAGER);
                mCodeConfig = bundle.getParcelable(EXTRAS_COFIG);
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegister();
    }

    /**
     * 包访问级别:提高性能
     * 从接收者中得到短信验证码
     *
     * @param intent
     */
    void getSmsCodeFromReceiver(Intent intent) {
        SmsMessage[] messages = null;
        if (Build.VERSION.SDK_INT >= 19) {
            messages = android.provider.Telephony.Sms.Intents.getMessagesFromIntent(intent);
            if (messages == null) return;
        } else {
            messages = getSmsUnder19(intent);
            if (messages == null) return;
        }

        if (messages.length > 0) {
            for (int i = 0; i < messages.length; i++) {
                SmsMessage sms = messages[i];
                String smsSender = sms.getOriginatingAddress();
                String smsBody = sms.getMessageBody();
                if (checkSmsSender(smsSender) && checkSmsBody(smsBody)) {
                    String smsCode = parseSmsBody(smsBody);
                    sendMsg2Register(OBSERVER_SMS_CODE_MSG, smsCode);
                    break;
                }
            }
        }
    }

    @Nullable
    private SmsMessage[] getSmsUnder19(Intent intent) {
        SmsMessage[] messages;
        Bundle bundle = intent.getExtras();
        // 相关链接:https://developer.android.com/reference/android/provider/Telephony.Sms.Intents.html#SMS_DELIVER_ACTION
        Object[] pdus = (Object[]) bundle.get("pdus");

        if ((pdus == null) || (pdus.length == 0)) {
            return null;
        }

        messages = new SmsMessage[pdus.length];
        for (int i = 0; i < pdus.length; i++) {
            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
        }
        return messages;
    }

    /**
     * 包访问级别:提高性能
     * 从内容观察者得到短信验证码
     *
     * @param cursor
     */
    void getSmsCodeFromObserver(Cursor cursor) {
        if (cursor == null) return;

        while (cursor.moveToNext()) {
            String address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
            String smsBody = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
            if (checkSmsSender(address) && checkSmsBody(smsBody)) {
                String smsCode = parseSmsBody(smsBody);
                sendMsg2Register(RECEIVER_SMS_CODE_MSG, smsCode);
                break;
            }
        }

        closeCursor(cursor);
    }

    private void closeCursor(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) return;

        if (!cursor.isClosed()) {
            cursor.close();
        }
    }

    /**
     * @param smsBody
     * @return
     */
    private boolean checkSmsBody(String smsBody) {
        if (!TextUtils.isEmpty(mCodeConfig.getSmsStart()) && !TextUtils.isEmpty(mCodeConfig.getSmsContains())) {
            return smsBody.startsWith(mCodeConfig.getSmsStart()) && smsBody.contains(mCodeConfig.getSmsContains());
        } else if (!TextUtils.isEmpty(mCodeConfig.getSmsStart())) {
            return smsBody.startsWith(mCodeConfig.getSmsStart());
        } else if (!TextUtils.isEmpty(mCodeConfig.getSmsContains())) {
            return smsBody.contains(mCodeConfig.getSmsContains());
        } else {
            return true;
        }
    }

    /**
     * @param smsSender
     * @return
     */
    private boolean checkSmsSender(String smsSender) {
        if (mCodeConfig.getSmsFrom() != 0) {
            return smsSender.equals(String.valueOf(mCodeConfig.getSmsFrom()));
        }
        return smsSender.contains(String.valueOf(mCodeConfig.getSmsFromStart()));
    }

    /**
     * 注册广播接收者，内容观察者
     */
    private void register() {
        registerReceiver();
        registerObserver();
    }

    /**
     * 注册广播接收者
     */
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(SMS_RECEIVED_ACTION);
        filter.addAction(SMS_RECEIVED_ACTION);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(mReadSmsCodeReceiver, filter);
    }

    /**
     * 注册内容观察者
     */
    private void registerObserver() {
        getContentResolver().registerContentObserver(Uri.parse(SMS_URI), true, mReadSmsObserver);
    }

    /**
     * 注销广播接收者，内容观察者
     */
    private void unRegister() {
        unRegisterReceiver();
        unRegisterObserver();
    }

    /**
     * 注销广播接收者
     */
    private void unRegisterReceiver() {
        if (mReadSmsCodeReceiver == null) return;

        unregisterReceiver(mReadSmsCodeReceiver);
        mReadSmsCodeReceiver = null;
    }

    /**
     * 注销内容观察者
     */
    private void unRegisterObserver() {
        if (mReadSmsObserver == null) return;

        getContentResolver().unregisterContentObserver(mReadSmsObserver);
        mReadSmsObserver = null;
    }

    /**
     * 解析短信得到验证码
     *
     * @param smsBody
     * @return
     */
    private String parseSmsBody(String smsBody) {
        int len = mCodeConfig.getCodeLen();
        String regex = new String("(\\d{" + len + "})");// 匹配规则为短信中的连续数字
        String smsCode = "";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(smsBody);

        while (matcher.find()) {
            smsCode = matcher.group(0);
        }
        return smsCode;
    }

    /**
     * 发送消息到注册界面
     *
     * @param msgWhat
     * @param msgObj
     */
    private void sendMsg2Register(int msgWhat, String msgObj) {
        if (mMessenger != null) {
            Message msg = Message.obtain();
            msg.what = msgWhat;
            msg.obj = msgObj;

            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                stopSelf();
            }
        }
    }

}

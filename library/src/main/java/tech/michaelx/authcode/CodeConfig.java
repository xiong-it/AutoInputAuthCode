package tech.michaelx.authcode;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Author: michaelx
 * Create: 17-3-27.
 * <p>
 * Endcode: UTF-8
 * <p>
 * Blog:http://blog.csdn.net/xiong_it | https://xiong-it.github.io
 * github:https://github.com/xiong-it
 * <p>
 * Description: here is the description for this file.
 */

public class CodeConfig implements Parcelable {

    private int mCodeLen = 4;
    private String mSmsStart;
    private String mSmsContains;
    private int mSmsFrom;
    private int mSmsFromStart = 1069;

    public int getCodeLen() {
        return mCodeLen;
    }

    private void setCodeLen(int codeLen) {
        mCodeLen = codeLen;
    }

    public String getSmsStart() {
        return mSmsStart;
    }

    private void setSmsStart(String smsStart) {
        mSmsStart = smsStart;
    }

    public String getSmsContains() {
        return mSmsContains;
    }

    private void setSmsContains(String smsContains) {
        mSmsContains = smsContains;
    }

    public int getSmsFrom() {
        return mSmsFrom;
    }

    private void setSmsFrom(int smsFrom) {
        mSmsFrom = smsFrom;
    }

    public int getSmsFromStart() {
        return mSmsFromStart;
    }

    private void setSmsFromStart(int smsFromStart) {
        mSmsFromStart = smsFromStart;
    }

    public static class Builder {
        private int mSmsFrom;
        private int mSmsFromStart;
        private int mCodeLen;
        private String mSmsStart;
        private String mSmsContains;

        public Builder smsFrom(int phoneNumber) {
            mSmsFrom = phoneNumber;
            return this;
        }

        public Builder smsFromStart(int numberStart) {
            mSmsFromStart = numberStart;
            return this;
        }

        public Builder codeLength(int len) {
            mCodeLen = len;
            return this;
        }

        public Builder smsStartWith(String start) {
            mSmsStart = start;
            return this;
        }

        public Builder smsContains(String contains) {
            mSmsContains = contains;
            return this;
        }

        public CodeConfig build() {
            CodeConfig codeConfig = new CodeConfig();
            codeConfig.setSmsFrom(mSmsFrom);
            codeConfig.setSmsFromStart(mSmsFromStart);
            codeConfig.setCodeLen(mCodeLen);
            codeConfig.setSmsStart(mSmsStart);
            codeConfig.setSmsContains(mSmsContains);
            return codeConfig;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCodeLen);
        dest.writeString(this.mSmsStart);
        dest.writeString(this.mSmsContains);
        dest.writeInt(this.mSmsFrom);
        dest.writeInt(this.mSmsFromStart);
    }

    public CodeConfig() {
    }

    protected CodeConfig(Parcel in) {
        int tmpMType = in.readInt();
        this.mCodeLen = in.readInt();
        this.mSmsStart = in.readString();
        this.mSmsContains = in.readString();
        this.mSmsFrom = in.readInt();
        this.mSmsFromStart = in.readInt();
    }

    public static final Parcelable.Creator<CodeConfig> CREATOR = new Parcelable.Creator<CodeConfig>() {
        public CodeConfig createFromParcel(Parcel source) {
            return new CodeConfig(source);
        }

        public CodeConfig[] newArray(int size) {
            return new CodeConfig[size];
        }
    };
}

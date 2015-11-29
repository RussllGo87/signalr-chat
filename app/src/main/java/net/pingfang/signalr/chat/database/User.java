package net.pingfang.signalr.chat.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gongguopei87@gmail.com on 2015/10/10.
 */
public class User implements Parcelable {

    public static final int USER_STATUS_ONLINE = 0x01;
    public static final int USER_STATUS_OFFLINE = 0x00;

    public static final int USER_DEFAULT_EXP = 0;
    public static final String USER_DEFAULT_REMARK = "";
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    String uid;
    String nickname;
    String portrait;
    String remark;
    int status;
    int exp;

    public User(String uid, String nickname, String portrait) {
        this(uid, nickname, portrait, USER_STATUS_OFFLINE);
    }

    public User(String uid, String nickname, String portrait, int status) {
        this(uid, nickname, portrait, status, USER_DEFAULT_EXP);
    }

    public User(String uid, String nickname, String portrait, int status, int exp) {
        this(uid, nickname, portrait, USER_DEFAULT_REMARK, status, exp);
    }

    public User(String uid, String nickname, String portrait, String remark, int status, int exp) {
        this.uid = uid;
        this.nickname = nickname;
        this.portrait = portrait;
        this.remark = remark;
        this.status = status;
        this.exp = exp;
    }

    public User(Parcel in) {
        uid = in.readString();
        nickname = in.readString();
        portrait = in.readString();
        remark = in.readString();
        status = in.readInt();
        exp = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(nickname);
        dest.writeString(portrait);
        dest.writeString(remark);
        dest.writeInt(status);
        dest.writeInt(exp);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }
}

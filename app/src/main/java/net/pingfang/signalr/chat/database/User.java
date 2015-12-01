package net.pingfang.signalr.chat.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gongguopei87@gmail.com on 2015/10/10.
 */
public class User implements Parcelable {

    public static final int USER_STATUS_MSG_LIST_IN = 0x01;
    public static final int USER_STATUS_MSG_LIST_OUT = 0x00;

    public static final int USER_STATUS_NEARBY_LIST_IN = 0x01;
    public static final int USER_STATUS_NEARBY_LIST_OUT = 0x00;

    public static final int USER_GENDER_MALE = 0x01;
    public static final int USER_GENDER_FEMALE = 0x00;

    public static final String USER_DEFAULT_DISTANCE = "0.0";
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
    int gender;
    int msgListStatus;
    int nearbyListStatus;
    int exp;
    String distance;

    public User(String uid, String nickname, String portrait) {
        this(uid, nickname, portrait, USER_DEFAULT_REMARK, USER_GENDER_MALE,
                USER_STATUS_MSG_LIST_OUT, USER_STATUS_NEARBY_LIST_OUT, USER_DEFAULT_EXP,
                USER_DEFAULT_DISTANCE);
    }

    public User(String uid, String nickname, String portrait, String remark) {
        this(uid, nickname, portrait, remark, USER_GENDER_MALE,
                USER_STATUS_MSG_LIST_OUT, USER_STATUS_NEARBY_LIST_OUT, USER_DEFAULT_EXP,
                USER_DEFAULT_DISTANCE);
    }

    public User(String uid, String nickname, String portrait, String remark, int gender,
                int msgListStatus, int nearbyListStatus, int exp, String distance) {
        this.uid = uid;
        this.nickname = nickname;
        this.portrait = portrait;
        this.remark = remark;
        this.gender = gender;
        this.msgListStatus = msgListStatus;
        this.nearbyListStatus = nearbyListStatus;
        this.exp = exp;
        this.distance = distance;
    }

    public User(Parcel in) {
        uid = in.readString();
        nickname = in.readString();
        portrait = in.readString();
        remark = in.readString();
        msgListStatus = in.readInt();
        nearbyListStatus = in.readInt();
        exp = in.readInt();
        distance = in.readString();
        gender = in.readInt();
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
        dest.writeInt(msgListStatus);
        dest.writeInt(nearbyListStatus);
        dest.writeInt(exp);
        dest.writeString(distance);
        dest.writeInt(gender);
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

    public int getMsgListStatus() {
        return msgListStatus;
    }

    public void setMsgListStatus(int msgListStatus) {
        this.msgListStatus = msgListStatus;
    }

    public int getNearbyListStatus() {
        return nearbyListStatus;
    }

    public void setNearbyListStatus(int nearbyListStatus) {
        this.nearbyListStatus = nearbyListStatus;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }
}

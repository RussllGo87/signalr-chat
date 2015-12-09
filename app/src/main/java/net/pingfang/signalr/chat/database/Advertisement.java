package net.pingfang.signalr.chat.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gongguopei87@gmail.com on 2015/12/9.
 */
public class Advertisement implements Parcelable {

    public static final int AD_STATUS_UPLOAD_OK = 0x01;
    public static final int AD_STATUS_UPLOAD_ERROR = 0x00;
    public static final Parcelable.Creator<Advertisement> CREATOR = new Parcelable.Creator<Advertisement>() {
        @Override
        public Advertisement createFromParcel(Parcel source) {
            return new Advertisement(source);
        }

        @Override
        public Advertisement[] newArray(int size) {
            return new Advertisement[size];
        }
    };
    String uid;
    String address;
    String code;
    String length;
    String width;
    String remark;
    String lat;
    String lng;
    String path1;
    String path2;
    String path3;
    String path4;
    int status;

    public Advertisement(Parcel in) {
        uid = in.readString();
        address = in.readString();
        code = in.readString();
        length = in.readString();
        width = in.readString();
        remark = in.readString();
        lat = in.readString();
        lng = in.readString();
        path1 = in.readString();
        path2 = in.readString();
        path3 = in.readString();
        path4 = in.readString();
        status = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(address);
        dest.writeString(code);
        dest.writeString(length);
        dest.writeString(width);
        dest.writeString(remark);
        dest.writeString(lat);
        dest.writeString(lng);
        dest.writeString(path1);
        dest.writeString(path2);
        dest.writeString(path3);
        dest.writeString(path4);
        dest.writeInt(status);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getPath1() {
        return path1;
    }

    public void setPath1(String path1) {
        this.path1 = path1;
    }

    public String getPath2() {
        return path2;
    }

    public void setPath2(String path2) {
        this.path2 = path2;
    }

    public String getPath3() {
        return path3;
    }

    public void setPath3(String path3) {
        this.path3 = path3;
    }

    public String getPath4() {
        return path4;
    }

    public void setPath4(String path4) {
        this.path4 = path4;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

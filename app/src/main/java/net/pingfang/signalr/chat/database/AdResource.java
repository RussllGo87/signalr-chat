package net.pingfang.signalr.chat.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gongguopei87@gmail.com on 2015/12/23.
 */
public class AdResource implements Parcelable {

    public static final int RESOURCE_STATUS_UPLOAD_OK = 0x01;
    public static final int RESOURCE_STATUS_UPLOAD_ERROR = 0x00;
    public static final Parcelable.Creator<AdResource> CREATOR = new Parcelable.Creator<AdResource>() {
        @Override
        public AdResource createFromParcel(Parcel source) {
            return new AdResource(source);
        }

        @Override
        public AdResource[] newArray(int size) {
            return new AdResource[size];
        }
    };

    int id;
    String uid;
    String length;
    String width;
    String address;
    String contact;
    String phone;
    String material;
    String remark;
    String lat;
    String lng;
    String path1;
    String path2;
    String path3;
    String path4;
    int status;

    public AdResource(String uid) {
        this.uid = uid;
    }

    public AdResource(Parcel in) {
        id = in.readInt();
        uid = in.readString();
        length = in.readString();
        width = in.readString();
        address = in.readString();
        contact = in.readString();
        phone = in.readString();
        material = in.readString();
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
        dest.writeInt(id);
        dest.writeString(uid);
        dest.writeString(length);
        dest.writeString(width);
        dest.writeString(address);
        dest.writeString(contact);
        dest.writeString(phone);
        dest.writeString(material);
        dest.writeString(remark);
        dest.writeString(lat);
        dest.writeString(lng);
        dest.writeString(path1);
        dest.writeString(path2);
        dest.writeString(path3);
        dest.writeString(path4);
        dest.writeInt(status);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
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

package net.pingfang.signalr.chat.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gongguopei87@gmail.com on 2015/10/20.
 */
public class ResourceInfo implements Parcelable {

    private String width;
    private String height;
    private String resStatus;
    private String material;
    private String remark;

    private String url;
    private String postTime;
    private String address;
    private String contact;
    private String contactInfo;

    public ResourceInfo() {
    }

    public ResourceInfo(String width, String height, String resStatus, String material, String remark,
                        String url, String postTime, String address, String contact,
                        String contactInfo) {

        this.width = width;
        this.height = height;
        this.resStatus = resStatus;
        this.material = material;
        this.remark = remark;
        this.url = url;
        this.postTime = postTime;
        this.address = address;
        this.contact = contact;
        this.contactInfo = contactInfo;
    }

    public ResourceInfo(Parcel in) {
        width = in.readString();
        height = in.readString();
        resStatus = in.readString();
        material = in.readString();
        remark = in.readString();

        url = in.readString();
        postTime = in.readString();
        address = in.readString();
        contact = in.readString();
        contactInfo = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(width);
        dest.writeString(height);
        dest.writeString(resStatus);
        dest.writeString(material);
        dest.writeString(remark);
        dest.writeString(url);
        dest.writeString(postTime);
        dest.writeString(address);
        dest.writeString(contact);
        dest.writeString(contactInfo);
    }

    public static final Parcelable.Creator<ResourceInfo> CREATOR = new Parcelable.Creator<ResourceInfo>() {
        @Override
        public ResourceInfo createFromParcel(Parcel source) {
            return new ResourceInfo(source);
        }

        @Override
        public ResourceInfo[] newArray(int size) {
            return new ResourceInfo[size];
        }
    };

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
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

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getResStatus() {
        return resStatus;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public void setResStatus(String resStatus) {
        this.resStatus = resStatus;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

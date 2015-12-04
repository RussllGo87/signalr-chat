package net.pingfang.signalr.chat.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gongguopei87@gmail.com on 2015/12/4.
 */
public class BulkRule implements Parcelable {
    public static final Parcelable.Creator<BulkRule> CREATOR = new Parcelable.Creator<BulkRule>() {
        @Override
        public BulkRule createFromParcel(Parcel source) {
            return new BulkRule(source);
        }

        @Override
        public BulkRule[] newArray(int size) {
            return new BulkRule[size];
        }
    };
    int id;
    int integration;
    double distance;
    int maxMassTimes;

    public BulkRule(int id, int integration, double distance, int maxMassTimes) {
        this.id = id;
        this.integration = integration;
        this.distance = distance;
        this.maxMassTimes = maxMassTimes;
    }

    public BulkRule(Parcel in) {
        id = in.readInt();
        integration = in.readInt();
        distance = in.readDouble();
        maxMassTimes = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(integration);
        dest.writeDouble(distance);
        dest.writeInt(maxMassTimes);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIntegration() {
        return integration;
    }

    public void setIntegration(int integration) {
        this.integration = integration;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getMaxMassTimes() {
        return maxMassTimes;
    }

    public void setMaxMassTimes(int maxMassTimes) {
        this.maxMassTimes = maxMassTimes;
    }
}

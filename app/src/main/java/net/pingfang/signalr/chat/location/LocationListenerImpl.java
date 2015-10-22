package net.pingfang.signalr.chat.location;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

/**
 * Created by gongguopei87@gmail.com on 2015/10/19.
 */
public class LocationListenerImpl implements BDLocationListener {

    LocationNotify locationNotify;

    public LocationListenerImpl(LocationNotify notify) {
        this.locationNotify = notify;
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        if(locationNotify != null) {
            locationNotify.updateLoc(location);
        }

    }
}

package net.pingfang.signalr.chat.location;

import com.baidu.location.BDLocation;

/**
 * Created by gongguopei87@gmail.com on 2015/10/19.
 */
public interface LocationNotify {
    public void updateLoc(BDLocation bdLocation);
}

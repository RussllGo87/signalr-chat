package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import net.pingfang.signalr.chat.R;

public class NearbyFriendsActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView btn_activity_back;
    private TextView tv_menu_drop_down;

    private MapView mapView;
    private BaiduMap baiduMap;

    private LocationClient locationClient;
    private BDLocationListener locationListener;
    private LatLng currentLatlng = new LatLng(23.23d,112.6d);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_friends);

        initView();
        initLocation();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        tv_menu_drop_down = (TextView) findViewById(R.id.tv_menu_drop_down);
        tv_menu_drop_down.setOnClickListener(this);


        mapView = (MapView) findViewById(R.id.mapView);
    }

    /**
     * 初始化地图选项
     */
    public void initMap() {
        BaiduMapOptions options = new BaiduMapOptions();
        options.compassEnabled(true);
        options.zoomControlsEnabled(false);
        options.scaleControlEnabled(true);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
//                showLocation(marker);
                return false;
            }
        });

        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                baiduMap.hideInfoWindow();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return true;
            }

        });
    }

    /**
     * 初始化定位设置并开始定位
     */
    public void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setOpenGps(true);
        option.setIsNeedAddress(true);
        option.setIgnoreKillProcess(false);


        locationClient = new LocationClient(getApplicationContext(),option);
        locationListener = new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if (bdLocation == null || mapView == null)
                    return;
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(bdLocation.getRadius()).direction(100)
                        .latitude(bdLocation.getLatitude())
                        .longitude(bdLocation.getLongitude()).build();
                baiduMap.setMyLocationData(locData);

                currentLatlng = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(currentLatlng);
                baiduMap.animateMapStatus(u);
            }
        };
        locationClient.registerLocationListener(locationListener);
        locationClient.start();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.tv_menu_drop_down:
//                popupMenu(view);
                break;
        }
    }

    public void navigateUp() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if(NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities();
        } else {
            onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.stop();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
    }



    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapView.onResume();
        initMap();

    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mapView.onPause();

    }
}

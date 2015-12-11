package net.pingfang.signalr.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.squareup.okhttp.Response;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.net.HttpBaseCallback;
import net.pingfang.signalr.chat.net.OkHttpCommonUtil;
import net.pingfang.signalr.chat.util.GlobalApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class NearbyAdsActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = NearbyAdsActivity.class.getSimpleName();

    public static final String URL_LIST_AD = GlobalApplication.URL_WEB_API_HOST + "/api/WebAPI/Advertisement/GetList";
    public static final String KEY_LIST_AD_LAT = "lat";
    public static final String KEY_LIST_AD_LNG = "lng";
    public static final String KEY_LIST_AD_DISTANCE = "distance";


    private TextView btn_activity_back;
    private TextView tv_menu_drop_down;

    private MapView mapView;
    private BaiduMap baiduMap;

    private LinearLayout ll_nearby_ad_container;
    private TextView tv_title_nearby_ad;
    private TextView tv_address_nearby_ad;
    private TextView tv_detail_nearby_ad;

    private LocationClient locationClient;
    private BDLocationListener locationListener;
    private LatLng currentLatlng = new LatLng(23.23d, 112.6d);

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_ads);

        initView();
        initLocation();
    }

    private void initView() {
        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        tv_menu_drop_down = (TextView) findViewById(R.id.tv_menu_drop_down);
        tv_menu_drop_down.setOnClickListener(this);


        mapView = (MapView) this.findViewById(R.id.mapView);

        ll_nearby_ad_container = (LinearLayout) findViewById(R.id.ll_nearby_ad_container);
        tv_title_nearby_ad = (TextView) findViewById(R.id.tv_title_nearby_ad);
        tv_address_nearby_ad = (TextView) findViewById(R.id.tv_address_nearby_ad);
        tv_detail_nearby_ad = (TextView) findViewById(R.id.tv_detail_nearby_ad);
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

        baiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                showLocation(marker);
                return false;
            }
        });

        baiduMap.setOnMapClickListener(new OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                ll_nearby_ad_container.setVisibility(View.GONE);
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


        locationClient = new LocationClient(getApplicationContext(), option);
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

                currentLatlng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
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
        switch (viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.tv_menu_drop_down:
                popupMenu(view);
                break;
        }
    }

    private void popupMenu(View view) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(getApplicationContext(), R.style.AppMainTheme);
        PopupMenu popup = new PopupMenu(wrapper, view);
        final MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_nearby, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_radius_half:
                        tv_menu_drop_down.setText(R.string.action_radius_half);
                        requestAd(1);
                        break;
                    case R.id.action_radius_kilometer:
                        tv_menu_drop_down.setText(R.string.action_radius_kilometer);
                        requestAd(2);
                        break;
                    case R.id.action_radius_kilometer_2:
                        tv_menu_drop_down.setText(R.string.action_radius_kilometer_2);
                        requestAd(5);
                        break;
                    case R.id.action_radius_kilometer_5:
                        tv_menu_drop_down.setText(R.string.action_radius_kilometer_5);
                        requestAd(10);
                        break;
                    case R.id.action_radius_all:
                        tv_menu_drop_down.setText(R.string.action_radius_all);
                        requestAd(0);
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    public void requestAd(int distance) {

        OkHttpCommonUtil okHttp = OkHttpCommonUtil.newInstance(getApplicationContext());
        okHttp.getRequest(URL_LIST_AD,
                new OkHttpCommonUtil.Param[]{
                        new OkHttpCommonUtil.Param(KEY_LIST_AD_LAT, currentLatlng.latitude),
                        new OkHttpCommonUtil.Param(KEY_LIST_AD_LNG, currentLatlng.longitude),
                        new OkHttpCommonUtil.Param(KEY_LIST_AD_DISTANCE, (distance * 1000))
                },
                new HttpBaseCallback() {
                    @Override
                    public void onResponse(Response response) throws IOException {
                        String result = response.body().string();
                        Log.d(TAG, "URL_LIST_AD return " + result);
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(result);
                            int status = jsonObject.getInt("status");
                            String message = jsonObject.getString("message");
                            if (status == 0) {
                                JSONArray list = jsonObject.getJSONArray("list");
                                if (list != null && list.length() > 0) {
                                    for (int i = 0; i < list.length(); i++) {
                                        JSONObject js = list.getJSONObject(i);
                                        final String adContent = js.getString("adContent");
                                        final String pic = js.getString("pic");
                                        final double lat = Double.parseDouble(js.getString("lat"));
                                        final double lng = Double.parseDouble(js.getString("lng"));
                                        final String address = js.getString("address");
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                addMark(lat, lng, adContent, pic, address);
                                            }
                                        });

                                    }
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void addMark(final double latitude, final double longitude, final String title, final String picUrl, String address) {
        LatLng point = new LatLng(latitude, longitude);
        MarkerOptions options = new MarkerOptions();
        options.position(point);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.point_map_info);
        options.icon(bitmapDescriptor);

        Marker marker = (Marker) (baiduMap.addOverlay(options));
        marker.setTitle(title);
        Bundle bundle = new Bundle();
        bundle.putString("url", picUrl);
        bundle.putString("title", title);
        bundle.putString("address", address);
        marker.setExtraInfo(bundle);
    }

    private void showLocation(final Marker marker) {

        Bundle bundle = marker.getExtraInfo();
        final String url = bundle.getString("url");
        final String title = bundle.getString("title");
        final String address = bundle.getString("address");

        ll_nearby_ad_container.setVisibility(View.VISIBLE);
        ll_nearby_ad_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NearbyAdDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("text", title);
                bundle.putString("url", url);
                bundle.putString("address", address);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        tv_title_nearby_ad.setText(title);
        tv_address_nearby_ad.setText(address);
        tv_detail_nearby_ad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NearbyAdDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("text", title);
                bundle.putString("url", url);
                bundle.putString("address", address);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    public void navigateUp() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
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


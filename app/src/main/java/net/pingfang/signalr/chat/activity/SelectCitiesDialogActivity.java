package net.pingfang.signalr.chat.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import net.pingfang.signalr.chat.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 选择地址对话�?
 * @author wtw
 * @version
 * @create_date 2015-5-8下午4:50:26
 */
public class SelectCitiesDialogActivity extends Activity {
    public static final int SELECT_CITY=1002;

    private JSONObject mJsonObj;//把全国的省市区的信息以json的格式保存，解析完成后赋值为null
    private String[] mProvinceDatas;//�?���?

    private Map<String, String[]> mCitisDatasMap = new HashMap<String, String[]>();//key - �?value - 市s
    private Map<String, String[]> mAreaDatasMap = new HashMap<String, String[]>();//key - �?values - 区s
    private String mCurrentProviceName;//当前省的名称
    private String mCurrentAreaName ="";//当前区的名称
    private Spinner mSpProvince;//省下拉控�?

    private Spinner mSpCity;//城市下拉控件
    private Spinner mSpArea;//地区下拉控件

    private ArrayAdapter<String> mProvinceAdapter;//省份数据适配�?

    private ArrayAdapter<String> mCityAdapter;//城市数据适配�?

    private ArrayAdapter<String> mAreaAdapter;//省份数据适配�?

    private EditText mEtDetailAddre;//地址的详细信�?

    private String mAddress;//原有的地�?

    private String[] mAddressList;//原有地址，用空格进行切分地址
    private Boolean isFirstLord=true;//判断是不是最近进入对话框
    private Boolean ifSetFirstAddress=true;//判断是否已经设置了，初始的详细地�?


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_address_picker);
        initJsonData();
        initDatas();
        initView();
    }

    private void initView(){
        mEtDetailAddre=(EditText)findViewById(R.id.et_detail_address);
        mSpProvince=(Spinner)findViewById(R.id.sp_province);
        mSpCity=(Spinner)findViewById(R.id.sp_city);
        mSpArea=(Spinner)findViewById(R.id.sp_area);

        int selectPro=0;//有传输数据时
        mAddress=getIntent().getStringExtra("address");//通过传入的地区的信息进行初始化，控件数据
        if(mAddress!=null&&!mAddress.equals("")){
            mAddressList=mAddress.split(" ");
        }

        /**
         * 设置，省，市，县，的适配器，进行动�?设置其中的�?  begin
         */
        mProvinceAdapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item);
        for (int i = 0; i < mProvinceDatas.length; i++) {
            //当有传入值时，进行判断�?中的条目，设置默认�?
            if(mAddress!=null&&!mAddress.equals("")&&mAddressList.length>0&&mAddressList[0].equals(mProvinceDatas[i])){
                selectPro=i;
            }
            mProvinceAdapter.add(mProvinceDatas[i]);
        }
        mProvinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpProvince.setAdapter(mProvinceAdapter);
        mSpProvince.setSelection(selectPro);
        mCityAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
        mCityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpCity.setAdapter(mCityAdapter);
        mAreaAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
        mAreaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpArea.setAdapter(mAreaAdapter);
        /**
         * 设置，省，市，县，的适配器，进行动�?设置其中的�?  end
         */
        setupViewsListener();
    }

    /**
     * 设置控件点击选择监听
     */
    private void setupViewsListener(){
        mSpProvince.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                mCurrentProviceName=arg0.getSelectedItem()+"";
                if(isFirstLord){
                    if(mAddress!=null&&!mAddress.equals("")&&mAddressList.length>1&&mAddressList.length<3){
                        updateCitiesAndAreas(mCurrentProviceName,mAddressList[1],null);
                    }else if(mAddress!=null&&!mAddress.equals("")&&mAddressList.length>=3){
                        updateCitiesAndAreas(mCurrentProviceName,mAddressList[1],mAddressList[2]);
                    }else{
                        updateCitiesAndAreas(mCurrentProviceName,null,null);
                    }
                }else{
                    updateCitiesAndAreas(mCurrentProviceName,null,null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        mSpCity.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                if(!isFirstLord){
                    updateAreas(arg0.getSelectedItem(),null);
                }else{
                    if(mAddress!=null&&!mAddress.equals("")&&mAddressList.length==4){
                        mEtDetailAddre.setText(mAddressList[3]);
                    }
                }
                isFirstLord=false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        mSpArea.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                mCurrentAreaName=arg0.getSelectedItem()+"";
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    /**
     * 根据当前的省，更新市和地区信�?
     */
    private void updateCitiesAndAreas(Object object,Object city,Object myArea)
    {
        int selectPosition = 0;//当有数据时，进行匹配城市，默认�?�?

        String[] cities = mCitisDatasMap.get(object);
        mCityAdapter.clear();
        for (int i = 0; i < cities.length; i++) {
            if(city!=null&&city.toString().equals(cities[i])){
                selectPosition=i;
            }
            mCityAdapter.add(cities[i]);
        }
        mCityAdapter.notifyDataSetChanged();
        if(city==null){
            updateAreas(cities[0],null);
        }else{
            mSpCity.setSelection(selectPosition);
            updateAreas(city,myArea);
        }
    }

    /**
     * 根据当前的市，更新地区信�?
     */
    private void updateAreas(Object object,Object myArea)
    {
        boolean isAreas=false;//判断第三个地�?��地区还是详细地址
        int selectPosition = 0;//当有数据时，进行匹配地区，默认�?�?

        String[] area = mAreaDatasMap.get(object);
        mAreaAdapter.clear();
        if(area!=null){
            for (int i = 0; i < area.length; i++) {
                if(myArea!=null&&myArea.toString().equals(area[i])){
                    selectPosition=i;
                    isAreas=true;
                }
                mAreaAdapter.add(area[i]);
            }
            mAreaAdapter.notifyDataSetChanged();
            mSpArea.setSelection(selectPosition);
        }
        //第三个地�?��详细地址，并且是第一次设置edtext值，正好，地�?��长度�?的时候，设置详细地址
        if(!isAreas&&ifSetFirstAddress&&mAddress!=null&&!mAddress.equals("")&&mAddressList.length==3){
            mEtDetailAddre.setText(mAddressList[2]);
            ifSetFirstAddress=false;
        }
    }

    //点击确定后，进行将�?择的值返�?

    public void showChoose(View view)
    {
        Intent intent=new Intent();
        mCurrentAreaName=mSpArea.getSelectedItem()==null?"":mSpArea.getSelectedItem().toString()+" ";
        Bundle bundle=new Bundle();
        bundle.putString("address", mSpProvince.getSelectedItem() + " "+
                mSpCity.getSelectedItem() + " "+
                mCurrentAreaName+
                mEtDetailAddre.getText().toString());
        bundle.putString("province",mSpProvince.getSelectedItem()+"");
        bundle.putString("City", mSpCity.getSelectedItem()+"");
        bundle.putString("AreaName", mCurrentAreaName);
        bundle.putString("Addre", mEtDetailAddre.getText().toString().trim());

        //		intent.putExtra("address", mSpProvince.getSelectedItem() + " "+
        //				mSpCity.getSelectedItem() + " "+
        //				mCurrentAreaName+
        //				mEtDetailAddre.getText().toString());
        //		intent.putExtra("province", mSpProvince.getSelectedItem());

        intent.putExtras(bundle);
        setResult(SELECT_CITY, intent);
        finish();
    }

    /**
     * 解析整个Json对象，完成后释放Json对象的内�?
     */
    private void initDatas()
    {
        try
        {
            JSONArray jsonArray = mJsonObj.getJSONArray("citylist");
            mProvinceDatas = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonP = jsonArray.getJSONObject(i);// 每个省的json对象
                String province = jsonP.getString("p");// 省名�?

                mProvinceDatas[i] = province;

                JSONArray jsonCs = null;
                try
                {
                    /**
                     * Throws JSONException if the mapping doesn't exist or is
                     * not a JSONArray.
                     */
                    jsonCs = jsonP.getJSONArray("c");
                } catch (Exception e1)
                {
                    continue;
                }
                String[] mCitiesDatas = new String[jsonCs.length()];
                for (int j = 0; j < jsonCs.length(); j++)
                {
                    JSONObject jsonCity = jsonCs.getJSONObject(j);
                    String city = jsonCity.getString("n");// 市名�?

                    mCitiesDatas[j] = city;
                    JSONArray jsonAreas = null;
                    try
                    {
                        /**
                         * Throws JSONException if the mapping doesn't exist or
                         * is not a JSONArray.
                         */
                        jsonAreas = jsonCity.getJSONArray("a");
                    } catch (Exception e)
                    {
                        continue;
                    }

                    String[] mAreasDatas = new String[jsonAreas.length()];// 当前市的�?���?

                    for (int k = 0; k < jsonAreas.length(); k++)
                    {
                        String area = jsonAreas.getJSONObject(k).getString("s");// 区域的名�?

                        mAreasDatas[k] = area;
                    }
                    mAreaDatasMap.put(city, mAreasDatas);
                }

                mCitisDatasMap.put(province, mCitiesDatas);
            }

        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        mJsonObj = null;
    }

    /**
     * 从assert文件夹中读取省市区的json文件，然后转化为json对象
     */
    private void initJsonData()
    {
        try
        {
            StringBuffer sb = new StringBuffer();
            InputStream is = getAssets().open("city.json");
            int len = -1;
            byte[] buf = new byte[is.available()];
            while ((len = is.read(buf)) != -1)
            {
                sb.append(new String(buf, 0, len, "gb2312"));
            }
            is.close();
            mJsonObj = new JSONObject(sb.toString());
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

}


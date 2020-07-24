package com.stefan.airconditioningmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.com.newland.nle_sdk.responseEntity.SensorDataPageDTO;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.NCallBack;
import cn.com.newland.nle_sdk.util.NetWorkBusiness;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @Author: Stefan Charles
 * @Date: 2020-07-19
 * @Website: www.stefancharles.cn
 * @E-mail: stefancharles@qq.com
 * @Copyright: Copyright (c) 2020 Security Plus.All rights reserved.
 **/

public class HistoryActivity extends AppCompatActivity {

    private NetWorkBusiness netWorkBusiness;
    private Button GetTemp, SaveTemp, DeleteTemp;
    private ListView listView;
    private DBOpenHelper dbOpenHelper;
    private int lastrecord = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        init();

        SaveTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //保存记录
                saveRecord();
            }
        });

        listView = findViewById(R.id.listView1);
        GetTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取温度记录时间
                getTempData();
            }
        });
        DeleteTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbOpenHelper.getReadableDatabase().delete("record", null, null); //删除记录
            }
        });
    }

    /**
     * 获取温度记录
     */
    private void getTempData() {
        netWorkBusiness.getSensorData("41210", "currentTemp", "6", "30", "2020-07-19 00:00:00",
                "2020-07-22 00:00:00", "DESC", "20", "0", new NCallBack<BaseResponseEntity<SensorDataPageDTO>>() {
                    @Override
                    protected void onResponse(BaseResponseEntity<SensorDataPageDTO> response) {

                    }

                    public void onResponse(final Call<BaseResponseEntity<SensorDataPageDTO>> call, final Response<BaseResponseEntity<SensorDataPageDTO>> response) {
                        BaseResponseEntity baseResponseEntity = response.body();
                        if (baseResponseEntity != null) {
                            //有返回的数据
                            final Gson gson = new Gson();
                            try {
                                JSONObject jsonObject;
                                String msg = gson.toJson(baseResponseEntity);
                                jsonObject = new JSONObject(msg);   //解析数据.
                                JSONObject resultobj = jsonObject.getJSONObject("ResultObj");
                                int count = Integer.parseInt(resultobj.get("Count").toString());    //获取记录数20
                                JSONArray jsonArray = resultobj.getJSONArray("DataPoints");
                                JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                                JSONArray jsonArray1 = jsonObject1.getJSONArray("PointDTO");
                                List<HashMap<String, Object>> data = new ArrayList<>(100);
                                Toast.makeText(HistoryActivity.this, "++", Toast.LENGTH_SHORT).show();

                                for (int i = 0; i < count; i++) {
                                    JSONObject resultObj1 = jsonArray1.getJSONObject(i);
                                    HashMap<String, Object> item = new HashMap<>();
                                    item.put("Id", i + 1);//Id","Temp","Time"
                                    item.put("Temp", resultObj1.get("Value"));
                                    item.put("Time", resultObj1.get("RecordTime"));
                                    data.add(item); //往数组添加元素

                                    ContentValues values = new ContentValues(); //插入到数据库里面
                                    values.put("_id", lastrecord + i + 1);
                                    values.put("temperature", resultObj1.get("Value").toString());
                                    values.put("time", resultObj1.get("RecordTime").toString());
                                    //把数据库的数据存到data1中,然后显示在listview里面.
                                    //dbOpenHelper.getReadableDatabase().insert("record", null, values);
                                }
                                lastrecord = lastrecord + count;    //20,40
                                SimpleAdapter adapter = new SimpleAdapter(HistoryActivity.this, data, R.layout.item, new String[]{"Id", "Temp", "Time"}, new int[]{R.id.showid, R.id.showTemp, R.id.showTime});
                                listView.setAdapter(adapter);   //添加适配器.
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    /**
     * 保存记录。先读取数据库，有没有记录
     */
    private void saveRecord() {
        @SuppressLint("Recycle") Cursor cursor = dbOpenHelper.getReadableDatabase().query("record", null, null, null, null, null, null);
        List<HashMap<String, Object>> data1 = new ArrayList<>(100);
        while (cursor.moveToNext()) {
            //查询数据.
            HashMap<String, Object> item = new HashMap<>();
            item.put("Id", cursor.getString(0));
            item.put("Temp", cursor.getString(1));
            item.put("Time", cursor.getString(2));
            data1.add(item);    //查询.
        }
        SimpleAdapter adapter = new SimpleAdapter(HistoryActivity.this, data1, R.layout.item, new String[]{"Id", "Temp", "Time"}, new int[]{R.id.showid, R.id.showTemp, R.id.showTime});
        listView.setAdapter(adapter);   //添加适配器.
    }


    /**
     * 进行一些初始化操作
     */
    private void init() {
        Bundle bundle = getIntent().getExtras();
        String accessToken = bundle.getString("accessToken");   //获得传输秘钥
        netWorkBusiness = new NetWorkBusiness(accessToken, "http://api.nlecloud.com:80/");
        GetTemp = findViewById(R.id.getTemp);
        SaveTemp = findViewById(R.id.saveTemp);
        DeleteTemp = findViewById(R.id.deleteTemp);
    }
}
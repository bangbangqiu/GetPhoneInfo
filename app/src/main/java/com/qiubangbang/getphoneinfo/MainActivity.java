package com.qiubangbang.getphoneinfo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.qiubangbang.adapter.Adapter;
import com.qiubangbang.utils.GetPhoneInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "MainActivity";
    @BindView(R.id.btn_getContacts)
    Button btnGetContacts;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.activity_main)
    RelativeLayout activityMain;
    @BindView(R.id.btn_getMessage)
    Button btnGetMessage;
    @BindView(R.id.btn_getLocation)
    Button btnGetLocation;
    private Adapter adapter;
    private List<PhoneInfo> contants = new ArrayList<>();
    private RequestQueue queue;
    /**
     * json示例：
     * http://api.map.baidu.com/geocoder/v2/?callback=renderReverse&location=39.983424,116.322987&output=json&pois=1&ak=您的ak
     */
    private String urlParam1 = "http://api.map.baidu.com/geocoder/v2/?callback=renderReverse&location=";
    private String urlParam2 = "&output=json&pois=1&ak=akm7CxvEcZGrHaKtcrTlE3DaXcOhnRrV";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
        eventBind();
    }

    private void init() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(contants, this, 1);
        recyclerView.setAdapter(adapter);
        //初始化volley
        queue = Volley.newRequestQueue(this);

    }

    private void eventBind() {
        btnGetContacts.setOnClickListener(this);
        btnGetMessage.setOnClickListener(this);
        btnGetLocation.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_getContacts:
                contants.clear();
                contants.addAll(GetPhoneInfo.getContants(this));
                adapter.notifyDataSetChanged();
                break;
            case R.id.btn_getMessage:
                adapter.setmType(2);
                contants.clear();
                contants.addAll(GetPhoneInfo.getSmsInPhone(this));
                adapter.notifyDataSetChanged();
                break;
            case R.id.btn_getLocation:
                Map<Integer, String> locationMap = GetPhoneInfo.getPhoneLocation(this);
                if (locationMap.size() > 0) {
                    String url = urlParam1 + locationMap.get(0) + "," + locationMap.get(1)
                            + urlParam2;
                    Log.d(TAG, "onClick: "+url);
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            Log.d(TAG, "onResponse: " + s);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.d(TAG, "onErrorResponse: " + volleyError.toString());
                        }
                    });
                    queue.add(stringRequest);
                }
                break;
        }
    }
}

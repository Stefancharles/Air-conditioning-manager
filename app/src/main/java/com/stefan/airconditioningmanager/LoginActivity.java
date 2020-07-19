package com.stefan.airconditioningmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.com.newland.nle_sdk.requestEntity.SignIn;
import cn.com.newland.nle_sdk.responseEntity.User;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.NetWorkBusiness;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @Author: Stefan Charles
 * @Date: 2020-07-19
 * @Website: www.stefancharles.cn
 * @E-mail: stefancharles@qq.com
 * @Copyright: Copyright (c) 2020 Security Plus.All rights reserved.
 **/

public class LoginActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String _username = "";
    private String _password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // TODO: 2020-07-19 初始化SharedPreferences
        sp = getSharedPreferences("nlecloud", MODE_PRIVATE);
        editor = sp.edit();
        // TODO: 2020-07-19 绑定ID
        Button login = findViewById(R.id.login);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

        // TODO: 2020-07-19  判断SharedPreferences文件中，用户名、密码是否存在；第二个参数是该值如果获取不到的默认值
        if (sp.getString("username", _username) != null && sp.getString("password", _password) != null) {
            if (!sp.getString("username", _username).equals("") && !sp.getString("password", _password).equals("")) {
                username.setText(sp.getString("username", "1"));
                password.setText(sp.getString("password", "2"));
            }
        }

        // TODO: 2020-07-19 按钮点击事件
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }


    /**
     * 登录函数
     * Stefan注：此处我使用的是旧的新大陆SDK，后面新大陆官方微调了SDK，请大家自行修改！
     * 2020-07-19
     */
    private void signIn() {
        String platformAddress = "http://api.nlecloud.com:80/";
        // TODO: 2020-07-19 获取输入框的用户名和密码
        _username = username.getText().toString();
        _password = password.getText().toString();
        // TODO: 2020-07-19 判空
        if (_username.equals("") || _password.equals("")) {
            Toast.makeText(this, "用户名或密码不为空", Toast.LENGTH_SHORT).show();
            return;
        }
        // TODO: 2020-07-19 调用SDK登录
        final NetWorkBusiness netWorkBusiness = new NetWorkBusiness("", platformAddress);
        netWorkBusiness.signIn(new SignIn(_username, _password), new Callback<BaseResponseEntity<User>>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponseEntity<User>> call, @NonNull Response<BaseResponseEntity<User>> response) {
                BaseResponseEntity<User> baseResponseEntity = response.body();
                if (baseResponseEntity != null) {
                    if (baseResponseEntity.getStatus() == 0) {//0为成功
                        // TODO: 2020-07-19 保存正确的用户名和密码，以免下次重复输入
                        editor.putString("username", _username);
                        editor.putString("password", _password);
                        editor.apply();

                        // TODO: 2020-07-19 获取accessToken，并传输到下一个界面
                        String accessToken = baseResponseEntity.getResultObj().getAccessToken();
                        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("accessToken", accessToken);
                        intent.putExtras(bundle);

                        // TODO: 2020-07-19 开启另一个界面
                        startActivity(intent);
                        finish();
                    } else {//登录失败,输出信息
                        Toast.makeText(LoginActivity.this, baseResponseEntity.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponseEntity<User>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "登录失败 " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
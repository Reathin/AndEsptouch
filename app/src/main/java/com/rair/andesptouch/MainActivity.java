package com.rair.andesptouch;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rairmmd.andesptouch.AndEsptouch;
import com.rairmmd.andesptouch.AndEsptouchHelper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AndEsptouch.OnEsptouchTaskListener {

    private EditText etPassword;
    private AndEsptouch andEsptouch;
    private final String TAG = "Rairmmd";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        TextView tvSsid = findViewById(R.id.tv_ssid);
        etPassword = findViewById(R.id.et_password);
        Button btnConnect = findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(this);
        String currentWifiSsid = AndEsptouchHelper.getInstance(this).getCurrentWifiSsid();
        tvSsid.setText(String.format("当前WiFi：%s", currentWifiSsid));
    }

    @Override
    public void onClick(View v) {
        String currentWifiSsid = AndEsptouchHelper.getInstance(this).getCurrentWifiSsid();
        String bssid = AndEsptouchHelper.getInstance(this).getBSSID();
        String password = etPassword.getText().toString().trim();
//        if (TextUtils.isEmpty(password)) {
//            Toast.makeText(this, "请输入WiFi密码", Toast.LENGTH_SHORT).show();
//            return;
//        }
        andEsptouch = new AndEsptouch.Builder(this).setSsid(currentWifiSsid)
                .setBssid(bssid).setPassWord(password).build();
        andEsptouch.startEsptouchConfig();
        showProgressDialog("努力配网中...");
        andEsptouch.setOnEsptouchTaskListener(new AndEsptouch.OnEsptouchTaskListener() {
            @Override
            public void onEsptouchTaskCallback(int code, String message) {
                Log.d(TAG, "code:" + code + "\nmessage:" + message);
                dismissProgressDialog();
                if (code == AndEsptouch.RESULT_CONFIG_SUCCESS) {
                    Toast.makeText(MainActivity.this, "配网成功", Toast.LENGTH_SHORT).show();
                } else if (code == AndEsptouch.RESULT_CONFIG_TIMEOUT) {
                    Toast.makeText(MainActivity.this, "配网超时", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "配网失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onEsptouchTaskCallback(int code, String message) {
        Log.d(TAG, "code:" + code + "\nmessage:" + message);
        dismissProgressDialog();
        if (code == AndEsptouch.RESULT_CONFIG_SUCCESS) {
            Toast.makeText(this, "配网成功", Toast.LENGTH_SHORT).show();
        } else if (code == AndEsptouch.RESULT_CONFIG_TIMEOUT) {
            Toast.makeText(this, "配网超时", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "配网失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setIndeterminate(true);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (andEsptouch != null) {
                    andEsptouch.stopEsptouchConfig();
                }
            }
        });
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (andEsptouch != null) {
            andEsptouch.stopEsptouchConfig();
        }
    }
}

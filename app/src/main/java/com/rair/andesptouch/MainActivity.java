package com.rair.andesptouch;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rair.andesptouch.utils.SPUtils;
import com.rairmmd.andesptouch.AndEsptouch;
import com.rairmmd.andesptouch.AndEsptouchHelper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AndEsptouch.OnEsptouchTaskListener {

    private EditText etPassword;
    private AndEsptouch andEsptouch;
    private final String TAG = "Rairmmd";
    private ProgressDialog progressDialog;
    private CountDownTimer countDownTimer;

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
        String ssid = AndEsptouchHelper.getInstance(this).getWifiSSID();
        tvSsid.setText(String.format("当前WiFi：%s", ssid));
        String password = SPUtils.getInstance().getString("password", "");
        etPassword.setText(password);
    }

    @Override
    public void onClick(View v) {
        String ssid = AndEsptouchHelper.getInstance(this).getWifiSSID();
        String bssid = AndEsptouchHelper.getInstance(this).getBSSID();
        String password = etPassword.getText().toString().trim();
        SPUtils.getInstance().put("password", password);
        Log.i(TAG, "(MainActivity.java:51)-onClick:->"+ssid);
        Log.i(TAG, "(MainActivity.java:51)-onClick:->"+bssid);
        Log.i(TAG, "(MainActivity.java:51)-onClick:->"+password);
        andEsptouch = new AndEsptouch.Builder(this).setSSID(ssid)
                .setBSSID(bssid).setPassWord(password).build();
        andEsptouch.startConfig();
        andEsptouch.setOnEsptouchTaskListener(this);
        if (countDownTimer != null) {
            countDownTimer = null;
        }
        startCountDown();
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

    private void startCountDown() {
        showProgressDialog("努力配网中...");
        countDownTimer = new CountDownTimer(20000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("Rair", "onTick:" + millisUntilFinished);
            }

            @Override
            public void onFinish() {
                Log.d("Rair", "onFinish");
                dismissProgressDialog();
            }
        };
    }

    private void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setIndeterminate(true);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (andEsptouch != null) {
                    andEsptouch.stopConfig();
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
            andEsptouch.stopConfig();
        }
    }
}

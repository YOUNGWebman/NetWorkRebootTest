package com.example.networkreboottest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import android.os.Looper;


import android.content.SharedPreferences;



public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String TAG = "MainActivity";
    private TextView wifiStatusTextView;
    private TextView eth0StatusTextView;
    private TextView eth1StatusTextView;
    private TextView eth2StatusTextView;
    private TextView t4gStatusTextView;
    private TextView tw4gStatusTextView;
    private TextView t5gStatusTextView;
    private TextView rebootCountTextView;
    private volatile boolean wifi_flag = true;
    private volatile boolean eth_flag = true;
    private volatile boolean t4g_flag = true;
    private volatile boolean t5g_flag = true;
    private Handler handler = new Handler();
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String REBOOT_COUNT_KEY = "rebootCount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiStatusTextView = findViewById(R.id.wifi_status_text_view);
        eth0StatusTextView = findViewById(R.id.eth0_status_text_view);
        eth1StatusTextView = findViewById(R.id.eth1_status_text_view);
        eth2StatusTextView = findViewById(R.id.eth2_status_text_view);
        t4gStatusTextView = findViewById(R.id.usb0_status_text_view);
        tw4gStatusTextView = findViewById(R.id.wwan0_status_text_view);
        t5gStatusTextView = findViewById(R.id.test_5g_status_text_view);
        rebootCountTextView = findViewById(R.id.reboot_count_text_view);

        // 初始化 SharedPreferences 和 Editor
        sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        editor = sharedPreferences.edit();

        // 获取SharedPreferences对象
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        // 读取重启次数
        int rebootCount = settings.getInt(REBOOT_COUNT_KEY, 0);

        // 打印当前的重启次数
        Log.d("MainActivity", "设备已重启 " + rebootCount + " 次");
        rebootCountTextView.setText("重启次数：" + rebootCount);

        // 增加重启次数并保存
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(REBOOT_COUNT_KEY, rebootCount + 1);
        editor.apply();

        Button rebootButton = findViewById(R.id.start_reboot_button);
        rebootButton.setEnabled(getButtonState("my_button_state"));
        rebootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rebootButton.setEnabled(false);
                setSystemProperty("network_reboot_test", "1");
                saveButtonState("my_button_state", false);
                Log.d(TAG, "already  action!!!" );

            }
        });
        Button stopButton = findViewById(R.id.stop_reboot_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rebootButton.setEnabled(true);
                saveButtonState("my_button_state", true);
                setSystemProperty("network_reboot_test" , "0");
                resetRebootCount();
                rebootCountTextView.setText("重启次数：0" );
            }
        });


        if(!getButtonState("my_button_state"))
        {
            setSystemProperty("network_reboot_test", "1");
            Log.d(TAG, "already  action!!!" );

        }



        // 获取复选框的状态
        CheckBox wifi_reboot = findViewById(R.id.wifi_Box);
        wifi_reboot.setChecked(getCheckBoxState("wifi_Box"));
        wifi_reboot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveCheckBoxState("wifi_Box", isChecked);
            wifi_flag = isChecked;
            if (isChecked) {
                startWifiStatusThread();
            } else {
                wifiStatusTextView.setText("wifi状态：未知");
            }
        });

        CheckBox eth_reboot = findViewById(R.id.eth_Box);
        eth_reboot.setChecked(getCheckBoxState("eth_Box"));
        eth_reboot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveCheckBoxState("eth_Box", isChecked);
            eth_flag = isChecked;
            if (isChecked) {
                startEthStatusThread();
            } else {
                eth0StatusTextView.setText("eth0状态：未知");
                eth1StatusTextView.setText("eth1状态：未知");
                eth2StatusTextView.setText("eth2状态：未知");
            }
        });

        CheckBox test_4G_reboot = findViewById(R.id.test_4G_box);
        test_4G_reboot.setChecked(getCheckBoxState("test_4G_Box"));
        test_4G_reboot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveCheckBoxState("test_4G_Box", isChecked);
            t4g_flag = isChecked;
            if (isChecked) {
                start4GStatusThread();
            } else {
                t4gStatusTextView.setText("4G状态（USB0）：未知");
                tw4gStatusTextView.setText("4G状态（WWAN0）：未知");
            }
        });

        CheckBox test_5G_reboot = findViewById(R.id.test_5G_Box);
        test_5G_reboot.setChecked(getCheckBoxState("test_5G_Box"));
        test_5G_reboot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveCheckBoxState("test_5G_Box", isChecked);
            t5g_flag = isChecked;
            if (isChecked) {
                start5GStatusThread();
            } else {
                t5gStatusTextView.setText("5G状态：未知");
            }
        });

        // 如果复选框在启动时被选中，则开始相应的状态检查线程
        if (wifi_reboot.isChecked()) {
            startWifiStatusThread();
        }
        if (eth_reboot.isChecked()) {
            startEthStatusThread();
        }
        if (test_4G_reboot.isChecked()) {
            start4GStatusThread();
        }
        if (test_5G_reboot.isChecked()) {
            start5GStatusThread();
        }
    }

    private void startWifiStatusThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (wifi_flag) {
                    final String Wifi_status_Str = getSystemProperty("rp_wifi_state");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            wifiStatusTextView.setText("wifi状态：" + Wifi_status_Str);
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void startEthStatusThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (eth_flag) {
                    final String eth0_status_Str = getSystemProperty("rp_eth0_state");
                    final String eth1_status_Str = getSystemProperty("rp_eth1_state");
                    final String eth2_status_Str = getSystemProperty("rp_eth2_state");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            eth0StatusTextView.setText("eth0状态：" + eth0_status_Str);
                            eth1StatusTextView.setText("eth1状态：" + eth1_status_Str);
                            eth2StatusTextView.setText("eth2状态：" + eth2_status_Str);
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void start4GStatusThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (t4g_flag) {
                    final String test_4G_status_Str = getSystemProperty("rp_usb0_state");
                    final String test_w4G_status_Str = getSystemProperty("rp_wwan0_state");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            t4gStatusTextView.setText("4G状态（USB0）：" + test_4G_status_Str);
                            tw4gStatusTextView.setText("4G状态（WWAN0）：" + test_w4G_status_Str);
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void start5GStatusThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (t5g_flag) {
                    final String test_5G_status_Str = getSystemProperty("rp_rmnet_state");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            t5gStatusTextView.setText("5G状态：" + test_5G_status_Str);
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private String getSystemProperty(String propertyName) {
        Process process = null;
        BufferedReader reader = null;
        try {
            process = new ProcessBuilder("getprop", propertyName).start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
    }

    private void setSystemProperty(String propertyName, String propertyValue) {
        Process process = null;
        try {
            process = new ProcessBuilder("setprop", propertyName, propertyValue).start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }


    // 保存复选框的状态
    private void saveCheckBoxState(String key, boolean isChecked) {
        editor.putBoolean(key, isChecked);
        editor.apply();
    }

    // 获取复选框的状态
    private boolean getCheckBoxState(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    // 保存 Button 的状态
    private void saveButtonState(String key, boolean isEnabled) {
        editor.putBoolean(key, isEnabled);
        editor.apply();
    }

    // 获取 Button 的状态
    private boolean getButtonState(String key) {
        return sharedPreferences.getBoolean(key, true);  // 默认状态为启用
    }
    // 重置重启次数
    public void resetRebootCount() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(REBOOT_COUNT_KEY, 0);
        editor.apply();
    }
}

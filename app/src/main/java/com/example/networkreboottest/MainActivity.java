package com.example.networkreboottest;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.os.Looper;
import android.os.PowerManager;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import android.net.Uri;


import android.provider.Settings;



import android.content.SharedPreferences;



public class MainActivity extends AppCompatActivity {



    private static final String TAG = "MainActivity";
    private TextView wifiStatusTextView;
    private TextView eth0StatusTextView;
    private TextView eth1StatusTextView;
    private TextView eth2StatusTextView;
    private TextView t4gStatusTextView;
    private TextView tw4gStatusTextView;
    private TextView tp4gStatusTextView;
    private TextView t5gStatusTextView;
    private TextView rebootCountTextView;
    private CheckBox wifi_reboot;
    private CheckBox eth_reboot;
    private CheckBox eth1_reboot;
    private CheckBox eth2_reboot;
    private CheckBox test_4G_reboot;
    private CheckBox test_5G_reboot;
    private volatile boolean wifi_flag = true;
    private volatile boolean eth_flag = true;
    private volatile boolean eth1_flag = true;
    private volatile boolean eth2_flag = true;
    private volatile boolean t4g_flag = true;
    private volatile boolean t5g_flag = true;
    private volatile boolean reboot_flag = true;
    private volatile boolean olReboot_flag = true;

    private Handler handler = new Handler();
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String REBOOT_COUNT_KEY = "rebootCount";
    private SharedPreferences sharedPreferences;
    private final int REQUEST_PERMISSION_CODE = 1008;


    private SharedPreferences.Editor editor;
    File f1= new File("/data/rpRbtest.txt");





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // 应用没有获取“显示在其他应用上层”的权限
                requestWriteSettings();
                Log.d(TAG, "Successfully upgraded  permission");
            } else {
                // 应用已经获取了“显示在其他应用上层”的权限
                Log.d(TAG, "already upgraded  permission");
            }
        }

        setSystemProperty("network_reboot_test", "1");
        wifiStatusTextView = findViewById(R.id.wifi_status_text_view);
        eth0StatusTextView = findViewById(R.id.eth0_status_text_view);
        eth1StatusTextView = findViewById(R.id.eth1_status_text_view);
        eth2StatusTextView = findViewById(R.id.eth2_status_text_view);
        t4gStatusTextView = findViewById(R.id.usb0_status_text_view);
        tw4gStatusTextView = findViewById(R.id.wwan0_status_text_view);
        tp4gStatusTextView = findViewById(R.id.ppp0_status_text_view);
        t5gStatusTextView = findViewById(R.id.test_5g_status_text_view);
        rebootCountTextView = findViewById(R.id.reboot_count_text_view);
        wifi_reboot = findViewById(R.id.wifi_Box);
        eth_reboot = findViewById(R.id.eth_Box);
        eth1_reboot = findViewById(R.id.eth1_Box);
        eth2_reboot = findViewById(R.id.eth2_Box);
        test_4G_reboot = findViewById(R.id.test_4G_box);
        test_5G_reboot = findViewById(R.id.test_5G_Box);


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
                reboot_flag = true;
                setSystemProperty("network_reboot_test", "0");
                setSystemProperty("network_reboot_test", "1");
                resetRebootCount();
                rebootCountTextView.setText("重启次数：0" );
                saveButtonState("my_button_state", false);

                Log.d(TAG, "already  action!!!" );
                    // 创建一个新的 Handler 对象
                    Handler handler = new Handler();
                    // 使用 postDelayed() 方法实现延时调用
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 在这里调用 checkConditionsAndReboot() 方法
                            upgradeRootPermission("/data");
                            if(!f1.exists())
                            {runCmd("touch " + f1, 1); }
                            try {
                                Thread.sleep(2000);  // 延迟2秒
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            checkConditionsAndReboot();
                        }
                    }, 3000);  // 延时 3 秒

            }
        });



        Button onlyReboot = findViewById(R.id.only_reboot_button);
        onlyReboot.setEnabled(getButtonState("only_button_state"));
        onlyReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlyReboot.setEnabled(false);
                resetRebootCount();
                rebootCountTextView.setText("重启次数：0" );
                saveButtonState("only_button_state", false);
                // 创建一个新的 Handler 对象
                Handler handler = new Handler();
                // 使用 postDelayed() 方法实现延时调用
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        upgradeRootPermission("/data");
                        // 在这里调用 checkConditionsAndReboot() 方法
                        if(!f1.exists())
                        { runCmd("touch " + f1, 1);}
                        try {
                            Thread.sleep(2000);  // 延迟1秒
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        RebootCommand();
                    }
                }, 5000);  // 延时 5 秒


            }
        });

        Button stopButton = findViewById(R.id.stop_reboot_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rebootButton.setEnabled(true);
                onlyReboot.setEnabled(true);
                saveButtonState("my_button_state", true);
                saveButtonState("only_button_state", true);
                setSystemProperty("network_reboot_test" , "0");
                if(f1.exists())
                {      upgradeRootPermission("/data");
                    runCmd("rm " + f1 , 1);
                }
                reboot_flag = false;
                olReboot_flag = false;
            }
        });

        if(!getButtonState("my_button_state") ) {
            reboot_flag = true;
              setSystemProperty("network_reboot_test", "0");
            setSystemProperty("network_reboot_test", "1");
            Log.d(TAG, "already  action!!!" );

            // 创建一个新的 ScheduledExecutorService 对象
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            // 使用 scheduleAtFixedRate() 方法实现定时调用
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    // 在这里调用 checkConditionsAndReboot() 方法
                    Log.d("RRRRR cmd ", "ready to reboot!!!");
                    checkConditionsAndReboot();
                }
            }, 0, 5, TimeUnit.SECONDS);  // 每隔 5 秒执行一次
        }

        if(!getButtonState("only_button_state") ) {
            try {
                Thread.sleep(2000);  // 延迟1秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (olReboot_flag)
            {
                // 创建一个新的 ScheduledExecutorService 对象
                ScheduledExecutorService executorService1 = Executors.newSingleThreadScheduledExecutor();
                // 使用 scheduleAtFixedRate() 方法实现定时调用
                executorService1.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(5000);  // 延迟1秒
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(olReboot_flag)
                        {
                            Log.d("RRRRR cmd ", "reboot now!!!");
                        RebootCommand();
                        }
                    }
                }, 0, 5, TimeUnit.SECONDS);  // 每隔 5 秒执行一次
            }

        }




        test_5G_reboot.setChecked(getCheckBoxState("test_5G_Box"));
        test_4G_reboot.setChecked(getCheckBoxState("test_4G_Box"));
        eth_reboot.setChecked(getCheckBoxState("eth_Box"));
        eth1_reboot.setChecked(getCheckBoxState("eth1_Box"));
        eth2_reboot.setChecked(getCheckBoxState("eth2_Box"));
        wifi_reboot.setChecked(getCheckBoxState("wifi_Box"));

        // 获取复选框的状态


        wifi_reboot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveCheckBoxState("wifi_Box", isChecked);
            wifi_flag = isChecked;
            if (isChecked) {
                startWifiStatusThread();
            } else {
                wifiStatusTextView.setText("wifi状态：未知");
            }
        });


        eth_reboot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveCheckBoxState("eth_Box", isChecked);
            eth_flag = isChecked;
            if (isChecked) {
                startEthStatusThread();
            } else {
                eth0StatusTextView.setText("eth0状态：未知");
            }
        });


        eth1_reboot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveCheckBoxState("eth1_Box", isChecked);
            eth1_flag = isChecked;
            if (isChecked) {
                startEth1StatusThread();
            } else {
                eth1StatusTextView.setText("eth1状态：未知");
            }
        });


        eth2_reboot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveCheckBoxState("eth2_Box", isChecked);
            eth2_flag = isChecked;
            if (isChecked) {
                startEth2StatusThread();
            } else {
                eth2StatusTextView.setText("eth2状态：未知");
            }
        });



        test_4G_reboot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveCheckBoxState("test_4G_Box", isChecked);
            t4g_flag = isChecked;
            if (isChecked) {
                start4GStatusThread();
            } else {
                t4gStatusTextView.setText("4G状态（USB0）：未知");
                tw4gStatusTextView.setText("4G状态（WWAN0）：未知");
                tp4gStatusTextView.setText("4G状态（PPP0）：未知");
            }
        });


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
        if (eth1_reboot.isChecked()) {
            startEth1StatusThread();
        }
        if (eth2_reboot.isChecked()) {
            startEth2StatusThread();
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

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            eth0StatusTextView.setText("eth0状态：" + eth0_status_Str);

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
    private void startEth1StatusThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (eth1_flag) {

                    final String eth1_status_Str = getSystemProperty("rp_eth1_state");

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            eth1StatusTextView.setText("eth1状态：" + eth1_status_Str);

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
    private void startEth2StatusThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (eth2_flag) {

                    final String eth2_status_Str = getSystemProperty("rp_eth2_state");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

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
                    final String test_p4G_status_Str = getSystemProperty("rp_ppp0_state");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            t4gStatusTextView.setText("4G状态（USB0）：" + test_4G_status_Str);
                            tw4gStatusTextView.setText("4G状态（WWAN0）：" + test_w4G_status_Str);
                            tp4gStatusTextView.setText("4G状态（PPP0）：" + test_p4G_status_Str);
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
        editor.putInt(REBOOT_COUNT_KEY, 1);
        editor.apply();
    }

    public void RebootCommand() {
        Process processa = null;
        DataOutputStream os = null;
        try {
            String cmd="sleep 5;reboot";
            processa = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(processa.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            processa.waitFor();
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading root permission", e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (processa != null) {
                    processa.destroy();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error closing process", e);
            }
        }
    }

    public class CmdResult {
        CmdResult(int exitVal, String output) {
            this.exitVal = exitVal;
            this.output = output;
        }
        public int exitVal;
        public String output;
    }
    public CmdResult runCmd(String cmd, int timeout) {
        java.lang.Process process = null;
        int exitVal = -1;
        DataOutputStream os = null;
        BufferedReader reader = null;
        StringBuilder output = new StringBuilder();

        try {
            process = Runtime.getRuntime().exec("sh");
            os = new DataOutputStream(process.getOutputStream());
            Log.d("RRRRR cmd ", cmd);
            os.writeBytes(cmd + "\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();

            boolean processFinished = process.waitFor(timeout, TimeUnit.SECONDS);

            exitVal = process.exitValue();
            if (processFinished) {
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            } else {
                process.destroyForcibly();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) os.close();
                if (reader != null) reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) process.destroy();
        }

        return new CmdResult(exitVal, output.toString());
    }


    private void checkConditionsAndReboot() {
        if (f1.exists()) {
            try {
                Thread.sleep(1000);  // 延迟2秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (reboot_flag){
            Log.d("RRRRR cmd ", "ready to reboot!!!");
            boolean allCheckedAreSuccess = true;
            if (wifi_reboot.isChecked() && !"success".equals(getSystemProperty("rp_wifi_state"))) {
                allCheckedAreSuccess = false;
            }
            if (eth_reboot.isChecked() && !"success".equals(getSystemProperty("rp_eth0_state"))) {
                allCheckedAreSuccess = false;
            }
            if (eth1_reboot.isChecked() && !"success".equals(getSystemProperty("rp_eth1_state"))) {
                allCheckedAreSuccess = false;
            }
            if (eth2_reboot.isChecked() && !"success".equals(getSystemProperty("rp_eth2_state"))) {
                allCheckedAreSuccess = false;
            }
            if (test_4G_reboot.isChecked() && !(("success".equals(getSystemProperty("rp_usb0_state")) || "success".equals(getSystemProperty("rp_wwan0_state")) || "success".equals(getSystemProperty("rp_ppp0_state"))))) {
                allCheckedAreSuccess = false;
            }
            if (test_5G_reboot.isChecked() && !"success".equals(getSystemProperty("rp_rmnet_state"))) {
                allCheckedAreSuccess = false;
            }

            if (allCheckedAreSuccess) {
                Log.d("RRRRR cmd ", "reboot now!!!");
                RebootCommand();
            } }
        }
    }


    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd="chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            //  Log.e(TAG, "Error upgrading root permission", e);
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                //    Log.e(TAG, "Error closing process", e);
            }
        }
        return true;
    }

    public void  requestWriteSettings(){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));

        startActivityForResult(intent, REQUEST_PERMISSION_CODE);

    }

     @Override
    protected  void  onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == REQUEST_PERMISSION_CODE) {

            if(Settings.System.canWrite(this)){


                Log.d("RRRRR cmd ", "onAcitvityResult write settings granted");
            }

        }

     }
}

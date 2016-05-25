package com.baofeng.test;

import java.util.HashMap;

import com.baofeng.R;
import com.baofeng.aone.AndroidManager;
import com.baofeng.aone.battery.BatteryCallback;
import com.google.gson.Gson;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class TestVolumeAndDisplayManagerActivity extends Activity {

    private SeekBar seekbar, seekbar1;
    private TextView battery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);
        init();
        setListener();
    }

    private void init() {
        Gson gson = new Gson();
        battery = (TextView) findViewById(R.id.battery);
        HashMap map3 = new HashMap();
        map3.put("className", "com.baofeng.aone.battery.BatteryManager");
        map3.put("method", "void_registerCallback_BatteryCallback");
        map3.put("arg", new String[] {});
        String jsonBattery = gson.toJson(map3);
        System.out.println("--------------battery:"+jsonBattery);
        AndroidManager.request(jsonBattery, new BatteryCallback() {
            int  i=0;
            @Override
            public void onBatteryStatusChanged(int status, int batteryLevel,
                    int batterySum) {
                String str = null;
                switch (status) {
                case 1:
                    str = "未知";
                    break;
                case 2:
                    str = "充电";
                    break;
                case 3:
                    str = "放电";
                    break;
                case 4:
                    str = "未充电";
                    break;
                case 5:
                    str = "满电";
                    break;

                default:
                    break;
                }
                System.out.println("--------------battery callback :"+str);
                Toast.makeText(TestVolumeAndDisplayManagerActivity.this, "当前状态："+str+"-当前电量：" + batteryLevel + "-总电量：" + batterySum, Toast.LENGTH_SHORT).show();
                battery.setText("当前状态："+str+"-当前电量：" + batteryLevel + "-总电量：" + batterySum+"-count:"+(i++));

            }
        });

        seekbar = (SeekBar) findViewById(R.id.seekbar);
        
        HashMap map1 = new HashMap();
        map1.put("className", Constant.DISPLAY_MANAGER);
        map1.put("method",
                Constant.DisplayManagerMethods.string_getDisplayValue_void
                        .name());
        map1.put("arg", new String[] {});
        String json1 = gson.toJson(map1);
        String display = AndroidManager.request(json1, null);
        if (!TextUtils.isEmpty(display)) {
            seekbar.setProgress(Integer.parseInt(display.split(",")[0]));            
            seekbar.setMax(Integer.parseInt(display.split(",")[1]));            
        }

        seekbar1 = (SeekBar) findViewById(R.id.seekbar1);

        HashMap map2 = new HashMap();
        map2.put("className", Constant.VOLUME_MANAGER);
        map2.put("method",
                Constant.VolumeManagerMethods.strings_getVolumeValue_void
                        .name());
        map2.put("arg", new String[] {});
        String json2 = gson.toJson(map2);
        String volume = AndroidManager.request(json2, null);
        if (!TextUtils.isEmpty(volume)) {
            seekbar1.setProgress(Integer.parseInt(volume.split(",")[0]));
            seekbar1.setMax(Integer.parseInt(volume.split(",")[1]));
        }

        
    }

    private void setListener() {
        /* DisplayManager test */
        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                HashMap map = new HashMap();
                map.put("className", Constant.DISPLAY_MANAGER);
                map.put("method",
                        Constant.DisplayManagerMethods.void_setDisplayValue_string
                                .name());
                map.put("arg", new String[] { seekBar.getProgress() + "" });
                Gson gson = new Gson();
                String json = gson.toJson(map);
                AndroidManager.request(json, null);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                HashMap map = new HashMap();
                map.put("className", Constant.DISPLAY_MANAGER);
                map.put("method",
                        Constant.DisplayManagerMethods.void_setDisplayValue_string
                                .name());
                map.put("arg", new String[] { progress + "" });
                Gson gson = new Gson();
                String json = gson.toJson(map);
                AndroidManager.request(json, null);
            }
        });

        /* VolumeManager test by AndroidManager */
        seekbar1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                HashMap map = new HashMap<String, String>();
                map.put("className", Constant.VOLUME_MANAGER);
                map.put("method",
                        Constant.VolumeManagerMethods.void_setVolumeChange_string
                                .name());
                map.put("arg", new Object[] { seekBar.getProgress() + "" });
                Gson gson = new Gson();
                String json = gson.toJson(map);
                AndroidManager.request(json, null);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                HashMap map = new HashMap<String, String>();
                map.put("className", Constant.VOLUME_MANAGER);
                map.put("method",
                        Constant.VolumeManagerMethods.void_setVolumeChange_string
                                .name());
                map.put("arg", new Object[] { seekBar.getProgress() + "" });
                Gson gson = new Gson();
                String json = gson.toJson(map);
                AndroidManager.request(json, null);

            }
        });
    }
}

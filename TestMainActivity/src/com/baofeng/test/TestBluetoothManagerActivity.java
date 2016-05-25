package com.baofeng.test;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.baofeng.R;
import com.baofeng.aone.bluetooth.BluetoothManager;
import com.baofeng.aone.bluetooth.bean.DeviceBean;
import com.baofeng.aone.bluetooth.bean.ResultBean;
import com.baofeng.aone.bluetooth.callback.BluetoothStateChangedCallback;
import com.baofeng.aone.bluetooth.callback.BondedDevicesCallback;
import com.baofeng.aone.bluetooth.callback.DeviceStateChangedCallback;
import com.baofeng.aone.bluetooth.callback.DiscoveryCallback;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class TestBluetoothManagerActivity extends Activity
        implements OnClickListener {
    private static final String TAG = "--dhj--";
    private Button enable, disable, discovery, cancle;
    private ListView listview1;
    private ListView listview2;
    private BluetoothManager manager;
    private ArrayList<DeviceBean> devices;
    private ArrayList<DeviceBean> bondeds;

    private ResultBean<DeviceBean> result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        devices = new ArrayList<DeviceBean>();
        bondeds = new ArrayList<DeviceBean>();

        setContentView(R.layout.activity_bluetooth_manager);
        enable = (Button) findViewById(R.id.enable);
        disable = (Button) findViewById(R.id.disable);
        discovery = (Button) findViewById(R.id.discovery);
        cancle = (Button) findViewById(R.id.cancle);
        listview2 = (ListView) findViewById(R.id.result_list);
        listview1 = (ListView) findViewById(R.id.bonded_list);

        enable.setOnClickListener(this);
        disable.setOnClickListener(this);
        discovery.setOnClickListener(this);
        cancle.setOnClickListener(this);

        manager = BluetoothManager.getAndroidManager();
        manager.registDiscoveryCallback(new DiscoveryCallback() {
            ItemAdapter adapter2 = new ItemAdapter();

            @Override
            public void onResult(String json) {
                DeviceBean device = new Gson().fromJson(json, DeviceBean.class);
                Log.d(TAG, "--discovery callback ------" + json);
                devices.add(device);
                adapter2.setDatas(devices);
                listview2.setAdapter(adapter2);
            }
        });
        manager.registDeviceStateChangedCallback(
                new DeviceStateChangedCallback() {
                    @Override
                    public void onPairStateChanged(String state, String json,
                            String reason) {
                        DeviceBean bean = new Gson().fromJson(json,
                                DeviceBean.class);
                        Log.d(TAG, "-------pair state changed---state:" + state
                                + "--json:" + json);
                        if ("1".equals(state)) {
                            Log.d(TAG,
                                    "----unpaired:" + bean != null
                                            ? bean.toString()
                                            : bean.getAddress() + "---reason:"
                                                    + reason);
                            // manager.setPin(bean.getAddress(), "0000");
                        } else if ("2".equals(state)) {
                            Log.d(TAG, "------pairing:" + bean != null
                                    ? bean.toString() : bean.getAddress());
                            manager.connect(bean.getAddress());
                        } else if ("3".equals(state)) {
                            Log.d(TAG, "-----paired:" + bean != null
                                    ? bean.toString() : bean.getAddress());
                        }
                    }

                    @Override
                    public void onConnectionStateChanged(String state,
                            String json) {
                        Log.d(TAG, "--connection state --state:" + state
                                + "--json:" + json);

                    }

                    @Override
                    public void onDeviceNameChanged(String json) {
                        Log.d(TAG, "device name changed--result:" + json);

                    }
                });
        manager.registBluetoothStateChangedCallback(
                new BluetoothStateChangedCallback() {
                    ItemAdapter adapter1 = new ItemAdapter();

                    @Override
                    public void onStateChanged(String state) {
                        int value = Integer.parseInt(state);
                        switch (value) {
                        case 0:
                            // 关闭状态
                            break;
                        case 1:
                            // 正在打开
                            break;
                        case 2:
                            // 已开启
                            manager.getBondedDevices(
                                    new BondedDevicesCallback() {
                                @Override
                                public void onResult(String json) {
                                    Type type = new TypeToken<ResultBean<DeviceBean>>() {
                                    }.getType();
                                    ResultBean<DeviceBean> bean = new Gson()
                                            .fromJson(json, type);
                                    if (bean.isHasDatas()) {
                                        bondeds.clear();
                                        bondeds.addAll(bean.getList());
                                    }
                                    adapter1.setDatas(bondeds);
                                    listview1.setAdapter(adapter1);
                                }
                            });
                            manager.startDiscovery();
                            break;
                        case 3:
                            // 正在关闭
                            break;
                        case 4:
                            // 开始扫描
                            break;
                        case 5:
                            // 扫描完成
                            break;
                        default:

                            break;
                        }

                    }
                });
        listview1.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                String address = bondeds.get(position).getAddress();
                manager.connect(address);
            }
        });
        listview2.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                String address = devices.get(position).getAddress();
                manager.pair(address);

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.enable:
            if ("false".equals(manager.isEnabled())) {
                manager.enable();
            }
            manager.startDiscovery();
            manager.getBondedDevices(new BondedDevicesCallback() {
                @Override
                public void onResult(String json) {
                    Type type = new TypeToken<ResultBean<DeviceBean>>() {
                    }.getType();
                    ItemAdapter adapter1 = new ItemAdapter();
                    ResultBean<DeviceBean> bean = new Gson().fromJson(json,
                            type);
                    if (bean.isHasDatas()) {
                        bondeds.clear();
                        bondeds.addAll(bean.getList());
                    }
                    adapter1.setDatas(bondeds);
                    listview1.setAdapter(adapter1);
                }
            });
            break;
        case R.id.disable:
            manager.disable();
            break;
        case R.id.discovery:
            devices.clear();
            manager.startDiscovery();
            break;
        case R.id.cancle:
            manager.cancelDiscovery();
            break;
        default:
            break;
        }
    }

    class ItemAdapter extends BaseAdapter {
        private ArrayList<DeviceBean> datas = new ArrayList<DeviceBean>();

        public void setDatas(ArrayList<DeviceBean> datas) {
            if (null == datas || datas.size() == 0) {
                return;
            }
            this.datas.clear();
            this.datas.addAll(datas);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return datas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView,
                ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater
                        .from(TestBluetoothManagerActivity.this)
                        .inflate(R.layout.bt_device_itemt, null);
                holder.text = (TextView) convertView
                        .findViewById(R.id.device_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            DeviceBean bean = datas.get(position);
            holder.text.setText("name:" + bean.getName() + "-address:"
                    + bean.getAddress() + "-type:" + bean.getType());

            return convertView;
        }
    }

    class ViewHolder {
        TextView text;
    }
}

package com.baofeng.test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import com.baofeng.R;
import com.baofeng.aone.wifi.ResultBean;
import com.baofeng.aone.wifi.WifiManager;
import com.baofeng.aone.wifi.callback.WifiSettingsCallback;
import com.baofeng.aone.wifi.callback.WifiSystemUICallback;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TestWifiActivity extends Activity
        implements OnClickListener, OnItemClickListener {
    private Button open, stop;
    private ListView resultList;
    private WifiManager wm;
    ArrayList<ResultBean> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_activity);
        open = (Button) findViewById(R.id.open);
        stop = (Button) findViewById(R.id.stop);
        resultList = (ListView) findViewById(R.id.result_list);
        open.setOnClickListener(this);
        stop.setOnClickListener(this);
        list = new ArrayList<ResultBean>();
        wm = WifiManager.getAndroidManager();
        final ResultListAdapter adapter = new ResultListAdapter(
                TestWifiActivity.this);
        wm.registSystemUICallback(new WifiSystemUICallback() {

            @Override
            public void onWifiChanged(String state) {
                Toast.makeText(TestWifiActivity.this, "--state:" + state,
                        Toast.LENGTH_SHORT).show();
                Log.d("login", "---onwifichange state = "+state);

            }

            @Override
            public void onNetworkChanged(String json) {
                Type type = new TypeToken<NetWorkBean<ResultBean>>() {
                }.getType();
                NetWorkBean<ResultBean> bean = new Gson().fromJson(json, type);
                if (!bean.isNetworkEnable()) {
                    Toast.makeText(TestWifiActivity.this, "SystemUI--网络未连接",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TestWifiActivity.this,
                            "SystemUI--网络连接--ssid" + bean.getBean().getSSID()
                                    + "--signalLevel:"
                                    + bean.getBean().getLevel(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        wm.registWifiSettingsCallback(new WifiSettingsCallback() {

            @Override
            public void onForgeted(boolean b, String json) {
                adapter.setData(json);

                list.clear();
                list.addAll(adapter.getData());

                adapter.setData(json);
                resultList.setAdapter(adapter);
            }

            @Override
            public void onDisconnected(boolean b, String json) {
                adapter.setData(json);

                list.clear();
                list.addAll(adapter.getData());
                resultList.setAdapter(adapter);
            }

            @Override
            public void onConnected(int state, String json) {
                System.out.println(
                        "-----------------------on connected:" + state);
                new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						
						isWifiSetPortal();
					}
				}).start();
                adapter.setData(json);
                list.clear();
                list.addAll(adapter.getData());
                resultList.setAdapter(adapter);
            }

            @Override
            public void onScanResult(boolean state, String json) {
                adapter.setData(json);
                list.clear();
                list.addAll(adapter.getData());
                resultList.setAdapter(adapter);

            }
        });
        resultList.setOnItemClickListener(this);
    }
    
    private boolean isWifiSetPortal() {  
    	 Log.d("login", "-----isWifiSetPortal ");
        // 商定的请求链接
        final String mWalledGardenUrl = "http://connect.rom.miui.com/generate_204";  
        // 设置请求超时
        final int WALLED_GARDEN_SOCKET_TIMEOUT_MS = 10000;  

        HttpURLConnection urlConnection = null;  
        try {  
            URL url = new URL(mWalledGardenUrl);  
            urlConnection = (HttpURLConnection) url.openConnection();  
            // 是否应该自动执行 http 重定向（响应代码为 3xx 的请求）。
            urlConnection.setInstanceFollowRedirects(false);  
            urlConnection.setConnectTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);  
            urlConnection.setReadTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);  
//            urlConnection.setUseCaches(false);  
            urlConnection.getInputStream();  
            // 判断返回状态码是否与预设的204相同
            Log.d("login", "-----getResponseCode = "+urlConnection.getResponseCode());
            return urlConnection.getResponseCode() == 204;  
        } catch (IOException e) {  
        	 Log.d("login", "-----IOException e"+e);
            return false;  
        } finally {  
            if (urlConnection != null) {  
                // 记得释放资源
                urlConnection.disconnect();  
            }  
        }  
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view,
            final int position, long id) {
    	Log.d("login", "----onitemclick position = "+position+"  getSecurity ="+list.get(position).getSecurity()+"  type="+list.get(position).getType()+" leve = "+list.get(position).getLevel()
    			+" frequenty = "+list.get(position).getFrequency()+" ssid="+list.get(position).getSSID());

        if (list.get(position).isConnected()) {
            String actionStr;
            // 如果目前连接了此网络
            if ("true".equals(WifiManager.getAndroidManager()
                    .whetherConnected(list.get(position).getSSID()))) {
                actionStr = "断开";
            } else {
                actionStr = "连接";
            }
            new AlertDialog.Builder(TestWifiActivity.this).setTitle("提示")
                    .setMessage("请选择你要进行的操作？").setPositiveButton(actionStr,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {

                                    if ("true".equals(
                                            WifiManager.getAndroidManager()
                                                    .whetherConnected(list
                                                            .get(position)
                                                            .getSSID()))) {
                                        WifiManager.getAndroidManager()
                                                .disconnectWifiAp(
                                                        list.get(position)
                                                                .getSSID());
                                    } else {
                                        WifiManager.getAndroidManager()
                                                .connectWifiAp(
                                                        list.get(position)
                                                                .getSSID());
                                    }
                                }
                            })
                    .setNeutralButton("忘记",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                    WifiManager.getAndroidManager()
                                            .removeNetWork(list.get(position)
                                                    .getSSID());
                                    return;
                                }
                            })
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                    return;
                                }
                            })
                    .show();
        } else {
            View inflate = LayoutInflater.from(TestWifiActivity.this)
                    .inflate(R.layout.dialog_edittext, null);
            final TextView name = (TextView) inflate
                    .findViewById(R.id.wifi_name);
            name.setText(list.get(position).getSSID());
            final EditText psw = (EditText) inflate.findViewById(R.id.psw);

            new AlertDialog.Builder(TestWifiActivity.this).setTitle("身份验证")
                    .setView(inflate).setPositiveButton("连接",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    Toast.makeText(TestWifiActivity.this,
                                            psw.getText().toString(),
                                            Toast.LENGTH_SHORT).show();

                                    WifiManager.getInstance()
                                            .addNetWork(name.getText()
                                                    .toString(),
                                            psw.getText().toString(),
                                            list.get(position).getType() + "");
                                }
                            })
                    .show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.open:
            if ("false".equals(wm.isWifiOpen())) {
                wm.openWifi();
            } else {
                wm.startScan();
            }
            break;
        case R.id.stop:
            wm.closeWifi();
        default:
            break;
        }

    }

    @Override
    protected void onResume() {
        if ("true".equals(wm.isWifiOpen())) {
            wm.openWifi();
        }
        super.onResume();
    }

    class ResultListAdapter extends BaseAdapter {
        private ArrayList<ResultBean> datas;
        private Context mContext;

        public void setData(String json) {
            datas = new ArrayList<ResultBean>();
            Type listType = new TypeToken<ArrayList<ResultBean>>() {
            }.getType();
            datas.addAll((ArrayList<ResultBean>) new Gson().fromJson(json,
                    listType));
            notifyDataSetChanged();
        }

        public ArrayList<ResultBean> getData() {
            return datas;
        }

        public ResultListAdapter(Context context) {

            mContext = context;
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext)
                        .inflate(R.layout.wifi_item, null);
                holder.encryption = (TextView) convertView
                        .findViewById(R.id.encryption);
                holder.SSID = (TextView) convertView.findViewById(R.id.ssid);

                holder.signal = (TextView) convertView
                        .findViewById(R.id.signal);

                // ResultBean bean = datas.get(position);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.SSID.setText(datas.get(position).getSSID());
            holder.encryption
                    .setText(datas.get(position).isEncryption() ? "安全" : "不安全");
            holder.signal.setText(datas.get(position).getLevel() + "格"
                    + "---是否连接：" + datas.get(position).isConnected());
            return convertView;
        }

    }

    class ViewHolder {
        TextView encryption;
        TextView SSID;
        TextView signal;
    }

    class NetWorkBean<T> {
        private boolean isNetworkEnable;
        private T bean;

        public boolean isNetworkEnable() {
            return isNetworkEnable;
        }

        public void setNetworkEnable(boolean isNetworkEnable) {
            this.isNetworkEnable = isNetworkEnable;
        }

        public T getBean() {
            return bean;
        }

        public void setBean(T bean) {
            this.bean = bean;
        }
    }
}

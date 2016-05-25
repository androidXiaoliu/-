package com.baofeng.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.baofeng.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 测试activity的主入口，将需要添加的activity
 * @author donghuajie
 *
 */
public class TestMainActivity extends Activity {
    private Properties prop;
    private ListView list;
    private ListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_main);
        list = (ListView) findViewById(R.id.main_list);
        adapter = new ListAdapter(this);
        prop = new Properties();
        try {
            prop.load(this.getAssets().open("ActivityConfig.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Set<Entry<Object,Object>> entrySet = prop.entrySet();
        adapter.setDatas(entrySet);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                
                String value = getValues(entrySet).get(position);
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(TestMainActivity.this, value));
                startActivity(intent);
            }
        });
    }
    private ArrayList<String> getKeys(Set<Entry<Object,Object>> set){
        ArrayList<String> al = new ArrayList<>();
        if (set == null || set.isEmpty()) {
            return null;
        }
        for (Entry<Object, Object> entry : set) {
            String key = (String) entry.getKey();
            al.add(key);
        }
        return al;
    }
    private ArrayList<String> getValues(Set<Entry<Object,Object>> set){
        ArrayList<String> al = new ArrayList<>();
        if (set == null || set.isEmpty()) {
            return null;
        }
        for (Entry<Object, Object> entry : set) {
            String value = (String) entry.getValue();
            al.add(value);
        }
        return al;
    }
    private class ListAdapter extends BaseAdapter{
        private List<String> arratList = new ArrayList<>();
        private Context mContext;
        public ListAdapter(Context context){
            this.mContext = context;
        }
        
        public void setDatas(Set<Entry<Object,Object>> set){
            if (set.isEmpty()) {
                return;
            }
            ArrayList<String> key = getKeys(set);
            this.arratList.clear();
            this.arratList.addAll(key);
            notifyDataSetChanged();
        }
        
        @Override
        public int getCount() {
            return arratList.size();
        }

        @Override
        public Object getItem(int position) {
            return arratList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null ) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.test_main_item,null);
                holder.activity_name = (TextView) convertView.findViewById(R.id.activity_name);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            String key = arratList.get(position);
            holder.activity_name.setText(key);
            return convertView;
        }
        
    }
    class ViewHolder{
        TextView activity_name;
    }
}

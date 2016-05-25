package com.baofeng.test;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.baofeng.aone.packagemanager.AppPackageManager;
import com.baofeng.aone.packagemanager.IconData;
import com.baofeng.aone.packagemanager.PackageItem;
import com.baofeng.aone.packagemanager.callback.IconCallback;
import com.baofeng.R;

public class PackageAdapter extends BaseAdapter implements IconCallback {
    /**
     * listview中的数据集
     */
    private List<PackageItem> mDataList = new ArrayList<>();
    private List<Bitmap> mIconDatas = new ArrayList<>();
    // private Map<String, IconData> mIconDatas = new HashMap<>();

    private Context mContext;
    private GridView mGridView;
    private AppPackageManager mAppPackageManager;
    private boolean mIsLoaded = false;

    public PackageAdapter(List<PackageItem> list, Context cont,
            AppPackageManager manager) {
        this.mDataList.clear();
        this.mDataList.addAll(list);
        this.mContext = cont;
        this.mAppPackageManager = manager;
    }

    public void setGridView(GridView lisv) {
        this.mGridView = lisv;
    }

    // public void setData(IconData data,String name) {
    // mIconDatas.put(name, data);
    // notifyDataSetChanged();
    // }

    public void removeData(String name) {
        // PackageItem item = findItemByName(name);
        int item = findIndexByName(name);
        if (item != -1) {
            mDataList.remove(item);
            mIconDatas.remove(item);
            notifyDataSetChanged();
        }
    }

    private int findIndexByName(String packageName) {
        int item = -1;
        for (int i = 0; i < mDataList.size(); i++) {
            if (mDataList.get(i).getPackageName().equals(packageName)) {
                item = i;
                return item;
            }
        }
        return item;
    }

    private PackageItem findItemByName(String packageName) {
        PackageItem item = null;
        for (int i = 0; i < mDataList.size(); i++) {
            if (mDataList.get(i).getPackageName().equals(packageName)) {
                item = mDataList.get(i);
                return item;
            }
        }
        return item;
    }

    public void addItem(PackageItem item) {
        String name = item.getPackageName();
        if (findItemByName(name) == null) {
            mDataList.add(item);
            notifyDataSetChanged();
        }

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (!mIsLoaded) {

            mIsLoaded = true;
            // return convertView;
        } else {
            if (mIconDatas.size() != mDataList.size()) {

                String name = mDataList.get(position).getPackageName();
                mAppPackageManager.getAppIconFromPackageName(name, this);
            }
        }

        ViewHolder holder = null;
        if (convertView == null || convertView.getTag() == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.browse_app_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvAppLabel.setText(mDataList.get(position).getAppName());
        Log.d("ymy", "===mIconDatas.size()=" + mIconDatas.size()
                + "  position:" + position);
        if (mIconDatas.size() > position) {
            Bitmap bitmap = mIconDatas.get(position);
            if (bitmap != null) {
                holder.appIcon.setImageBitmap(bitmap);
            } else {
                holder.appIcon.setImageResource(R.drawable.ic_launcher);
            }
        } else {
            holder.appIcon.setImageResource(R.drawable.ic_launcher);
        }

        return convertView;
    }

    class ViewHolder {
        ImageView appIcon;
        TextView tvAppLabel;

        public ViewHolder(View view) {
            this.appIcon = (ImageView) view.findViewById(R.id.itemImage);
            this.tvAppLabel = (TextView) view.findViewById(R.id.itemName);
        }
    }

    @Override
    public void onApplicationIconBytes(IconData data) {
        Log.d("ymy", "===onApplicationIconBytes==");
        if (data != null) {
            Bitmap bitmap = null;
            byte[] buf = data.mIconArray;
            bitmap = BitmapFactory.decodeByteArray(buf, 0, buf.length);
            mIconDatas.add(bitmap);
        }
    }
}

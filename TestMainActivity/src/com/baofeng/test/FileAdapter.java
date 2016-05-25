package com.baofeng.test;

import java.util.ArrayList;
import java.util.List;

import com.baofeng.R;
import com.baofeng.R.drawable;
import com.baofeng.R.id;
import com.baofeng.R.layout;
import com.baofeng.aone.filemanager.bean.FileItem;
import com.baofeng.aone.filemanager.utils.ResUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class FileAdapter extends BaseAdapter {
    /** 
     * listview中的数据集 
     */  
    private List<FileItem> mDataList;  
  
    private Context               mContext;  
    private GridView              mGridView;  
  
    public FileAdapter(List<FileItem> list, Context cont)  
    {  
        this.mDataList = list;
        this.mContext = cont;  
    }  
  
    public void setGridView(GridView lisv)  
    {  
        this.mGridView = lisv;  
    }  
    
//    public void setData(IconData data,int position) {
//        mDataList.get(position).setData(data);
//        notifyDataSetChanged();
//    }
    
    public void removeData(FileItem item) {
        if(mDataList.contains(item)){
            mDataList.remove(item);
            notifyDataSetChanged();
        }
    }
    
    public void addItem(FileItem item) {
        mDataList.add(item);
    }
  
  
    @Override  
    public int getCount()  
    {  
        // TODO Auto-generated method stub  
        return mDataList.size();  
    }  
  
    @Override  
    public Object getItem(int position)  
    {  
        // TODO Auto-generated method stub  
        return mDataList.get(position);  
    }  
  
    @Override  
    public long getItemId(int position)  
    {  
        // TODO Auto-generated method stub  
        return position;  
    }  
  
    @Override  
    public View getView(int position, View convertView, ViewGroup parent)  
    {  
        // TODO Auto-generated method stub  
        if (convertView == null)  
        {  
            convertView = LayoutInflater.from(mContext).inflate(R.layout.file_item, null);  
        }  
        TextView txt = (TextView) convertView.findViewById(R.id.text);  
        txt.setText(mDataList.get(position).getName());
        ImageView imageView = (ImageView)convertView.findViewById(R.id.icon);
        String type = mDataList.get(position).getType();
        if(type.equals(ResUtils.filetype_folder)) {
        	//imageView.setImageDrawable(R.drawable.icon_folder_folder);
        	imageView.setImageResource(R.drawable.icon_folder_folder);
        }else {
        	imageView.setImageResource(R.drawable.icon_folder_text);
        }
    
        return convertView;
    }
}

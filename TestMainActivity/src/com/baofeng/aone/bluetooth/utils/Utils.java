package com.baofeng.aone.bluetooth.utils;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.baofeng.aone.bluetooth.bean.DeviceBean;

public class Utils {
    public static DeviceBean fillData(BluetoothDevice device) {
        DeviceBean bean = new DeviceBean();
        String name = device.getName();
        String address = device.getAddress();
        if (!TextUtils.isEmpty(address)) {
            if (!TextUtils.isEmpty(name)) {
                bean.setName(name);
            } else {
                bean.setName(address);
            }
            bean.setAddress(address);
        }
        // 类型待定
        int majorDeviceClass = device.getBluetoothClass().getMajorDeviceClass();
        bean.setType(getType(majorDeviceClass));
        return bean;
    }

    public static DeviceBean fillData(BluetoothDevice device, String newName) {
        DeviceBean bean = new DeviceBean();
        String address = device.getAddress();
        if (!TextUtils.isEmpty(address)) {
            if (!TextUtils.isEmpty(newName)) {
                bean.setName(newName);
            } else {
                bean.setName(address);
                bean.setAddress(address);
            }
        }
        // 类型待定
        int majorDeviceClass = device.getBluetoothClass().getMajorDeviceClass();
        bean.setType(getType(majorDeviceClass));
        return bean;
    }

    public static String getType(int value) {
        String type = null;
        switch (value) {
        case BluetoothClass.Device.Major.AUDIO_VIDEO:
            // 影音(蓝牙耳机)
            type = 1 + "";
            break;
        case BluetoothClass.Device.Major.COMPUTER:
            // 计算机
            type = 2 + "";
            break;
        case BluetoothClass.Device.Major.HEALTH:
            // 医疗设备
            type = 3 + "";
            break;
        case BluetoothClass.Device.Major.IMAGING:
            // 影像
            type = 4 + "";
            break;
        case BluetoothClass.Device.Major.MISC:
            // 混合
            type = 5 + "";
            break;
        case BluetoothClass.Device.Major.NETWORKING:
            // 网络
            type = 6 + "";
            break;
        case BluetoothClass.Device.Major.PERIPHERAL:
            // 外设(蓝牙手柄等)
            type = 7 + "";
            break;
        case BluetoothClass.Device.Major.PHONE:
            // 手机
            type = 8 + "";
            break;
        case BluetoothClass.Device.Major.TOY:
            // 玩具
            type = 9 + "";
            break;
        case BluetoothClass.Device.Major.WEARABLE:
            // 可穿戴设备
            type = 10 + "";
            break;
        case BluetoothClass.Device.Major.UNCATEGORIZED:
            // 未分类
            type = 11 + "";
            break;
        }
        return type;
    }
}

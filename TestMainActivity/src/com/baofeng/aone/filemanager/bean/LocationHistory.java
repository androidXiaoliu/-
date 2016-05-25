package com.baofeng.aone.filemanager.bean;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.ArrayList;

import com.baofeng.aone.filemanager.utils.Log;

import android.content.Intent;
import android.os.Bundle;

public class LocationHistory extends Object {
    private static final String TAG = "LocationHistory";

    private static final String LH_KEY = "history";

    public interface OnHistoryUpdatedListener {
        void onHistoryUpdated(LocationHistory lh);
    }

    private LinkedList<Location> mLocQueue = new LinkedList<Location>();
    private OnHistoryUpdatedListener mListener = null;
    private boolean isUpdateAtOnce = true;

    private final void notifyObserver() {
        if (null != mListener)
            mListener.onHistoryUpdated(this);
    }

    public void setisUpdateAtOnce(boolean needOrNot) {
        isUpdateAtOnce = needOrNot;
    }

    public void pushLoc(Location loc) {
        if (null != loc) {
            mLocQueue.addFirst(loc);
            updateInternal();
        }
    }

    public Location popLoc() {
        if (mLocQueue.size() != 0) {
            Location loc = mLocQueue.poll();
            updateInternal();
            return loc;
        } else
            return null;
    }

    public Location getCurLoc() {
        if (mLocQueue.isEmpty())
            return null;
        else
            return mLocQueue.getFirst();
    }

    public Location getPrevLoc() {
        return mLocQueue.get(1);
    }

    public void rollBack() {
        mLocQueue.poll();
        updateInternal();
    }

    public void clearAll() {
        mLocQueue.clear();
        updateInternal();
    }

    public void replaceLast(Location loc) {
        if (0 != mLocQueue.size())
            mLocQueue.poll();
        mLocQueue.addFirst(loc);
        updateInternal();
    }

    public ListIterator<Location> getIterator() {
        return mLocQueue.listIterator();
    }

    public int getSize() {
        return mLocQueue.size();
    }

    public void setOnHistoryUpdatedListener(OnHistoryUpdatedListener listener) {
        mListener = listener;
    }

    public void update() {
        notifyObserver();
    }

    private void updateInternal() {
        if (isUpdateAtOnce)
            notifyObserver();
    }

    public void getHistoryFromBundle(Bundle bundle) {
        if (null == bundle)
            return;
        else {
            Serializable serializable = bundle.getSerializable(LH_KEY);
            if (null == serializable)
                return;
            else
                getHistoryFromSerializable(serializable);
        }
    }

    public void getHistoryFromIntent(Intent intent) {
        Log.i(TAG, "==> getHistoryFromIntent");
        if (null == intent) {
            Log.e(TAG, "intent is null");
            return;
        } else {
            Serializable serializable = intent.getSerializableExtra(LH_KEY);
            if (null == serializable) {
                Log.e(TAG, "serializable is null");
                return;
            } else
                getHistoryFromSerializable(serializable);
        }
    }

    public void getHistoryFromSerializable(Serializable serial) {
        if (null == serial)
            return;

        if (!mLocQueue.isEmpty())
            mLocQueue.clear();
        ArrayList<Location> temp = (ArrayList<Location>) serial;

        for (Location loc : temp) {
            if (null != loc)
                mLocQueue.add(loc.clone());
        }
    }

    public Serializable putHistoryToSerializable() {
        if (null == mLocQueue || mLocQueue.isEmpty())
            return null;
        else {
            ArrayList<Location> temp = new ArrayList<Location>();
            for (Location loc : mLocQueue) {
                if (null != loc)
                    temp.add(loc.clone());
            }
            return temp;
        }
    }

    public void putHistoryToIntent(Intent intent) {
        if (null == intent) {
            Log.e(TAG, "intent is null");
            return;
        } else {
            Serializable serial = putHistoryToSerializable();
            if (null != serial) {
                intent.putExtra(LH_KEY, serial);
            }
        }
    }

    public void putHistoryToBundle(Bundle bundle) {
        if (null == bundle)
            return;
        else {
            Serializable serial = putHistoryToSerializable();
            if (null != serial) {
                bundle.putSerializable(LH_KEY, serial);
            }
        }
    }

    // for test
    public void display() {
        ListIterator<Location> iterator = mLocQueue.listIterator();
        int i = 0;
        while (iterator.hasNext()) {
            Location loc = iterator.next();
            Log.i(TAG, "<< " + String.valueOf(i++) + " >>");
            // loc.display();
        }
    }
}

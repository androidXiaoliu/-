package com.baofeng.aone.filemanager.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class FileHandler extends Handler {
    private static final String TAG = "WorkHandler";

    private Map<Integer, List<MsgHandleFunc>> mmapHandles = null;

    public interface MsgHandleFunc {
        boolean onMessage(Message msg);
    }

    public static class MsgHandler {
        private int miMsgId = 0;
        private MsgHandleFunc mMsgHandleFunc = null;

        public MsgHandler(int id, MsgHandleFunc func) {
            miMsgId = id;
            mMsgHandleFunc = func;
        }
    }

    public FileHandler(Looper looper) {
        super(looper);

        if (null == mmapHandles) {
            mmapHandles = new HashMap<Integer, List<MsgHandleFunc>>();
        }
    }

    @Override
    public void handleMessage(Message msg) {
        Log.i(TAG, "==> handleMessage");

        Integer msgId = Integer.valueOf(msg.what);

        if (null == mmapHandles) {
            Log.i(TAG, "the mmapHandles is null in addMsgWithHandler");
        } else {
            List<MsgHandleFunc> mlFunc = mmapHandles
                    .get(Integer.valueOf(msgId));

            if (null != mlFunc) {
                Log.i(TAG, "mlFunc is not none");
                for (MsgHandleFunc func : mlFunc) {
                    func.onMessage(msg);
                }
            } else {
                Log.i(TAG, "can not find handler in map");
            }
        }
    }

    public void addMsgWithHandler(int msgId, MsgHandleFunc handler) {
        Log.i(TAG, "==> addMsgWithHandler");

        if (null == handler) {
            Log.i(TAG, "the handler is null in addMsgWithHandler");
            return;
        } else {
            if (null == mmapHandles) {
                Log.i(TAG, "the mmapHandles is null in addMsgWithHandler");
            } else {
                List<MsgHandleFunc> mlFunc = mmapHandles.get(Integer
                        .valueOf(msgId));

                if (null == mlFunc) {
                    mlFunc = new ArrayList<MsgHandleFunc>();
                    mlFunc.add(handler);
                    mmapHandles.put(Integer.valueOf(msgId), mlFunc);
                } else {
                    if (!mlFunc.contains(handler)) {
                        mlFunc.add(handler);
                    } else {
                        Log.i(TAG, "the handler already in map");
                    }
                }
            }
        }
    }

    public void addMsgHandler(MsgHandler handler) {
        addMsgWithHandler(handler.miMsgId, handler.mMsgHandleFunc);
    }

    public void removeMsg(int msgId) {
        Log.i(TAG, "==> removeMsg");

        if (null == mmapHandles) {
            Log.i(TAG, "the mmapHandles is null in addMsgWithHandler");
        } else {
            if (mmapHandles.containsKey(Integer.valueOf(msgId))) {
                mmapHandles.remove(Integer.valueOf(msgId));
            } else {
                Log.i(TAG, "there is not this msg in map");
            }
        }
    }

}
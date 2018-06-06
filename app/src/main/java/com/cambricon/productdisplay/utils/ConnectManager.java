package com.cambricon.productdisplay.utils;

import android.util.Log;

import com.huawei.hiai.vision.common.ConnectionCallback;


/**
 * Created by xiaoxiao on 18-6-5.
 */

public class ConnectManager {
    private static final String TAG = "ConnectManager";
    private static ConnectManager mInstance = null;
    private Object mWaitConnect = new Object();
    private boolean isConnected = false;

    protected ConnectManager() {
    }

    public static ConnectManager getInstance() {
        if (mInstance == null) {
            mInstance = new ConnectManager();
        }
        return mInstance;
    }

    public ConnectionCallback getmConnectionCallback() {
        return mConnectionCallback;
    }

    private ConnectionCallback mConnectionCallback = new ConnectionCallback() {
        @Override
        public void onServiceConnect() {
            Log.d(TAG, "onServiceConnect");
            synchronized (mWaitConnect) {
                setConnected(true);
                mWaitConnect.notifyAll();
            }
        }


        @Override
        public void onServiceDisconnect() {
            synchronized (mWaitConnect) {
                setConnected(false);
                mWaitConnect.notifyAll();
            }
        }
    };

    public synchronized boolean isConnected() {
        return isConnected;
    }

    public synchronized void setConnected(boolean connected) {
        isConnected = connected;
    }
    public void waitConnect() {
        try {
            synchronized (mWaitConnect) {
                Log.d(TAG, "before start connect!!!");
                mWaitConnect.wait(3000); // Wait for 3 seconds at most.
                Log.d(TAG, "after stop connect !!!");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
    }
    }

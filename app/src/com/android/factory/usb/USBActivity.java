package com.android.factory.usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import com.android.factory.R;
import com.android.factory.TTSBaseActivity;
import com.android.factory.android.SystemExtraActivity;

public class USBActivity extends TTSBaseActivity {
    public static final String ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE";
    private boolean isUSBTestSuccess;
    @Override
    protected void initData() {
        String mPlayText = getResources().getString(R.string.start_usb);
        if (mSystemTTS != null) {
            mSystemTTS.playText(mPlayText);
        }
        usbRegisterReceiver();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_usb;
    }

    @Override
    public void handleMsg(Message msg) {
    }

    @Override
    protected void systemTTSComplete() {
        super.systemTTSComplete();
        isTTSComplete = true;
        if (mGlobalHandler != null && !isUSBTestSuccess){
            mGlobalHandler.postDelayed(startUSBFailRunnable,20*1000);
        }
    }

    private final Runnable startUSBFailRunnable = new Runnable() {
        @Override
        public void run() {
            if (mSystemTTS != null){
                mSystemTTS.playText(getResources().getString(R.string.start_usb_fail));
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            startActivityIntent(this, SystemExtraActivity.class);
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterUsbActionReceiver();
        mGlobalHandler.removeCallbacks(startUSBFailRunnable);
    }

    public void usbRegisterReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_STATE);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);
    }

    public void unRegisterUsbActionReceiver() {
        if (mBroadcastReceiver != null){
            unregisterReceiver(mBroadcastReceiver);
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public static final String ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && !"".equals(action)) {
                if (action.equals(ACTION_USB_STATE) && intent.getExtras() != null){
                    boolean connected = intent.getExtras().getBoolean("connected");
                    Log.d("mBroadcastReceiver","usb_connected------------>"+connected);
                    systemUSBSpeech(connected);
                }
            }
        }
    };

    private void systemUSBSpeech(boolean connected) {
        if (connected && mSystemTTS != null){
            isUSBTestSuccess = true;
            mGlobalHandler.removeCallbacks(startUSBFailRunnable);
            mSystemTTS.playText(getResources().getString(R.string.start_usb_success));
        }
    }
}

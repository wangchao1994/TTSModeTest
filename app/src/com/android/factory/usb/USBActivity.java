package com.android.factory.usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.Toast;
import com.android.factory.R;
import com.android.factory.TTSBaseActivity;
import com.android.factory.android.SystemExtraActivity;

/**
 * USB测试
 */
public class USBActivity extends TTSBaseActivity {
    public static final String ACTION_USB_STATE = "android.hardware.usb.action.USB_STATE";
    private boolean isUSBTestSuccess;
    private static boolean isUSBChanged = false;
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
        if (mGlobalHandler != null && !isUSBTestSuccess){
            mGlobalHandler.postDelayed(startUSBFailRunnable,20*1000);
        }
        isTTSComplete = true;
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
    protected void startActivityIntentClass() {
        startActivityIntent(this, SystemExtraActivity.class);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterUsbActionReceiver();
        mGlobalHandler.removeCallbacks(startUSBFailRunnable);
        mGlobalHandler.removeCallbacks(startUSBSuccessRunnable);
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
                    if (connected){
                        if (!isUSBChanged){
                            isUSBChanged = true;
                            systemUSBSpeech(true);
                        }
                    }else {
                        if (isUSBChanged){
                            isUSBChanged = false;
                            systemUSBSpeech(false);
                        }
                    }
                    Toast.makeText(mContext,connected+"",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private void systemUSBSpeech(boolean connected) {
        if (connected && mSystemTTS != null){
            isUSBTestSuccess = true;
            mGlobalHandler.removeCallbacks(startUSBFailRunnable);
            mGlobalHandler.postDelayed(startUSBSuccessRunnable,2000);

        }
    }

    private final Runnable startUSBSuccessRunnable = new Runnable() {
        @Override
        public void run() {
            if (mSystemTTS != null){
                mSystemTTS.playText(getResources().getString(R.string.start_usb_success));
            }
        }
    };

}

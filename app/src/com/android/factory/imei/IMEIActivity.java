package com.android.factory.imei;

import android.content.Context;
import android.os.Message;
import android.telephony.TelephonyManager;
import com.android.factory.R;
import com.android.factory.TTSBaseActivity;
import com.android.factory.led.LEDActivity;
import java.util.ArrayList;
import java.util.List;

public class IMEIActivity extends TTSBaseActivity {
    private String mIMEIOneString;
    private String mIMEITwoString;
    @Override
    protected void initData() {
        List<String> deviceIMEI = getDeviceIMEI();
        if (deviceIMEI != null && deviceIMEI.size() > 0){
            playText(deviceIMEI);
        }
    }

    @Override
    protected void systemTTSComplete() {
        super.systemTTSComplete();
        isTTSComplete = true;
    }
    @Override
    protected int getLayoutId() {
        return R.layout.activity_imei;
    }

    private void playText(List<String> deviceIMEI) {
        String mBuildPlayText = getResources().getString(R.string.start_imei);
        if (deviceIMEI != null && deviceIMEI.get(0) != null){
            mIMEIOneString = deviceIMEI.get(0).substring(8);
        }
        if (deviceIMEI != null && deviceIMEI.get(1) != null){
            mIMEITwoString = deviceIMEI.get(1).substring(8);
        }
        String mPlayText = String.format(mBuildPlayText, mIMEIOneString,mIMEITwoString);
        if (mSystemTTS != null){
            mSystemTTS.playText(mPlayText);
        }
    }

    private List<String> getDeviceIMEI() {
        List<String> mIMEIList = null;
        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null){
            mIMEIList = new ArrayList<String>();
            for (int slotId = 0; slotId < telephonyManager.getPhoneCount(); slotId++) {
                String mIMEINumber = telephonyManager.getDeviceId(slotId);
                mIMEIList.add(mIMEINumber);
            }
        }
        return mIMEIList;
    }


    @Override
    protected void startActivityIntentClass() {
        startActivityIntent(this, LEDActivity.class);
    }

    @Override
    public void handleMsg(Message msg) {

    }
}

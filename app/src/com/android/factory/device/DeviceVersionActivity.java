package com.android.factory.device;


import android.os.Build;
import android.os.Message;
import com.android.factory.R;
import com.android.factory.TTSBaseActivity;
import com.android.factory.imei.IMEIActivity;
import android.os.SystemProperties;

/**
 * DeviceVersion Test
 */
public class DeviceVersionActivity extends TTSBaseActivity {
    @Override
    protected void initData() {
        String mPlayText = getDeviceVersionInfo();
        if (mSystemTTS != null){
            mSystemTTS.playText(mPlayText);
        }
    }

    @Override
    protected void systemTTSComplete() {
        super.systemTTSComplete();
        isTTSComplete = true;
    }
    private String getDeviceVersionInfo() {
        String mBuildPlayText = getResources().getString(R.string.start_version);
        return String.format(mBuildPlayText,Build.DISPLAY,getVersionDate());
    }

    private String getVersionDate(){
        String strCurrentVersion = SystemProperties.get("ro.custom.build.version");
        if (strCurrentVersion != null && !"".equals(strCurrentVersion)){
            String[] mCurrentDateStr = strCurrentVersion.split("\\.");
            return mCurrentDateStr[5];
        }
        return "";
    }
    @Override
    protected int getLayoutId() {
        return R.layout.activity_device_version;
    }

    @Override
    protected void startActivityIntentClass() {
        startActivityIntent(this, IMEIActivity.class);
    }

    @Override
    public void handleMsg(Message msg) {
    }
}

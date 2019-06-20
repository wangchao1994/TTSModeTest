package com.android.factory.reset;

import android.content.Intent;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.android.factory.R;
import com.android.factory.TTSBaseActivity;

/**
 * 恢复出厂设置测试
 */
public class ResetActivity extends TTSBaseActivity {
    private long[] mHits = new long[2];
    @Override
    protected void initData() {
        String mPlayText = getResources().getString(R.string.start_system_reset);
        if (mSystemTTS != null) {
            mSystemTTS.playText(mPlayText);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_reset;
    }

    @Override
    public void handleMsg(Message msg) {
    }

    @Override
    protected void systemTTSComplete() {
        super.systemTTSComplete();
        isTTSComplete = true;
    }

    private void startResetFactory() {
        Intent intent = new Intent(Intent.ACTION_FACTORY_RESET);
        intent.setPackage("android");
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.putExtra(Intent.EXTRA_REASON, "MasterClearConfirm");
        intent.putExtra(Intent.EXTRA_WIPE_EXTERNAL_STORAGE, false);
        intent.putExtra(Intent.EXTRA_WIPE_ESIMS, false);
        sendBroadcast(intent);
    }

    @Override
    protected void startActivityIntentClass() {
        voidStartReset();
    }

    //3S内连续2次
    private void voidStartReset() {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);
        mHits[mHits.length-1] = SystemClock.uptimeMillis();
        Log.d("startReset","voidStartReset------------->"+mHits[0]);
        if ((SystemClock.uptimeMillis()-mHits[0]) <= 3000) {
            startResetFactory();
        }
    }
}

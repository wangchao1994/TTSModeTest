package com.android.factory.reset;

import android.content.Intent;
import android.os.Message;

import com.android.factory.R;
import com.android.factory.TTSBaseActivity;

public class ResetActivity extends TTSBaseActivity {
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
        if (mGlobalHandler != null){
            mGlobalHandler.postDelayed(startResetRunnable,2*1000);
        }
    }

    private final Runnable startResetRunnable = new Runnable() {
        @Override
        public void run() {
            startResetFactory();
        }
    };

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
    protected void onPause() {
        super.onPause();
        mGlobalHandler.removeCallbacks(startResetRunnable);
    }
}

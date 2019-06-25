package com.android.factory.horn;

import android.media.MediaPlayer;
import android.os.Message;
import com.android.factory.R;
import com.android.factory.TTSBaseActivity;
import com.android.factory.usb.USBActivity;

public class HornActivity extends TTSBaseActivity {
    private MediaPlayer mMediaPlayer;
    private boolean isPlaySuccess;
    @Override
    protected void initData() {
        String mPlayText = getResources().getString(R.string.start_horn);
        if (mSystemTTS != null){
            mSystemTTS.playText(mPlayText);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_horn;
    }

    @Override
    public void handleMsg(Message msg) {

    }

    @Override
    protected void systemTTSComplete() {
        super.systemTTSComplete();
        isTTSComplete = true;
        playAudio();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }

    protected void playAudio(){
        mMediaPlayer = MediaPlayer.create(this,R.raw.tada);
        mMediaPlayer.setLooping(false);
        mMediaPlayer.start();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                isPlaySuccess = true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    @Override
    protected void startActivityIntentClass() {
        startActivityIntent(this, USBActivity.class);
    }


    private void releaseMediaPlayer(){
        if(mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}

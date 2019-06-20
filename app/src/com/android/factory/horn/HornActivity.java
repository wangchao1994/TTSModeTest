package com.android.factory.horn;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Message;
import android.view.KeyEvent;

import com.android.factory.R;
import com.android.factory.TTSBaseActivity;
import com.android.factory.usb.USBActivity;

public class HornActivity extends TTSBaseActivity {
    private AudioManager audiomanager;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void initData() {
        String mPlayText = getResources().getString(R.string.start_horn);
        if (mSystemTTS != null){
            mSystemTTS.playText(mPlayText);
        }
        //initAudioParams();//初始化参数
    }

    private void initAudioParams() {
        audiomanager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
        if (audiomanager == null)return;
        audiomanager.setSpeakerphoneOn(true);
        audiomanager.setRouting(AudioManager.MODE_RINGTONE,AudioManager.ROUTE_EARPIECE,AudioManager.ROUTE_ALL);
        setVolumeControlStream(AudioManager.STREAM_RING);
        audiomanager.adjustVolume(AudioManager.ADJUST_RAISE,0);
        audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC,audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
        audiomanager.setMode(AudioManager.MODE_IN_CALL);
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
        if(mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (audiomanager != null){
            audiomanager.setMode(AudioManager.MODE_NORMAL);
        }
    }

    protected void playAudio(){
        mMediaPlayer = MediaPlayer.create(this,R.raw.tada);
        //mMediaPlayer.setLooping(true);
        mMediaPlayer.start();
    }

    @Override
    protected void startActivityIntentClass() {
        startActivityIntent(this, USBActivity.class);
    }
}

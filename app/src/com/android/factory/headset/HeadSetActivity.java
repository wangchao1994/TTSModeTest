package com.android.factory.headset;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import com.android.factory.R;
import com.android.factory.TTSBaseActivity;
import com.android.factory.mic.MicPhoneActivity;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class HeadSetActivity extends TTSBaseActivity {
    private final String RECORD_PATH = Environment.getExternalStorageDirectory()+File.separator+ "HeadSetTestTTSAmr.amr";
    private static final int TEST_IDLE = 0;
    private static final int TEST_RECORDERING = 1;
    private static final int TEST_PLAYYING =2;
    private int curTestState = TEST_IDLE;
    private MediaRecorder mMediaRecorder;
    private MediaPlayer mMediaPlayer;
    private boolean headSetConnected = false;

    @Override
    protected void initData() {
        playHeadSetText();
        registerHeadSet();
    }

    public void playHeadSetText(){
        String mPlayText = getResources().getString(R.string.start_headset);
        if (mSystemTTS != null) {
            mSystemTTS.playText(mPlayText);
        }
    }

    private void registerHeadSet() {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        mIntentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        this.registerReceiver(mIntentReceiver, mIntentFilter);
    }

    @Override
    protected void systemTTSComplete() {
        super.systemTTSComplete();
        isTTSComplete = true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_head_set;
    }

    @Override
    public void handleMsg(Message msg) {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_EXTERNAL_PTT_TX) {
            voidStartRecord();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void startActivityIntentClass() {
        startActivityIntent(this, MicPhoneActivity.class);
    }

    private void voidStartRecord() {
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            if(curTestState == TEST_IDLE){
                curTestState = TEST_RECORDERING;
                startRecord();
            }else if(curTestState == TEST_PLAYYING){
                curTestState = TEST_IDLE;
                stopPlay();
            }
        }
    }

    private void playRecordFile(){
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.reset();
        try{
            mMediaPlayer.setDataSource(RECORD_PATH);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startRecord() {
        File mOutRecordFile = new File(RECORD_PATH);
        if (!mOutRecordFile.exists()){
            try {
                boolean newFile = mOutRecordFile.createNewFile();
                Log.d("startRecord","create newFile------->"+newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mMediaRecorder == null){
            mMediaRecorder = new MediaRecorder();
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mMediaRecorder.setOutputFile(mOutRecordFile.getAbsolutePath());
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
        }
    }

    private void stopPlay(){
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_EXTERNAL_PTT_TX){
            if (getHeadsetState() == 0){
                playHeadSetText();
            }else{
                if(curTestState == TEST_RECORDERING){
                    curTestState = TEST_PLAYYING;
                    stopRecorder();
                    playRecordFile();
                }
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        release();
        deleteFile();
        if (mIntentReceiver != null){
            unregisterReceiver(mIntentReceiver);
            mIntentReceiver = null;
        }
    }

    /**
     * delete Record file
     */
    private void deleteFile() {
        File mRecordFile = new File(RECORD_PATH);
        if (mRecordFile.exists()) {
            mRecordFile.delete();
        }
    }

    private void release(){
        try{
            if(curTestState == TEST_RECORDERING){
                stopRecorder();
            }
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (Exception e) {
            Log.d("startRecord","release Exception------->"+e.getMessage());
        }
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && !"".equals(action)){
                doCurrentHeadSetAction(action,intent);
            }
        }
    };

    private void doCurrentHeadSetAction(String action,Intent intent) {
        if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
            int currentHeatSet = intent.getIntExtra("state", 0);
            Log.d("currentHeatSet","currentHeatSet--------------->"+currentHeatSet);
            if(currentHeatSet == 0){
                if(curTestState == TEST_RECORDERING){
                    curTestState = TEST_IDLE;
                    stopRecorder();
                }else if(curTestState == TEST_PLAYYING){
                    curTestState = TEST_IDLE;
                    stopPlay();
                }
            }
        }
    }

    private static final String HEADSET_STATE_PATH = "/sys/class/switch/h2w/state";
    public static int getHeadsetState() {
        try {
            FileReader file = new FileReader(HEADSET_STATE_PATH);
            char[] buffer = new char[1024];
            int len = file.read(buffer, 0, 1024);
            int headsetState = Integer.valueOf((new String(buffer, 0, len)).trim());
            Log.v("HeadsetState", "HeadsetState---------------" + headsetState);
            return headsetState;
        } catch (Exception e) {
            return 0;
        }
    }
}

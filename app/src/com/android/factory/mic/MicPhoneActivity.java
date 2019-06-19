package com.android.factory.mic;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import com.android.factory.R;
import com.android.factory.TTSBaseActivity;
import com.android.factory.horn.HornActivity;

import java.io.File;
import java.io.IOException;
import android.media.AudioSystem;

public class MicPhoneActivity extends TTSBaseActivity {
    private final String RECORD_PATH = Environment.getExternalStorageDirectory()+File.separator+ "micTestTTSAmr.amr";
    private static final int TEST_IDLE = 0;
    private static final int TEST_RECORDERING = 1;
    private static final int TEST_PLAYYING =2;
    private int curTestState = TEST_IDLE;
    private MediaRecorder mMediaRecorder;
    private MediaPlayer mMediaPlayer;
    private boolean isRecordInit;
    protected void initData() {
        String mPlayText = getResources().getString(R.string.start_mic);
        if (mSystemTTS != null){
            mSystemTTS.playText(mPlayText);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_mic_phone;
    }

    @Override
    public void handleMsg(Message msg) {
    }

    @Override
    protected void systemTTSComplete() {
        super.systemTTSComplete();
        isTTSComplete = true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_EXTERNAL_PTT_TX) {
            voidStartRecord();
        }else if(keyCode == KeyEvent.KEYCODE_BACK){
            startActivityIntent(this, HornActivity.class);
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_EXTERNAL_PTT_TX){
            if(curTestState == TEST_RECORDERING && isRecordInit){
                curTestState = TEST_PLAYYING;
                stopRecorder();
                playRecordFile();
            }
        }
        return true;
    }

    private void stopRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
        }
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
        //AudioSystem.setParameters("LRChannelSwitch=1");
        AudioSystem.setParameters("SET_MIC_CHOOSE=1");
        mMediaRecorder.setOutputFile(mOutRecordFile.getAbsolutePath());
        try {
            Log.d("startRecord","create prepare------->");
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("startRecord","create prepare--IOException----->"+e.getMessage());
        }
        isRecordInit = true;
    }

    private void playRecordFile(){
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.reset();
        try{
            Log.d("startRecord","create prepare--playRecordFile----->");
            mMediaPlayer.setDataSource(RECORD_PATH);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void stopPlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        release();
        deleteFile();
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
}

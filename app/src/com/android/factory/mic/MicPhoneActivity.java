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

/**
 * MIC测试
 */
public class MicPhoneActivity extends TTSBaseActivity {
    private final String RECORD_PATH = Environment.getExternalStorageDirectory()+File.separator+ "micTestTTSAmr.amr";
    private static final int TEST_IDLE = 0;
    private static final int TEST_RECORDERING = 1;
    private static final int TEST_PLAYYING =2;
    private int curTestState = TEST_IDLE;
    private MediaRecorder mMediaRecorder;
    private MediaPlayer mMediaPlayer;
    private boolean isRecordInit;
    private boolean isStartRecord;
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
            if (mGlobalHandler != null && !isStartRecord){
                mGlobalHandler.removeCallbacks(startRecordRunnable);
                mGlobalHandler.postDelayed(startRecordRunnable,200);//避免短按录音初始化参数异常
                isStartRecord = true;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private final Runnable startRecordRunnable = new Runnable() {
        @Override
        public void run() {
            voidStartRecord();
        }
    };
    @Override
    protected void startActivityIntentClass() {
        startActivityIntent(this, HornActivity.class);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_EXTERNAL_PTT_TX){
            if (mGlobalHandler != null){
                mGlobalHandler.removeCallbacks(startRecordRunnable);
            }
            if(curTestState == TEST_RECORDERING && isRecordInit){
                curTestState = TEST_PLAYYING;
                stopRecorder();
                playRecordFile();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void stopRecorder(){
        if (mMediaRecorder != null) {
            setErrorListener();
            try {
                mMediaRecorder.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                mOutRecordFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(mMediaRecorder == null){
            mMediaRecorder = new MediaRecorder();
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        //AudioSystem.setParameters("LRChannelSwitch=1");
        AudioSystem.setParameters("SET_MIC_CHOOSE=1");
        mMediaRecorder.setOutputFile(mOutRecordFile.getAbsolutePath());
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("startRecord","create prepare--IOException----->"+e.getMessage());
        }
        isRecordInit = true;
    }

    private void playRecordFile(){
        if(mMediaPlayer == null){
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.reset();
        try{
            mMediaPlayer.setDataSource(RECORD_PATH);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    isStartRecord = false;
                    curTestState = TEST_IDLE;
                }
            });
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
                setErrorListener();
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        } catch (Exception e) {
            Log.d("startRecord","release Exception------->"+e.getMessage());
        }
    }
    public void setErrorListener(){
        mMediaRecorder.setOnErrorListener(null);
        mMediaRecorder.setOnInfoListener(null);
        mMediaRecorder.setPreviewDisplay(null);
    }
}

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
import java.io.IOException;

public class HeadSetActivity extends TTSBaseActivity {
    private final String RECORD_PATH = Environment.getExternalStorageDirectory() + File.separator + "HeadSetTestTTSAmr.amr";
    private static final int TEST_IDLE = 0;
    private static final int TEST_RECORDERING = 1;
    private static final int TEST_PLAYYING = 2;
    private int curTestState = TEST_IDLE;
    private MediaRecorder mMediaRecorder;
    private MediaPlayer mMediaPlayer;
    private boolean isPlaySuccess = false;

    @Override
    protected void initData() {
        playHeadSetText();
        registerHeadSet();
    }

    public void playHeadSetText() {
        String mPlayText = getResources().getString(R.string.start_headset);
        if (mSystemTTS != null) {
            mSystemTTS.playText(mPlayText);
        }
    }

    private void registerHeadSet() {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        mIntentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mIntentReceiver, mIntentFilter);
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
            if (mGlobalHandler != null){
                mGlobalHandler.postDelayed(startRecordRunnable,200);//避免短按录音初始化参数异常
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void startActivityIntentClass() {
        Log.i("lx_log"," isPlaySuccess == " + isPlaySuccess);
        if (isPlaySuccess){
            startActivityIntent(this, MicPhoneActivity.class);
        }
    }


    private final Runnable startRecordRunnable = new Runnable() {
        @Override
        public void run() {
            voidStartRecord();
        }
    };

    private void voidStartRecord() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (curTestState == TEST_IDLE) {
                curTestState = TEST_RECORDERING;
                startRecord();
                Log.d("startRecord", "voidStartRecord create startRecord------->");
            } else if (curTestState == TEST_PLAYYING) {
                curTestState = TEST_IDLE;
                stopPlay();
                Log.d("startRecord", "voidStartRecord create stopPlay------->");
            }
        }
    }

    private void playRecordFile() {
        Log.d("startRecord", "create playRecordFile------->");
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.reset();
        try {
            mMediaPlayer.setDataSource(RECORD_PATH);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    isPlaySuccess = true;
                }
            });
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startRecord() {
        Log.d("startRecord", "create startRecord------->");
        File mOutRecordFile = new File(RECORD_PATH);
        if (!mOutRecordFile.exists()) {
            try {
                boolean newFile = mOutRecordFile.createNewFile();
                Log.d("startRecord", "create newFile------->" + newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mMediaRecorder.setOutputFile(mOutRecordFile.getAbsolutePath());
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            Log.d("startRecord", "create newFile------->" + mMediaRecorder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecorder() {
        Log.d("startRecord", "create stopRecorder------->");
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

    private void stopPlay() {
        Log.d("startRecord", "create stopPlay------->");
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_EXTERNAL_PTT_TX) {
            if (mGlobalHandler != null){
                mGlobalHandler.removeCallbacks(startRecordRunnable);
            }
            Log.d("startRecord", "onKeyUp headSetConnected--3---->=" + (curTestState == TEST_RECORDERING));
            if (curTestState == TEST_RECORDERING) {
                curTestState = TEST_PLAYYING;
                stopRecorder();
                playRecordFile();
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
        if (mIntentReceiver != null) {
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

    private void release() {
        try {
            if (curTestState == TEST_RECORDERING) {
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
            Log.d("startRecord", "release Exception------->" + e.getMessage());
        }
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && !"".equals(action)) {
                doCurrentHeadSetAction(action, intent);
            }
        }
    };

    private void doCurrentHeadSetAction(String action, Intent intent) {
        Log.d("currentHeatSet", "currentHeatSet--------------->" + intent.hasExtra("state"));
        if (action.equals(Intent.ACTION_HEADSET_PLUG) && intent.hasExtra("state")) {
            int currentHeatSet = intent.getIntExtra("state", 0);
            Log.d("currentHeatSet", "currentHeatSet--------------->" + currentHeatSet);
            if (currentHeatSet == 0) {
                if (curTestState == TEST_RECORDERING) {
                    curTestState = TEST_IDLE;
                    stopRecorder();
                } else if (curTestState == TEST_PLAYYING) {
                    curTestState = TEST_IDLE;
                    stopPlay();
                }
            }
        }
    }
    public void setErrorListener(){
        mMediaRecorder.setOnErrorListener(null);
        mMediaRecorder.setOnInfoListener(null);
        mMediaRecorder.setPreviewDisplay(null);
    }
}

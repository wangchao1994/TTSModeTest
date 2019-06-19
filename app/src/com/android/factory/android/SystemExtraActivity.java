package com.android.factory.android;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import com.android.factory.R;
import com.android.factory.TTSBaseActivity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.os.ServiceManager;
import com.android.factory.reset.ResetActivity;
import com.android.internal.telephony.ITelephony;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import com.android.internal.telephony.PhoneConstants;
/**
 * System Test (wifi,blue,sim,gps)
 */
public class SystemExtraActivity extends TTSBaseActivity {

    private WifiManager mWifiManager;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean isWifiSuccess;
    private boolean isBlueSuccess;
    private boolean isSimSuccess;
    private boolean isGpsSuccess;
    private boolean mSim1Exist = false;
    private boolean mSim2Exist = false;
    @Override
    protected void initData() {
        String mPlayText = getResources().getString(R.string.start_system);
        if (mSystemTTS != null) {
            mSystemTTS.playText(mPlayText);
        }
        initParams();
    }

    private void initParams() {
        initWifiParams();
        initBluetoothParams();
        initSimParams();
    }

    private void initSimParams() {
        ITelephony iTelephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
        try {
            mSim1Exist = iTelephony.hasIccCardUsingSlotIndex(PhoneConstants.SIM_ID_1);
            mSim2Exist = iTelephony.hasIccCardUsingSlotIndex(PhoneConstants.SIM_ID_2);
            Log.d("system_log","mSim1Exist------------>"+mSim1Exist);
            Log.d("system_log","mSim2Exist------------>"+mSim2Exist);
        }catch (RemoteException e){
            Log.d("system_log","RemoteException-RemoteException----------->"+e.getMessage());
        }
    }

    private void initBluetoothParams() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) return;
        mBluetoothAdapter.enable();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mBluetoothStateReceiver, intentFilter);
        mBluetoothAdapter.startDiscovery();
    }

    private void initWifiParams() {
        mWifiManager= (WifiManager)this.getSystemService(WIFI_SERVICE);
        if (mWifiManager == null)return;
        mWifiManager.setWifiEnabled(true);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mWifiStateReceiver, intentFilter);
        mWifiManager.startScan();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_system;
    }

    @Override
    public void handleMsg(Message msg) {
    }

    @Override
    protected void systemTTSComplete() {
        super.systemTTSComplete();
        isTTSComplete = true;
        if (mGlobalHandler != null){
            mGlobalHandler.postDelayed(startSystemRunnable,5*1000);
        }
    }

    private final Runnable startSystemRunnable = new Runnable() {
        @Override
        public void run() {
            speechTestResult();
        }
    };

    private void speechTestResult() {
        Log.d("system_log","isWifiSuccess------------>"+isWifiSuccess);
        Log.d("system_log","isBlueSuccess------------>"+isBlueSuccess);
        Log.d("system_log","isSimSuccess------------>"+isSimSuccess);
        Log.d("system_log","isGpsSuccess------------>"+isGpsSuccess);
        if (isWifiSuccess && isBlueSuccess && isSimSuccess && isGpsSuccess){
            mSystemTTS.playText(getResources().getString(R.string.start_system_success));
        }else{
            mSystemTTS.playText(getResources().getString(R.string.start_system_fail));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWifiStateReceiver != null){
            unregisterReceiver(mWifiStateReceiver);
            mWifiStateReceiver = null;
        }
        if (mBluetoothStateReceiver != null){
            unregisterReceiver(mBluetoothStateReceiver);
            mBluetoothStateReceiver = null;
        }
        mGlobalHandler.removeCallbacks(startSystemRunnable);
        closeReceiver();
    }

    private void closeReceiver() {
        if (mBluetoothAdapter != null){
            mBluetoothAdapter.disable();
            mBluetoothAdapter = null;
        }
        if (mWifiManager != null){
            mWifiManager.setWifiEnabled(false);
        }
    }

    private BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)){
                List<ScanResult> scanResults = mWifiManager.getScanResults();
                for (int i = scanResults.size() - 1; i >= 0; i--){
                    String mCurrentSSID = scanResults.get(i).SSID;
                    Log.d("system_log","mCurrentSSID------------>"+mCurrentSSID);
                    if (mCurrentSSID != null && !"".equals(mCurrentSSID)){
                        isWifiSuccess = true;
                    }
                }
            }
        }
    };

    private BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null){
                    String address = device.getAddress();
                    Log.d("system_log","address------------>"+address);
                    if (address != null && !"".equals(address)){
                        isBlueSuccess = true;
                    }
                }
            }else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                String stateExtra = BluetoothAdapter.EXTRA_STATE;
                int  mBtState = intent.getIntExtra(stateExtra, -1);
                if ((mBtState == BluetoothAdapter.STATE_TURNING_ON) || (mBtState == BluetoothAdapter.STATE_ON)) {
                    mBluetoothAdapter.startDiscovery();
                }
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            startActivityIntent(this, ResetActivity.class);
        }
        return true;
    }
}

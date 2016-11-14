package uri.egr.biosensing.arduinologgingapp.fragments;

import android.app.Fragment;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;

import uri.egr.biosensing.arduinologgingapp.MainActivity;
import uri.egr.biosensing.arduinologgingapp.R;
import uri.egr.biosensing.arduinologgingapp.gatt_attributes.GattCharacteristics;
import uri.egr.biosensing.arduinologgingapp.gatt_attributes.GattServices;
import uri.egr.biosensing.arduinologgingapp.services.BLEConnectionService;
import uri.egr.biosensing.arduinologgingapp.services.CSVLoggingService;

/**
 * Created by mcons on 11/14/2016.
 */

public class DeviceStreamingFragment extends Fragment {
    public static final String HEADER = "date,time,pin A0";
    public static final String BUNDLE_DEVICE_ADDRESS = "bundle_device_address";

    private String mDeviceAddress;
    private BLEConnectionService mService;
    private MainActivity mActivity;
    private boolean mServiceBound;
    private File mLogFile;
    private ServiceConnection mServiceConnection = new BLEServiceConnection();

    private Button mDisconnectButton;

    private BroadcastReceiver mBLEUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BLE", "Received Update");
            String action = intent.getStringExtra(BLEConnectionService.INTENT_EXTRA);
            switch (action) {
                case BLEConnectionService.GATT_STATE_CONNECTED:
                    mActivity.showMessage("Gatt Server Connected");
                    mService.discoverServices(mDeviceAddress);
                    break;
                case BLEConnectionService.GATT_STATE_DISCONNECTED:
                    mActivity.showMessage("Gatt Server Disconnected");
                    break;
                case BLEConnectionService.GATT_DISCOVERED_SERVICES:
                    mActivity.showMessage("Gatt Services Discovered");
                    BluetoothGattCharacteristic characteristic = mService.getCharacteristic(mDeviceAddress, GattServices.UART_SERVICE, GattCharacteristics.TX_CHARACTERISTIC);
                    if (characteristic != null) {
                        mService.enableNotifications(mDeviceAddress, characteristic);
                    }
                    break;
                case BLEConnectionService.GATT_CHARACTERISTIC_READ:
                    byte[] data = intent.getByteArrayExtra(BLEConnectionService.INTENT_DATA);
                    //Parse contents from data here
                    //Convert contents to String if necessary
                    String contents = String.valueOf(data);
                    CSVLoggingService.start(mActivity, mLogFile, HEADER, contents);
                    break;
                case BLEConnectionService.GATT_DESCRIPTOR_WRITE:
                    break;
                case BLEConnectionService.GATT_NOTIFICATION_TOGGLED:
                    break;
                case BLEConnectionService.GATT_DEVICE_INFO_READ:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle args) {
        super.onCreate(args);
        mActivity = (MainActivity) getActivity();
        mDeviceAddress = getArguments().getString(BUNDLE_DEVICE_ADDRESS, null);
        if (mDeviceAddress == null) {
            mActivity.finish();
            return;
        }

        mLogFile = new File(Environment.getExternalStorageDirectory(), "StreamingLog.csv");

        mActivity.registerReceiver(mBLEUpdateReceiver, new IntentFilter(BLEConnectionService.INTENT_FILTER_STRING));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_streaming, container, false);

        mDisconnectButton = (Button) view.findViewById(R.id.disconnect_button);
        mDisconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.disconnect(mDeviceAddress);
                mActivity.disconnect();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mActivity.bindService(new Intent(getActivity(), BLEConnectionService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unbindService(mServiceConnection);
        try {
            mActivity.unregisterReceiver(mBLEUpdateReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private class BLEServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mServiceBound = true;
            mService = ((BLEConnectionService.BLEConnectionBinder) iBinder).getService();
            Log.d("BLEServiceConnection", "Connecting to Device...");
            mService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("BLEServiceConnection", "Connection Failed");
            mServiceBound = false;
        }
    }
}

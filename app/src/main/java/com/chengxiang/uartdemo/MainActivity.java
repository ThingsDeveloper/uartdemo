package com.chengxiang.uartdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "LoopbackActivity";

    private static final String UART_DEVICE_NAME = "UART0";
    private static final int BAUD_RATE = 115200;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;
    private static final int CHUNK_SIZE = 512;

    private UartDevice mUartDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PeripheralManagerService peripheralManagerService = new PeripheralManagerService();
        try {
            mUartDevice = peripheralManagerService.openUartDevice(UART_DEVICE_NAME);
            mUartDevice.setBaudrate(BAUD_RATE);
            mUartDevice.setDataSize(DATA_BITS);
            mUartDevice.setParity(UartDevice.PARITY_NONE);
            mUartDevice.setStopBits(STOP_BITS);
            mUartDevice.registerUartDeviceCallback(mUartDeviceCallback);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private UartDeviceCallback mUartDeviceCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            try {
                byte[] buffer = new byte[CHUNK_SIZE];
                int read;
                while ((read = mUartDevice.read(buffer, buffer.length)) > 0) {
                    Log.w(TAG, "read from PC:"+ new String(buffer));
                    mUartDevice.write(buffer, read);
                }
            } catch (IOException e) {
                Log.w(TAG, "Unable to transfer data over UART", e);
            }
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.w(TAG, uart + ": Error event " + error);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUartDevice != null) {
            try {
                mUartDevice.close();
                mUartDevice = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close UART device", e);
            }
        }
    }
}

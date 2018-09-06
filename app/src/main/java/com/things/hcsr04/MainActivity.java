package com.things.hcsr04;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.TextView;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {

    Handler mHandler = new Handler();
    Gpio TrigPin;
    Gpio EchoPin;
    private boolean needMasure;
    private int masureCount;
    private long startTime;
    private long endTime;
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text=findViewById(R.id.text);

        PeripheralManager pio = PeripheralManager.getInstance();

        try {
            TrigPin =pio.openGpio("BCM20");//TRIGPIN
            EchoPin = pio.openGpio("BCM21");//echoPin

            TrigPin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            TrigPin.setActiveType(Gpio.ACTIVE_HIGH);

            EchoPin.setDirection(Gpio.DIRECTION_IN);
            EchoPin.setActiveType(Gpio.ACTIVE_HIGH);
            EchoPin.setEdgeTriggerType(Gpio.EDGE_BOTH);
            EchoPin.registerGpioCallback(mGpioCallback1);
            mHandler.post(mSendRunnable);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /**
     * 有信号返回，通过IO口ECHO输出一个高电平，高电平持续的时间就是超声波从发射到返回的时间。测试距离=(高电平时间*声速(340M/S))/2;
     * 一个控制口trig发一个10US以上的高电平，就可以在接收口echo等待高电平输出。一有输出就可以开定时器计时，
     当此口变为低电平时就可以读定时器的值，此时就为此次测距的时间，方可算出距离。如此不断的周期测，即可以达到你移动测量的值
     */

    private GpioCallback mGpioCallback1 = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            // 读取高电平有效状态
            try {
                if (gpio.getValue()) {//发现高电平开始计时
                    if (needMasure) {//第N次发现高电平计时中
                        Log.e("收到高电平", "计时中");
                        text.setText("masureCount:" + masureCount + ",Result : In measurement");
                    } else {//第一次发现高电平开始计时
                        startTime = System.currentTimeMillis();
                        needMasure = true;
                    }
                } else {//如果计时中发现低电平信号结束测量，如果没有计时中忽略掉
                    if (needMasure) {
                        endTime = System.currentTimeMillis();
                        double time = (endTime - startTime) / 1000.0;
                        Log.e("第" + masureCount + "次测量时间：", "经过时间：" + time);
                        //测量结果，距离=（声音飞行时间 * 声音速度） / 2 因为是声音来回的时间。
                        Log.e("第" + masureCount + "次测量，距离为：", " " + (time * 340 / 2) + " M");

                        text.setText("masureCount:" + masureCount + ",Result : " + (time * 340 / 2) + " M");
                        needMasure = false;
                    } else {
                        Log.e("第" + masureCount + "次测量", "未接收到高电平");
                        text.setText("masureCount:" + masureCount + ",Result : No Found Hign pin");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;//返回true 接受更多的状态
        }

        @Override
        public void onGpioError(Gpio gpio, int error) {
            Log.w("123", gpio + ": Error event " + error);
        }
    };

    private Runnable mSendRunnable = new Runnable() {
        @Override
        public void run() {
            // Exit Runnable if the GPIO is already closed
            if (TrigPin == null) {
                return;
            }
            try {
                masureCount++;
                TrigPin.setValue(true);//输出高电平
                Log.d("123", "State set to High");
                Thread.sleep((long) 0.015);//高电平输出15US
                TrigPin.setValue(false);//恢复低电平
                Log.d("123", "State set to Low");
                mHandler.postDelayed(mSendRunnable, 1000);
            } catch (IOException e) {
                Log.e("123", "Error on PeripheralIO API", e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

}

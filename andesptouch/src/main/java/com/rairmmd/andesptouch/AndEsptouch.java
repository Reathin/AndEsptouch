package com.rairmmd.andesptouch;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Rair
 * @date 2018/6/27
 * <p>
 * desc:
 */
public class AndEsptouch implements Callback {

    private final String TAG = "AndEsptouch";

    private IEsp8266TouchTask mEsptouchTaskTemp;

    private IEsptouchTask mEsptouchTask;

    private Context mContext;

    /**
     * SSID
     */
    private String mSsid;
    /**
     * BSSID
     */
    private String mBssid;
    /**
     * 密码
     */
    private String mPassword;

    /**
     * 配对的设备个数,默认是1
     */
    private int mDeviceCount = 1;

    /**
     * 端口
     */
    private int mPort = 8686;

    private OnEsptouchTaskListener listener;

    private boolean isUDPReceive = false;
    private boolean isReceive = true;
    private boolean isUDPReceiveFail = true;

    private int timesOut;

    private Thread mThread;

    private Selector selector = null;
    private DatagramChannel channel = null;
    private Handler mHandler;


    private AndEsptouch(Context mContext) {
        this.mContext = mContext;
        this.mHandler = new Handler(this);
    }

    public static class Builder {

        private AndEsptouch mAndEsptouch;

        public Builder(Context mContext) {
            mAndEsptouch = new AndEsptouch(mContext);
        }

        /**
         * @param ssid 设置路由器名字
         */
        public Builder setSsid(String ssid) {
            mAndEsptouch.mSsid = ssid;
            return this;
        }

        /**
         * 设置bssid
         */
        public Builder setBssid(String bssid) {
            mAndEsptouch.mBssid = bssid;
            return this;
        }

        /**
         * @param passWord 设置路由器密码
         */
        public Builder setPassWord(String passWord) {
            mAndEsptouch.mPassword = passWord;
            return this;
        }

        /**
         * @param deviceCount 设置要配对的设备个数
         */
        public Builder setDeviceCount(int deviceCount) {
            mAndEsptouch.mDeviceCount = deviceCount;
            return this;
        }

        public AndEsptouch build() {
            return mAndEsptouch;
        }
    }

    /**
     * 不接受自定义广播包开始配置
     */
    public void startEsptouchConfig() {
        Log.d(TAG, "start esptouch config");
        mEsptouchTaskTemp = new IEsp8266TouchTask();
        mEsptouchTaskTemp.execute(mSsid, mBssid, mPassword, Integer.toString(mDeviceCount));
        isUDPReceive = false;
    }

    /**
     * 设置为配网模式+接受此设备UDP信息
     *
     * @param timesOut 设置超时时间
     * @param port     设置UDP本地的端口
     */
    public void startEsptouchConfig(int timesOut, int port) {
        Log.d(TAG, "start esptouch config");
        mEsptouchTaskTemp = new IEsp8266TouchTask();
        mEsptouchTaskTemp.execute(mSsid, mBssid, mPassword, Integer.toString(mDeviceCount));
        isUDPReceive = true;
        this.timesOut = timesOut;
        this.mPort = port;
    }

    /**
     * 停止配置
     */
    public void stopEsptouchConfig() {
        Log.d(TAG, "stop esptouch config");
        if (mEsptouchTask != null) {
            mEsptouchTask.interrupt();
        }
        isReceive = false;
        if (mThread != null && channel != null) {
            mThread.interrupt();
            channel.socket().disconnect();
            channel.socket().close();
        }
    }

    public void setOnEsptouchTaskListener(OnEsptouchTaskListener listener) {
        this.listener = listener;
    }

    public interface OnEsptouchTaskListener {

        /**
         * 回调
         */
        void onEsptouchTaskCallback(int code, String message);


    }

    private class IEsp8266TouchTask extends AsyncTask<String, Void, List<IEsptouchResult>> {

        private IEsp8266TouchTask() {

        }

        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
            if (mEsptouchTask != null) {
                mEsptouchTask.interrupt();
            }
        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            int taskResultCount = -1;
            synchronized (mLock) {
                String apSsid = params[0];
                String apBssid = params[1];
                String apPassword = params[2];
                String taskResultCountStr = params[3];
                taskResultCount = Integer.parseInt(taskResultCountStr);
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, mContext);
                mEsptouchTask.setEsptouchListener(new IEsptouchListener() {

                    @Override
                    public void onEsptouchResultAdded(IEsptouchResult result) {
                        Log.d(TAG, "esptouch config result" + result.getInetAddress().toString());
                        if (listener != null) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("mac", result.getBssid());
                                jsonObject.put("ip", result.getInetAddress().getHostAddress());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Message message = new Message();
                            message.what = 108;
                            message.obj = jsonObject.toString();
                            mHandler.sendMessage(message);
                            if (isUDPReceive) {
                                startUDPRecieve(result.getBssid(), result.getInetAddress().getHostAddress());
                            }
                        }
                    }
                });
            }
            return mEsptouchTask.executeForResults(taskResultCount);
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> iEsptouchResults) {
            IEsptouchResult firstResult = iEsptouchResults.get(0);
            if (!firstResult.isCancelled()) {
                String macAddress = null;
                String ipAddress = null;
                int count = 0;
                final int maxDisplayCount = 5;
                if (firstResult.isSuc()) {
                    JSONObject jsonObject = new JSONObject();
                    for (IEsptouchResult resultInList : iEsptouchResults) {
                        try {
                            jsonObject.put("mac", resultInList.getBssid());
                            jsonObject.put("ip", resultInList.getInetAddress().getHostAddress());
                            macAddress = resultInList.getBssid();
                            ipAddress = resultInList.getInetAddress().getHostAddress();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        count++;
                        if (count >= maxDisplayCount) {
                            break;
                        }
                    }
                    if (count < iEsptouchResults.size()) {
                        try {
                            jsonObject.put("downNum", (iEsptouchResults.size() - count));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    if (listener != null && mDeviceCount != 1) {
                        Message message = new Message();
                        message.what = 106;
                        message.obj = jsonObject.toString();
                        mHandler.sendMessage(message);
                        if (isUDPReceive) {
                            startUDPRecieve(macAddress, ipAddress);
                        }
                    }
                } else {
                    if (listener != null) {
                        mHandler.sendEmptyMessage(107);
                    }
                }
            }

        }

    }

    private void startUDPRecieve(final String macAddress, final String ipAddress) {

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        isReceive = false;
                        if (listener != null && isUDPReceiveFail) {
                            mHandler.sendEmptyMessage(109);
                        }
                    }
                }, timesOut * 1000);

                try {
                    channel = DatagramChannel.open();
                    channel.configureBlocking(false);
                    channel.socket().setReuseAddress(false);
                    channel.socket().bind(new InetSocketAddress(mPort));

                    selector = Selector.open();
                    channel.register(selector, SelectionKey.OP_READ);
                } catch (IOException e) {
                    e.printStackTrace();

                }
                ByteBuffer byteBuffer = ByteBuffer.allocate(640);
                while (isReceive) {
                    try {
                        if (selector == null) {
                            return;
                        }
                        int n = selector.select();
                        if (n > 0) {
                            Iterator iterator = selector.selectedKeys().iterator();
                            while (iterator.hasNext()) {
                                SelectionKey key = (SelectionKey) iterator.next();
                                iterator.remove();
                                if (key.isReadable()) {
                                    DatagramChannel dataChannel = (DatagramChannel) key.channel();
                                    byteBuffer.clear();
                                    InetSocketAddress address = (InetSocketAddress) dataChannel.receive(byteBuffer);
                                    String message = new String(byteBuffer.array(), 0, byteBuffer.position());
                                    if (address.getAddress().getHostAddress().equalsIgnoreCase(ipAddress)) {
                                        Log.e(TAG, "address.getAddress().getHostAddress():" + address.getAddress().getHostAddress());
                                        Message message1 = new Message();
                                        message1.what = 105;
                                        message1.obj = message;
                                        mHandler.sendMessage(message1);
                                    }

                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mThread.start();
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == 105) {
            if (listener != null) {
                isReceive = false;
                isUDPReceiveFail = false;
                String message = (String) msg.obj;
                listener.onEsptouchTaskCallback(RESULT_CONFIG_RECEIVE_SUCCESS, message);
            }
        } else if (msg.what == 106) {
            String message = (String) msg.obj;
            listener.onEsptouchTaskCallback(RESULT_CONFIG_MULTI_SUCCESS, message);
        } else if (msg.what == 107) {
            listener.onEsptouchTaskCallback(RESULT_CONFIG_FAILURE, "esptouch fail ...");
        } else if (msg.what == 108) {
            String message = (String) msg.obj;
            listener.onEsptouchTaskCallback(RESULT_CONFIG_SUCCESS, message);
        } else if (msg.what == 109) {
            listener.onEsptouchTaskCallback(RESULT_CONFIG_TIMEOUT, "can not recieve device message...");
        }
        return true;
    }

    /**
     * 0：表示成功配网，接着看message的信息 ；
     * 1：为多个配网信息，还在配网中，其中message是刚刚配对成功的设备 ;
     * 2:表示配网失败;
     * 3:表示成功接受到设备的UDP信息
     * 4:表示超过了设置超时时间，未接受到设备的UDP信息
     */
    public static final int RESULT_CONFIG_SUCCESS = 0x100;
    public static final int RESULT_CONFIG_MULTI_SUCCESS = 0x101;
    public static final int RESULT_CONFIG_FAILURE = 0x102;
    public static final int RESULT_CONFIG_RECEIVE_SUCCESS = 0x103;
    public static final int RESULT_CONFIG_TIMEOUT = 0x104;
}

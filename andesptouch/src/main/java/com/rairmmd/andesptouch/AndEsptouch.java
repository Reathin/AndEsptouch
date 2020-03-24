package com.rairmmd.andesptouch;

import android.content.Context;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.rairmmd.andesptouch.util.ByteUtil;
import com.rairmmd.andesptouch.util.TouchNetUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

/**
 * @author Rair
 * <p>
 * desc:
 */
public class AndEsptouch implements Handler.Callback {

    private final String TAG = "AndEsptouch";

    /**
     * 上下文
     */
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
     * 是否广播   反之为组播
     */
    private boolean isBroadcast = true;

    /**
     * 自定义upd回调
     */
    private boolean isCustomUdpReceiver;

    /**
     * 端口
     */
    private int mPort = 12306;

    /**
     * 自定义的配置异步任务
     */
    private EsptouchAsyncTask mEsptouchAsyncTask;

    /**
     * 配置任务
     */
    private IEsptouchTask mEsptouchTask;

    private OnEsptouchTaskListener onEsptouchTaskListener;

    private int mTimeOut;

    private Handler mHandler;

    private CountDownTimer mCountDownTimer;

    private AndEsptouch(Context mContext) {
        this.mContext = mContext;
        mHandler = new Handler(mContext.getMainLooper(), this);
    }

    public static class Builder {

        private AndEsptouch andEsptouch;

        public Builder(Context mContext) {
            andEsptouch = new AndEsptouch(mContext);
        }

        /**
         * @param ssid 设置路由器名字
         */
        public Builder setSSID(String ssid) {
            andEsptouch.mSsid = ssid;
            return this;
        }

        /**
         * 设置bssid
         */
        public Builder setBSSID(String bssid) {
            andEsptouch.mBssid = bssid;
            return this;
        }

        /**
         * @param passWord 设置路由器密码
         */
        public Builder setPassWord(String passWord) {
            andEsptouch.mPassword = passWord;
            return this;
        }

        /**
         * @param deviceCount 设置要配对的设备个数
         */
        public Builder setDeviceCount(int deviceCount) {
            andEsptouch.mDeviceCount = deviceCount;
            return this;
        }

        /**
         * 是否广播
         */
        public Builder isBroadcast(boolean broadcast) {
            andEsptouch.isBroadcast = broadcast;
            return this;
        }

        /**
         * 是否需要自定义udp消息
         */
        public Builder isCustomUdpReceiver(boolean isCustom) {
            andEsptouch.isCustomUdpReceiver = isCustom;
            return this;
        }

        public AndEsptouch build() {
            return andEsptouch;
        }
    }

    public void setOnEsptouchTaskListener(OnEsptouchTaskListener listener) {
        this.onEsptouchTaskListener = listener;
    }

    public interface OnEsptouchTaskListener {

        /**
         * 配置回调code  message
         *
         * @param code    错误码
         * @param message 消息
         */
        void onEsptouchTaskCallback(int code, String message);
    }

    /**
     * 不接受自定义广播包开始配置
     */
    public void startConfig() {
        Log.d(TAG, "start esptouch config");
        stopConfig();
        mEsptouchAsyncTask = new EsptouchAsyncTask();
        byte[] ssid = ByteUtil.getBytesByString(mSsid);
        byte[] bssid = TouchNetUtil.parseBssid2bytes(mBssid);
        byte[] password = ByteUtil.getBytesByString(mPassword);
        byte[] count = ByteUtil.getBytesByString(String.valueOf(mDeviceCount));
        mEsptouchAsyncTask.execute(ssid, bssid, password, count);
    }

    /**
     * 设置为配网模式+接受此设备UDP信息
     *
     * @param timesOut 设置超时时间 秒
     * @param port     设置UDP本地的端口
     */
    public void startConfig(int timesOut, int port) {
        Log.d(TAG, "start esptouch config");
        stopConfig();
        mEsptouchAsyncTask = new EsptouchAsyncTask();
        byte[] ssid = ByteUtil.getBytesByString(mSsid);
        byte[] bssid = TouchNetUtil.parseBssid2bytes(mBssid);
        byte[] password = ByteUtil.getBytesByString(mPassword);
        byte[] count = ByteUtil.getBytesByString(String.valueOf(mDeviceCount));
        mEsptouchAsyncTask.execute(ssid, bssid, password, count);
        this.mTimeOut = timesOut;
        this.mPort = port;
    }

    /**
     * 停止配置
     */
    public void stopConfig() {
        Log.d(TAG, "stop esptouch config");
        if (mEsptouchTask != null) {
            mEsptouchTask.interrupt();
            mEsptouchTask = null;
        }
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        if (mEsptouchAsyncTask != null) {
            mEsptouchAsyncTask = null;
        }
    }

    private class EsptouchAsyncTask extends AsyncTask<byte[], IEsptouchResult, List<IEsptouchResult>> {

        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
            synchronized (mLock) {
                if (mEsptouchTask != null) {
                    mEsptouchTask.interrupt();
                }
            }
        }

        @Override
        protected List<IEsptouchResult> doInBackground(byte[]... params) {
            int taskResultCount;
            synchronized (mLock) {
                byte[] ssid = params[0];
                byte[] bssid = params[1];
                byte[] password = params[2];
                byte[] deviceCount = params[3];
                taskResultCount = deviceCount.length == 0 ? -1 : Integer.parseInt(new String(deviceCount));
                mEsptouchTask = new EsptouchTask(ssid, bssid, password, mContext);
                mEsptouchTask.setPackageBroadcast(isBroadcast);
                mEsptouchTask.setEsptouchListener(new IEsptouchListener() {
                    @Override
                    public void onEsptouchResultAdded(IEsptouchResult result) {
                        Log.d(TAG, "esptouch config result:" + result.getInetAddress().toString());
                        String resultMsg = String.format("BSSID:%s-IP:%s", result.getBssid(), result.getInetAddress().getHostName());
                        Message message = Message.obtain();
                        message.what = RESULT_CONFIG_SUCCESS;
                        message.obj = resultMsg;
                        mHandler.sendMessage(message);
                        if (isCustomUdpReceiver) {
                            startReceiverData(result.getInetAddress().getHostAddress());
                        }
                    }
                });
            }
            return mEsptouchTask.executeForResults(taskResultCount);
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> iEsptouchResults) {
            if (iEsptouchResults == null) {
                //端口可能被其它程序占用
                return;
            }
            //检查任务是否取消或者没有收到结果
            IEsptouchResult result = iEsptouchResults.get(0);
            if (result.isCancelled()) {
                return;
            }
            if (!result.isSuc()) {
                //配网失败
                mHandler.sendEmptyMessage(RESULT_CONFIG_FAILURE);
                return;
            }
            //配网成功
            StringBuilder builder = new StringBuilder();
            for (IEsptouchResult touchResult : iEsptouchResults) {
                String message = String.format("BSSID:%s-IP:%s", touchResult.getBssid(),
                        touchResult.getInetAddress().getHostName());
                builder.append(message).append("\n");
                if (isCustomUdpReceiver) {
                    startReceiverData(touchResult.getInetAddress().getHostName());
                }
            }
            if (mDeviceCount != 1) {
                Message message = Message.obtain();
                message.what = RESULT_CONFIG_MULTI_SUCCESS;
                message.obj = builder.toString();
                mHandler.sendMessage(message);
            }
        }
    }

    private void startReceiverData(final String ip) {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 绑定了指定的端口
                    DatagramSocket socket = new DatagramSocket(mPort);
                    // 创建一个用来接受发来的数据报的数据报包
                    DatagramPacket datagramPacket = new DatagramPacket(new byte[1024], 1024);
                    // 把收到的信息封装到DatagramPacket
                    // 也是一个阻塞式方法。一直等到有人发来数据报
                    socket.receive(datagramPacket);
                    // 发送人
                    InetAddress address = datagramPacket.getAddress();
                    // 发送方的端口
                    int port = datagramPacket.getPort();
                    // 存储发送过来的数据的字节数组
                    byte[] data = datagramPacket.getData();
                    // 发送过来的信息的实际长度
                    int length = datagramPacket.getLength();
                    String resultMsg = new String(data, 0, length);
                    Log.i(TAG, address + " " + port + "  " + resultMsg);
                    if (ip.equals(address)) {
                        Message message = Message.obtain();
                        message.what = RESULT_CONFIG_RECEIVE_SUCCESS;
                        message.obj = resultMsg;
                        mHandler.sendMessage(message);
                    } else {
                        mHandler.sendEmptyMessage(RESULT_CONFIG_RECEIVE_FAILURE);
                    }
                } catch (SocketException e) {
                    mHandler.sendEmptyMessage(RESULT_CONFIG_RECEIVE_FAILURE);
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(RESULT_CONFIG_RECEIVE_FAILURE);
                }
            }
        });
        thread.start();
        mCountDownTimer = new CountDownTimer(mTimeOut * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                mHandler.sendEmptyMessage(RESULT_CONFIG_TIMEOUT);
                if (thread != null) {
                    thread.interrupt();
                }
            }
        };
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == RESULT_CONFIG_RECEIVE_SUCCESS) {
            String message = (String) msg.obj;
            onEsptouchTaskListener.onEsptouchTaskCallback(RESULT_CONFIG_RECEIVE_SUCCESS, message);
        } else if (msg.what == RESULT_CONFIG_RECEIVE_FAILURE) {
            onEsptouchTaskListener.onEsptouchTaskCallback(RESULT_CONFIG_RECEIVE_FAILURE, "receiver is error");
        } else if (msg.what == RESULT_CONFIG_MULTI_SUCCESS) {
            String message = (String) msg.obj;
            onEsptouchTaskListener.onEsptouchTaskCallback(RESULT_CONFIG_MULTI_SUCCESS, message);
        } else if (msg.what == RESULT_CONFIG_SUCCESS) {
            String message = (String) msg.obj;
            onEsptouchTaskListener.onEsptouchTaskCallback(RESULT_CONFIG_SUCCESS, message);
        } else if (msg.what == RESULT_CONFIG_FAILURE) {
            onEsptouchTaskListener.onEsptouchTaskCallback(RESULT_CONFIG_FAILURE, "esptouch fail ...");
        } else if (msg.what == RESULT_CONFIG_TIMEOUT) {
            onEsptouchTaskListener.onEsptouchTaskCallback(RESULT_CONFIG_TIMEOUT, "can not recieve device message...");
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
    public static final int RESULT_CONFIG_FAILURE = 0x102;
    public static final int RESULT_CONFIG_MULTI_SUCCESS = 0x101;
    public static final int RESULT_CONFIG_RECEIVE_SUCCESS = 0x103;
    public static final int RESULT_CONFIG_TIMEOUT = 0x104;
    public static final int RESULT_CONFIG_RECEIVE_FAILURE = 0x105;

}

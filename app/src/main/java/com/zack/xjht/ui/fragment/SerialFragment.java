package com.zack.xjht.ui.fragment;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.zack.xjht.R;
import com.zack.xjht.Utils.DialogUtils;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.Utils.TransformUtil;
import com.zack.xjht.ui.dialog.AlarmDialog;

import org.winplus.serial.utils.SerialPort;
import org.winplus.serial.utils.SerialPortFinder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 串口调试
 * A simple {@link Fragment} subclass.
 */
public class SerialFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "SerialFragment";
    Unbinder unbinder;
    @BindView(R.id.edt_serial_receive)
    TextView edtSerialReceive;
    @BindView(R.id.compat_spinner_serial_device)
    AppCompatSpinner spinnerDevice;
    @BindView(R.id.compat_spinner_serial_baudrate)
    AppCompatSpinner spinnerBaudrate;
    @BindView(R.id.btn_spinner_open)
    Button btnSpinnerOpen;
    @BindView(R.id.edt_serial_emission)
    EditText edtSerialEmission;
    @BindView(R.id.serial_rb_txt_send)
    RadioButton serialRbTxtSend;
    @BindView(R.id.serial_rb_hex_send)
    RadioButton serialRbHexSend;
    @BindView(R.id.btn_receive_clear)
    Button btnReceiveClear;
    @BindView(R.id.btn_send_clear)
    Button btnSendClear;
    @BindView(R.id.cb_serial_auto_send)
    CheckBox cbSerialAutoSend;
    @BindView(R.id.serial_btn_send)
    Button serialBtnSend;
    @BindView(R.id.send_sleep_time)
    EditText sendSleepTime;

    private View view;
    private SerialPortFinder finder;
    private SerialPort serialPort;
    private String[] allDevicesPath;
    private String[] allBaudrate;
    private boolean isTxt;
    private boolean isHex;

    private static final long SLEEP_TIME = 500L;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String data = msg.getData().getString("data");
            byte[] bytes = msg.getData().getByteArray("bytes");
            int size = msg.getData().getInt("size");
            StringBuilder sb = new StringBuilder();
            if (isTxt) {//txt
                if (!TextUtils.isEmpty(data)) {
                    setTxt(sb, "TXT ");
                    sb.append(data);
                    if(edtSerialReceive !=null) {
                        edtSerialReceive.append(sb);
                    }
                }
            } else if (isHex) { //hex
                if (bytes != null) {
                    //接收16进制字符串
                    String hexStr = TransformUtil.BinaryToHexString(bytes);
//                    Log.i(TAG, "handleMessage hex: " + hexStr);
                    setTxt(sb, "HEX ");
                    sb.append(hexStr);
                    if(edtSerialReceive !=null){
                        edtSerialReceive.append(sb);
                    }
                }
            }
        }
    };
    private AlarmDialog alarmDialog;

    public void format(String hexStr) {
        if (!TextUtils.isEmpty(hexStr)) {
            String[] split = hexStr.split(" "); //拆分单个字节
            if (split.length == 7) { //长度为7
                Log.i(TAG, "format  split: " + split[1]); //锁地址
                int i2 = Integer.parseInt(split[1]);
//                for (int i = 0; i < nums.length; i++) {
//                    if(nums[i] ==i2){ //打开的枪锁
//                    }
            }
        } else {
            Log.i(TAG, "数据格式错误！: ");
            return;
        }
    }

    private void setTxt(StringBuilder sb, String txtType) {
        String format = new SimpleDateFormat("HH:mm:ss:SSS").format(new Date());
//        Log.i(TAG, "setText time: " + format);
        sb.append("\r\n");
        sb.append(txtType);
        sb.append(" [" + format + "] ");
    }

    private String serialPath;
    private String baudrate;

    private HandlerThread mThread;
    private Handler mHandler;

    public SerialFragment() {
    }

    private boolean isAuto;
    private TaskSend taskSend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        view = inflater.inflate(R.layout.fragment_serial, container, false);
        unbinder = ButterKnife.bind(this, view);

        mThread = new HandlerThread("send thread");
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
        taskSend = new TaskSend();
        initSpinner();
        sendSleepTime.setText(SLEEP_TIME + "");
        edtSerialEmission.setText("51310d");
        serialRbHexSend.setChecked(true);
        if (serialRbHexSend.isChecked()) {
            isTxt = false;
            isHex = true;
        }
        initEvent();
        return view;
    }

    Dialog dialog = null;

    private void initEvent() {
        //自动发送
        cbSerialAutoSend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "onCheckedChanged AutoSend isChecked: " + isChecked);

                if (isChecked) {//自动发送 根据间隔时间发送
                    isAuto = true;
                    if (serialPort == null) {
                        showDialog();
                        return;
                    }
                } else {
                    isAuto = false;
                }
                if (isAuto) {
                    taskSend.run();
                } else {
                    mHandler.removeCallbacks(taskSend);
                }
            }
        });
        //字符
        serialRbTxtSend.setOnCheckedChangeListener(this);
        serialRbHexSend.setOnCheckedChangeListener(this);
    }

    private void showDialog() {
        dialog = DialogUtils.creatTipDialog(getContext(), "提示", "请先打开串口",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cbSerialAutoSend.setChecked(false);
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    public void initSpinner() {
        Log.i(TAG, "initSpinner: ");
        finder = new SerialPortFinder();
        allDevicesPath = finder.getAllDevicesPath();

        ArrayAdapter<String> deviceAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item, allDevicesPath);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        allBaudrate = getResources().getStringArray(R.array.baud_rate);
        Log.i(TAG, "initSpinner  allDevicesPath size: " + allDevicesPath.length +
                " allBaudrate size:" + allBaudrate.length);
        ArrayAdapter<String> baudAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item, allBaudrate);
        baudAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerDevice.setAdapter(deviceAdapter);
        spinnerBaudrate.setAdapter(baudAdapter);
        Log.i(TAG, "initSpinner  device path: " + SharedUtils.getPathPos() +
                " baud position:" + SharedUtils.getBaudPos());

        try {
            spinnerDevice.setSelection(SharedUtils.getPathPos());
            spinnerBaudrate.setSelection(SharedUtils.getBaudPos());
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            ToastUtil.showShort("ERROR:" + e.getMessage());
//            spinnerDevice.setSelection(0);
//            spinnerBaudrate.setSelection(0);
        }

        spinnerDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String path = allDevicesPath[position];
                Log.i(TAG, "onItemSelected path: " + path);
                SharedUtils.saveSerialPath(path);
                SharedUtils.savePathPos(position);
                if (btnSpinnerOpen.getText().equals("关闭")) {
                    close();
                    btnSpinnerOpen.setText("打开");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i(TAG, "onNothingSelected: ");
            }
        });

        spinnerBaudrate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String baud = allBaudrate[position];
                Log.i(TAG, "onItemSelected baud: " + baud);
                SharedUtils.saveBaudrate(baud);
                SharedUtils.saveBaudPos(position);
                if (btnSpinnerOpen.getText().equals("关闭")) {
                    close();
                    btnSpinnerOpen.setText("打开");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i(TAG, "onNothingSelected: ");
            }
        });
    }

    private ReadThread mReadThread;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (this.serialPort == null) {
            serialPath = SharedUtils.getSerialPath();
            baudrate = SharedUtils.getBaudrate();
            //获取串口设备
            Log.i(TAG, "getSerialPort 串口地址: " + serialPath);
            //获取波特率
            int i = Integer.parseInt(baudrate);
            Log.i(TAG, "getSerialPort 波特率： " + i);
            if ((serialPath.length() == 0) || (i == -1)) {
                throw new InvalidParameterException();
            }
            this.serialPort = new SerialPort(new File(serialPath), i, 0);
        }
        return this.serialPort;
    }

    public void initSerialPort() {
        if (serialPort == null) {
            try {
                serialPort = getSerialPort();
                mOutputStream = this.serialPort.getOutputStream();
                mInputStream = this.serialPort.getInputStream();
                this.mReadThread = new ReadThread();
                this.mReadThread.start();
            } catch (SecurityException e) {
                DisplayError(R.string.error_security);
                return;
            } catch (IOException e) {
                DisplayError(R.string.error_unknown);
                return;
            } catch (InvalidParameterException e) {
                DisplayError(R.string.error_configuration);
            }
        }
    }

    private void DisplayError(int paramInt) {
        final AlertDialog.Builder localBuilder = new AlertDialog.Builder(getActivity());
        localBuilder.setTitle("Error");
        localBuilder.setMessage(paramInt);
        localBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {

            }
        });
        localBuilder.show();
    }

    @OnClick({R.id.btn_spinner_open, R.id.btn_receive_clear, R.id.btn_send_clear, R.id.serial_btn_send})
    public void onViewClicked(View view) {

        switch (view.getId()) {
            case R.id.btn_spinner_open:
                //保存当前选择
                if (btnSpinnerOpen.getText().equals("打开")) {
                    initSerialPort();
                    isStop = false;
                    btnSpinnerOpen.setText("关闭");
                } else if (btnSpinnerOpen.getText().equals("关闭")) {
                    close();
                    btnSpinnerOpen.setText("打开");
                }
                break;
            case R.id.btn_receive_clear:
                edtSerialReceive.setText("");
                break;
            case R.id.btn_send_clear:
                edtSerialEmission.setText("");
                break;
            case R.id.serial_btn_send: //发送

                if (serialPort == null) {
                    showDialog();
                    return;
                }
                String cmd = edtSerialEmission.getText().toString();
                if (!TextUtils.isEmpty(cmd)) {
                    if (mOutputStream != null) {
                        try {
                            byte[] bytes = TransformUtil.hex2bytes(cmd);
                            mOutputStream.write(bytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                break;
        }
    }

    class TaskSend implements Runnable {

        @Override
        public void run() {
            if (isAuto) {
                sendData();
                String sleepTime = sendSleepTime.getText().toString();
                if (!TextUtils.isEmpty(sleepTime)) {
                    mHandler.postDelayed(this, Long.parseLong(sleepTime));
                }
            }
        }
    }

    private void sendData() {
        String data = edtSerialEmission.getText().toString();
        if (!TextUtils.isEmpty(data)) {
            if (isTxt) { //字符发送
                Log.i(TAG, "sendData  isTxt: ");
                if (mOutputStream != null) {
                    try {
                        mOutputStream.write(data.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (isHex) {
                Log.i(TAG, "sendData isHex: ");
                try {
                    if (mOutputStream != null) {
                        mOutputStream.write(TransformUtil.hex2bytes(data));
                        Log.i(TAG, "sendData send over: ");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isStop;

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == serialRbTxtSend) {
            Log.i(TAG, "onCheckedChanged serialRbTxtSend: " + isChecked);
            if (isChecked) {
                isTxt = true;
                isHex = false;
            }
        } else if (buttonView == serialRbHexSend) {
            Log.i(TAG, "onCheckedChanged serialRbHexSend: " + isChecked);
            if (isChecked) {
                isHex = true;
                isTxt = false;
            }
        }
    }

    private class ReadThread extends Thread {
        private Message msg;
        private Bundle bundle = null;

        public void run() {
            super.run();
            Log.i(TAG, "run : ");
            for (; ; ) {
                if (!isStop && !isInterrupted()) {
                    try {
                        if (mInputStream == null) {
                            return;
                        }
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        int available = mInputStream.available();
                        if (available == 0) {
                            continue;
                        }
                        byte[] bytes = new byte[available];
                        int size = mInputStream.read(bytes);
                        if (size > 0) {
                            final String data = new String(bytes, 0, size);
                            msg = handler.obtainMessage();
                            bundle = new Bundle();
                            bundle.putString("data", data);
                            bundle.putByteArray("bytes", bytes);
                            bundle.putInt("size", size);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                            String hexString = TransformUtil.BinaryToHexString(bytes);
//                            sendAlarmInfo(hexString);
                            Log.i(TAG, "run  bytes: " + hexString);

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach: ");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated: ");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView: ");
        unbinder.unbind();
        close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "onDetach: ");
    }

    private void close() {
        mHandler.removeCallbacks(taskSend);
        if (cbSerialAutoSend != null) {
            cbSerialAutoSend.setChecked(false);
        }
        isAuto = false;
        isStop = true;
        if (this.mReadThread != null && !this.mReadThread.isInterrupted()) {
            this.mReadThread.interrupt();
        }
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
    }
}

package com.zack.xjht.ui.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.zack.xjht.R;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.alcohol.Alcohol;
import com.zack.xjht.alcohol.OnAlcoholValueListener;
import com.zack.xjht.hardware.Sensor;
import com.zack.xjht.humiture.OnHumitureListener;
import com.zack.xjht.serial.SerialPortUtil;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * debug
 */
public class DebugFragment extends Fragment implements OnHumitureListener, OnAlcoholValueListener {

    private static final String TAG = "DebugFragment";
    Unbinder unbinder;
    @BindView(R.id.btn_volume_up)
    Button btnVolumeUp;
    @BindView(R.id.btn_volume_down)
    Button btnVolumeDown;
    @BindView(R.id.music_et_switch_code)
    EditText musicEtSwitchCode;
    @BindView(R.id.btn_open_alarm)
    Button btnOpenAlarm;
    @BindView(R.id.btn_open_alcohol)
    Button btnOpenAlcohol;
    @BindView(R.id.btn_open_door_lock)
    Button btnOpenDoorLock;
    @BindView(R.id.btn_read_humiture_value)
    Button btnReadHumitureValue;
    @BindView(R.id.btn_read_alcohol_value)
    Button btnReadAlcoholValue;
    @BindView(R.id.btn_read_ups_status)
    Button btnReadUpsStatus;
    @BindView(R.id.edt_receive_msg)
    EditText edtReceiveMsg;
    @BindView(R.id.btn_clear_receive)
    Button btnClearReceive;
    @BindView(R.id.btn_switch_power)
    Button btnSwitchPower;
    @BindView(R.id.btn_close_alarm)
    Button btnCloseAlarm;
    private View view;
    private Context mContext;

    private AudioManager audioManager;

    public DebugFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_music_setup, container, false);
        if (getActivity() != null) {
            audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        }
        unbinder = ButterKnife.bind(this, view);
        btnSwitchPower.setVisibility(View.GONE);
        SerialPortUtil.getInstance().setOnHumitureValueListener(this);
        Alcohol.getInstance().setOnAlcoholValueListener(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        Alcohol.getInstance().close();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (edtReceiveMsg != null) {
                edtReceiveMsg.append((String) msg.obj + "\n");
            }
        }
    };

    @OnClick({R.id.btn_volume_up, R.id.btn_volume_down, R.id.btn_open_alarm, R.id.btn_open_alcohol,
            R.id.btn_open_door_lock, R.id.btn_read_humiture_value, R.id.btn_read_alcohol_value,
            R.id.btn_read_ups_status, R.id.btn_clear_receive, R.id.btn_switch_power,
            R.id.btn_close_alarm})
    public void onViewClicked(View view) {
        String code = musicEtSwitchCode.getText().toString();
        switch (view.getId()) {
            case R.id.btn_volume_up://加大音量
                if (audioManager != null) {
                    audioManager.adjustStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_RAISE,
                            AudioManager.FLAG_SHOW_UI);
                }
                break;
            case R.id.btn_volume_down://减小音量
                if (audioManager != null) {
                    audioManager.adjustStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_LOWER,
                            AudioManager.FLAG_SHOW_UI);
                }
                break;
            case R.id.btn_open_alarm: //打开报警器 1打开 0关闭
                edtReceiveMsg.append("打开报警器\n");
                SerialPortUtil.getInstance().openAlertor(Integer.parseInt(SharedUtils.getLeftCabNo()));
                break;
            case R.id.btn_close_alarm:
                edtReceiveMsg.append("关闭报警器\n");
                SerialPortUtil.getInstance().closeAlertor(Integer.parseInt(SharedUtils.getLeftCabNo()));
                break;
            case R.id.btn_open_alcohol://打开酒精传感器 1打开 0关闭
                if (!TextUtils.isEmpty(code)) {
                    if (code.equals("1")) {
                        edtReceiveMsg.append("打开酒精传感器\n");
                    } else if (code.equals("0")) {
                        edtReceiveMsg.append("关闭酒精传感器\n");
                    }
                }
                break;
            case R.id.btn_open_door_lock://打开门锁 1打开 0关闭
                if (!TextUtils.isEmpty(code)) {
                    if (code.equals("1")) {
                        edtReceiveMsg.append("打开枪柜门锁\n");
                    } else if (code.equals("0")) {
                        edtReceiveMsg.append("关闭枪柜门锁\n");
                    }
                    Sensor.getInstance().switchDoorLock(Integer.parseInt(code));
                }
                break;
            case R.id.btn_read_humiture_value: //读取温湿度值
                SerialPortUtil.getInstance().checkHumiture();
                break;
            case R.id.btn_read_alcohol_value://读取酒精溶度值
                Alcohol.getInstance().checkAlcohol();
                break;
            case R.id.btn_read_ups_status:
                //查询电源参数
                SerialPortUtil.getInstance().checkStatus(SharedUtils.getPowerAddress());
                break;
            case R.id.btn_clear_receive:
                edtReceiveMsg.setText("");
                break;
            case R.id.btn_switch_power:
                if (btnSwitchPower.getText().toString().equals("通电")) {
                    btnSwitchPower.setText("断电");
                    SerialPortUtil.getInstance().powerOn();
                } else if (btnSwitchPower.getText().toString().equals("断电")) {
                    btnSwitchPower.setText("通电");
                    SerialPortUtil.getInstance().powerOff();
                }
                break;
        }
    }

    @Override
    public void onHumitureValue(final float temperature, final float humidity) {
        SharedUtils.saveHumidityValue(humidity);
        SharedUtils.saveTemperatureValue(temperature);
        if (edtReceiveMsg != null) {
            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    edtReceiveMsg.append("温度：" + temperature + "℃"
                            + "湿度：" + humidity + "%\n");
                }
            });
        }
    }

    @Override
    public void onAlcoholValue(final String alcohol) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (edtReceiveMsg != null) {
                    edtReceiveMsg.append("酒精传感器检测值：" + alcohol + "V\n");
                }
            }
        });

    }
}

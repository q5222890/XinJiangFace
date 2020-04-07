package com.zack.xjht.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zack.xjht.R;
import com.zack.xjht.event.StatusEvent;
import com.zack.xjht.serial.SerialPortUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SetBulletWeightDialog extends DialogFragment {
    private static final String TAG = "SetAddressDialog";
    @BindView(R.id.dl_tv_address_tv_msg)
    TextView dlTvAddressTvMsg;
    @BindView(R.id.dl_btn_clear_bullet_weight)
    Button dlBtnAddressBtnStart;
    Unbinder unbinder;
    @BindView(R.id.dl_edt_bullet_num)
    EditText dlEdtBulletNum;

    private Context mContext;
    private int address = 1;
    private boolean isNext = false;
    private int time = 5;


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0://修改按钮文字
                    String btnText = (String) msg.obj;
                    if (dlBtnAddressBtnStart != null && !TextUtils.isEmpty(btnText)) {
                        dlBtnAddressBtnStart.setText(btnText);
                    }
                    break;
                case 1://改变提示文字
                    String msgText = (String) msg.obj;
                    if (dlTvAddressTvMsg != null && !TextUtils.isEmpty(msgText)) {
                        dlTvAddressTvMsg.setText(msgText);
                    }
                    break;
                case 2://设置按钮禁用
//                    if (dlBtnAddressBtnStart != null) {
//                        dlBtnAddressBtnStart.setEnabled(false);
//                    }
                    break;
                case 3://设置按钮启用
//                    if (dlBtnAddressBtnStart != null) {
//                        dlBtnAddressBtnStart.setEnabled(true);
//                    }
                    break;
            }
        }
    };
    private int num;

    public SetBulletWeightDialog() {
        super();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dl_set_bullet_weight, container, false);
        unbinder = ButterKnife.bind(this, view);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        setCancelable(true);

        assert getArguments() != null;
        num = getArguments().getInt("num");
        Log.i(TAG, "onCreateView num: " + num);


        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubscriber(StatusEvent event) {
        int address1 = event.getAddress();
        String message = event.getMessage();
        Log.i(TAG, "onSubscriber  address : " + address1 + " message:" + message);
        if (address1 == address) {
            //
            isNext = true;
        }
//        edtLockReceiveMsg.setText(message);
    }

    private Timer timer;

    @OnClick(R.id.dl_btn_clear_bullet_weight)
    public void onViewClicked() {
        //先发送设置地址命令 再延时发送查询命令 没收到应答间隔时间查询 当查询命令收到应答 将地址+1继续循环发送
        //地址开始淸0
        new Thread(new Runnable() {
            @Override
            public void run() {
                String btnText = dlBtnAddressBtnStart.getText().toString();
                /*
                1.按钮显示‘开始设置’
                2.点击开始设置
                3.自动打开抽屉
                4.显示文字提示-请清空子弹抽屉 倒计时10秒，按钮显示‘清空完成’
                5.点击清空完成，显示文字提示-请在抽屉内放置子弹 倒计时10s，按钮显示‘放置子弹完成’
                6.点击放置子弹完成，开始设置子弹数量，倒计时10s，按钮显示‘设置子弹数量完成’
                7.点击设置子弹数量完成 完成设置
                */
                if (btnText.equals("开始设置")) {
                    SerialPortUtil.getInstance().openLock(num);
                    setButtonText(0, "设置皮重");//按钮文字
                    setButtonText(1, "请清空子弹抽屉，清空完成后请点击设置皮重按钮");
                } else if (btnText.equals("设置皮重")) {
                    //清空子弹完成， 开始设置皮重
                    SerialPortUtil.getInstance().setBulletTare(String.valueOf(num));
                    setButtonText(0, "设置子弹数量");//按钮文字
                    setButtonText(1, "开始设置皮重");//提示文字
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            if (time >= 0) {
                                setButtonText(1, "正在设置皮重，请稍候。。。");//提示文字
                                time--;
                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dlEdtBulletNum.setVisibility(View.VISIBLE);
                                        setButtonText(1, "设置皮重完成，" +
                                                "请输入子弹数量并在抽屉中放置对应子弹个数，" +
                                                "放置完成后点击按钮开始设置子弹数量");//提示文字
                                    }
                                });
                                timer.cancel();
                                time = 5;
                            }
                        }
                    };

                    timer = new Timer();
                    timer.schedule(timerTask, 0, 1000);
                } else if (btnText.equals("设置子弹数量")) {
                    String number = dlEdtBulletNum.getText().toString().trim();
                    if(TextUtils.isEmpty(number)){
                        setButtonText(1, "请输入放置子弹数量并保持抽屉中子弹数量一致");
                       return;
                    }
                    SerialPortUtil.getInstance().setBulletWeight(num, Integer.parseInt(number));
                    setButtonText(0, "设置完成");
                    setButtonText(1, "正在设置子弹数量。。。");

                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            if (time >= 0) {
                                setButtonText(1, "正在设置子弹数量，倒计时:" + time);
                                time--;
                            } else {
                                setButtonText(1, "设置子弹数量已完成" );
                                timer.cancel();
                                time = 5;
                            }
                        }
                    };

                    timer = new Timer();
                    timer.schedule(timerTask, 0, 1000);
                } else if (btnText.equals("设置完成")) {
                    dismiss();
                }
            }
        }).start();
    }

    public void setButtonText(int what, String text) {
        Message msg = handler.obtainMessage();
        msg.what = what;
        msg.obj = text;
        handler.sendMessage(msg);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        time =0;
        if(timer !=null){
            timer.cancel();
        }
    }
}

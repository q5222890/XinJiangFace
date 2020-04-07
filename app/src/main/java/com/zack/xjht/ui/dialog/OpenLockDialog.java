package com.zack.xjht.ui.dialog;

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
import android.widget.TextView;

import com.zack.xjht.R;
import com.zack.xjht.event.StatusEvent;
import com.zack.xjht.serial.SerialPortUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class OpenLockDialog extends DialogFragment {
    private static final String TAG = "SetAddressDialog";
    @BindView(R.id.dl_set_address_tv_msg)
    TextView dlSetAddressTvMsg;
    @BindView(R.id.dl_set_address_btn_start)
    Button dlSetAddressBtnStart;
    Unbinder unbinder;

    private Context mContext;
    private int address =1;
    private boolean isNext =false;

    Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0://修改按钮文字
                    String btnText = (String) msg.obj;
                    if(dlSetAddressBtnStart !=null && !TextUtils.isEmpty(btnText)){
                        dlSetAddressBtnStart.setText(btnText);
                    }
                    break;
                case 1://改变提示文字
                    String msgText = (String) msg.obj;
                    if(dlSetAddressTvMsg !=null && !TextUtils.isEmpty(msgText)){
                        dlSetAddressTvMsg.setText(msgText);
                    }
                    break;
            }
        }
    };
    private int num;

    public OpenLockDialog() {
        super();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dl_open_lock, container, false);
        unbinder = ButterKnife.bind(this, view);
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
         num = getArguments().getInt("num");
        Log.i(TAG, "onCreateView num: "+num);
        setCancelable(false);
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubscriber(StatusEvent event) {
        int address1 = event.getAddress();
        String message = event.getMessage();
        Log.i(TAG, "onSubscriber  address : " + address1 + " message:" + message);
        if(address1 ==address){
            //是当前设置的地址 改变状态 设置下一个地址
            isNext =true;
        }
//        edtLockReceiveMsg.setText(message);
    }

   private boolean clean =true;
    private boolean stop =false;
    private int lockNo =1;
    @OnClick(R.id.dl_set_address_btn_start)
    public void onViewClicked() {

        //先发送设置地址命令 再延时发送查询命令 没收到应答间隔时间查询 当查询命令收到应答 将地址+1继续循环发送
        //地址开始淸0
        new Thread(new Runnable() {
            @Override
            public void run() {
                String btnText = dlSetAddressBtnStart.getText().toString();
                if(btnText.equals("开锁")){
//                    dlSetAddressBtnStart.setText("清零完成");
                    setButtonText(0,"开锁完成");
//                    setButtonText(1,"正在重新设置所有枪锁地址，请按压枪锁");
//                    while(stop){
                    while (!stop){
                        for (int i = 0; i <= num; i++) {
                            setButtonText(1,"正在打开"+(i)+"号枪锁");
                            SerialPortUtil.getInstance().openLock(i);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(stop){
                                break;
                            }
                        }
                        for (int i = 0; i <= num; i++) {
                            setButtonText(1,"正在关闭"+(i)+"号枪锁");
                            SerialPortUtil.getInstance().closeLock(i);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(stop){
                                break;
                            }
                        }
                    }
//                    }
                }else if(btnText.equals("开锁完成")){
                    stop =true;
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

}

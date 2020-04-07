package com.zack.xjht.ui.fragment;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;
import com.zack.xjht.R;
import com.zack.xjht.Utils.DialogUtils;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.SoundPlayUtil;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.ethernet.EthernetMain;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 基本设置
 */
public class BasicFragment extends Fragment implements OnDateSetListener {

    private static final String TAG = BasicFragment.class.getSimpleName();
    @BindView(R.id.basic_et_server_ip)
    EditText basicEtServerIp;
    @BindView(R.id.bt_set_save)
    Button btSetSave;
    Unbinder unbinder;
    @BindView(R.id.basic_tv_server_ip)
    TextView basicTvServerIp;
    @BindView(R.id.basic_tv_server_port)
    TextView basicTvServerPort;
    @BindView(R.id.basic_et_server_port)
    EditText basicEtServerPort;
    @BindView(R.id.basic_tv_tittle_server)
    TextView basicTvTittleServer;
    @BindView(R.id.basic_rl_server_set)
    RelativeLayout basicRlServerSet;
    @BindView(R.id.basic_tv_set_local_ip)
    TextView basicTvSetLocalIp;
    @BindView(R.id.basic_edt_static_ip)
    EditText basicEdtStaticIp;
    @BindView(R.id.basic_edt_static_dns)
    EditText basicEdtStaticDns;
    @BindView(R.id.basic_ll_local_set)
    LinearLayout basicLlLocalSet;
    @BindView(R.id.basic_edt_static_netmask)
    EditText basicEdtStaticNetmask;
    @BindView(R.id.basic_edt_static_gateway)
    EditText basicEdtStaticGateway;
    @BindView(R.id.basic_btn_save_local)
    Button basicBtnSaveLocal;
    @BindView(R.id.basic_ll_local_set02)
    LinearLayout basicLlLocalSet02;
    @BindView(R.id.basic_edt_static_dns2)
    EditText basicEdtStaticDns2;
    @BindView(R.id.basic_ll_local_set03)
    LinearLayout basicLlLocalSet03;
    @BindView(R.id.basic_tv_set_date)
    TextView basicTvSetDate;
    @BindView(R.id.basic_edt_date_time)
    EditText basicEdtDateTime;
    @BindView(R.id.basic_btn_date_time)
    Button basicBtnDateTime;
    @BindView(R.id.basic_ll_set_date_time)
    LinearLayout basicLlSetDateTime;
    @BindView(R.id.basic_tv_set_platform_ip)
    TextView basicTvSetPlatformIp;
    @BindView(R.id.basic_edt_server)
    EditText basicEdtServer;
    @BindView(R.id.basic_btn_save_server)
    Button basicBtnSaveServer;
    @BindView(R.id.basic_rb_optical)
    RadioButton basicRbOptical;
    @BindView(R.id.basic_rb_capacitive)
    RadioButton basicRbCapacitive;
    @BindView(R.id.basic_rb_fingerprint_type)
    RadioGroup basicRbFingerprintType;
    private View view;
    private EthernetMain ethernetMain;
    private Context mContext;
    private TimePickerDialog mDialogAll;
    private long millseconds;

    public BasicFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_basic, container, false);
        unbinder = ButterKnife.bind(this, view);
        Log.i(TAG, "onCreateView: ");
        init();
        return view;
    }

    private void init() {
        basicEtServerIp.setText(SharedUtils.getServerIp());
        basicEtServerPort.setText(SharedUtils.getServerPort());
        ethernetMain = new EthernetMain(mContext);
        String staticIP = ethernetMain.getStaticIP();
        String gateway = ethernetMain.getGateway();
        String netMask = ethernetMain.getNetMask();
        String dns1 = ethernetMain.getDNS1();
        String dns2 = ethernetMain.getDNS2();
        basicEdtStaticIp.setText(staticIP);
        basicEdtStaticDns.setText(dns1);
        basicEdtStaticDns2.setText(dns2);
        basicEdtStaticNetmask.setText(netMask);
        basicEdtStaticGateway.setText(gateway);

        String dateTime = Utils.longTime2String(System.currentTimeMillis());
        basicEdtDateTime.setText(dateTime);

        long tenYears = 10L * 365 * 1000 * 60 * 60 * 24L; //年天毫秒秒分小时
        mDialogAll = new TimePickerDialog.Builder()
                .setCallBack(this)
                .setCancelStringId("取消")
                .setSureStringId("确定")
                .setTitleStringId("选择日期和时间")
                .setYearText("年")
                .setMonthText("月")
                .setDayText("日")
                .setHourText("时")
                .setMinuteText("分")
                .setCyclic(false)
                .setMinMillseconds(System.currentTimeMillis() - tenYears)
                .setMaxMillseconds(System.currentTimeMillis() + tenYears)
                .setCurrentMillseconds(System.currentTimeMillis())
                .setThemeColor(getResources().getColor(R.color.timepicker_dialog_bg))
                .setType(Type.ALL)
                .setWheelItemTextNormalColor(getResources().getColor(R.color.timetimepicker_default_text_color))
                .setWheelItemTextSelectorColor(getResources().getColor(R.color.timepicker_toolbar_bg))
                .setWheelItemTextSize(20)
                .build();

        if (SharedUtils.getFingerprintType() == 0) {
            basicRbOptical.setChecked(true);
        } else {
            basicRbCapacitive.setChecked(true);
        }
        //电容
        basicRbCapacitive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SharedUtils.setFingerprintType(1);
                }
            }
        });

        //光学
        basicRbOptical.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SharedUtils.setFingerprintType(0);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach: ");
        mContext = context;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView: ");
        unbinder.unbind();
    }

    /**
     * 保存服务器地址 获取当前枪库的数据
     */
    private void savaBaseUrlAndRoomId() {
        String serverIp = basicEtServerIp.getText().toString();
        String serverPort = basicEtServerPort.getText().toString();
        Log.i(TAG, "savaBaseUrlAndRoomId serverIp: " + serverIp + " serverPort:" + serverPort);
        if (TextUtils.isEmpty(serverIp)) {
            SoundPlayUtil.getInstance().play(R.raw.enter_ip);
            return;
        }

        if (TextUtils.isEmpty(serverPort)) {
            SoundPlayUtil.getInstance().play(R.raw.enter_port);
            return;
        }
        SharedUtils.saveServerIp(serverIp);
        SharedUtils.saveServerPort(serverPort);
        Log.i(TAG, "savaBaseUrlAndRoomId serverUrl: http://" + SharedUtils.getServerIp() + ":" + SharedUtils.getServerPort());
        showDialog("保存成功！");
    }

    private Dialog dialog;

    private void showDialog(String msg) {
        if (dialog != null) {
            if (!dialog.isShowing()) {
                dialog.show();
            }
            DialogUtils.setTipText(msg);
            Log.i(TAG, "dialog is not null ");
        } else { //dialog为null
            dialog = DialogUtils.creatTipDialog(getContext(), "提示", msg,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //获取最新的数据 并刷新适配器
                            dialog.dismiss();
                        }
                    });
            if (!dialog.isShowing()) {
                dialog.show();
            }
            Log.i(TAG, "dialog is null");
        }
    }

    @OnClick({R.id.bt_set_save, R.id.basic_btn_save_local, R.id.basic_btn_date_time,
            R.id.basic_edt_date_time, R.id.basic_btn_save_server})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_set_save: //保存服务IP
                savaBaseUrlAndRoomId();
                break;
            case R.id.basic_btn_save_local: //设置本地IP
                String IP = basicEdtStaticIp.getText().toString().trim();
                String netmask = basicEdtStaticNetmask.getText().toString().trim();
                String gateway = basicEdtStaticGateway.getText().toString().trim();
                String dns1 = basicEdtStaticDns.getText().toString().trim();
                String dns2 = basicEdtStaticDns2.getText().toString().trim();
                if (TextUtils.isEmpty(IP)) {
                    showDialog("IP地址不能为空");
                    return;
                }
                if (TextUtils.isEmpty(netmask)) {
                    showDialog("子网掩码不能为空");
                    return;
                }
                if (TextUtils.isEmpty(gateway)) {
                    showDialog("网关不能为空");
                    return;
                }
                if (TextUtils.isEmpty(dns1)) {
                    showDialog("DNS1不能为空");
                    return;
                }
                if (TextUtils.isEmpty(dns2)) {
                    showDialog("DNS2不能为空");
                    return;
                }
                ethernetMain.staticEth(IP, netmask, dns1, dns2, gateway);
                break;
            case R.id.basic_btn_date_time: //设置日期时间
                if (millseconds <= 0) {
                    return;
                }
                boolean isSetTime = SystemClock.setCurrentTimeMillis(millseconds);
                Log.i(TAG, "onDateSet  isSetTime: " + isSetTime);
                if (isSetTime) {
                    showDialog("时间设置成功!");
                } else {
                    showDialog("时间设置失败!");
                }
//                mContext.startActivity(new Intent(mContext, TestActivity.class));
                break;
            case R.id.basic_edt_date_time: //点击选择时间和日期
                assert getFragmentManager() != null;
                mDialogAll.show(getFragmentManager(), "all");
                break;
            case R.id.basic_btn_save_server: //保存平台地址
                String serverString = basicEdtServer.getText().toString();
                if (!TextUtils.isEmpty(serverString)) {
                    SharedUtils.savePlatformServer(serverString);
                }
                break;
        }
    }

    @Override
    public void onDateSet(TimePickerDialog timePickerView, long millseconds) {
        String dateTime = Utils.longTime2String(millseconds);
        basicEdtDateTime.setText(dateTime);
        this.millseconds = millseconds;
        Log.i(TAG, "onDateSet dateTime: " + dateTime);
    }


}

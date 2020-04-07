package com.zack.xjht.ui.fragment;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONException;
import com.androidhiddencamera.CameraConfig;
import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.RuntimeABI;
import com.yanzhenjie.nohttp.rest.Response;
import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.DialogUtils;
import com.zack.xjht.Utils.FileUtils;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.ToastUtil;
import com.zack.xjht.Utils.TransformUtil;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.entity.UserBiosBean;
import com.zack.xjht.face.faceserver.FaceServer;
import com.zack.xjht.finger.FingerManager;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;
import com.zack.xjht.iris.IrisManager;
import com.zx.zxlibrary.SystemUtils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * 其它设置
 */
public class OtherFragment extends Fragment {
    private static final String TAG = "OtherFragment";

    private static final int INIT_CODE = 3;
    @BindView(R.id.other_btn_update_system)
    Button otherBtnUpdateSystem;
    @BindView(R.id.other_btn_recovery)
    Button otherBtnRecovery;
    Unbinder unbinder;
    @BindView(R.id.other_btn_sync_time)
    Button otherBtnSyncTime;
    @BindView(R.id.other_tv_function_switch)
    TextView otherTvFunctionSwitch;
    @BindView(R.id.other_cb_alarm)
    CheckBox otherCbAlarm;
    @BindView(R.id.other_cb_alcohol)
    CheckBox otherCbAlcohol;
    @BindView(R.id.other_cb_capture)
    CheckBox otherCbCapture;
    @BindView(R.id.other_tv_verify_number)
    TextView otherTvVerifyNumber;
    @BindView(R.id.other_rb_one_user)
    RadioButton otherRbOneUser;
    @BindView(R.id.other_rb_two_user)
    RadioButton otherRbTwoUser;
    @BindView(R.id.other_rb_three_user)
    RadioButton otherRbThreeUser;
    @BindView(R.id.other_rg_verify_user)
    RadioGroup otherRgVerifyUser;
    @BindView(R.id.other_tv_first_verify_setting)
    TextView otherTvFirstVerifySetting;
    @BindView(R.id.other_cb_first_alcohol)
    CheckBox otherCbFirstAlcohol;
    @BindView(R.id.other_ll_first_verify_setting)
    LinearLayout otherLlFirstVerifySetting;
    @BindView(R.id.other_tv_second_verify_setting)
    TextView otherTvSecondVerifySetting;
    @BindView(R.id.other_cb_second_alcohol)
    CheckBox otherCbSecondAlcohol;
    @BindView(R.id.other_ll_second_verify_setting)
    LinearLayout otherLlSecondVerifySetting;
    @BindView(R.id.other_tv_third_verify_setting)
    TextView otherTvThirdVerifySetting;
    @BindView(R.id.other_cb_third_alcohol)
    CheckBox otherCbThirdAlcohol;
    @BindView(R.id.other_ll_third_verify_setting)
    LinearLayout otherLlThirdVerifySetting;
    @BindView(R.id.other_btn_save_first_verify)
    Button otherBtnSaveFirstVerify;
    @BindView(R.id.other_btn_save_second_verify)
    Button otherBtnSaveSecondVerify;
    @BindView(R.id.other_btn_save_three_verify)
    Button otherBtnSaveThreeVerify;
    @BindView(R.id.btn_sync_data)
    Button btnSyncData;
    @BindView(R.id.other_btn_capture)
    Button otherBtnCapture;
    @BindView(R.id.other_btn_reboot)
    Button otherBtnReboot;
    @BindView(R.id.other_btn_test)
    Button otherBtnTest;
    @BindView(R.id.other_rb_first_finger)
    RadioButton otherRbFirstFinger;
    @BindView(R.id.other_rb_first_face)
    RadioButton otherRbFirstFace;
    @BindView(R.id.other_rb_first_iris)
    RadioButton otherRbFirstIris;
    @BindView(R.id.other_rb_first_finger_face)
    RadioButton otherRbFirstFingerFace;
    @BindView(R.id.other_rb_first_finger_iris)
    RadioButton otherRbFirstFingerIris;
    @BindView(R.id.other_rb_first_face_iris)
    RadioButton otherRbFirstFaceIris;
    @BindView(R.id.other_rg_first)
    RadioGroup otherRgFirst;
    @BindView(R.id.other_rb_second_finger)
    RadioButton otherRbSecondFinger;
    @BindView(R.id.other_rb_second_face)
    RadioButton otherRbSecondFace;
    @BindView(R.id.other_rb_second_iris)
    RadioButton otherRbSecondIris;
    @BindView(R.id.other_rb_second_finger_face)
    RadioButton otherRbSecondFingerFace;
    @BindView(R.id.other_rb_second_finger_iris)
    RadioButton otherRbSecondFingerIris;
    @BindView(R.id.other_rb_second_face_iris)
    RadioButton otherRbSecondFaceIris;
    @BindView(R.id.other_rg_second)
    RadioGroup otherRgSecond;
    @BindView(R.id.other_rb_third_finger)
    RadioButton otherRbThirdFinger;
    @BindView(R.id.other_rb_third_face)
    RadioButton otherRbThirdFace;
    @BindView(R.id.other_rb_third_iris)
    RadioButton otherRbThirdIris;
    @BindView(R.id.other_rb_third_finger_face)
    RadioButton otherRbThirdFingerFace;
    @BindView(R.id.other_rb_third_finger_iris)
    RadioButton otherRbThirdFingerIris;
    @BindView(R.id.other_rb_third_face_iris)
    RadioButton otherRbThirdFaceIris;
    @BindView(R.id.other_rg_third)
    RadioGroup otherRgThird;
    @BindView(R.id.other_cb_humiture)
    CheckBox otherCbHumiture;
    @BindView(R.id.other_tv_humiture_setup)
    TextView otherTvHumitureSetup;
    @BindView(R.id.other_tv_temperature_value)
    TextView otherTvTemperatureValue;
    @BindView(R.id.other_edt_temprature)
    EditText otherEdtTemprature;
    @BindView(R.id.other_tv_humidity_value)
    TextView otherTvHumidityValue;
    @BindView(R.id.other_edt_humidity)
    EditText otherEdtHumidity;
    @BindView(R.id.other_tv_degree)
    TextView otherTvDegree;
    @BindView(R.id.other_tv_percent)
    TextView otherTvPercent;
    @BindView(R.id.other_btn_set_humiture)
    Button otherBtnSetHumiture;
    @BindView(R.id.other_cb_switch_systembar)
    CheckBox otherCbSwitchSystembar;
    @BindView(R.id.other_btn_active_engine)
    Button otherBtnActiveEngine;
    @BindView(R.id.other_ll_function)
    LinearLayout otherLlFunction;
    @BindView(R.id.btn_clear_system_log)
    Button btnClearSystemLog;
    @BindView(R.id.btn_clear_basic_data)
    Button btnClearBasicData;
    @BindView(R.id.btn_clear_get_gun_data)
    Button btnClearGetGunData;
    @BindView(R.id.btn_clear_urgent_task_data)
    Button btnClearUrgentTaskData;
    @BindView(R.id.btn_clear_gun_status_data)
    Button btnClearGunStatusData;
    @BindView(R.id.other_ll_function_2)
    LinearLayout otherLlFunction2;
    @BindView(R.id.other_cb_query)
    CheckBox otherCbQuery;
    @BindView(R.id.other_ll_switch)
    LinearLayout otherLlSwitch;
    @BindView(R.id.other_ll_humiture_alarm)
    LinearLayout otherLlHumitureAlarm;
    private Context mContext;
    private CameraConfig mCameraConfig;
    private FragmentActivity activity;

    public OtherFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other, container, false);
        unbinder = ButterKnife.bind(this, view);
//        configCamera();
        initView();
        return view;
    }

    private void initView() {
        otherCbAlarm.setChecked(SharedUtils.getIsAlarmOpen());
        otherCbAlcohol.setChecked(SharedUtils.getIsAlcoholOpen());
        otherCbCapture.setChecked(SharedUtils.getIsCaptureOpen());
        otherCbHumiture.setChecked(SharedUtils.getIsHumitureOpen());
        otherCbQuery.setChecked(SharedUtils.getIsQuery()); //查询状态

        otherCbAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedUtils.setIsAlarmOpen(isChecked);
            }
        });
        otherCbAlcohol.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedUtils.setIsAlcoholOpen(isChecked);
            }
        });
        otherCbCapture.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedUtils.setIsCaptureOpen(isChecked);
            }
        });
        otherCbHumiture.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedUtils.setIsHumitureOpen(isChecked);
            }
        });

        otherCbQuery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedUtils.setIsQuery(isChecked);
            }
        });
        //温度值
        otherEdtTemprature.setText(String.valueOf(SharedUtils.getTempratureAlarmValue()));
        //湿度值
        otherEdtHumidity.setText(String.valueOf(SharedUtils.getHumitureAlarmValue()));

        //获取验证人数
        switch (SharedUtils.getVerifyUserNumber()) {
            case 1:
                otherRbOneUser.setChecked(true);
                break;
            case 2:
                otherRbTwoUser.setChecked(true);
                break;
            case 3:
                otherRbThreeUser.setChecked(true);
                break;
        }

        switch (SharedUtils.getFirstUserVerify()) {
            case 1:
                otherRbFirstFinger.setChecked(true);
                break;
            case 2:
                otherRbFirstFace.setChecked(true);
                break;
            case 3:
                otherRbFirstIris.setChecked(true);
                break;
            case 4:
                otherRbFirstFingerFace.setChecked(true);
                break;
            case 5:
                otherRbFirstFingerIris.setChecked(true);
                break;
            case 6:
                otherRbFirstFaceIris.setChecked(true);
                break;
        }
        switch (SharedUtils.getSecondUserVerify()) {
            case 1:
                otherRbSecondFinger.setChecked(true);
                break;
            case 2:
                otherRbSecondFace.setChecked(true);
                break;
            case 3:
                otherRbSecondIris.setChecked(true);
                break;
            case 4:
                otherRbSecondFingerFace.setChecked(true);
                break;
            case 5:
                otherRbSecondFingerIris.setChecked(true);
                break;
            case 6:
                otherRbSecondFaceIris.setChecked(true);
                break;
        }
        switch (SharedUtils.getThirdUserVerify()) {
            case 1:
                otherRbThirdFinger.setChecked(true);
                break;
            case 2:
                otherRbThirdFace.setChecked(true);
                break;
            case 3:
                otherRbThirdIris.setChecked(true);
                break;
            case 4:
                otherRbThirdFingerFace.setChecked(true);
                break;
            case 5:
                otherRbThirdFingerIris.setChecked(true);
                break;
            case 6:
                otherRbThirdFaceIris.setChecked(true);
                break;
        }

        if (SharedUtils.getFirstVerifyAlcohol()) {
            otherCbFirstAlcohol.setChecked(true);
        } else {
            otherCbFirstAlcohol.setChecked(false);
        }

        if (SharedUtils.getSecondVerifyAlcohol()) {
            otherCbSecondAlcohol.setChecked(true);
        } else {
            otherCbSecondAlcohol.setChecked(false);
        }

        if (SharedUtils.getThirdVerifyAlcohol()) {
            otherCbThirdAlcohol.setChecked(true);
        } else {
            otherCbThirdAlcohol.setChecked(false);
        }
        //设置验证人数
        otherRgVerifyUser.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.other_rb_one_user:
                        SharedUtils.setVerifyUserNumber(1);
                        break;
                    case R.id.other_rb_two_user:
                        SharedUtils.setVerifyUserNumber(2);
                        break;
                    case R.id.other_rb_three_user:
                        SharedUtils.setVerifyUserNumber(3);
                        break;
                }
            }
        });

        otherCbFirstAlcohol.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedUtils.setFirstVerifyAlcohol(isChecked);
            }
        });

        otherCbSecondAlcohol.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedUtils.setSecondVerifyAlcohol(isChecked);
            }
        });

        otherCbThirdAlcohol.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedUtils.setThirdVerifyAlcohol(isChecked);
            }
        });

        otherRgFirst.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.other_rb_first_finger: //指纹
                        SharedUtils.setFirstUserVerify(1);
                        break;
                    case R.id.other_rb_first_face://人脸
                        SharedUtils.setFirstUserVerify(2);
                        break;
                    case R.id.other_rb_first_iris://虹膜
                        SharedUtils.setFirstUserVerify(3);
                        break;
                    case R.id.other_rb_first_finger_face://指纹+人脸
                        SharedUtils.setFirstUserVerify(4);
                        break;
                    case R.id.other_rb_first_finger_iris://指纹+虹膜
                        SharedUtils.setFirstUserVerify(5);
                        break;
                    case R.id.other_rb_first_face_iris://人脸+虹膜
                        SharedUtils.setFirstUserVerify(6);
                        break;
                }
            }
        });

        otherRgSecond.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.other_rb_second_finger: //指纹
                        SharedUtils.setSecondUserVerify(1);
                        break;
                    case R.id.other_rb_second_face://人脸
                        SharedUtils.setSecondUserVerify(2);
                        break;
                    case R.id.other_rb_second_iris://虹膜
                        SharedUtils.setSecondUserVerify(3);
                        break;
                    case R.id.other_rb_second_finger_face://指纹+人脸
                        SharedUtils.setSecondUserVerify(4);
                        break;
                    case R.id.other_rb_second_finger_iris://指纹+虹膜
                        SharedUtils.setSecondUserVerify(5);
                        break;
                    case R.id.other_rb_second_face_iris://人脸+虹膜
                        SharedUtils.setSecondUserVerify(6);
                        break;
                }
            }
        });

        otherRgThird.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.other_rb_third_finger: //指纹
                        SharedUtils.setThirdUserVerify(1);
                        break;
                    case R.id.other_rb_third_face://人脸
                        SharedUtils.setThirdUserVerify(2);
                        break;
                    case R.id.other_rb_third_iris://虹膜
                        SharedUtils.setThirdUserVerify(3);
                        break;
                    case R.id.other_rb_third_finger_face://指纹+人脸
                        SharedUtils.setThirdUserVerify(4);
                        break;
                    case R.id.other_rb_third_finger_iris://指纹+虹膜
                        SharedUtils.setThirdUserVerify(5);
                        break;
                    case R.id.other_rb_third_face_iris://人脸+虹膜
                        SharedUtils.setThirdUserVerify(6);
                        break;
                }
            }
        });

        otherCbSwitchSystembar.setChecked(true);
        otherCbSwitchSystembar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SystemUtils.systembar(mContext, isChecked);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.other_btn_update_system, R.id.other_btn_recovery, R.id.other_btn_sync_time,
            R.id.other_btn_save_first_verify, R.id.other_btn_save_second_verify,
            R.id.other_btn_save_three_verify, R.id.btn_sync_data, R.id.other_btn_capture,
            R.id.other_btn_reboot, R.id.other_btn_test, R.id.other_btn_set_humiture,
            R.id.other_btn_active_engine, R.id.btn_clear_system_log, R.id.btn_clear_basic_data,
            R.id.btn_clear_get_gun_data, R.id.btn_clear_urgent_task_data, R.id.btn_clear_gun_status_data})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.other_btn_update_system: //系统更新
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/vnd.android.package-archive");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(intent, INIT_CODE);
                break;
            case R.id.other_btn_recovery: //系统还原
                //清除所有缓存和保存的数据
                showDialog("还原系统成功！");

                break;
            case R.id.other_btn_sync_time: //同步系统时间
                syncTime();
                break;
            case R.id.btn_sync_data: //同步数据
                try {
                    List<UserBiosBean> userBiosBeans = DBManager.getInstance().getUserBiosBeanDao().loadAll();
                    downCHar(userBiosBeans);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.other_btn_save_first_verify: //保存第一人员设置
                break;
            case R.id.other_btn_save_second_verify: //保存第二人验证方式
                break;
            case R.id.other_btn_save_three_verify: //保存第三人验证方式
                break;
            case R.id.other_btn_capture: //抓拍照片
//                takePicture();
                break;
            case R.id.other_btn_reboot: //重启
//                try {
////                    Runtime.getRuntime().exec(new String[]{"sh","-c","reboot -p"}); //关机
//                    Runtime.getRuntime().exec("sh -c reboot"); //重启
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                try {
                    PowerManager pm = (PowerManager) this.mContext.getSystemService(Context.POWER_SERVICE);
                    pm.reboot("reboot");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.other_btn_test:
//                startActivity(new Intent(mContext, TestActivity.class));
                break;
            case R.id.other_btn_set_humiture: //设置温湿度报警值
                String humidityValue = otherEdtHumidity.getText().toString().trim();
                String temptureValue = otherEdtTemprature.getText().toString().trim();
                if (!TextUtils.isEmpty(humidityValue)) {
                    SharedUtils.setHumitureAlarmValue(Integer.parseInt(humidityValue));
                }
                if (!TextUtils.isEmpty(temptureValue)) {
                    SharedUtils.setTempratureAlarmValue(Integer.parseInt(temptureValue));
                }
                showDialog("设置温湿度报警值成功");
                break;
            case R.id.other_btn_active_engine: //激活人脸识别引擎
                activeEngine();
                break;
            case R.id.btn_clear_system_log:
                //日志
                DBManager.getInstance().getOperLogBeanDao().deleteAll();
                DBManager.getInstance().getAlarmLogBeanDao().deleteAll();
                DBManager.getInstance().getCommonLogBeanDao().deleteAll();
                break;
            case R.id.btn_clear_basic_data:
                //基础数据
                DBManager.getInstance().getUserBeanDao().deleteAll();
                DBManager.getInstance().getUserBiosBeanDao().deleteAll();
                DBManager.getInstance().getSubCabBeanDao().deleteAll();
                break;
            case R.id.btn_clear_get_gun_data:
                //领枪任务数据
                DBManager.getInstance().getOfflineTaskDao().deleteAll();
                DBManager.getInstance().getOfflineTaskItemDao().deleteAll();
                break;
            case R.id.btn_clear_urgent_task_data:
                //应急处突数据
                DBManager.getInstance().getUrgentOutBeanDao().deleteAll();
                DBManager.getInstance().getUrgentGetListBeanDao().deleteAll();
                DBManager.getInstance().getUrgentBackListBeanDao().deleteAll();
                break;
            case R.id.btn_clear_gun_status_data:
                //枪支状态数据
                DBManager.getInstance().getGunStateBeanDao().deleteAll();
                break;
        }
    }

    public void deleteFaceLib() {
        //获取人脸注册数量
        int faceNum = FaceServer.getInstance().getFaceNumber(mContext);
        Log.i(TAG, "deleteFaceLib faceNum: " + faceNum);
        if (faceNum == 0) {
            Log.i(TAG, "deleteFaceLib 人脸数据为空: ");
        } else {
            /**
             * 清空所有人脸注册数据
             */
            int deleteCount = FaceServer.getInstance().clearAllFaces(mContext);
            Log.i(TAG, "deleteFaceLib 清空人脸数据完成: " + deleteCount);
        }
    }

    /**
     * 激活引擎
     */
    public void activeEngine() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) {
                RuntimeABI runtimeABI = FaceEngine.getRuntimeABI();
                Log.i(TAG, "subscribe: getRuntimeABI() " + runtimeABI);
                int activeCode = FaceEngine.activeOnline(mContext,
                        Constants.APP_ID, Constants.SDK_KEY);
                emitter.onNext(activeCode);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK) {//激活成功
                            ToastUtil.showShort(getString(R.string.active_success));
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            //已激活
                            ToastUtil.showShort(getString(R.string.already_activated));
                        } else {//激活失败
                            ToastUtil.showShort(getString(R.string.active_failed, activeCode));
                        }

//                        if (view != null) {
//                            view.setClickable(true);
//                        }
                        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                        int res = FaceEngine.getActiveFileInfo(mContext, activeFileInfo);
                        Log.i(TAG, "onNext res: " + res);
                        if (res == ErrorInfo.MOK) {
                            Log.i(TAG, activeFileInfo.toString());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtil.showShort(e.getMessage());
//                        if (view != null) {
//                            view.setClickable(true);
//                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 同步时间
     */
    private void syncTime() {
        HttpClient.getInstance().getDateAndTime(getContext(), new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                try {
                    if (!TextUtils.isEmpty(response.get())) {
                        long longTime = Utils.stringTime2Long(response.get());
                        boolean isSetTime = SystemClock.setCurrentTimeMillis(longTime);
                        if (isSetTime) {
                            showDialog("同步时间成功!");
                        } else {
                            showDialog("同步时间失败!");
                        }
                    } else {
                        showDialog("获取日期时间为空");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                showDialog("获取时间失败！");
            }
        });
    }

    //下发模版
    private void downCHar(List<UserBiosBean> userBiosBeans) {
        Log.i(TAG, "downCHar: ");
        if (!userBiosBeans.isEmpty()) {
            Log.i(TAG, "downCHar size: " + userBiosBeans.size());
            if (Constants.isFingerConnect && Constants.isFingerInit) {
                FingerManager.getInstance().clearAllFinger(); //清除设备中所有指纹
            }

            if (Constants.isFaceInit) {
                deleteFaceLib(); //清除人脸数据
            }
            //清除虹膜模版
            if (Constants.isIrisInit) {
                IrisManager.getInstance().deleteAllTemp();
            }

            for (UserBiosBean policeBiosBean : userBiosBeans) {
                String deviceType = policeBiosBean.getBiometricsType();
                String key = policeBiosBean.getBiometricsKey();
                int id = policeBiosBean.getBiometricsNumber();
                Log.i(TAG, "downCHar id: " + id);
//                byte[] decodeKey = Base64.decode(key, Base64.DEFAULT);
                byte[] decodeKey = TransformUtil.hexStrToBytes(key);
                Log.i(TAG, "downCHar key " + key + " \n decodeKey: " + decodeKey.length);
                switch (deviceType) {
                    case Constants.DEVICE_FINGER: //指纹
                        if (Constants.isFingerConnect && Constants.isFingerInit) {
                            Log.i(TAG, "downCHar fingerID: " + id);
                            FingerManager.getInstance().fpDownChar(id, decodeKey);
                        }
                        break;
                    case Constants.DEVICE_VEIN: //指静脉
                        //保存用户模板到算法库
                        break;
                    case Constants.DEVICE_IRIS://虹膜
                        Log.i(TAG, "downCHar irisID: " + id);
                        if (Constants.isIrisInit) {
                            IrisManager.getInstance().downTemplate(String.valueOf(id), key);
                        }
                        break;
                    case Constants.DEVICE_FACE://人脸
                        if (Constants.isFaceInit) {
                            boolean result = FaceServer.getInstance().saveFaceFeature(mContext, decodeKey, String.valueOf(id));
                            Log.i(TAG, "downCHar  faceID: " + id + " result: " + result);
                        }
                        break;
                    default:
                        break;
                }
            }
            showDialog("生物特征更新成功");
        } else {
            showDialog("无生物特征数据");
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: ");
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (INIT_CODE == requestCode) {
                String FILE_PATH = FileUtils.getPath(mContext, uri);
                Log.i(TAG, "onActivityResult filePath: " + FILE_PATH);
                if (!TextUtils.isEmpty(FILE_PATH)) {
                    install(FILE_PATH);
                }
            }
        }
    }

    /**
     * 安装apk
     *
     * @param filePath
     */
    private void install(String filePath) {
        Log.i(TAG, "开始执行安装: " + filePath);
        File apkFile = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 24) {
            Log.w(TAG, "版本大于 N ，开始使用 fileProvider 进行安装");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(
                    mContext
                    , "com.zack.gunlibrary.fileprovider"
                    , apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            Log.w(TAG, "正常进行安装");
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        startActivity(intent);
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
                            dialog.dismiss();
                        }
                    });
            if (!dialog.isShowing()) {
                dialog.show();
            }
            Log.i(TAG, "dialog is null");
        }
    }

    public void ShowDialog(String msg) {
        new AlertDialog.Builder(mContext).setTitle("提示").setMessage(msg)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                }).show();
    }


}

package com.zack.xjht.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.yanzhenjie.nohttp.rest.Response;
import com.zack.xjht.Constants;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.db.gen.AlarmLogBeanDao;
import com.zack.xjht.db.gen.CommonLogBeanDao;
import com.zack.xjht.entity.AlarmLogBean;
import com.zack.xjht.entity.CommonLogBean;
import com.zack.xjht.hardware.Sensor;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;
import com.zack.xjht.serial.SerialPortUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 *
 */
public class QueryService extends Service {
    private static final String TAG = "QueryService";
    private boolean isQuery;
    private CommonLogBeanDao commonLogBeanDao;
    private AlarmLogBeanDao alarmLogBeanDao;
    private ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: ");
         commonLogBeanDao = DBManager.getInstance().getCommonLogBeanDao();
         alarmLogBeanDao = DBManager.getInstance().getAlarmLogBeanDao();
        SerialPortUtil.getInstance().onCreate();
        executorService =Executors.newCachedThreadPool();
        isQuery = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: ");
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (isQuery) {
//                    Log.v(TAG, "run readHumitureValue: ");
                    SerialPortUtil.getInstance().checkHumiture();
                    try {
                        Thread.sleep(3* 60 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (isQuery) {
                    if (SharedUtils.getIsQuery()) {
//                        Log.v(TAG, "run 查询枪柜状态: ");
                        String leftCabNo = SharedUtils.getLeftCabNo();
                        SerialPortUtil.getInstance().checkStatus(Integer.parseInt(leftCabNo));
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (isQuery) {
                    if (SharedUtils.getIsQuery()) {
//                        Log.v(TAG, "run 查询电源状态: ");
                        int powerAddress = SharedUtils.getPowerAddress();
                        SerialPortUtil.getInstance().checkStatus(powerAddress);
                        try {
                            Thread.sleep( 5*1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (isQuery) {
                    if (SharedUtils.getIsServerOnline()) {
//                        Log.v(TAG, "run 查询未上传日志: ");
                        List<AlarmLogBean> alarmLogBeanList = alarmLogBeanDao.queryBuilder().where(AlarmLogBeanDao
                                .Properties.IsSync.eq(false)).list();
                        if(!alarmLogBeanList.isEmpty()){
                            for (AlarmLogBean alarmLogBean:alarmLogBeanList) {
                                List<AlarmLogBean> alarmLogBeans =new ArrayList<>();
                                alarmLogBeans.add(alarmLogBean);
                                postAlarmLog(alarmLogBeans, alarmLogBean);
                            }
                        }

                        List<CommonLogBean> commonLogBeanList = commonLogBeanDao.queryBuilder().where(CommonLogBeanDao
                                .Properties.IsSync.eq(false)).list();
                        if(commonLogBeanList.isEmpty()){
                            for (CommonLogBean commonLogBean:commonLogBeanList) {
                                List<CommonLogBean> commonLogBeans =new ArrayList<>();
                                commonLogBeans.add(commonLogBean);
                                postCommonLogBean(commonLogBeans, commonLogBean);
                            }
                        }
                        try {
                            Thread.sleep(10 * 60 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        return START_STICKY;
    }

    private void postCommonLogBean(final List<CommonLogBean> commonLogBeans, final CommonLogBean commonLogBean) {
        String jsonString = JSON.toJSONString(commonLogBeans);
        Log.i(TAG, "postCommonLogBean jsonString: "+jsonString);
        HttpClient.getInstance().postCommonLog(this, jsonString, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed response: "+response.get());
                if(!TextUtils.isEmpty(response.get()) && response.get().equals("success")){
                    if(commonLogBean !=null){
                        commonLogBean.setIsSync(true);
                        commonLogBeanDao.update(commonLogBean);
                    }
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {

            }
        });
    }

    private void postAlarmLog(List<AlarmLogBean> alarmLogBeans, final AlarmLogBean alarmLogBean) {
        String jsonString = JSON.toJSONString(alarmLogBeans);
        Log.i(TAG, "postAlarmLog jsonString: "+ jsonString);
        HttpClient.getInstance().postAlarmLog(this, jsonString, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postAlarmLog response: "+response.get());
                if(!TextUtils.isEmpty(response.get()) && response.get().equals("success")){
                    if(alarmLogBean !=null){
                        alarmLogBean.setIsSync(true);
                        alarmLogBeanDao.update(alarmLogBean);
                    }

                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {


            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        isQuery = false;
        if(executorService !=null && !executorService.isShutdown()){
            executorService.shutdown();
            executorService =null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

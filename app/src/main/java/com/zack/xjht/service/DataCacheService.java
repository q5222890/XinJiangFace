package com.zack.xjht.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.yanzhenjie.nohttp.rest.Response;
import com.zack.xjht.Utils.LogUtil;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.Utils.Utils;
import com.zack.xjht.db.DBManager;
import com.zack.xjht.db.gen.CabInfoBeanDao;
import com.zack.xjht.db.gen.OfflineTaskDao;
import com.zack.xjht.db.gen.OfflineTaskItemDao;
import com.zack.xjht.db.gen.OperLogBeanDao;
import com.zack.xjht.db.gen.SubCabBeanDao;
import com.zack.xjht.db.gen.UrgentOutBeanDao;
import com.zack.xjht.db.gen.UserBeanDao;
import com.zack.xjht.db.gen.UserBiosBeanDao;
import com.zack.xjht.entity.CabInfoBean;
import com.zack.xjht.entity.OfflineTask;
import com.zack.xjht.entity.OfflineTaskItem;
import com.zack.xjht.entity.OperLogBean;
import com.zack.xjht.entity.SubCabBean;
import com.zack.xjht.entity.UrgentBackListBean;
import com.zack.xjht.entity.UrgentGetListBean;
import com.zack.xjht.entity.UrgentOutBean;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.entity.UserBiosBean;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataCacheService extends Service {
    private static final String TAG = "DataCacheService";

    private boolean isStop = false;
    private SubCabBeanDao subCabBeanDao;
    private UserBeanDao userBeanDao;
    private UserBiosBeanDao userBiosBeanDao;
    private OfflineTaskDao offlineTaskDao;
    private OfflineTaskItemDao offlineTaskItemDao;
    private OperLogBeanDao operLogBeanDao;
    private ExecutorService executorService = Executors.newScheduledThreadPool(6);
    private UrgentOutBeanDao urgentOutBeanDao;
    private CabInfoBeanDao cabInfoBeanDao;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
        userBeanDao = DBManager.getInstance().getUserBeanDao();
        userBiosBeanDao = DBManager.getInstance().getUserBiosBeanDao();
        subCabBeanDao = DBManager.getInstance().getSubCabBeanDao();
        offlineTaskDao = DBManager.getInstance().getOfflineTaskDao();
        offlineTaskItemDao = DBManager.getInstance().getOfflineTaskItemDao();
        operLogBeanDao = DBManager.getInstance().getOperLogBeanDao();
        urgentOutBeanDao = DBManager.getInstance().getUrgentOutBeanDao();
        cabInfoBeanDao =DBManager.getInstance().getCabInfoBeanDao();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");
        /**
         * 获取枪柜数据
         */
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (!isStop) {
//                    Log.i(TAG, "run :获取枪柜数据 ");
                    if (Utils.isNetworkAvailable()) {
                        getCabData();
                        try {
                            Thread.sleep(30 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (SharedUtils.getIsServerOnline()) {
                            SharedUtils.setIsServerOnline(false);
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        /**
         * 获取人员数据
         */
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (!isStop) {
//                    Log.i(TAG, "run :获取人员数据 ");
                    if (Utils.isNetworkAvailable()) {
                        getPoliceList();
                        try {
                            Thread.sleep(30 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (SharedUtils.getIsServerOnline()) {
                            SharedUtils.setIsServerOnline(false);
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        /**
         * 获取用户生物特征
         */
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (!isStop) {
//                    Log.i(TAG, "run:获取用户生物特征 ");
                    if (Utils.isNetworkAvailable()) {
                        getUserBios();
                        try {
                            Thread.sleep(30 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (SharedUtils.getIsServerOnline()) {
                            SharedUtils.setIsServerOnline(false);
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        /**
         * 上传任务日志
         */
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (!isStop) {
                    //当服务器在线，查询是否有需要上传日志数据
                    if (SharedUtils.getIsServerOnline()) {
                        List<OperLogBean> operLogBeans = operLogBeanDao.loadAll();
                        if (!operLogBeans.isEmpty()) {
                            String jsonString = JSON.toJSONString(operLogBeans);
//                            LogUtil.i(TAG, "run operLogBeans: "+jsonString);
                            for (OperLogBean operLogBean : operLogBeans) {
                                int uploadStatus = operLogBean.getUploadStatus();
                                String status = operLogBean.getStatus();
                                if (uploadStatus == 0) { //未上传
                                    if (status.equals("1")) {
                                        //上传领取数据
                                        postOfflineTaskLog(operLogBean, 1);
                                    } else if (status.equals("2")) {
                                        //上传归还数据
                                        postOfflineTaskLog(operLogBean, 2);
                                    }
                                } else if (uploadStatus == 1) { //领取上传完成
                                    if (status.equals("2")) { //归还完成
                                        //上传归还数据
                                        postOfflineTaskLog(operLogBean, 2);
                                    }
                                }
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        /**
         * 上传应急处突记录日志
         */
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (!isStop) {
                    //当服务器在线，查询是否有需要上传数据
                    if (SharedUtils.getIsServerOnline()) {
//                        Log.i(TAG, "run upload urgent data: ");

                        List<UrgentOutBean> urgentOutBeans = urgentOutBeanDao.queryBuilder().whereOr(
                                UrgentOutBeanDao.Properties.IsGetUpload.eq(false),
                                UrgentOutBeanDao.Properties.IsBackUpload.eq(false)
                        ).list();

//                        List<UrgentOutBean> urgentTaskBeans = urgentOutBeanDao.loadAll();
//                        LogUtil.i(TAG, "run urgentOutBeans1: "+JSON.toJSONString(urgentOutBeans));
//                        LogUtil.i(TAG, "run urgentInBeans: "+JSON.toJSONString(urgentInBeans));
//                        LogUtil.i(TAG, "run urgentTaskBeans: "+JSON.toJSONString(urgentTaskBeans));
                        if (!urgentOutBeans.isEmpty()) {
//                            Log.i(TAG, "run urgentOutBeans size: "+urgentOutBeans.size());
                            Collections.sort(urgentOutBeans);
//                            LogUtil.i(TAG, "run urgentOutBeans2: "+JSON.toJSONString(urgentOutBeans));
                            for (UrgentOutBean urgentOutBean : urgentOutBeans) {
//                                String jsonString = JSON.toJSONString(urgentOutBean);
//                                Log.i(TAG, "run  jsonString: "+jsonString);
                                boolean isGetUpload = urgentOutBean.getIsGetUpload();
                                boolean isBackUpload = urgentOutBean.getIsBackUpload();
                                List<UrgentGetListBean> urgentGetList = urgentOutBean.getUrgentGetList();
                                List<UrgentBackListBean> urgentBackList = urgentOutBean.getUrgentBackList();
                                if ((!isGetUpload && urgentGetList.size() > 0)
                                        || (!isBackUpload && urgentBackList.size() > 0)) {
                                    Log.i(TAG, "run 提交: ");
                                    postUrgentTaskData(urgentOutBean);
                                }
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        return START_NOT_STICKY;
    }

    private void postUrgentTaskData(final UrgentOutBean urgentOutBean) {
        String jsonString = JSON.toJSONString(urgentOutBean);
        HttpClient.getInstance().postUrgentData(this, jsonString, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postUrgentTaskData response: " + response.get());
                if (!TextUtils.isEmpty(response.get()) && response.get().equals("success")) {
                    Log.i(TAG, "onSucceed 提交成功: ");
                    //修改状态
                    urgentOutBean.setUpdateTime(Utils.longTime2String(System.currentTimeMillis()));
                    if (!urgentOutBean.getIsGetUpload()) {
                        urgentOutBean.setIsGetUpload(true);
                    }
                    List<UrgentBackListBean> urgentBackList = urgentOutBean.getUrgentBackList();
                    Log.i(TAG, "onSucceed urgentBackList size: " + urgentBackList.size());

                    if (!urgentOutBean.getIsBackUpload() && urgentBackList.size() > 0) {
                        urgentOutBean.setIsBackUpload(true);
                    }
                    urgentOutBeanDao.update(urgentOutBean);
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                Log.i(TAG, "onFailed error: " + response.getException().getMessage());
            }
        });
    }

    private void postOfflineTaskLog(final OperLogBean operLogBean, final int status) {
        HttpClient.getInstance().postOfflineTaskLog(this, JSON.toJSONString(operLogBean),
                new HttpListener<String>() {
                    @Override
                    public void onSucceed(int what, Response<String> response) throws JSONException {
                        String responseStr = response.get();
                        Log.i(TAG, "postOfflineTaskLog onSucceed responseStr: " + responseStr);
                        if (!TextUtils.isEmpty(responseStr) && responseStr.equals("success")) {
                            //数据提交成功 修改同步状态
                            if (status == 1) {
                                //已领取上传
                                operLogBean.setUploadStatus(1);
                            } else if (status == 2) {
                                //已归还上传
                                operLogBean.setUploadStatus(2);
                            }
                            operLogBeanDao.update(operLogBean);
                        } else {
                            Log.i(TAG, "onSucceed  上传失败: ");
                        }
                    }

                    @Override
                    public void onFailed(int what, Response<String> response) {

                    }
                });

    }

    private void getUserBios() {
        HttpClient.getInstance().getCharList(this, "", new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                if (!SharedUtils.getIsServerOnline()) {
                    SharedUtils.setIsServerOnline(true);
                }
//                LogUtil.v(TAG, "onSucceed getBios response : " + response.get());
                if (!TextUtils.isEmpty(response.get())) {
                    List<UserBiosBean> userBiosBeans = JSON.parseArray(response.get(), UserBiosBean.class);
                    userBiosBeanDao.deleteAll();
                    userBiosBeanDao.insertInTx(userBiosBeans);
                } else {

                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                if (SharedUtils.getIsServerOnline()) {
                    SharedUtils.setIsServerOnline(false);
                }
            }
        });
    }

    private void getCabData() {
        HttpClient.getInstance().getCabByMac(this, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                if (!SharedUtils.getIsServerOnline()) {
                    SharedUtils.setIsServerOnline(true);
                }
//                LogUtil.v(TAG, "onSucceed getCabData response : " + response.get());
                if (!TextUtils.isEmpty(response.get())) {
                    CabInfoBean cabInfoBean = JSON.parseObject(response.get(), CabInfoBean.class);
                    if (cabInfoBean != null) {
                        List<CabInfoBean> cabInfoBeans = cabInfoBeanDao.loadAll();
                        if(!cabInfoBeans.isEmpty()){
                            cabInfoBeanDao.deleteAll();
                            cabInfoBeanDao.insert(cabInfoBean);
                        }

                        List<SubCabBean> listLocation = cabInfoBean.getListLocation();
                        if (!listLocation.isEmpty()) {
//                            Log.i(TAG, "onSucceed listLocation size: " + listLocation.size());
                            subCabBeanDao.deleteAll();
                            subCabBeanDao.insertInTx(listLocation);
//                                Log.i(TAG, "onSucceed insert: "+insert);
                        }
                    }
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                if (SharedUtils.getIsServerOnline()) {
                    SharedUtils.setIsServerOnline(false);
                }
            }
        });
    }

    private void getPoliceList() {
        HttpClient.getInstance().getUserList(this, "", new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                if (!SharedUtils.getIsServerOnline()) {
                    SharedUtils.setIsServerOnline(true);
                }
//                Log.v(TAG, "onSucceed getPoliceList response: " + response.get());
                if (!TextUtils.isEmpty(response.get())) {
                    List<UserBean> userBeans = JSON.parseArray(response.get(), UserBean.class);
                    userBeanDao.deleteAll();
                    userBeanDao.insertInTx(userBeans);
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                if (SharedUtils.getIsServerOnline()) {
                    SharedUtils.setIsServerOnline(false);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStop = true;
    }
}

package com.zack.xjht.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.yanzhenjie.nohttp.rest.Response;
import com.zack.xjht.App;
import com.zack.xjht.Utils.SharedUtils;
import com.zack.xjht.db.gen.AlarmLogBeanDao;
import com.zack.xjht.db.gen.CabInfoBeanDao;
import com.zack.xjht.db.gen.CommonLogBeanDao;
import com.zack.xjht.db.gen.DaoMaster;
import com.zack.xjht.db.gen.DaoSession;
import com.zack.xjht.db.gen.GunStateBeanDao;
import com.zack.xjht.db.gen.OfflineTaskDao;
import com.zack.xjht.db.gen.OfflineTaskItemDao;
import com.zack.xjht.db.gen.OperLogBeanDao;
import com.zack.xjht.db.gen.SubCabBeanDao;
import com.zack.xjht.db.gen.UrgentBackListBeanDao;
import com.zack.xjht.db.gen.UrgentGetListBeanDao;
import com.zack.xjht.db.gen.UrgentOutBeanDao;
import com.zack.xjht.db.gen.UserBeanDao;
import com.zack.xjht.db.gen.UserBiosBeanDao;
import com.zack.xjht.entity.CommonLogBean;
import com.zack.xjht.entity.UserBean;
import com.zack.xjht.http.HttpClient;
import com.zack.xjht.http.HttpListener;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */

public class DBManager {

    private static final String TAG = "DBManager";
    private static DBManager mInstance;
    private DaoMaster.DevOpenHelper openHelper;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private static final String DB_NAME = "intelligent";
    private AlarmLogBeanDao alarmLogBeanDao;
    private CommonLogBeanDao commonLogBeanDao;
    private GunStateBeanDao gunStateBeanDao;
    private UserBiosBeanDao userBiosBeanDao;
    private UserBeanDao userBeanDao;
    private SubCabBeanDao subCabBeanDao;
    private OfflineTaskDao offlineTaskDao;
    private OfflineTaskItemDao offlineTaskItemDao;
    private OperLogBeanDao operLogBeanDao;
    private UrgentOutBeanDao urgentOutBeanDao;
    private UrgentGetListBeanDao urgentGetListBeanDao;
    private UrgentBackListBeanDao urgentBackListBeanDao;
    private CabInfoBeanDao cabInfoBeanDao;

    private DBManager() {
    }

    public static DBManager getInstance() {
        if (mInstance == null) {
            synchronized (DBManager.class) {
                if (mInstance == null) {
                    mInstance = new DBManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 获取可读数据库
     */
    private SQLiteDatabase getReadableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(App.getContext(), DB_NAME, null);
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        return db;
    }

    /**
     * 获取可写数据库
     */
    private SQLiteDatabase getWritableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(App.getContext(), DB_NAME, null);
        }
        return openHelper.getWritableDatabase();
    }

    private DaoMaster getDaoMaster() {
        if (daoMaster == null) {
            daoMaster = new DaoMaster(getWritableDatabase());
        }
        return daoMaster;
    }

    private DaoSession getDaoSession() {
        if (daoSession == null) {
            if (daoMaster == null) {
                daoMaster = getDaoMaster();
            }
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }


    public AlarmLogBeanDao getAlarmLogBeanDao() {
        if (alarmLogBeanDao == null) {
            if (daoSession == null) {
                daoSession = getDaoSession();
            }
            alarmLogBeanDao = daoSession.getAlarmLogBeanDao();
        }
        return alarmLogBeanDao;
    }

    public CommonLogBeanDao getCommonLogBeanDao() {
        if (commonLogBeanDao == null) {
            if (daoSession == null) {
                daoSession = getDaoSession();
            }
            commonLogBeanDao = daoSession.getCommonLogBeanDao();
        }
        return commonLogBeanDao;
    }

    public GunStateBeanDao getGunStateBeanDao() {
        if (gunStateBeanDao == null) {
            if (daoSession == null) {
                daoSession = getDaoSession();
            }
            gunStateBeanDao = daoSession.getGunStateBeanDao();
        }
        return gunStateBeanDao;
    }

    public UserBiosBeanDao getUserBiosBeanDao() {
        if(userBiosBeanDao ==null){
            if (daoSession == null) {
                daoSession = getDaoSession();
            }
            userBiosBeanDao = daoSession.getUserBiosBeanDao();
        }
        return userBiosBeanDao;
    }

    public UserBeanDao getUserBeanDao() {
        if(userBeanDao ==null){
            if(daoSession ==null){
                daoSession =getDaoSession();
            }
            userBeanDao =daoSession.getUserBeanDao();
        }
        return userBeanDao;
    }

    public SubCabBeanDao getSubCabBeanDao() {
        if(subCabBeanDao ==null){
            if(daoSession ==null){
                daoSession =getDaoSession();
            }
            subCabBeanDao =daoSession.getSubCabBeanDao();
        }
        return subCabBeanDao;
    }

    public OfflineTaskDao getOfflineTaskDao() {
        if(offlineTaskDao ==null){
            if(daoSession ==null){
                daoSession =getDaoSession();
            }
            offlineTaskDao =daoSession.getOfflineTaskDao();
        }
        return offlineTaskDao;
    }

    public OfflineTaskItemDao getOfflineTaskItemDao() {
        if(offlineTaskItemDao ==null){
            if(daoSession ==null){
                daoSession =getDaoSession();
            }
            offlineTaskItemDao =daoSession.getOfflineTaskItemDao();
        }
        return offlineTaskItemDao;
    }

    public OperLogBeanDao getOperLogBeanDao() {
        if(operLogBeanDao ==null){
            if(daoSession ==null){
                daoSession =getDaoSession();
            }
            operLogBeanDao =daoSession.getOperLogBeanDao();
        }
        return operLogBeanDao;
    }

    public UrgentOutBeanDao getUrgentOutBeanDao() {
        if (urgentOutBeanDao == null) {
            if (daoSession == null) {
                daoSession = getDaoSession();
            }
            urgentOutBeanDao = daoSession.getUrgentOutBeanDao();
        }
        return urgentOutBeanDao;
    }

    public UrgentGetListBeanDao getUrgentGetListBeanDao() {
        if (urgentGetListBeanDao == null) {
            if (daoSession == null) {
                daoSession = getDaoSession();
            }
            urgentGetListBeanDao = daoSession.getUrgentGetListBeanDao();
        }
        return urgentGetListBeanDao;
    }

    public UrgentBackListBeanDao getUrgentBackListBeanDao() {
        if (urgentBackListBeanDao == null) {
            if (daoSession == null) {
                daoSession = getDaoSession();
            }
            urgentBackListBeanDao = daoSession.getUrgentBackListBeanDao();
        }
        return urgentBackListBeanDao;
    }

    public CabInfoBeanDao getCabInfoBeanDao() {
        return cabInfoBeanDao == null ? (daoSession == null ? getDaoSession():daoSession).getCabInfoBeanDao() : cabInfoBeanDao;
    }

    public void insertCommLog(Context context, UserBean userBean, String content){
       try {
           if(userBean !=null){
               CommonLogBean commonLogBean =new CommonLogBean();
               commonLogBean.setUserId(String.valueOf(userBean.getUserId()));
               commonLogBean.setUserName(userBean.getUserName());
               commonLogBean.setAddTime(System.currentTimeMillis());
               commonLogBean.setContent(content);
               commonLogBean.setMac(SharedUtils.getMacAddress());
               commonLogBeanDao.insert(commonLogBean);
               List<CommonLogBean> commonLogBeanList =new ArrayList<>();
               commonLogBeanList.add(commonLogBean);
               String jsonString = JSON.toJSONString(commonLogBeanList);
               Log.i(TAG, "insertCommLog  jsonString: "+jsonString);
               if(SharedUtils.getIsServerOnline()){
                   postCommonLog(context, jsonString);
               }
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

    private void postCommonLog(Context context, String jsonString) {
        HttpClient.getInstance().postCommonLog(context, jsonString, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) throws JSONException {
                Log.i(TAG, "onSucceed postCommonLog response: "+response.get());

            }

            @Override
            public void onFailed(int what, Response<String> response) {

            }
        });

    }
}

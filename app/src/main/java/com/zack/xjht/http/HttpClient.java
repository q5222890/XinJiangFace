package com.zack.xjht.http;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.RequestQueue;
import com.zack.xjht.Constants;
import com.zack.xjht.Utils.LogUtil;
import com.zack.xjht.Utils.SharedUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yanzhenjie.nohttp.RequestMethod.GET;
import static com.yanzhenjie.nohttp.RequestMethod.POST;


/**
 * 网络请求类
 */

public class HttpClient {

    private static final String TAG = "HttpClient";
    private static HttpClient instance;
    private RequestQueue requestQueue;

    private HttpClient() {
        requestQueue = NoHttp.newRequestQueue();
    }

    public static HttpClient getInstance() {
        if (instance == null) {
            synchronized (HttpClient.class) {
                if (instance == null) {
                    instance = new HttpClient();
                }
            }
        }
        return instance;
    }

    /**
     * @param context
     * @param request
     * @param what
     * @param callback
     */
    public void addStringRequest(Context context, Request<String> request, int what,
                                 HttpListener<String> callback) {
        requestQueue.add(what, request, new HttpResponseListener<>(context,
                request, callback, false, false));
    }

    /**
     * 获取枪柜数据和
     *
     * @param context
     * @param callBack
     */
    public void getCabByMac(Context context, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.GET_CAB_INFO, GET);
        request.set("mac",SharedUtils.getMacAddress());
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 获取枪弹入库任务列表
     *
     * @param context
     * @param callBack
     */
    public void getInStoreTaskList(Context context, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.GET_INSTORE_TASK_LIST, GET);
        request.set("mac", SharedUtils.getMacAddress());
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 获取枪弹入库任务清单列表
     *
     * @param context
     * @param callBack
     */
    public void getInStoreTaskInfo(Context context, String taskId, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.GET_INSTORE_TASK_INFO, GET);
        request.set("mac", SharedUtils.getMacAddress());
        request.set("gunStorageId",taskId);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 获取最新日期和时间
     *
     * @param context
     * @param callBack
     */
    public void getDateAndTime(Context context, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.GET_DATE_AND_TIME, GET);
//        request.set("mac", SharedUtils.getMacAddress());
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 获取用户
     *
     * @param context
     * @param callBack
     */
    public void getUserList(Context context, String userId, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.GET_USER_LIST, GET);
        request.set("mac", SharedUtils.getMacAddress());
        request.set("userId", userId);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 获取生物特征
     *
     * @param context
     * @param callBack
     */
    public void getCharList(Context context, String userId, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.GET_CHAR_LIST, GET);
        request.set("mac", SharedUtils.getMacAddress());
        request.set("userId", userId);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 删除用户生物特征
     *
     * @param context
     * @param callBack
     */
    public void deleteUserChar(Context context, String biosId, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.DELETE_USER_CHAR, GET);
        Log.i(TAG, "deleteUserChar biosId: "+biosId);
        request.set("id", biosId);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 获取用户角色
     *
     * @param context
     * @param callBack
     */
    public void getUserRole(Context context, int userId, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.GET_USER_ROLE, GET);
        request.set("userId", String.valueOf(userId));
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 提交入库数据
     *
     * @param context
     * @param callBack
     */
    public void postInstoreData(Context context, String jsonBody, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.POST_INSTORE_DATA, POST);

        request.setDefineRequestBodyForJson(jsonBody);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 获取报废任务
     *
     * @param context
     * @param callBack
     */
    public void getScrapTaskList(Context context,  HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.GET_SCRAP_TASK_LIST, GET);

        request.add("mac",SharedUtils.getMacAddress());
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 获取报废任务清单
     *
     * @param context
     * @param callBack
     */
    public void getScrapTaskInfo(Context context, String scrapTaskId, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.GET_SCRAP_TASK_INFO, GET);

        request.add("mac",SharedUtils.getMacAddress());
        request.add("scrapTaskId", scrapTaskId);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 提交报废数据
     *
     * @param context
     * @param callBack
     */
    public void postScrapData(Context context, String jsonBody, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.POST_SCRAP_DATA, POST);

        request.setDefineRequestBodyForJson(jsonBody);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 获取报废任务
     *
     * @param context
     * @param callBack
     */
    public void getKeepTaskList(Context context, String operation, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.GET_KEEP_TASK_LIST, GET);

        request.add("mac",SharedUtils.getMacAddress());
        request.add("operation",operation);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 获取保养任务清单
     *
     * @param context
     * @param callBack
     * @param maintainTaskId
     * @param operation
     */
    public void getKeepTaskInfo(Context context, String maintainTaskId, String operation, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.GET_KEEP_TASK_INFO, GET);

        request.add("mac",SharedUtils.getMacAddress());
        request.add("maintainTaskId", maintainTaskId);
        request.add("operation", operation); //getGun：领枪操作 preservationGun：还枪操作
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 提交保养数据
     *
     * @param context
     * @param callBack
     */
    public void postKeepData(Context context, String jsonBody, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.POST_KEEP_DATA, POST);

        request.setDefineRequestBodyForJson(jsonBody);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 上传用户生物特征
     *
     * @param context
     * @param callBack
     */
    public void postUserChar(Context context, String jsonBody, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.POST_USER_CHAR, POST);

        request.setDefineRequestBodyForJson(jsonBody);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 提交临时存放枪支数据
     *
     * @param context
     * @param callBack
     */
    public void postTempStoreGun(Context context, String jsonBody, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.POST_TEMP_STORE_GUN, POST);

        request.setDefineRequestBodyForJson(jsonBody);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 获取临时存放枪支数据
     *
     * @param context
     * @param callBack
     */
    public void getTempStoreGun(Context context,  HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.GET_TEMP_STORE_GUN, GET);

        request.add("mac", SharedUtils.getMacAddress());
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 提交取出临时存放枪支
     *
     * @param context
     * @param callBack
     */
    public void postTempStoreGunGet(Context context, String jsonBody, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.POST_OUT_TEMP_STORE_GUN, POST);

        request.setDefineRequestBodyForJson(jsonBody);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 上传抓拍照片
     *
     * @param context
     * @param callBack
     */
    public void postCapturePhoto(Context context, String base64Pic, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.POST_CAPTURE_PHOTO, POST);

        List<Map<String, Object>> mapList =new ArrayList<>();
        Map<String, Object> params =new HashMap<>();
        params.put("mac", SharedUtils.getMacAddress());
        params.put("pictureContent", base64Pic);
        mapList.add(params);

        String jsonString = JSON.toJSONString(mapList, SerializerFeature.WriteMapNullValue);
//        LogUtil.i(TAG, "postCapturePhoto  jsonString: "+jsonString);
        request.setDefineRequestBodyForJson(jsonString);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 上传报警日志
     *
     * @param context
     * @param callBack
     */
    public void postAlarmLog(Context context, String jsonBody, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.POST_ALARM_LOG, POST);

        request.setDefineRequestBodyForJson(jsonBody);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 提交紧急领枪任务
     *
     * @param context
     * @param callBack
     */
    public void postUrgentGet(Context context, String jsonBody, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.POST_URGENT_GET, POST);

        request.setDefineRequestBodyForJson(jsonBody);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 获取紧急领枪任务
     *
     * @param context
     * @param callBack
     */
    public void getUrgentTask(Context context,  HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.GET_URGENT_TASK, GET);

        request.add("mac", SharedUtils.getMacAddress());
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 获取紧急领枪任务清单
     *
     * @param context
     * @param callBack
     */
    public void getUrgentTaskInfo(Context context, String taskId, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.GET_URGENT_TASK_INFO, GET);

        request.add("urgentTaskId",taskId);
        request.add("mac",SharedUtils.getMacAddress());
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 提交紧急领枪任务归还数据
     *
     * @param context
     * @param callBack
     */
    public void postUrgenTaskBackData(Context context, String jsonBody, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.POST_URGENT_TASK_BACK_DATA, POST);

        request.setDefineRequestBodyForJson(jsonBody);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 获取唯一特征id
     *
     * @param context
     * @param callBack
     */
    public void userLogin(Context context, String policeNo, String password, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.USER_NO_LOGIN, POST);

        Map<String, Object> params =new HashMap<>();
        params.put("loginName",policeNo);
        params.put("passWord",password);

        String jsonString = JSON.toJSONString(params, SerializerFeature.WriteMapNullValue);
        request.setDefineRequestBodyForJson(jsonString);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 获取领枪任务
     *
     * @param context
     * @param callBack
     */
    public void getPoliceTaskList(Context context,  String operation, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.GET_POLICE_TASK_LIST, GET);

        request.add("mac",SharedUtils.getMacAddress());
        request.add("operation", operation);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 获取领枪任务详情
     *
     * @param context
     * @param callBack
     */
    public void getPoliceListInfo(Context context, String taskId,  HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.GET_POLICE_TASK_INFO, GET);

        request.add("mac",SharedUtils.getMacAddress());
        request.add("policeTaskId",taskId);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 获取唯一特征id
     *
     * @param context
     * @param callBack
     */
    public void postPoliceTaskData(Context context, String json, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.POST_POLICE_TASK_DATA, POST);

        Log.i(TAG, "postPoliceTaskData json: "+json);
        request.setDefineRequestBodyForJson(json);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 上传脱机领还枪日志记录
     *
     * @param context
     * @param callBack
     */
    public void postOfflineTaskLog(Context context, String json, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.POST_OFFLINE_TASK_LOG, POST);

        Log.i(TAG, "postPoliceTaskData json: "+json);
        request.setDefineRequestBodyForJson(json);
        addStringRequest(context, request, 0, callBack);
    }

    /**
     * 获取唯一特征id
     *
     * @param context
     * @param callBack
     */
    public void postCommonLog(Context context, String json, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.POST_COMMON_LOG, POST);

        request.setDefineRequestBodyForJson(json);
        addStringRequest(context, request, 0, callBack);
    }

    public void uploadOpenMessage(Context context, String json, HttpListener<String> callBack){
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getPlatformServer() + Constants.UPLOAD_OPEN_MESSAGE, POST);

        request.setDefineRequestBodyForJson(json);
        addStringRequest(context, request, 0, callBack);
    }

    public void uploadAlarmMessage(Context context, String json, HttpListener<String> callBack){
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getPlatformServer() + Constants.UPLOAD_ALARM_MESSAGE, POST);

        request.setDefineRequestBodyForJson(json);
        addStringRequest(context, request, 0, callBack);
    }


    /**
     * 提交紧急领枪数据
     *
     * @param context
     * @param callBack
     */
    public void postUrgentData(Context context, String json, HttpListener<String> callBack) {
        Request<String> request = NoHttp.createStringRequest(
                "http://"+ SharedUtils.getServerIp() +":"+SharedUtils.getServerPort()
                        + Constants.POST_URGENT_DATA, POST);

        request.setDefineRequestBodyForJson(json);
        addStringRequest(context, request, 0, callBack);
    }

}

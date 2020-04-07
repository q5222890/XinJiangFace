package com.zack.xjht.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.zack.xjht.App;
import com.zack.xjht.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * sp工具类
 */

public class SharedUtils {

    private static String Name = "intelligent";


    private static SharedPreferences getShared() {
        SharedPreferences sp = App.getInstance().getSharedPreferences(Name,
                Context.MODE_PRIVATE);
        return sp;
    }

    public static Editor getEditor() {
        return getShared().edit();
    }

    public static void putString(String key, String content) {
        getEditor().putString(key, content).commit();
    }

    public static String getString(String key) {
        return getShared().getString(key, "");
    }

    public static void putFloat(String key, float content) {
        getEditor().putFloat(key, content).apply();
    }

    public static Float getFloat(String key) {
        return getShared().getFloat(key, 0f);
    }

    public static void putInt(String key, int val) {
        getEditor().putInt(key, val).apply();
    }

    public static int getInt(String key) {
        return getShared().getInt(key, 0);
    }

    public static void putBoolean(String key, boolean val) {
        getEditor().putBoolean(key, val).apply();
    }

    public static boolean getBoolean(String key) {
        return getShared().getBoolean(key, false);
    }

    public static boolean saveArray(List<String> list) {
        Editor editor = getShared().edit();
        editor.putInt("list_size", list.size());

        for (int i = 0; i < list.size(); i++) {
            editor.remove("Status_" + i);
            editor.putString("Status_" + i, list.get(i));
        }
        return editor.commit();
    }

    public static List<String> loadArray() {
        int size = getShared().getInt("list_size", 0);

        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(getShared().getString("Status_" + i, ""));
        }
        return list;
    }

    public static void saveSerialPath(String path) {
        getEditor().putString("serial_path", path).apply();
    }

    public static String getSerialPath() {
        return getShared().getString("serial_path", "");
    }

    public static void savePathPos(int pos) {
        getEditor().putInt("device_position", pos).apply();
    }

    public static int getPathPos() {
        return getShared().getInt("device_position", 0);
    }

    public static void saveBaudrate(String baudrate) {
        getEditor().putString("serial_baudrate", baudrate).apply();
    }

    public static String getBaudrate() {
        return getShared().getString("serial_baudrate", "-1");
    }

    public static void saveBaudPos(int pos) {
        getEditor().putInt("baudrate_position", pos).apply();
    }

    public static int getBaudPos() {
        return getShared().getInt("baudrate_position", 0);
    }

    //服务器ip地址
    public static void saveServerIp(String url) {
        getEditor().putString("server_ip", url).apply();
    }

    public static String getServerIp() {
        return getShared().getString("server_ip", Constants.IP);
    }
    //服务器端口号
    public static void saveServerPort(String url) {
        getEditor().putString("server_port", url).apply();
    }

    public static String getServerPort() {
        return getShared().getString("server_port", Constants.PORT);
    }

    //酒精检测
    public static void saveAlcoholDetect(boolean val){
        getEditor().putBoolean("alcohol_detect", val).apply();
    }

    public static boolean getAlcoholDetect(){
        return getShared().getBoolean("alcohol_detect",false);
    }

    //温湿度检测
    public static void saveHumitureOpen(boolean val){
        getEditor().putBoolean("humiture_open", val).apply();
    }

    public static boolean getHumitureOpen(){
        return getShared().getBoolean("humiture_open",false);
    }

    /**
     * 震动状态
     */
    public static void saveVibrationStatus(int status){
        getEditor().putInt("vibration_status", status).apply();
    }

    public static int getVibrationStatus(){
        return getShared().getInt("vibration_status", 0);
    }

    public static void saveOperGunStatus(int status){
        getEditor().putInt("oper_gun_status", status).apply();
    }

    public static int getOperGunStatus(){
        return getShared().getInt("oper_gun_status", 0);
    }
    //柜门超时报警状态
    public static void saveOutTimeStatus(int status){
        getEditor().putInt("out_time_status", status).apply();
    }

    public static int getOutTimeStatus(){
        return getShared().getInt("out_time_status", 0);
    }
    //电源状态
    public static void savePowerStatus(int status){
        getEditor().putInt("power_status", status).apply();
    }

    public static int getPowerStatus(){
        return getShared().getInt("power_status", 0);
    }
    //网络状态 0.连接 1.断开
    public static void saveNetworkStatus(int status){
        getEditor().putInt("network_status", status).apply();
    }

    public static int getNetworkStatus(){
        return getShared().getInt("network_status", 0);
    }
    //备用钥匙开启状态
    public static void saveBackupOpenStatus(int status){
        getEditor().putInt("backup_open_status", status).apply();
    }

    public static int getBackupOpenStatus(){
        return getShared().getInt("backup_open_status", 0);
    }

    //枪柜开启状态
    public static void saveCabOpenStatus(int status){
        getEditor().putInt("cab_open_status", status).apply();
    }

    public static int getCabOpenStatus(){
        return getShared().getInt("cab_open_status", 0);
    }

    //枪柜开门超时状态
    public static void saveOpenOvertimeStatus(int status){
        getEditor().putInt("open_overtime_status", status).apply();
    }

    public static int getOpenOvertimeStatus(){
        return getShared().getInt("open_overtime_status", 0);
    }

    //非正常开启柜门状态
    public static void saveOpenAbnormalStatus(int status){
        getEditor().putInt("open_abnormal_status", status).apply();
    }

    public static int getOpenAbnormalStatus(){
        return getShared().getInt("open_abnormal_status", 0);
    }

    //非正常领取枪支弹药状态
    public static void saveIllegalGetGunStatus(int status){
        getEditor().putInt("illegal_get_gun", status).apply();
    }

    public static int getIllegalGetGunStatus(){
        return getShared().getInt("illegal_get_gun", 0);
    }

    //温湿度异常状态
    public static void saveHumitureAlarmStatus(int status){
        getEditor().putInt("humiture_alarm_status", status).apply();
    }

    public static int getHumitureAlarmStatus(){
        return getShared().getInt("humiture_alarm_status", 0);
    }

    //酒精检测
    public static void saveAlcoholStatus(int status){
        getEditor().putInt("alcohol_status", status).apply();
    }

    public static int getAlcoholStatus(){
        return getShared().getInt("alcohol_status", 0);
    }

    //报警开关
    public static void setUserLogin(boolean userLogin){
        getEditor().putBoolean("user_login", userLogin).apply();
    }

    public  static boolean getUserLogin(){
        return getShared().getBoolean("user_login",false);
    }

    //震动报警
    public static void setVibration(int val){
        getEditor().putInt("vibration", val).apply();
    }

    public  static int getVibration(){
        return getShared().getInt("vibration",0);
    }

    //是否打开抓拍
    public static void setOpenCapture(boolean isCapture){
        getEditor().putBoolean("open_capture", isCapture).apply();
    }

    public static boolean getOpenCapture(){
        return getShared().getBoolean("open_capture", false);
    }

    //是否在抓拍
    public static void setIsCapturing(boolean isCapture){
        getEditor().putBoolean("is_capture", isCapture).apply();
    }

    public static boolean getIsCapturing(){
        return getShared().getBoolean("is_capture", false);
    }

    //枪柜类型
    public static void setCabType(String val){
        getEditor().putString("cab_type", val).apply();
    }

    public static String getCabType(){
        return getShared().getString("cab_type", "3");
    }

    //枪柜id
    public static void saveGunCabId(String val) {
        getEditor().putString("cab_id", val).apply();
    }

    public static String getGunCabId() {
        return getShared().getString("cab_id", "1234567890");
    }

    //左柜门编号
    public static void saveLeftCabNo(String val){
        getEditor().putString("left_cab_no", val).apply();
    }

    public static String getLeftCabNo(){
        return getShared().getString("left_cab_no", "0");
    }
    //右柜门编号
    public static void saveRightCabNo(String val){
        getEditor().putString("right_cab_no", val).apply();
    }

    public static String getRightCabNo(){
        return getShared().getString("right_cab_no", "255");
    }
    // 温度
    public static void saveTemperatureValue(float temp){
        getEditor().putFloat("temperature_value", temp).apply();
    }

    public static float getTemperatureValue(){
        return getShared().getFloat("temperature_value", 26.00f);
    }
    // 湿度
    public static void saveHumidityValue(float humidity){
        getEditor().putFloat("humidity_value", humidity).apply();
    }

    public static float getHumidityValue(){
        return getShared().getFloat("humidity_value", 50.00f);
    }

    // 保存是否第一次验证
    public static void saveIsSecondVerify(boolean val){
        getEditor().putBoolean("is_second_verify", val).apply();
    }

    //获取是否第一次验证
    public static boolean getIsSecondVerify(){
        return getShared().getBoolean("is_second_verify", false);
    }

    // 保存MAC地址
    public static void setMacAddress(String mac){
        getEditor().putString("mac_addr", mac).apply();
    }

    //获取MAC地址
    public static String getMacAddress(){
        return getShared().getString("mac_addr", "D2:66:E8:6C:50:25");
    }

    // 报警开关
    public static void setIsAlarmOpen(boolean isOpen){
        getEditor().putBoolean("is_alarm_open", isOpen).apply();
    }

    public static boolean getIsAlarmOpen(){
        return getShared().getBoolean("is_alarm_open", false);
    }

    // 酒精开关
    public static void setIsAlcoholOpen(boolean isOpen){
        getEditor().putBoolean("is_alcohol_open", isOpen).apply();
    }

    public static boolean getIsAlcoholOpen(){
        return getShared().getBoolean("is_alcohol_open", false);
    }

    // 抓拍开关
    public static void setIsCaptureOpen(boolean isOpen){
        getEditor().putBoolean("is_capture_open", isOpen).apply();
    }

    public static boolean getIsCaptureOpen(){
        return getShared().getBoolean("is_capture_open", false);
    }

    // 验证人数
    public static void setVerifyUserNumber(int num){
        getEditor().putInt("verify_number", num).apply();
    }

    public static int getVerifyUserNumber(){
        return getShared().getInt("verify_number", 2);
    }

    // 第一人验证

    /**
     * 1.指纹 2.人脸 3.虹膜4.指纹+人脸 5.指纹+虹膜 6.人脸+虹膜
     * @param val
     */
    public static void setFirstUserVerify(int val){
        getEditor().putInt("first_user_verify", val).apply();
    }

    public static int getFirstUserVerify(){
        return getShared().getInt("first_user_verify", 1);
    }

    // 第二人验证
    public static void setSecondUserVerify(int val){
        getEditor().putInt("second_user_verify", val).apply();
    }

    public static int getSecondUserVerify(){
        return getShared().getInt("second_user_verify", 1);
    }

    // 第三人验证
    public static void setThirdUserVerify(int val){
        getEditor().putInt("third_user_verify", val).apply();
    }

    public static int getThirdUserVerify(){
        return getShared().getInt("third_user_verify", 1);
    }

    // 第一人酒精检测
    public static void setFirstVerifyAlcohol(boolean val){
        getEditor().putBoolean("first_verify_alcohol", val).apply();
    }

    public static boolean getFirstVerifyAlcohol(){
        return getShared().getBoolean("first_verify_alcohol", false);
    }

    // 第二人酒精检测
    public static void setSecondVerifyAlcohol(boolean val){
        getEditor().putBoolean("second_verify_alcohol", val).apply();
    }

    public static boolean getSecondVerifyAlcohol(){
        return getShared().getBoolean("second_verify_alcohol", false);
    }

    // 第三人酒精检测
    public static void setThirdVerifyAlcohol(boolean val){
        getEditor().putBoolean("third_verify_alcohol", val).apply();
    }

    public static boolean getThirdVerifyAlcohol(){
        return getShared().getBoolean("third_verify_alcohol", false);
    }

    /**
     * 设置电源板地址
     * @param powerAddr
     */
    public static void setPowerAddress(int powerAddr) {
        getEditor().putInt("power_addr",powerAddr).apply();
    }

    public static int getPowerAddress(){
        return getShared().getInt("power_addr", 254);
    }

    /**
     * 是否打开温湿度
     * @param isOpen
     */
    public static void setIsHumitureOpen(boolean isOpen) {
        getEditor().putBoolean("humiture_open", isOpen).apply();
    }

    public static boolean getIsHumitureOpen() {
        return getShared().getBoolean("humiture_open", false);
    }

    /**
     * 湿度报警值
     * @param val
     */
    public static void setHumitureAlarmValue(int val) {
        getEditor().putInt("humiture_alarm_value", val).apply();
    }

    public static int getHumitureAlarmValue() {
        return getShared().getInt("humiture_alarm_value", 80);
    }

    /**
     * 温度报警值
     * @param val
     */
    public static void setTempratureAlarmValue(int val) {
        getEditor().putInt("temprature_alarm_value", val).apply();
    }

    public static int getTempratureAlarmValue() {
        return getShared().getInt("temprature_alarm_value", 60);
    }

    /**
     * 检查状态
     * @param isCheckStatus
     */
    public static void setIsCheckStatus(boolean isCheckStatus) {
        getEditor().putBoolean("is_check_status", isCheckStatus).apply();
    }

    public static boolean getIsCheckStatus() {
        return getShared().getBoolean("is_check_status", true);
    }


    //服务器是否正常连接
    public static void setIsServerOnline(boolean isOnline){
        getEditor().putBoolean("is_online", isOnline).apply();
    }

    public static boolean getIsServerOnline(){
        return getShared().getBoolean("is_online",false);
    }

    /**
     * 保存平台服务器地址
     * @param serverString
     */
    public static void savePlatformServer(String serverString) {
        getEditor().putString("platform_server", serverString).apply();
    }

    /**
     * 获取平台服务器地址
     * @return
     */
    public static String getPlatformServer(){
        return getShared().getString("platform_server","" );
    }

    /**
     * 设置指纹类型  0.光学 1.电容
     * @param type
     */
    public static void setFingerprintType(int type) {
        getEditor().putInt("fingerprint_type", type).apply();
    }

    public static int getFingerprintType() {
        return getShared().getInt("fingerprint_type", 0);
    }


    /**
     * 设置是否查询状态
     * @param isQuery
     */
    public static void setIsQuery(boolean isQuery){
        getEditor().putBoolean("is_query", isQuery).apply();
    }

    public static boolean getIsQuery(){
        return getShared().getBoolean("is_query",false);
    }
}

package com.zack.xjht.Utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.zack.xjht.App;


public class ToastUtil {
	
	private static Toast sToast = null;
	
	public static Toast show(Context context, String msg, int duration) {
		
		if (sToast == null) {
			sToast = Toast.makeText(App.getInstance(), msg, duration);
		} else {
			sToast.setDuration(duration);
			sToast.setText(msg);
		}

		sToast.show();
		return sToast;
	}
	
	public static Toast show(String msg, int duration) {
		
		if (sToast == null) {
			sToast = Toast.makeText(App.getInstance(), msg, duration);
		} else {
			sToast.setDuration(duration);
			sToast.setText(msg);
		}

		sToast.show();
		return sToast;
	}
	
	public static Toast showLong(String msg){
		return show(msg, Toast.LENGTH_LONG);
	}
	
	public static Toast showShort(String msg){
		return show(msg, Toast.LENGTH_SHORT);
	}

	public static Toast showShort(int msgId){
		return show(App.getInstance().getResources().getString(msgId), Toast.LENGTH_SHORT);
	}
	/**
	 * 显示屏幕中间的Toast（短时间）
	 * @param context
	 * @param msg
	 */
	public static void showCenterToast(Context context, String msg)
	{
		Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	/**
	 * 显示屏幕中间的Toast（长时间）
	 * @param context
	 * @param msg
	 */
	public static void showCenterToastToLong(Context context, String msg)
	{
		Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}

package com.zack.xjht.Utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.zack.xjht.R;


public class DialogUtils {

    private static TextView txt_tip;

    public static Dialog creatLoadDialog(Context context, String titleTxt) {
        Dialog loadDialog = new Dialog(context,  R.style.dialog);
        TextView title = (TextView) loadDialog.findViewById(R.id.txt_dialog_title);
        title.setText(titleTxt);
        loadDialog.setContentView(R.layout.ac_load_dlg);
        loadDialog.setCancelable(false);
        loadDialog.setCanceledOnTouchOutside(false);
        return loadDialog;
    }

    public static Dialog creatTipDialog(Context context, String title, String tip, OnClickListener listener) {
        Dialog tipDialog = null;
        if(context !=null){
            tipDialog = new Dialog(context, R.style.dialog);
        }
        tipDialog.setContentView(R.layout.ac_tip_dlg);

        TextView txt_title = (TextView) tipDialog.findViewById(R.id.title);
        txt_tip = (TextView) tipDialog.findViewById(R.id.tip);
        Button btn_confirm = (Button) tipDialog.findViewById(R.id.btn_confirm);
        btn_confirm.setOnClickListener(listener);
        tipDialog.setCancelable(false);
        tipDialog.setCanceledOnTouchOutside(false);
        txt_title.setText(title);
        txt_tip.setText(tip);
        return tipDialog;
    }

    public static void setTipText(String txt){
        txt_tip.setText(txt);
    }

    public static Dialog createChoiceDialog(Context context, String msg, OnClickListener onClickListener){
        final Dialog choiceDialog =new Dialog(context, R.style.dialog);
            choiceDialog.setContentView(R.layout.custome_dialog);
        choiceDialog.setCancelable(false);
        choiceDialog.setCanceledOnTouchOutside(false);
        TextView messageTxt = (TextView) choiceDialog.findViewById(R.id.message);
        TextView cancelTxt = (TextView) choiceDialog.findViewById(R.id.dl_cancel);
        TextView confirmTxt = (TextView) choiceDialog.findViewById(R.id.dl_confirm);
        cancelTxt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                choiceDialog.dismiss();
            }
        });
        confirmTxt.setOnClickListener(onClickListener);
        messageTxt.setText(msg);
        return choiceDialog;
    }

    public static Dialog createChoiceDialog(Context context, String msg,
                                            OnClickListener getClickListener,
                                            OnClickListener backClickListener){
        Dialog choiceDialog =new Dialog(context, R.style.dialog);
        choiceDialog.setContentView(R.layout.choice_urgent_type_dialog);
//        choiceDialog.setCancelable(false);
//        choiceDialog.setCanceledOnTouchOutside(false);
        TextView messageTxt = (TextView) choiceDialog.findViewById(R.id.message);
        Button btnGet =  (Button) choiceDialog.findViewById(R.id.dl_urgent_get);
        Button btnBack = (Button) choiceDialog.findViewById(R.id.dl_urgent_back);
        btnGet.setOnClickListener(getClickListener);
        btnBack.setOnClickListener(backClickListener);
        messageTxt.setText(msg);
        return choiceDialog;
    }

    public static Dialog createInitDialog(Context context){
        Dialog initDialog =new Dialog(context,  R.style.dialog);
        initDialog.setContentView(R.layout.ac_load_dlg);
        initDialog.setCancelable(true);
        initDialog.setCanceledOnTouchOutside(false);
        return initDialog;
    }

    public static Dialog createUserDialog(Context context, OnClickListener clickListener,
                                          OnConfirmListener confirmListener){
        final Dialog userDialog =new Dialog(context,  R.style.dialog);
        userDialog.setContentView(R.layout.super_user_dialog);

        EditText editName = (EditText) userDialog.findViewById(R.id.edt_user_name);
        EditText edtPwd = (EditText) userDialog.findViewById(R.id.edt_user_pwd);
        Button btnCancel = (Button) userDialog.findViewById(R.id.open_btn_login);
        Button btnConfirm = (Button) userDialog.findViewById(R.id.open_btn_cancel);
        btnConfirm.setOnClickListener(clickListener);
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                userDialog.dismiss();
            }
        });

        userDialog.setCancelable(true);
        userDialog.setCanceledOnTouchOutside(false);
        return userDialog;
    }

    interface OnConfirmListener{
        void onConfirm(String name, String password);
    }

    public OnConfirmListener onConfirmListener;
    public void setOnConfirmListener(OnConfirmListener onConfirmListener){
        this.onConfirmListener =onConfirmListener;
    }
}

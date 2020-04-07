package com.zack.xjht.ui.dialog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zack.xjht.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ImageDialog extends DialogFragment {

    @BindView(R.id.dialog_iv_image)
    ImageView dialogIvImage;
    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_image, container, false);
        setStyle(R.style.dialog, R.style.AppTheme);
        unbinder = ButterKnife.bind(this, view);
        assert getArguments() != null;
        String imagePath = getArguments().getString("imagePath");
        if(!TextUtils.isEmpty(imagePath)){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if(bitmap !=null){
                dialogIvImage.setImageBitmap(bitmap);
            }
        }

        return view;
    }


//    @NonNull
//    @Override
//    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        AlertDialog dialog =  new AlertDialog.Builder(getActivity())
//                .setTitle("神灯")
//                .setMessage("来选择你要实现的一个愿望吧")
//                .setPositiveButton("车子", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                })
//                .setNegativeButton("房子", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                }).create();
//        return dialog;
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

    }

}

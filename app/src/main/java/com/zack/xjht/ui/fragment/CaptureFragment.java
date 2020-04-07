package com.zack.xjht.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zack.xjht.R;
import com.zack.xjht.entity.PhotoBean;
import com.zack.xjht.ui.dialog.ImageDialog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 抓拍照片
 */
public class CaptureFragment extends Fragment {
    private static final String TAG = "CaptureFragment";

    Unbinder unbinder;

    @BindView(R.id.btn_pre_page)
    Button btnPrePage;
    @BindView(R.id.tv_cur_page)
    TextView tvCurPage;
    @BindView(R.id.btn_next_page)
    Button btnNextPage;
    @BindView(R.id.btn_delete_photo)
    Button btnDeletePhoto;
    @BindView(R.id.ll_page)
    LinearLayout llPage;
    @BindView(R.id.capture_recycler_view)
    RecyclerView captureRecyclerView;
    private CaptureAdapter captureAdapter;
    private List<PhotoBean> imageList;
    private int index = 0;
    private int pageCount = 20;
    private FragmentActivity activity;
    private Context mContext;

    public CaptureFragment() {
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
        View view = inflater.inflate(R.layout.fragment_capture, container, false);
        unbinder = ButterKnife.bind(this, view);
        imageList = new ArrayList<>();
        btnPrePage.setVisibility(View.INVISIBLE);
        btnNextPage.setVisibility(View.INVISIBLE);

        GridLayoutManager glm = new GridLayoutManager(getContext(), 5);
        captureRecyclerView.setLayoutManager(glm);
        captureAdapter = new CaptureAdapter();
        captureRecyclerView.setAdapter(captureAdapter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    imageList = getImagePathFromSD();
                    Collections.reverse(imageList);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            captureAdapter.notifyDataSetChanged();
                            if (!activity.isFinishing()) {
                                initPreNextBtn();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return view;
    }

    private void initPreNextBtn() {
        if (imageList.isEmpty()) {
            btnDeletePhoto.setVisibility(View.INVISIBLE);
            tvCurPage.setText(index + 1 + "/1");
        } else {
            if (imageList.size() <= pageCount) {
                btnNextPage.setVisibility(View.INVISIBLE);
            } else {
                btnNextPage.setVisibility(View.VISIBLE);
            }
            tvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) imageList.size() / pageCount));
        }
    }

    private void nexPager() {
        index++;
        tvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) imageList.size() / pageCount));
        captureAdapter.notifyDataSetChanged();
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "nexPager index: " + index);
    }

    private void checkButton() {
        if (index <= 0) {
            Log.i(TAG, "checkButton index <=0: ");
            btnPrePage.setVisibility(View.INVISIBLE);
            btnNextPage.setVisibility(View.VISIBLE);
        } else if (imageList.size() - index * pageCount <= pageCount) {    //数据总数减每页数当小于每页可显示的数字时既是最后一页
            Log.i(TAG, "checkButton 2: ");
            btnPrePage.setVisibility(View.VISIBLE);
            btnNextPage.setVisibility(View.INVISIBLE);
        } else {
            Log.i(TAG, "checkButton 3: ");
            btnNextPage.setVisibility(View.VISIBLE);
            btnPrePage.setVisibility(View.VISIBLE);
        }
    }

    private void prePager() {
        index--;
        captureAdapter.notifyDataSetChanged();
        tvCurPage.setText(index + 1 + "/" + (int) Math.ceil((double) imageList.size() / pageCount));
        //隐藏上一个或下一个按钮
        checkButton();
        Log.i(TAG, "prePager index: " + index);
    }

    @OnClick({R.id.btn_pre_page, R.id.btn_next_page, R.id.btn_delete_photo})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_pre_page://上一页
                prePager();
                break;
            case R.id.btn_next_page://下一页
                nexPager();
                break;
            case R.id.btn_delete_photo: //删除图片
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!imageList.isEmpty()) {
                            for (int i = 0; i < imageList.size(); i++) {
                                PhotoBean photoBean = imageList.get(i);
                                String imagePath = photoBean.getImagePath();
                                String imageName = photoBean.getImageName();
                                Log.i(TAG, "onCreateView  imagePath: " + imagePath + " imageName: " + imageName);
//                                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
//                                String base64Str = Utils.bitmapToBase64Str(bitmap);
//                        Log.i(TAG, "onViewClicked base64Str length: "+base64Str.length());
                                File file = new File(imagePath);
                                if (file.exists()) {
                                    file.delete();
                                    imageList.remove(i);
                                }
                            }
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    captureAdapter.notifyDataSetChanged();
                                }
                            });

                        }
                    }
                }).start();

                break;
        }
    }

    public class CaptureAdapter extends RecyclerView.Adapter<CaptureAdapter.CaptureViewHolder> {

        @Override
        public CaptureAdapter.CaptureViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.capture_recycler_list_item, parent, false);
            return new CaptureViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CaptureAdapter.CaptureViewHolder holder, int position) {
            ViewGroup.LayoutParams layoutParams = holder.captureIvItemImage.getLayoutParams();
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            holder.captureIvItemImage.setAdjustViewBounds(true);
            int pos = position + index * pageCount;
            PhotoBean capturePictureBean = imageList.get(pos);
            String imageName = capturePictureBean.getImageName();
            final String imagePath = capturePictureBean.getImagePath();
//            Log.i(TAG, "onBindViewHolder  path: "+imagePath+" name: "+imageName);
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            holder.captureIvItemImage.setImageBitmap(bitmap);
            holder.captureTvItemName.setText(imageName);
            holder.captureIvItemImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //点击全屏显示大图
                    ImageDialog imageDialog = new ImageDialog();
                    Bundle args = new Bundle();
                    args.putString("imagePath", imagePath);
                    imageDialog.setArguments(args);
                    imageDialog.show(getChildFragmentManager(), "ImageDialog");
                }
            });
        }

        @Override
        public int getItemCount() {
            int current = index * pageCount;
            return imageList.size() - current < pageCount ? imageList.size() - current : pageCount;
        }

        class CaptureViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.capture_iv_item_image)
            ImageView captureIvItemImage;
            @BindView(R.id.capture_tv_item_name)
            TextView captureTvItemName;

            CaptureViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * 从sd卡获取图片资源
     *
     * @return
     */
    public List<PhotoBean> getImagePathFromSD() {
        // 图片列表
        List<PhotoBean> imageList = new ArrayList<PhotoBean>();
        // 得到sd卡内image文件夹的路径   File.separator(/)
//        String filePath = Environment.getExternalStoragePublicDirectory
//                (Environment.DIRECTORY_PICTURES).toString() + File.separator
//                + "Capture";
        String filePath = mContext.getExternalCacheDir().getAbsolutePath();
//        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator
//                + "Camera";
        Log.i(TAG, "getImagePathFromSD filePath: " + filePath);
        // 得到该路径文件夹下所有的文件
        File fileAll = new File(filePath);
        File[] files = fileAll.listFiles();
        // 将所有的文件存入ArrayList中,并过滤所有图片格式的文件
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (checkIsImageFile(file.getPath())) {
                    String name = file.getName();
//                Log.i("MAIN", "getImagePathFromSD name: "+name);
                    PhotoBean photoBean = new PhotoBean();
                    photoBean.setImagePath(file.getPath());
                    file.getName();
                    long lastmodified = file.lastModified();
//                Log.i("main", "getImagePathFromSD lastmodified : "+lastmodified);
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    String newDate = simpleDateFormat.format(new Date(lastmodified));
                    Log.i("main", "getImagePathFromSD date: " + newDate);
                    photoBean.setImageName(newDate);
                    imageList.add(photoBean);
                }
            }
        }
        // 返回得到的图片列表
        return imageList;
    }

    /**
     * 检查扩展名，得到图片格式的文件
     *
     * @param fName 文件名
     * @return
     */
    @SuppressLint("DefaultLocale")
    private static boolean checkIsImageFile(String fName) {
        boolean isImageFile = false;
        // 获取扩展名
        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
                fName.length()).toLowerCase();
        if (FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif")
                || FileEnd.equals("jpeg") || FileEnd.equals("bmp")) {
            isImageFile = true;
        } else {
            isImageFile = false;
        }
        return isImageFile;
    }


}

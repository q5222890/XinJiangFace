package com.zack.xjht.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zack.xjht.Constants;
import com.zack.xjht.R;
import com.zack.xjht.Utils.SharedUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class IntegratedActivity extends BaseActivity {
    private static final String TAG = "IntegratedActivity";

    @BindView(R.id.main_iv_emblem)
    ImageView mainIvEmblem;
    @BindView(R.id.integrated_ll_01)
    LinearLayout integratedLl01;
    @BindView(R.id.integrated_iv_keep)
    ImageView integratedIvKeep;
    @BindView(R.id.integrated_iv_scrap)
    ImageView integratedIvScrap;
    @BindView(R.id.integrated_iv_instore)
    ImageView integratedIvInstore;
    @BindView(R.id.integrated_iv_temp_store)
    ImageView integratedIvTempStore;
    @BindView(R.id.integrated_iv_info)
    ImageView integratedIvInfo;
    @BindView(R.id.integrated_iv_char)
    ImageView integratedIvChar;
    private Unbinder bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_integrated);
        bind = ButterKnife.bind(this);
        String cabType = SharedUtils.getCabType();
        if(cabType.equals(Constants.TYPE_AMMO_CAB)){
            integratedLl01.setVisibility(View.GONE);
            integratedIvInstore.setImageResource(R.drawable.instore_ammo);
        }else if(cabType.equals(Constants.TYPE_MIX_CAB)){
            integratedLl01.setVisibility(View.VISIBLE);
            integratedIvInstore.setImageResource(R.drawable.instore);
        }else {
            integratedLl01.setVisibility(View.VISIBLE);
            integratedIvInstore.setImageResource(R.drawable.instore_gun);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @OnClick({R.id.integrated_iv_keep, R.id.integrated_iv_scrap, R.id.integrated_iv_instore,
            R.id.integrated_iv_temp_store, R.id.integrated_iv_info, R.id.integrated_iv_char})
    public void onViewClicked(View view) {
        super.onViewClicked(view);
        Intent intent;
        switch (view.getId()) {
            case R.id.integrated_iv_keep:
                intent = new Intent(IntegratedActivity.this, KeepActivity.class);
                startActivity(intent);
                break;
            case R.id.integrated_iv_scrap:
                intent = new Intent(IntegratedActivity.this, ScrapActivity.class);
                startActivity(intent);
                break;
            case R.id.integrated_iv_instore:
                intent = new Intent(IntegratedActivity.this, InStoreActivity.class);
                startActivity(intent);
                break;
            case R.id.integrated_iv_temp_store:
                intent = new Intent(IntegratedActivity.this, TempStoreActivity.class);
                startActivity(intent);
                break;
            case R.id.integrated_iv_info:
                intent = new Intent(IntegratedActivity.this, QueryActivity.class);
                startActivity(intent);
                break;
            case R.id.integrated_iv_char:
                intent =new Intent(IntegratedActivity.this, LoginActivity.class);
                intent.putExtra("activity", Constants.ACTIVITY_USER);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bind.unbind();
    }
}

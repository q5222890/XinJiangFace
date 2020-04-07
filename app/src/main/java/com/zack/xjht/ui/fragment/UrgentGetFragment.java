package com.zack.xjht.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zack.xjht.R;
import com.zack.xjht.ui.widget.NoSrcollViewPage;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class UrgentGetFragment extends Fragment {
    private static final String TAG = "UrgentGetFragment";

    Unbinder unbinder;
    @BindView(R.id.urgent_get_tab_layout)
    TabLayout urgentGetTabLayout;
    @BindView(R.id.urgent_get_view_pager)
    NoSrcollViewPage urgentGetViewPager;

    private List<Fragment> fragments;
    private ShortPagerAdapter adapter;

    public UrgentGetFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_urgent_get, container, false);
        unbinder = ButterKnife.bind(this, view);

        fragments = new ArrayList<>();
        fragments.add(new UrgentGetGunsFragment());
        fragments.add(new UrgentGetAmmosFragment());

        urgentGetTabLayout.setupWithViewPager(urgentGetViewPager);
        adapter = new ShortPagerAdapter(getChildFragmentManager());
        urgentGetViewPager.setAdapter(adapter);

        urgentGetTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
//                Log.i(TAG, "onTabSelected tab: "+tab.getPosition()+" 标题:"+tab.getText());
                if(tab.getText().equals("领取枪支")){
                    //
                }else if(tab.getText().equals("领取弹药")){

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return view;
    }

    private class ShortPagerAdapter extends FragmentPagerAdapter {
        public String[] mTilte;

        public ShortPagerAdapter(FragmentManager fm) {
            super(fm);
            mTilte = new String[]{"领取枪支", "领取弹药"};
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTilte[position];
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}

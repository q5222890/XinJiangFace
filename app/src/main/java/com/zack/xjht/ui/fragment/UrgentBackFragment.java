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

public class UrgentBackFragment extends Fragment {

    private static final String TAG = "UrgentBackFragment";
    Unbinder unbinder;
    @BindView(R.id.urgent_back_tab_layout)
    TabLayout urgentBackTabLayout;
    @BindView(R.id.urgent_back_view_pager)
    NoSrcollViewPage urgentBackViewPager;

    private List<Fragment> fragments;
    private ShortPagerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_urgent_back, container, false);
        unbinder = ButterKnife.bind(this, view);
        fragments = new ArrayList<>();
        fragments.add(new UrgentBackGunsFragment());
        fragments.add(new UrgentBackAmmosFragment());

        urgentBackTabLayout.setupWithViewPager(urgentBackViewPager);
        adapter = new ShortPagerAdapter(getChildFragmentManager());
        urgentBackViewPager.setAdapter(adapter);
        return view;
    }

    private class ShortPagerAdapter extends FragmentPagerAdapter {
        public String[] mTilte;

        public ShortPagerAdapter(FragmentManager fm) {
            super(fm);
            mTilte = new String[]{"归还枪支", "归还弹药"};
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
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}

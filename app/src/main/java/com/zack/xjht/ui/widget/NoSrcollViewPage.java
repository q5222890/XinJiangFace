package com.zack.xjht.ui.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NoSrcollViewPage extends ViewPager {

    private boolean isCanScroll =false;

    public NoSrcollViewPage(Context context) {
        super(context);
    }

    public NoSrcollViewPage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCanScroll(boolean canScroll) {
        isCanScroll = canScroll;
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(isCanScroll){
            return super.onTouchEvent(ev);
        }else{
            return false;
        }
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(isCanScroll){
            return super.onInterceptTouchEvent(ev);
        }else{
            return false;
        }
    }
}

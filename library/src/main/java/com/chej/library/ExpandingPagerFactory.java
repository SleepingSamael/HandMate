package com.chej.library;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.chej.library.fragments.ExpandingFragment;


public class ExpandingPagerFactory {


    public static ExpandingFragment getCurrentFragment(ViewPager viewPager){
        if (viewPager.getAdapter() instanceof ExpandingViewPagerAdapter) {
            ExpandingViewPagerAdapter adapter = (ExpandingViewPagerAdapter) ((ExpandingViewPagerAdapter) viewPager.getAdapter());
            Fragment fragment = adapter.getCurrentFragment();
            if (fragment instanceof ExpandingFragment) {
                return (ExpandingFragment)fragment;
            }
        }
        return null;
    }
//主界面布局
    public static void setupViewPager(final ViewPager viewPager) {
        ViewGroup.LayoutParams layoutParams = viewPager.getLayoutParams();
        layoutParams.width = ((Activity) viewPager.getContext()).getWindowManager().getDefaultDisplay().getWidth() / 4;
       // layoutParams.width = ((Activity) viewPager.getContext()).getWindowManager().getDefaultDisplay().getWidth() / 7 * 5;
        layoutParams.height = (int) ((layoutParams.width / 0.65));


        viewPager.setOffscreenPageLimit(5);


        if (viewPager.getParent() instanceof ViewGroup) {
            ViewGroup viewParent = ((ViewGroup) viewPager.getParent());
            viewParent.setClipChildren(false);
            viewPager.setClipChildren(false);
        }

        viewPager.setPageTransformer(true, new ExpandingViewPagerTransformer());

    }
//游戏模式等的布局
    public static void setupViewPager2(final ViewPager viewPager) {
        ViewGroup.LayoutParams layoutParams = viewPager.getLayoutParams();
        layoutParams.width = ((Activity) viewPager.getContext()).getWindowManager().getDefaultDisplay().getWidth() / 15* 3;
        layoutParams.height = (int) ((layoutParams.width / 1.2));//0.75

        //设置预加载数量
        viewPager.setOffscreenPageLimit(5);
        //设置每页之间的左右间隔
        viewPager.setPageMargin(1);

        if (viewPager.getParent() instanceof ViewGroup) {
            ViewGroup viewParent = ((ViewGroup) viewPager.getParent());
            viewParent.setClipChildren(false);
            viewPager.setClipChildren(false);
        }

        viewPager.setPageTransformer(true, new ExpandingViewPagerTransformer());

    }

    public static boolean onBackPressed(ViewPager viewPager) {
        ExpandingFragment expandingFragment = getCurrentFragment(viewPager);
        return false;
    }
}

package com.qslll.expandingpager.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.qslll.expandingpager.Model.GalleryItems;
import com.qslll.expandingpager.fragments.GalleryExpandingFragment;
import com.qslll.library.ExpandingViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class GalleryViewPagerAdapter extends ExpandingViewPagerAdapter {

    List<GalleryItems> galleryItemses;

    public GalleryViewPagerAdapter(FragmentManager fm) {
        super(fm);
        galleryItemses = new ArrayList<>();
    }

    public void addAll(List<GalleryItems> galleryItemses){
        this.galleryItemses.addAll(galleryItemses);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        GalleryItems galleryItems = galleryItemses.get(position);
        return GalleryExpandingFragment.newInstance(galleryItems);
    }

    @Override
    public int getCount() {
        return galleryItemses.size();
    }

}

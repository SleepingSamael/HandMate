package com.chej.HandMate.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.chej.HandMate.Model.GalleryItems;
import com.chej.HandMate.fragments.GalleryExpandingFragment;
import com.chej.library.ExpandingViewPagerAdapter;

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

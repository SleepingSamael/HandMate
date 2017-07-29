package com.qslll.expandingpager.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.qslll.expandingpager.Model.GalleryItems;
import com.qslll.library.fragments.ExpandingFragment;

/**
 * this is control fragment , Top and Bottom is child in it.
 *
 * Created by florentchampigny on 21/06/2016.
 */
public class GalleryExpandingFragment extends ExpandingFragment {

    static final String ARG_TRAVEL = "ARG_TRAVEL";
    GalleryItems galleryItems;

    public static GalleryExpandingFragment newInstance(GalleryItems galleryItems){
        GalleryExpandingFragment fragment = new GalleryExpandingFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TRAVEL, galleryItems);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args != null) {
            galleryItems = args.getParcelable(ARG_TRAVEL);
        }
    }

    /**
     * include TopFragment
     * @return
     */
    @Override
    public Fragment getFragmentTop() {
        return FragmentTop.newInstance(galleryItems);
    }

    /**
     * include BottomFragment
     * @return
     */
    @Override
    public Fragment getFragmentBottom() {
        return FragmentBottom.newInstance();
    }
}

package com.chej.HandMate.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chej.HandMate.Model.GalleryItems;
import com.chej.HandMate.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FragmentTop extends Fragment {

    static final String ARG_TRAVEL = "ARG_TRAVEL";
    GalleryItems galleryItems;

    @Bind(R.id.image) ImageView image;
    @Bind(R.id.title) TextView title;

    public static FragmentTop newInstance(GalleryItems galleryItems) {
        Bundle args = new Bundle();
        FragmentTop fragmentTop = new FragmentTop();
        args.putParcelable(ARG_TRAVEL, galleryItems);
        fragmentTop.setArguments(args);
        return fragmentTop;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            galleryItems = args.getParcelable(ARG_TRAVEL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_front, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (galleryItems != null) {
            image.setImageResource(galleryItems.getImage());
            title.setText(galleryItems.getName());
        }

    }

}

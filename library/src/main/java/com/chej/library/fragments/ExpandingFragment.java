package com.chej.library.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.chej.library.R;

public abstract class ExpandingFragment extends Fragment {

    Fragment fragmentFront;
    Fragment fragmentBottom;

    private CardView front;

    private float startY;

    float defaultCardElevation;
    private OnExpandingClickListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.expanding_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.fragmentFront = getFragmentTop();
        this.fragmentBottom = getFragmentBottom();

        if (fragmentFront != null && fragmentBottom != null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.front, fragmentFront)
                    .commit();
        }

        front = (CardView) view.findViewById(R.id.front);
        view.setOnClickListener(new OnClick());
        setupDownGesture(view);

        defaultCardElevation = front.getCardElevation();
    }

    private void setupDownGesture(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float my = 0;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        my = event.getY();
                        break;
                }
                return false;
            }
        });

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnExpandingClickListener) {
            mListener = (OnExpandingClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + "ExpandingFragment must implement OnExpandingClickListener");
        }
    }

    public abstract Fragment getFragmentTop();

    public abstract Fragment getFragmentBottom();





    class OnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
                if (mListener != null) {
                    mListener.onExpandingClick(v);
                }

        }
    }

    public interface OnExpandingClickListener {
        void onExpandingClick(View v);

        //点击按钮后，加载弹出式菜单
        void onClick(View v);
    }

    /**
     * Temporarily not used
     */
    interface Child {
        void onAttachedToExpanding(ExpandingFragment expandingFragment);

        void onDetachedToExpanding();
    }

    public interface ChildTop extends ExpandingFragment.Child {
    }

    public interface ChildBottom extends ExpandingFragment.Child {
    }

}

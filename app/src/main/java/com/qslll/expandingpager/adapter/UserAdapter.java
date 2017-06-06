package com.qslll.expandingpager.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.widget.SimpleCursorAdapter;

/**
 * Created by samael on 2017/4/1.
 */

public class UserAdapter extends SimpleCursorAdapter {
    private Cursor m_cursor;
    private Context m_context;
    private LayoutInflater miInflater;

    public UserAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        m_context = context;
        m_cursor = c;
    }
}

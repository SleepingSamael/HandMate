package com.qslll.expandingpager.timeline;

/**
 * Created by samael on 2017/3/28.
 */
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.qslll.expandingpager.R;
import com.qslll.expandingpager.model.history.HistoryConstant;

import java.util.ArrayList;
import java.util.List;


public class TimeLineAdapter extends BaseAdapter {

    private Context mContext;
    private List<ItemBean> datas = new ArrayList<ItemBean>();

    public TimeLineAdapter(Context mContext, List<ItemBean> datas) {
        super();
        this.mContext = mContext;
        this.datas = datas;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Item item = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.timeline_item, null);

            item = new Item();
            item.time = (TextView) convertView.findViewById(R.id.show_time);
            item.title = (TextView) convertView.findViewById(R.id.show_title);
            item.subtitle = (TextView) convertView.findViewById(R.id.show_subtitle);
            item.id = (TextView) convertView.findViewById(R.id.hide_id);
            item.lineNorma = convertView.findViewById(R.id.line_normal);
            item.lineHiLight = convertView.findViewById(R.id.line_highlight);
            item.lineHiLight2 =convertView.findViewById(R.id.line_highlight2);
            item.image = (ImageView) convertView.findViewById(R.id.image);

            convertView.setTag(item);
        } else {
            item = (Item) convertView.getTag();
        }


        //根据数据状态对视图做不同的操作
        if (datas.get(position).getStatu() == 1) {
            item.lineHiLight.setVisibility(View.VISIBLE);
            item.image.setImageResource(R.drawable.point1);
            item.time.setVisibility(View.VISIBLE);
        }

        item.time.setText(datas.get(position).getTime());
        item.title.setText(datas.get(position).getTitle());
        item.subtitle.setText(datas.get(position).getSubTitle());
        item.id.setText(datas.get(position).getID());

        //这里在起始位置，就不显示“轴”了
        if (position == 0) {
            item.lineNorma.setVisibility(View.INVISIBLE);
            item.lineHiLight.setVisibility(View.INVISIBLE);
        }
        //末尾不显示后轴

        if(position== getCount()-1)
        {
            item.lineHiLight2.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    final static class Item {
        TextView time, title,subtitle,id;
        View lineNorma, lineHiLight;
        View lineHiLight2;
        ImageView image;
    }
}

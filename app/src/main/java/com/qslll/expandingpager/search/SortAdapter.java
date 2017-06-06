package com.qslll.expandingpager.search;

/**
 * Created by samael on 2017/3/15.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;
import com.qslll.expandingpager.R;

import java.util.List;

public class SortAdapter extends BaseAdapter {

    private List<SortModel> list = null;

    private Context mContext;

    public SortAdapter(Context mContext,List<SortModel> list){
        this.mContext = mContext;
        this.list = list;
    }
    public void updateListView(List<SortModel> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

       // System.out.println("getView " + position + " " + convertView);
        ViewHolder holder = null;
        final SortModel mContent = list.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.user_list_item, null);
            holder = new ViewHolder();
            holder.name = (TextView)convertView.findViewById(R.id.tv_name);
            holder.id = (TextView)convertView.findViewById(R.id.tv_id);
            holder.date = (TextView)convertView.findViewById(R.id.tv_indata);
            holder.sex = (TextView)convertView.findViewById(R.id.tv_sex);
            holder.age = (TextView)convertView.findViewById(R.id.tv_age);

            final ViewHolder finalViewHolder = holder;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.name.setText(list.get(position).getName()+"");
        holder.id.setText(list.get(position).getId()+"");
        holder.date.setText(list.get(position).getDate()+" ");
        holder.sex.setText(list.get(position).getSex()+"");
        holder.age.setText(list.get(position).getAge()+"");
        return convertView;

    }



    final static class ViewHolder{
        TextView name;
        TextView id;
        TextView date;
        TextView sex;
        TextView age;
    }
    /**
     * 提取英文的首字母，非英文字母用#代替。
     *
     * @param str
     * @return
     */
    private String getAlpha(String str) {
        String  sortStr = str.trim().substring(0, 1).toUpperCase();
        // 正则表达式，判断首字母是否是英文字母
        if (sortStr.matches("[A-Z]")) {
            return sortStr;
        } else {
            return "#";
        }
    }


}

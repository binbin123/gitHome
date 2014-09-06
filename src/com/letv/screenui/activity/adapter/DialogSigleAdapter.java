package com.letv.screenui.activity.adapter;

import com.letv.smartControl.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DialogSigleAdapter extends BaseAdapter
{
    String itmes[] = null;
    
    LayoutInflater inflater;
    
    int position;
    
    public DialogSigleAdapter(Context context, String items[], int position)
    {
        // TODO Auto-generated constructor stub
        inflater = LayoutInflater.from(context);
        if (items != null)
        {
            this.itmes = items;
        }
        this.position = position;
    }
    
    @Override
    public int getCount()
    {
        // TODO Auto-generated method stub
        return itmes.length;
    }
    
    @Override
    public Object getItem(int position)
    {
        // TODO Auto-generated method stub
        return itmes[position];
    }
    
    @Override
    public long getItemId(int position)
    {
        // TODO Auto-generated method stub
        return position;
    }
    
    @Override
    public View getView(int position, View contentView, ViewGroup parentView)
    {
        // TODO Auto-generated method stub
        ViewHolder holder = null;
        if (contentView == null)
        {
            holder = new ViewHolder();
            contentView = inflater.inflate(R.layout.sigleadapteritem, null);
            holder.title = (TextView)contentView.findViewById(R.id.item_yh_name);
            holder.item_img = (ImageView)contentView.findViewById(R.id.item_yh_img);
            
            contentView.setTag(holder);
        }
        else
            holder = (ViewHolder)contentView.getTag();
        holder.title.setText(this.itmes[position]);
        if (this.position == position)
        {
            holder.item_img.setVisibility(View.VISIBLE);
            // contentView.setSelected(true);
        }
        else
        {
            holder.item_img.setVisibility(View.INVISIBLE);
            // contentView.setSelected(false);
        }
        
        return contentView;
    }
    
    public class ViewHolder
    {
        public TextView title = null;
        
        public ImageView item_img = null;
    }
    
}

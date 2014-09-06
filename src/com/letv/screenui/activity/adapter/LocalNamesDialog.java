package com.letv.screenui.activity.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.ListView;


import com.letv.smartControl.R;

public class LocalNamesDialog extends AlertDialog
{
    
    private DialogSigleAdapter adapter;
    
    protected OnListItemClick mOnClickListener;
    
    private String[] poiItems;
    
    
    private ImageView scrollbar_img;
    
    public LocalNamesDialog(Context context)
    {
        this(context, R.style.dialog);
    }
    
    public LocalNamesDialog(Context context, int theme)
    {
        super(context, theme);
    }
    
    public LocalNamesDialog(Context context, String[] poiItems, int position)
    {
        this(context);
        setTitle(R.string.dialog_title);
        this.poiItems = poiItems;
        adapter = new DialogSigleAdapter(context, poiItems, position);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_name_dialog);
        ListView listView = (ListView)findViewById(R.id.names_yh_list);
        listView.setAdapter(adapter);
        listView.setSelector(R.drawable.focus_highlight);
        scrollbar_img = (ImageView)findViewById(R.id.scrollbar_img);
        final int heght = getContext().getResources().getDimensionPixelSize(R.dimen.textsize_bigddder);
        listView.setOnItemClickListener(new OnItemClickListener()
        {
            
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                
                mOnClickListener.onListItemClick(LocalNamesDialog.this, poiItems[position], position);
                dismiss();
            }
        });
        listView.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            
            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int arg2, long arg3)
            {
                // TODO Auto-generated method stub
                // Log.e("localName", arg2 + "******");
             //   Toast.makeText(getContext(), "", 3).show();
                scrollbar_img.setY((view.getY() + heght) / 10 * arg2);
                // view.setBackgroundResource(R.drawable.focus_highlight);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                // TODO Auto-generated method stub
            }
        });
    }
    
    public interface OnListItemClick
    {
        public void onListItemClick(LocalNamesDialog dialog, String item, int position);
    }
    
    public void setOnListClickListener(OnListItemClick l)
    {
        mOnClickListener = l;
    }
    
}

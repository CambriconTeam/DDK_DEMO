package com.cambricon.productdisplay.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.task.OnItemClickListener;

/**
 * Created by dell on 18-3-15.
 */

public class MoreFunctionRecylerAdaper extends RecyclerView.Adapter<MoreFunctionRecylerAdaper.MyViewHolder> implements OnItemClickListener {
    private LayoutInflater inflater;
    private Context mContext;
    private String[] mDatas, mSummary;
    private Integer[] mdraw;
    private OnItemClickListener mItemClickListener;

    private int[] iconBack = {
            R.color.more_bgcolor_color1,R.color.more_bgcolor_color3,R.color.more_bgcolor_color2,
            R.color.more_bgcolor_color5,R.color.more_bgcolor_color4,R.color.more_bgcolor_color5,
            R.color.more_bgcolor_color5,R.color.more_bgcolor_color1,R.color.more_bgcolor_color3,
            R.color.more_bgcolor_color2,R.color.more_bgcolor_color5,R.color.more_bgcolor_color4,
            R.color.more_bgcolor_color3,R.color.more_bgcolor_color3,R.color.more_bgcolor_color5,
            R.color.more_bgcolor_color2
    };

    public MoreFunctionRecylerAdaper(Context context, String[] datas, String[] summary, Integer[] mdraw) {
        this.mContext = context;
        this.mDatas = datas;
        this.mdraw = mdraw;
        this.mSummary = summary;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.more_functions_item, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view,mItemClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.tv.setText(mDatas[position]);
        holder.test.setText(mSummary[position]);
        holder.imageView.setImageResource(mdraw[position]);
//        holder.iconBack.setBackgroundColor(iconBack[position]);
        holder.iconBack.setBackgroundResource(iconBack[position]);
    }

    @Override
    public int getItemCount() {
        return mDatas.length;
    }

    @Override
    public void onItemClick(View view) {

    }

    @Override
    public void onItemLongClick(View view) {

    }
    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tv;
        TextView test;
        ImageView imageView;
        LinearLayout iconBack;

        private OnItemClickListener mItemClickListener;




        public MyViewHolder(View itemView,OnItemClickListener listener) {
            super(itemView);
            tv = itemView.findViewById(R.id.recycle_tv);
            test = itemView.findViewById(R.id.recycle_test);
            imageView = itemView.findViewById(R.id.header_img);
            iconBack = itemView.findViewById(R.id.iconback);
            this.mItemClickListener=listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(mItemClickListener!=null){
                mItemClickListener.onItemClick(view,getPosition());
            }
        }

    }

    /**
     * item click listener interface
     */
    public interface OnItemClickListener {
        void onItemClick(View view,int position);
    }

    public void setmItemClickListener(OnItemClickListener listener){
        this.mItemClickListener=listener;
    }
}


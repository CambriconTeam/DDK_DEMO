package com.cambricon.productdisplay.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.bean.SpeechItem;

import java.util.List;

/**
 * Created by dell on 2018/2/7.
 */

public class SemanticsAdapter extends RecyclerView.Adapter<SemanticsAdapter.ViewHolder>{

    private List<SpeechItem> mItemList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView resourse;
        TextView translate;
        ImageView horn;
        TextView voiceUrl;
        public ViewHolder(View itemView) {
            super(itemView);
            resourse = itemView.findViewById(R.id.resourse);
            translate = itemView.findViewById(R.id.translate);
            horn = itemView.findViewById(R.id.horn);
            voiceUrl = itemView.findViewById(R.id.voiceUrl);
        }

    }

    public SemanticsAdapter(List<SpeechItem> list){
        mItemList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.semantics_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SpeechItem item = mItemList.get(position);
        holder.resourse.setText(item.getResourse());
        holder.translate.setText(item.getTranslate());
        holder.horn.setImageResource(R.drawable.hornplay);
        holder.voiceUrl.setText(item.getVoiceUrl());
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }


}

package com.qiubangbang.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qiubangbang.getphoneinfo.PhoneInfo;
import com.qiubangbang.getphoneinfo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qiubangbang on 2017/2/17.
 */

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

    private List<PhoneInfo> list = new ArrayList<>();
    private Context mContext;
    private int mType;

    public Adapter(List<PhoneInfo> list, Context mContext, int type) {
        this.list = list;
        this.mContext = mContext;
        this.mType = type;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.item_phone, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if (mType == 1) {
            holder.tvUser.setText(list.get(position).getPhoneName());
            holder.tvPhone.setText(list.get(position).getPhoneNumber());
        } else {
            holder.tvUser.setText(list.get(position).getMessageContent());
            holder.tvPhone.setText(list.get(position).getPhoneNumber());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvUser;
        private final TextView tvPhone;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvUser = (TextView) itemView.findViewById(R.id.tv_user);
            tvPhone = (TextView) itemView.findViewById(R.id.tv_phone);
        }
    }

    public void setmType(int mType) {
        this.mType = mType;
    }
}

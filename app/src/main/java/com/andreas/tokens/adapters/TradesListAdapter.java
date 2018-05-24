package com.andreas.tokens.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andreas.tokens.R;
import com.andreas.tokens.models.Trade;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TradesListAdapter extends RecyclerView.Adapter<TradesListAdapter.ViewHolder> {
    private ArrayList<Trade> mTradesList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTypeTextView;
        private TextView mDateTextView;
        private TextView mPriceTextView;
        private TextView mAmountTextView;
        private ImageView mTradeImageView;
        private LinearLayout mItemContainer;

        public ViewHolder(View v) {
            super(v);
            mTypeTextView = v.findViewById(R.id.tradeType);
            mDateTextView = v.findViewById(R.id.tradeDate);
            mPriceTextView = v.findViewById(R.id.tradePrice);
            mAmountTextView = v.findViewById(R.id.tradeAmount);
            mTradeImageView = v.findViewById(R.id.tradeImage);
            mItemContainer = v.findViewById(R.id.itemContainer);
        }
    }

    public TradesListAdapter(ArrayList<Trade> tradesList) {
        this.mTradesList = tradesList;
        Collections.reverse(mTradesList);
    }

    @Override
    public TradesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        long unixTimestamp = mTradesList.get(position).datetime;
        Date date = new Date(unixTimestamp * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        String formattedDate = sdf.format(date);

        holder.mDateTextView.setText(formattedDate);
        holder.mTypeTextView.setText(mTradesList.get(position).type);
        holder.mPriceTextView.setText(mTradesList.get(position).price+"$");
        holder.mAmountTextView.setText(mTradesList.get(position).amount);

        if (mTradesList.get(position).type.equals("buy")) {
            holder.mTradeImageView.setImageResource(R.drawable.ic_outline_arrow_upward_24px);
        }

        if(position % 2 == 0) {
            holder.mItemContainer.setBackgroundColor(R.color.colorInvert);
        }

    }

    @Override
    public int getItemCount() {
        return mTradesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}

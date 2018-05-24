package com.andreas.tokens.activities;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.andreas.tokens.R;
import com.andreas.tokens.adapters.TradesListAdapter;
import com.andreas.tokens.models.Trade;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListActivity extends Activity {

    private RecyclerView mTradesRecyclerView;
    private RecyclerView.Adapter mTradesAdapter;
    private Gson mGson;
    private ArrayList<Trade> mTrades;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mTrades = new ArrayList<>();
        mGson = new Gson();
        mTradesRecyclerView = findViewById(R.id.tradesRecyclerView);
        mSwipeRefreshLayout = findViewById(R.id.swipeContainer);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mTradesRecyclerView.setHasFixedSize(true);
        mTradesRecyclerView.setLayoutManager(mLayoutManager);

        getDataFromAPI();
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDataFromAPI();
            }
        });

    }

    private void getDataFromAPI() {

        if (isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();

            HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.tokens.net/public/trades/hour/btcusdt/").newBuilder();
            urlBuilder.addQueryParameter("key", "01234567-89ab-cdef-0123-456789abcdef");
            urlBuilder.addQueryParameter("signature", "01234567-89ab-cdef-0123-456789abcdef");
            urlBuilder.addQueryParameter("nonce", "0");

            Request request = new Request.Builder()
                    .url(urlBuilder.build())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(ListActivity.this, "Failure", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        String jsonResponse = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            JSONArray jsonArray = jsonObject.getJSONArray("trades");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject tradesJsonObject = jsonArray.getJSONObject(i);
                                Trade trade = mGson.fromJson(tradesJsonObject.toString(), Trade.class);
                                mTrades.add(trade);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTradesAdapter = new TradesListAdapter(mTrades);
                                    mTradesRecyclerView.setAdapter(mTradesAdapter);
                                    mTradesAdapter.notifyDataSetChanged();
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else {
            Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

package com.andreas.tokens.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andreas.tokens.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private LineChart mChart;
    private Gson mGson;
    private ImageView mListImageView;
    private TextView mCurrentPriceTextView;
    private ArrayList<Entry> mEntries;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LineDataSet mLineDataSet;
    private LineData mLineData;
    private SimpleDateFormat mSimpleDateFormat;

    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGson = new Gson();
        mEntries = new ArrayList<Entry>();

        mSwipeRefreshLayout = findViewById(R.id.mainswipe);
        mChart = findViewById(R.id.chart);
        mCurrentPriceTextView = findViewById(R.id.currentPrice);
        mListImageView = findViewById(R.id.listImageView);

        mListImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), ListActivity.class);
                startActivity(intent);
            }
        });

        mChart.getAxisLeft().setTextColor(Color.WHITE);
        mChart.getAxisLeft().setTextSize(12);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getAxisRight().setTextColor(Color.WHITE);
        mChart.getAxisRight().setTextSize(12);
        mChart.getAxisRight().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getXAxis().setTextSize(12);
        mChart.getXAxis().setTextColor(Color.WHITE);
        mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mChart.setDragEnabled(false);

        if (isNetworkAvailable()) {
            getCurrentPriceFromAPI();
            get7DaysPriceFromAPI();
        } else {
            Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isNetworkAvailable()) {
                    getCurrentPriceFromAPI();
                    get7DaysPriceFromAPI();
                } else {
                    Toast.makeText(MainActivity.this, "No internet connection.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void getCurrentPriceFromAPI() {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.coindesk.com/v1/bpi/currentprice.json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this, "Failure", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONObject jsonBpiObject = jsonObject.getJSONObject("bpi");
                        final JSONObject jsonUSDObject = jsonBpiObject.getJSONObject("USD");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mCurrentPriceTextView.setText(jsonUSDObject.getString("rate") + " $");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void get7DaysPriceFromAPI() {

        OkHttpClient client = new OkHttpClient();
        mEntries = new ArrayList<>();
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);

        calendar.setTime(currentDate);
        String dateTo = mSimpleDateFormat.format(currentDate);
        calendar.add(Calendar.DATE, -7);
        currentDate = calendar.getTime();
        String dateFrom = mSimpleDateFormat.format(currentDate);

        Request request = new Request.Builder()
                .url("https://api.coindesk.com/v1/bpi/historical/close.json?start=" + dateFrom + "&end=" + dateTo)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this, "Failure", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    String jsonResponse = response.body().string();
                    final ArrayList<String> keyArray = new ArrayList<>();

                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONObject jsonBpiObject = jsonObject.getJSONObject("bpi");
                        int counter = 0;

                        for (Iterator<String> iterator = jsonBpiObject.keys(); iterator.hasNext(); ) {
                            final String key = iterator.next();
                            try {
                                Object value = jsonBpiObject.get(key);
                                String val = value + "";
                                float yValue = Float.parseFloat(val);

                                mEntries.add(new Entry(counter, yValue));
                                counter++;

                                mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                Date newDate = mSimpleDateFormat.parse(key);
                                mSimpleDateFormat = new SimpleDateFormat("dd.MM");
                                final String formattedDate = mSimpleDateFormat.format(newDate);
                                keyArray.add(formattedDate);

                            } catch (JSONException | ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                mLineDataSet = new LineDataSet(mEntries, "");
                                mLineDataSet.setValueTextColor(Color.WHITE);
                                mLineDataSet.setValueTextSize(12);

                                mLineData = new LineData(mLineDataSet);
                                mChart.setData(mLineData);
                                mChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
                                    @Override
                                    public String getFormattedValue(float value, AxisBase axis) {
                                        return keyArray.get((int) value);
                                    }
                                });
                                mChart.notifyDataSetChanged();
                                mChart.invalidate();

                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}

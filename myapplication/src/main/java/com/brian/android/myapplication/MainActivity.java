package com.brian.android.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<AdapterData> dataList = new ArrayList<>();
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new BookLayoutManager());
        adapter = new Adapter();
        recyclerView.setAdapter(adapter);
        BookSnapHelper snapHelper = new BookSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        AdapterData data = new AdapterData();
        dataList.add(data);
    }

    private void addData(AdapterData data) {
        if (dataList.indexOf(data) == 0) {
            AdapterData newData = new AdapterData();
            newData.id = data.id - 1;
            dataList.add(0, newData);
            adapter.notifyItemInserted(dataList.indexOf(newData));
        }

        if (dataList.indexOf(data) == dataList.size() - 1) {
            AdapterData newData = new AdapterData();
            newData.id = data.id + 1;
            dataList.add(newData);
            adapter.notifyItemInserted(dataList.indexOf(newData));
        }
    }

    class AdapterData {
        int id;
    }

    class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
        class MyViewHolder extends RecyclerView.ViewHolder {
            MyViewHolder(View itemView) {
                super(itemView);
            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(new DataView(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final AdapterData data = dataList.get(position);
            DataView dataView = (DataView) holder.itemView;
            dataView.bindData(data);

            if (dataList.indexOf(data) == 0 || dataList.indexOf(data) == dataList.size() - 1) {
                dataView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addData(data);
                    }
                }, 500);
            }
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }
    }
}

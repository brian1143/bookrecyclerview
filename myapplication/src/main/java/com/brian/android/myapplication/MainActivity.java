package com.brian.android.myapplication;

import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<AdapterData> dataList = new ArrayList<>();
    private Adapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new BookLayoutManager());
        adapter = new Adapter();
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new BookPageDecoration(this));
        BookSnapHelper snapHelper = new BookSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        AdapterData data = new AdapterData();
        dataList.add(data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_go:
                recyclerView.scrollToPosition(4);
                return true;
            case R.id.action_scroll:
                BookLayoutManager layoutManager = (BookLayoutManager) recyclerView.getLayoutManager();
                layoutManager.retainPage(6, new Rect(0, 0, 100, 200), 1000);
                recyclerView.smoothScrollToPosition(5);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void prependData(AdapterData data) {
        if (dataList.indexOf(data) == 0) {
            AdapterData newData = new AdapterData();
            newData.id = data.id - 1;
            dataList.add(0, newData);
            adapter.notifyItemInserted(dataList.indexOf(newData));
        }
    }
    private void appendData(AdapterData data) {
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
        private static final int MAX_SIZE = 20;
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

            if (dataList.indexOf(data) == dataList.size() - 1) {
                if (dataList.size() < MAX_SIZE) {
                    dataView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            appendData(data);
                        }
                    }, 500);
                }
            }

            if (dataList.indexOf(data) == 0) {
                if (dataList.size() < MAX_SIZE) {
                    dataView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            prependData(data);
                        }
                    }, 500);
                }
            }
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }
    }
}

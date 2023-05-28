package com.wanjian.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by wanjian on 2018/1/22.
 */
public class SecondAct extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        setAdapter(recyclerView);
    }

    private void setAdapter(final RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.divider));
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(new RecyclerView.Adapter() {

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item, recyclerView, false);
                view.setTag(R.id.txt, view.findViewById(R.id.txt));
                return new RecyclerView.ViewHolder(view) {
                };
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
                ((TextView) holder.itemView.getTag(R.id.txt)).setText(String.valueOf(position));
                holder.itemView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Toast.makeText(SecondAct.this, "POSITION " + position, Toast.LENGTH_SHORT).show();
                    }
                });
                if (position == 20) {
                    throw new RuntimeException("RecyclerView 设置数据出错 POSITION: " + position);
                }
            }

            @Override
            public int getItemCount() {
                return 100;
            }
        });
    }
}

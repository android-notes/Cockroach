package com.wanjian.demo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wanjian on 2018/5/19.
 */

public class LifecycleExceptionActivity extends Activity {

    public static final String METHOD = "method";
    private String exceptionPoint;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lifecycle_exception);

        init();

        exceptionPoint = getIntent().getStringExtra(METHOD);
        if ("onCreate".equals(exceptionPoint)) {
            throw new RuntimeException("生命周期抛出异常");
        }
    }

    private void init() {
        final TextView textView = findViewById(R.id.tv);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                textView.setText(time.format(new Date()));

                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if ("onStart".equals(exceptionPoint)) {
            throw new RuntimeException("生命周期抛出异常");
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if ("onRestart".equals(exceptionPoint)) {
            throw new RuntimeException("生命周期抛出异常");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ("onResume".equals(exceptionPoint)) {
            throw new RuntimeException("生命周期抛出异常");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if ("onPause".equals(exceptionPoint)) {
            throw new RuntimeException("生命周期抛出异常");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if ("onStop".equals(exceptionPoint)) {
            throw new RuntimeException("生命周期抛出异常");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ("onDestroy".equals(exceptionPoint)) {
            throw new RuntimeException("生命周期抛出异常");
        }
    }

    @Override
    public void finish() {
        super.finish();

        System.out.println("finish act.........");
        if ("finish".equals(exceptionPoint)) {
            throw new RuntimeException("生命周期抛出异常");
        }
    }
}

package com.wanjian.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.wanjian.cockroach.Cockroach;


/**
 * Created by wanjian on 2018/1/22.
 */

public class MainAct extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        findViewById(R.id.click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                throw new RuntimeException("点击异常");
            }
        });
        findViewById(R.id.thread).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        throw new RuntimeException("子线程异常");
                    }
                }.start();
            }
        });
        findViewById(R.id.handler).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        throw new RuntimeException("handler异常");
                    }
                });
            }
        });

        findViewById(R.id.act).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainAct.this, SecondAct.class));
            }
        });

        findViewById(R.id.noact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainAct.this, UnknowAct.class));
            }
        });
        ////////黑屏测试//////////
        findViewById(R.id.newActOnCreate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainAct.this, LifecycleExceptionActivity.class);
                intent.putExtra(LifecycleExceptionActivity.METHOD, "onCreate");
                startActivity(intent);
            }
        });
        findViewById(R.id.newActOnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainAct.this, LifecycleExceptionActivity.class);
                intent.putExtra(LifecycleExceptionActivity.METHOD, "onStart");
                startActivity(intent);
            }
        });
        findViewById(R.id.newActOnReStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainAct.this, LifecycleExceptionActivity.class);
                intent.putExtra(LifecycleExceptionActivity.METHOD, "onRestart");
                startActivity(intent);
            }
        });
        findViewById(R.id.newActOnResume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainAct.this, LifecycleExceptionActivity.class);
                intent.putExtra(LifecycleExceptionActivity.METHOD, "onResume");
                startActivity(intent);
            }
        });
        findViewById(R.id.newActOnPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainAct.this, LifecycleExceptionActivity.class);
                intent.putExtra(LifecycleExceptionActivity.METHOD, "onPause");
                startActivity(intent);
            }
        });
        findViewById(R.id.newActOnStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainAct.this, LifecycleExceptionActivity.class);
                intent.putExtra(LifecycleExceptionActivity.METHOD, "onStop");
                startActivity(intent);
            }
        });
        findViewById(R.id.newActonDestroy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainAct.this, LifecycleExceptionActivity.class);
                intent.putExtra(LifecycleExceptionActivity.METHOD, "onDestroy");
                startActivity(intent);
            }
        });
        findViewById(R.id.newActFinish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainAct.this, LifecycleExceptionActivity.class);
                intent.putExtra(LifecycleExceptionActivity.METHOD, "finish");
                startActivity(intent);
            }
        });


    }

}

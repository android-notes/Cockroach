package com.wanjian.demo.support;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.wanjian.demo.R;


/**
 * Created by wanjian on 2018/5/21.
 */

public class DebugSafeModeTipActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_mode_warning);
    }


}

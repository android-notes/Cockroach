package com.wanjian.demo.support;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.wanjian.cockroach.Cockroach;
import com.wanjian.demo.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wanjian on 2018/5/21.
 * 进入安全模式后给所有act添加一个渐变的绿色顶栏
 */

public class DebugSafeModeUI {

    private static int barHeight;
    private static List<WeakReference<Activity>> sActivitysWRef = new ArrayList<>();

    public static void init(Application application) {
        application.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacksAdapter() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                super.onActivityCreated(activity, savedInstanceState);
                sActivitysWRef.add(new WeakReference<>(activity));
                if (Cockroach.isSafeMode()) {
                    //进入安全模式后给新创建的act添加渐变绿色顶栏
                    enterSafeMode(activity);
                }
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                super.onActivityDestroyed(activity);
                for (WeakReference<Activity> reference : sActivitysWRef) {
                    Activity act = reference.get();
                    if (act == activity) {
                        sActivitysWRef.remove(reference);
                        return;
                    }
                }
            }
        });
        barHeight = (int) (application.getResources().getDisplayMetrics().density * 50);
    }

    /**
     * 进入安全模式后给当前已存在的act添加渐变绿色顶栏
     */
    public static void showSafeModeUI() {
        for (WeakReference<Activity> reference : sActivitysWRef) {
            Activity activity = reference.get();
            if (activity == null || activity.isFinishing()) {
                continue;
            }
            enterSafeMode(activity);
        }
    }

    public static void enterSafeMode(Activity activity) {
        try {
            View view = new View(activity);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, barHeight);
            view.setLayoutParams(params);
            view.setBackgroundResource(R.drawable.safe_mode_drawable);
            ((ViewGroup) activity.getWindow().getDecorView()).addView(view);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}

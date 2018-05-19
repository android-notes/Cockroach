package com.wanjian.cockroach;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wanjian on 2017/2/14.
 */

public final class Cockroach {

    public interface ExceptionHandler {

        void handlerException(Thread thread, Throwable throwable);
    }

    private Cockroach() {
    }

    private static ExceptionHandler sExceptionHandler;
    private static boolean sInstalled = false;//标记位，避免重复安装卸载
    private static List<WeakReference<Activity>> sActivitysWRef = new ArrayList<>();

    /**
     * 当主线程或子线程抛出异常时会调用exceptionHandler.handlerException(Thread thread, Throwable throwable)
     * <p>
     * exceptionHandler.handlerException可能运行在非UI线程中。
     * <p>
     * 若设置了Thread.setDefaultUncaughtExceptionHandler则可能无法捕获子线程异常。
     *
     * @param exceptionHandler
     */

    public static void install(Application context, ExceptionHandler exceptionHandler) {
        if (sInstalled) {
            return;
        }
        sInstalled = true;
        sExceptionHandler = exceptionHandler;

        if (canObserverActivity()) {
            context.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacksAdapter() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    super.onActivityCreated(activity, savedInstanceState);
                    sActivitysWRef.add(new WeakReference<>(activity));
                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                    super.onActivityDestroyed(activity);
                    for (WeakReference<Activity> reference : sActivitysWRef) {
                        Activity act = reference.get();
                        if (act != null && act == activity) {
                            sActivitysWRef.remove(reference);
                            return;
                        }
                    }
                }
            });
        }

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (sExceptionHandler != null) {
                    sExceptionHandler.handlerException(t, e);
                }
                if (t == Looper.getMainLooper().getThread()) {
                    finishExceptionActivityIfNeeded(e);
                    safeMode();
                }
            }
        });

    }

    private static boolean canObserverActivity() {
        return Build.VERSION.SDK_INT >= 14;
    }

    private static void finishExceptionActivityIfNeeded(Throwable e) {
        if (canObserverActivity() == false) {
            return;
        }

        if (e == null) {
            return;
        }
        Throwable cause = e.getCause();
        if (cause == null) {
            return;
        }
        StackTraceElement[] elements = cause.getStackTrace();
        for (int i = 0; i < elements.length; i++) {
            StackTraceElement element = elements[i];
            if ("android.app.Activity".equals(element.getClassName())
                    && "Activity.java".equals(element.getFileName())) {
                String method = element.getMethodName();
                String targetMethod = null;
                switch (method) {
                    case "performCreate":
                        targetMethod = "onCreate";
                        break;
                    case "performStart":
                        targetMethod = "onStart";
                        break;
                    case "performRestart":
                        targetMethod = "onRestart";
                        break;
                    case "performResume":
                        targetMethod = "onResume";
                        break;
                }
                if (targetMethod == null) {
                    continue;
                }

                Activity activity = findActivity(elements, i, targetMethod);

                if (activity != null) {
                    try {
                        activity.finish();
                    } catch (Throwable ex) {
                    }
                }
                return;
            }
        }


    }

    private static Activity findActivity(StackTraceElement[] es, int size, String targetMethod) {
        for (int i = 0; i < size; i++) {
            StackTraceElement element = es[i];
            if (targetMethod.equals(element.getMethodName())) {
                for (WeakReference<Activity> reference : sActivitysWRef) {
                    Activity activity = reference.get();
                    if (activity != null && activity.getClass().getName().equals(element.getClassName())) {
                        sActivitysWRef.remove(reference);
                        return activity;
                    }
                }
                return null;
            }
        }
        return null;
    }

    private static void safeMode() {
        while (true) {
            try {
                Looper.loop();
            } catch (Throwable e) {
                finishExceptionActivityIfNeeded(e);
                if (sExceptionHandler != null) {
                    sExceptionHandler.handlerException(Looper.getMainLooper().getThread(), e);
                }
            }
        }
    }


}

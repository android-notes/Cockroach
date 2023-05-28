package com.wanjian.cockroach.compat;

import android.app.Activity;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by wanjian on 2018/5/24.
 * <p>
 */
public class ActivityKillerV21_V23 implements IActivityKiller {

    @Override
    public void finishLaunchActivity(Message message) {
        try {
            Object activityClientRecord = message.obj;
            Field tokenField = activityClientRecord.getClass().getDeclaredField("token");
            tokenField.setAccessible(true);
            IBinder binder = (IBinder) tokenField.get(activityClientRecord);
            finish(binder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finishResumeActivity(Message message) {
        finishIBinderFromMessage(message);
    }

    @Override
    public void finishPauseActivity(Message message) {
        finishIBinderFromMessage(message);
    }

    @Override
    public void finishStopActivity(Message message) {
        finishIBinderFromMessage(message);
    }

    private void finish(IBinder binder) throws Exception {
        Class activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
        Method getDefaultMethod = activityManagerNativeClass.getDeclaredMethod("getDefault");
        Object activityManager = getDefaultMethod.invoke(null);
        Method finishActivityMethod = activityManager.getClass().getDeclaredMethod("finishActivity", IBinder.class, int.class, Intent.class, boolean.class);
        finishActivityMethod.invoke(activityManager, binder, Activity.RESULT_CANCELED, null, false);
    }

    private void finishIBinderFromMessage(Message message) {
        try {
            finish((IBinder) message.obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

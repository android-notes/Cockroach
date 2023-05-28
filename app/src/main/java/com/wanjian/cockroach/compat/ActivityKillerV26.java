package com.wanjian.cockroach.compat;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by wanjian on 2018/5/24.
 * <p>
 * <p>
 * handleDestroyActivity((IBinder)msg.obj, msg.arg1 != 0,msg.arg2, false);
 * ActivityManager.getService().finishActivity(mToken, resultCode, resultData, finishTask)
 */
public class ActivityKillerV26 implements IActivityKiller {

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
        finishSomeArgs(message);
    }

    @Override
    public void finishPauseActivity(Message message) {
        finishSomeArgs(message);
    }

    @Override
    public void finishStopActivity(Message message) {
        finishSomeArgs(message);
    }

    private void finishSomeArgs(Message message) {
        try {
            Object someArgs = message.obj;
            Field arg1Field = someArgs.getClass().getDeclaredField("arg1");
            arg1Field.setAccessible(true);
            IBinder binder = (IBinder) arg1Field.get(someArgs);
            finish(binder);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private void finish(IBinder binder) throws Exception {
        Method getServiceMethod = ActivityManager.class.getDeclaredMethod("getService");
        Object activityManager = getServiceMethod.invoke(null);
        Method finishActivityMethod = activityManager.getClass().getDeclaredMethod("finishActivity", IBinder.class, int.class, Intent.class, int.class);
        finishActivityMethod.setAccessible(true);
        int DONT_FINISH_TASK_WITH_ACTIVITY = 0;
        finishActivityMethod.invoke(activityManager, binder, Activity.RESULT_CANCELED, null, DONT_FINISH_TASK_WITH_ACTIVITY);
    }
}

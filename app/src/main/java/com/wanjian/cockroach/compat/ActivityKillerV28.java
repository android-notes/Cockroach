package com.wanjian.cockroach.compat;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.servertransaction.ClientTransaction;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class ActivityKillerV28 implements IActivityKiller {


    @Override
    public void finishLaunchActivity(Message message) {

        try {
            tryFinish1(message);
            return;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        try {
            tryFinish2(message);
            return;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        try {
            tryFinish3(message);
            return;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    private void tryFinish1(Message message) throws Throwable {
        ClientTransaction clientTransaction = (ClientTransaction) message.obj;
        IBinder binder = clientTransaction.getActivityToken();
        finish(binder);
    }

    private void tryFinish3(Message message) throws Throwable {
        Object clientTransaction = message.obj;
        Field mActivityTokenField = clientTransaction.getClass().getDeclaredField("mActivityToken");
        IBinder binder = (IBinder) mActivityTokenField.get(clientTransaction);
        finish(binder);
    }

    private void tryFinish2(Message message) throws Throwable {
        Object clientTransaction = message.obj;
        Method getActivityTokenMethod = clientTransaction.getClass().getDeclaredMethod("getActivityToken");
        IBinder binder = (IBinder) getActivityTokenMethod.invoke(clientTransaction);
        finish(binder);
    }


    @Override
    public void finishResumeActivity(Message message) {

    }


    @Override
    public void finishPauseActivity(Message message) {

    }

    @Override
    public void finishStopActivity(Message message) {
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

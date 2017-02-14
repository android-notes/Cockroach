package com.wanjian.cockroach;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by wanjian on 2017/2/14.
 */

public final class Cockroach {

    interface ExceptionHandler {

        void handlerException(Thread thread, Throwable throwable);
    }

    private Cockroach() {
    }

    private static ExceptionHandler mExceptionHandler;

    /**
     * 当主线程或子线程抛出异常时会调用exceptionHandler.handlerException(Thread thread, Throwable throwable)
     * <p>
     * exceptionHandler.handlerException可能运行在非UI线程中。
     * <p>
     * handlerException内部建议手动try{  你的异常处理逻辑  }catch(Throwable e){ } ，以防handlerException内部再次抛出异常，导致循环调用handlerException
     * <p>
     * 若设置了Thread.setDefaultUncaughtExceptionHandler则可能无法捕获子线程异常。
     *
     * @param exceptionHandler
     */
    public static void install(ExceptionHandler exceptionHandler) {
        mExceptionHandler = exceptionHandler;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Looper.loop();
                    } catch (Throwable e) {
                        if (mExceptionHandler != null) {
                            mExceptionHandler.handlerException(Looper.getMainLooper().getThread(), e);
                        }
                    }
                }
            }
        });

//        final Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (mExceptionHandler != null) {
                    mExceptionHandler.handlerException(t, e);
                }
            }
        });

    }
}

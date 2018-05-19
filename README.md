 

## Cockroach 2.0

### Cockroach 2.0新特性
* Cockroach 2.0减少了Cockroach 1.0版本中Activity生命周期中抛出异常黑屏的问题。
* Cockroach 1.0未雨绸缪，提前做好准备，等待异常到来。Cockroach 2.0马后炮，只有当抛出异常时才去拯救。
* Cockroach 2.0试图在APP即将崩溃时尽量去挽救，不至于情况更糟糕。


用一张图片来形容就是

![img](https://github.com/android-notes/Cockroach/blob/X/wanjiu.jpeg?raw=true)


## 使用姿势

* 必须要在Application初始化时装载

例如：

```java
  
    package com.wanjian.demo;
    
    import android.app.Application;
    import android.os.Handler;
    import android.os.Looper;
    import android.util.Log;
    import android.widget.Toast;
    
    import com.wanjian.cockroach.Cockroach;
    
    /**
     * Created by wanjian on 2018/5/19.
     */
    
    public class App extends Application {
    
        @Override
        public void onCreate() {
            super.onCreate();
            install();
        }
    
    
        private void install() {
            final Toast toast = Toast.makeText(this, "", 1);
            Cockroach.install(this, new Cockroach.ExceptionHandler() {
    
                // handlerException内部建议手动try{  你的异常处理逻辑  }catch(Throwable e){ } ，以防handlerException内部再次抛出异常，导致循环调用handlerException
    
                @Override
                public void handlerException(final Thread thread, final Throwable throwable) {
                    //由于handlerException可能运行在非ui线程中，Toast又需要在主线程，所以new了一个new Handler(Looper.getMainLooper())，
                    //所以千万不要在下面的run方法中执行耗时操作，因为run已经运行在了ui线程中。
                    //new Handler(Looper.getMainLooper())只是为了能弹出个toast，并无其他用途
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //建议使用下面方式在控制台打印异常，这样就可以在Error级别看到红色log
                                Log.e("AndroidRuntime", "--->CockroachException:" + thread + "<---", throwable);
                                toast.setText("Cockroach Worked");
                                toast.show();
    //                        throw new RuntimeException("..."+(i++));
                            } catch (Throwable e) {
    
                            }
                        }
                    });
                }
            });
    
        }
    }
    

```


### 测试

装载Cockroach，在新开启的Activity的生命周期中抛出异常和view点击时抛出异常

```java

 E/AndroidRuntime: --->CockroachException:Thread[main,5,main]<---
    java.lang.RuntimeException: Unable to start activity ComponentInfo{com.wanjian.demo/com.wanjian.demo.LifecycleExceptionActivity}: java.lang.RuntimeException: 生命周期抛出异常
        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2817)
        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2892)
        at android.app.ActivityThread.-wrap11(Unknown Source:0)
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1593)
        at android.os.Handler.dispatchMessage(Handler.java:105)
        at android.os.Looper.loop(Looper.java:164)
        at com.wanjian.cockroach.Cockroach.safeMode(Cockroach.java:161)
        at com.wanjian.cockroach.Cockroach.access$300(Cockroach.java:17)
        at com.wanjian.cockroach.Cockroach$2.uncaughtException(Cockroach.java:78)
        at java.lang.ThreadGroup.uncaughtException(ThreadGroup.java:1068)
        at java.lang.ThreadGroup.uncaughtException(ThreadGroup.java:1063)
        at java.lang.Thread.dispatchUncaughtException(Thread.java:1953)
     Caused by: java.lang.RuntimeException: 生命周期抛出异常
        at com.wanjian.demo.LifecycleExceptionActivity.onCreate(LifecycleExceptionActivity.java:30)
        at android.app.Activity.performCreate(Activity.java:6975)
        at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1213)
        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2770)
        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2892) 
        at android.app.ActivityThread.-wrap11(Unknown Source:0) 
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1593) 
        at android.os.Handler.dispatchMessage(Handler.java:105) 
        at android.os.Looper.loop(Looper.java:164) 
        at com.wanjian.cockroach.Cockroach.safeMode(Cockroach.java:161) 
        at com.wanjian.cockroach.Cockroach.access$300(Cockroach.java:17) 
        at com.wanjian.cockroach.Cockroach$2.uncaughtException(Cockroach.java:78) 
        at java.lang.ThreadGroup.uncaughtException(ThreadGroup.java:1068) 
        at java.lang.ThreadGroup.uncaughtException(ThreadGroup.java:1063) 
        at java.lang.Thread.dispatchUncaughtException(Thread.java:1953) 
        
        
        
        
        
        E/AndroidRuntime: --->CockroachException:Thread[main,5,main]<---
                          java.lang.RuntimeException: 点击异常
                              at com.wanjian.demo.MainAct$1.onClick(MainAct.java:31)
                              at android.view.View.performClick(View.java:6256)
                              at android.view.View$PerformClick.run(View.java:24701)
                              at android.os.Handler.handleCallback(Handler.java:789)
                              at android.os.Handler.dispatchMessage(Handler.java:98)
                              at android.os.Looper.loop(Looper.java:164)
                              at com.wanjian.cockroach.Cockroach.safeMode(Cockroach.java:161)
                              at com.wanjian.cockroach.Cockroach.access$300(Cockroach.java:17)
                              at com.wanjian.cockroach.Cockroach$2.uncaughtException(Cockroach.java:78)
                              at java.lang.ThreadGroup.uncaughtException(ThreadGroup.java:1068)
                              at java.lang.ThreadGroup.uncaughtException(ThreadGroup.java:1063)
                              at java.lang.Thread.dispatchUncaughtException(Thread.java:1953)

```

捕获到的堆栈如下,可以看到都已经被 `at com.wanjian.cockroach.Cockroach.safeMode(Cockroach.java:161) ` 拦截，APP没有任何影响，没有闪退，也没有重启进程

          
### 注意
 
* 当主线程或子线程抛出异常时都会调用exceptionHandler.handlerException(Thread thread, Throwable throwable)
     
* exceptionHandler.handlerException可能运行在非UI线程中。
    
* handlerException内部建议手动try{  你的异常处理逻辑  }catch(Throwable e){ } ，以防handlerException内部再次抛出异常，导致循环调用handlerException
    
* 若设置了Thread.setDefaultUncaughtExceptionHandler则可能无法捕获子线程异常。


* 最佳拍档`android.arch.lifecycle.LiveData`+`Cockroach`。当使用LiveData.postValue时，Observer会在一个单独的消息中执行，这时
若Observer中发生了异常，就可以被cockroach捕获到，不会有其他影响。





相关视频  [http://weibo.com/tv/v/EvM57BR6O?fid=1034:40b2f631632f0cf2a096a09c65db89ad](http://weibo.com/tv/v/EvM57BR6O?fid=1034:40b2f631632f0cf2a096a09c65db89ad)

[https://github.com/android-notes/Cockroach/blob/master/cockroach.mp4?raw=true](https://github.com/android-notes/Cockroach/blob/master/cockroach.mp4?raw=true)

 

### 原理分析  

核心逻辑

```java
    public static void install(Application context, ExceptionHandler exceptionHandler) {

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                
                if (t == Looper.getMainLooper().getThread()) {
                    finishExceptionActivityIfNeeded(e);
                    safeMode();
                }
            }
        });

    }
    
    
    
     private static void safeMode() {
            while (true) {
                try {
                    Looper.loop();
                } catch (Throwable e) {
                    finishExceptionActivityIfNeeded(e);
                }
            }
        }


```

[相关原理分析](https://github.com/android-notes/Cockroach/blob/master/%E5%8E%9F%E7%90%86%E5%88%86%E6%9E%90.md)



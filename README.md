 

## Cockroach 2.0

### Cockroach 2.0新特性
* Cockroach 2.0减少了Cockroach 1.0版本中Activity生命周期中抛出异常黑屏的问题。
* Cockroach 1.0未雨绸缪，提前做好准备，等待异常到来。Cockroach 2.0马后炮，只有当抛出异常时才去拯救。
* Cockroach 2.0试图在APP即将崩溃时尽量去挽救，不至于情况更糟糕。


用一张图片来形容就是

![img](https://github.com/android-notes/Cockroach/blob/X/wanjiu.jpeg?raw=true)


>特别注意： 当view的measure,layout,draw，以及recyclerview的bindviewholder 方法抛出异常时会导致
viewrootimpl挂掉，此时会回调 onMayBeBlackScreen 方法，建议直接杀死app。以后的版本会只
finish掉viewrootimpl挂掉的Activity而不是直接杀死app

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
            Cockroach.install(new ExceptionHandler() {
                       @Override
                       protected void onUncaughtExceptionHappened(Thread thread, Throwable throwable) {
                           Log.e("AndroidRuntime", "--->onUncaughtExceptionHappened:" + thread + "<---", throwable);
                           new Handler(Looper.getMainLooper()).post(new Runnable() {
                               @Override
                               public void run() {
                                   toast.setText(R.string.safe_mode_excep_tips);
                                   toast.show();
                               }
                           });
                       }
           
                       @Override
                       protected void onBandageExceptionHappened(Throwable throwable) {
                           throwable.printStackTrace();//打印警告级别log，该throwable可能是最开始的bug导致的，无需关心
                           toast.setText("Cockroach Worked");
                           toast.show();
                       }
           
                       @Override
                       protected void onEnterSafeMode() {
                           int tips = R.string.safe_mode_tips;
                           Toast.makeText(App.this, getResources().getString(tips), Toast.LENGTH_LONG).show();
                       
                       }
           
                       @Override
                       protected void onMayBeBlackScreen(Throwable e) {
                           Thread thread = Looper.getMainLooper().getThread();
                           Log.e("AndroidRuntime", "--->onUncaughtExceptionHappened:" + thread + "<---", e);
                           //黑屏时建议直接杀死app
                           sysExcepHandler.uncaughtException(thread, new RuntimeException("black screen"));
                       }
           
                   });
    
        }
    }
    

```


## 原理分析

cockroach2.0通过替换`ActivityThread.mH.mCallback`，实现拦截Activity生命周期，
通过调用ActivityManager的`finishActivity`结束掉生命周期抛出异常的Activity





相关视频  [http://weibo.com/tv/v/EvM57BR6O?fid=1034:40b2f631632f0cf2a096a09c65db89ad](http://weibo.com/tv/v/EvM57BR6O?fid=1034:40b2f631632f0cf2a096a09c65db89ad)

[https://github.com/android-notes/Cockroach/blob/master/cockroach.mp4?raw=true](https://github.com/android-notes/Cockroach/blob/master/cockroach.mp4?raw=true)

 

 
[相关原理分析](https://github.com/android-notes/Cockroach/blob/master/%E5%8E%9F%E7%90%86%E5%88%86%E6%9E%90.md)

[相关连接](https://github.com/android-notes/Cockroach/tree/master)



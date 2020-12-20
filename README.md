 

## Cockroach 2.0

## 他们都在用

![](https://github.com/android-notes/Cockroach/blob/X/logo.jpg)

## 为什么开发这个库
很多时候由于一些微不足道的bug导致app崩溃很可惜，android默认的异常杀进程机制简单粗暴，但很多时候让app崩溃其实也并不能解决问题。

有些bug可能是系统bug，对于这些难以预料的系统bug我们不好绕过，还有一些bug是我们自己编码造成的，对于有些bug来说直接忽略掉的话可能只是导致部分不重要的功能没法使用而已，又或者对用户来说完全没有影响，这种情况总比每次都崩溃要好很多。

下面介绍几个真实案例来说明这个库的优势：

* 有一款特殊的手机，每次开启某个Activity时都报错，提示没有在清单中声明，但其他几百万机型都没问题，这种情况很可能就是系统bug了，由于是在onclick回调里直接使用startActivity来开启Activity，onclick里没有其他逻辑，对于这种情况的话直接忽略掉是最好的选择，因为onclick回调是在一个单独的message中的，执行完了该message就接着执行下一个message，该message执行不完也不会影响下一个message的执行，调用startactivity后会同步等待ams返回的错误码，结果这款特殊的机型返回了没有声明这个Activity，所以对于这种情况可以直接忽略掉，唯一的影响就是这个Activity不会显示，就跟没有调用onClick一样

* 我们在app中集成了个三方的数据统计库，这个库是在Application的onCreate的最后初始化的，但上线后执行初始化时却崩溃了，对于这种情况直接忽略掉也是最好的选择。根据app的启动流程来分析，Application的创建以及onCreate方法的调用都是在同一个message中执行的，该message执行的最后调用了Application的onCreate方法，又由于这个数据统计库是在onCreate的最后才初始化的，所以直接忽略的话也没有影响，就跟没有初始化过一样

* 我们做了个检查app是否需要升级的功能，若需要升级，则使用context开启一个dialog风格的Activity提示是否需要升级，测试阶段没有任何问题，但一上线就崩溃了，提示没有设置FLAG_ACTIVITY_NEW_TASK,由于启动Activity的context是Application，但在高版本android中，可以使用Application启动Activity并且不设置这个FLAG，但在低版本中必须要设置这个FLAG，对于这种问题也可以直接忽略

  API28 ContextImpl startActivity源码
 ```java
  public void startActivity(Intent intent, Bundle options) {
        warnIfCallingFromSystemProcess();

        // Calling start activity from outside an activity without FLAG_ACTIVITY_NEW_TASK is
        // generally not allowed, except if the caller specifies the task id the activity should
        // be launched in. A bug was existed between N and O-MR1 which allowed this to work. We
        // maintain this for backwards compatibility.
        final int targetSdkVersion = getApplicationInfo().targetSdkVersion;

        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) == 0
                && (targetSdkVersion < Build.VERSION_CODES.N
                        || targetSdkVersion >= Build.VERSION_CODES.P)
                && (options == null
                        || ActivityOptions.fromBundle(options).getLaunchTaskId() == -1)) {
            throw new AndroidRuntimeException(
                    "Calling startActivity() from outside of an Activity "
                            + " context requires the FLAG_ACTIVITY_NEW_TASK flag."
                            + " Is this really what you want?");
        }
        mMainThread.getInstrumentation().execStartActivity(
                getOuterContext(), mMainThread.getApplicationThread(), null,
                (Activity) null, intent, -1, options);
    }
 ```

* 还有各种执行onclick时触发的异常，这些很多时候都是可以直接忽略掉的

### 更新日志
* 修复Android P反射限制导致的Activity生命周期异常无法finish Activity问题 

[cockroach1.0版在这](https://github.com/android-notes/Cockroach/tree/master)

### Cockroach 2.0新特性
* Cockroach 2.0减少了Cockroach 1.0版本中Activity生命周期中抛出异常黑屏的问题。
* Cockroach 1.0未雨绸缪，提前做好准备，等待异常到来。Cockroach 2.0马后炮，只有当抛出异常时才去拯救。
* Cockroach 2.0试图在APP即将崩溃时尽量去挽救，不至于情况更糟糕。


用一张图片来形容就是

![img](https://github.com/android-notes/Cockroach/blob/X/wanjiu.jpeg?raw=true)


>特别注意： 当view的measure,layout,draw，以及recyclerview的bindviewholder 方法抛出异常时会导致
viewrootimpl挂掉，此时会回调 onMayBeBlackScreen 方法，建议直接杀死app。目前可以拦截到抛出异常的ViewRootImpl，具体参考这https://github.com/android-notes/SwissArmyKnife/blob/master/saklib/src/main/java/com/wanjian/sak/system/traversals/ViewTraversalsCompact.java

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





相关视频 
[https://github.com/android-notes/Cockroach/blob/master/cockroach.mp4?raw=true](https://github.com/android-notes/Cockroach/blob/master/cockroach.mp4?raw=true)

 

 
[相关原理分析](https://github.com/android-notes/Cockroach/blob/master/%E5%8E%9F%E7%90%86%E5%88%86%E6%9E%90.md)

[相关连接](https://github.com/android-notes/Cockroach/tree/master)



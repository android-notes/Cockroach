
Language

* [English](https://github.com/android-notes/Cockroach/blob/master/README_en.md)
* [Chinese]


## Cockroach

> 打不死的小强,永不crash的Android。

> android 开发中最怕的就是crash，好好的APP测试时没问题，一发布就各种crash，只能通过紧急发布hotfix来解决，但准备hotfix的时间可能很长，导致这段时间用户体验非常差，android中虽然可以通过设置 Thread.setDefaultUncaughtExceptionHandler来捕获所有线程的异常，但主线程抛出异常时仍旧会导致activity闪退，app进程重启。使用Cockroach后就可以保证不管怎样抛异常activity都不会闪退，app进程也不会重启。

### 使用方式

自定义Application继承自android的Application，并在Application中装载，越早初始化越好，可以在Aplication的onCreate中初始化，当然也可以根据需要在任意地方（不一定要在主线程）装载，在任意地方卸载。可以多次装载和卸载。

例如：

```java
  

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by wanjian on 2017/2/14.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Cockroach.install(new Cockroach.ExceptionHandler() {

           // handlerException内部建议手动try{  你的异常处理逻辑  }catch(Throwable e){ } ，以防handlerException内部再次抛出异常，导致循环调用handlerException

            @Override
            public void handlerException(final Thread thread, final Throwable throwable) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.e("AndroidRuntime","--->CockroachException:"+thread+"<---",throwable);
                            Toast.makeText(App.this, "Exception Happend\n" + thread + "\n" + throwable.toString(), Toast.LENGTH_SHORT).show();
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
卸载 Cockroach

```java

 Cockroach.uninstall();
 
```


### 测试
装载Cockroach后点击view抛出异常和new Handler中抛出异常

```java


        final TextView textView = (TextView) findViewById(R.id.text);
        findViewById(R.id.install).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("已安装 Cockroach");
                install();
            }
        });

        findViewById(R.id.uninstall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("已卸载 Cockroach");
                Cockroach.uninstall();
            }
        });

        findViewById(R.id.but1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                throw new RuntimeException("click exception...");
            }
        });

        findViewById(R.id.but2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        throw new RuntimeException("handler exception...");
                    }
                });
            }
        });

        findViewById(R.id.but3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        throw new RuntimeException("new thread exception...");
                    }
                }.start();
            }
        });

        findViewById(R.id.but4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SecActivity.class));
            }
        });

    }

    private void install() {
        Cockroach.install(new Cockroach.ExceptionHandler() {
            @Override
            public void handlerException(final Thread thread, final Throwable throwable) {

                Log.d("Cockroach", "MainThread: " + Looper.getMainLooper().getThread() + "  curThread: " + Thread.currentThread());

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            Log.e("AndroidRuntime","--->CockroachException:"+thread+"<---",throwable);
                            Toast.makeText(getApplicationContext(), "Exception Happend\n" + thread + "\n" + throwable.toString(), Toast.LENGTH_SHORT).show();
//                        throw new RuntimeException("..."+(i++));
                        } catch (Throwable e) {

                        }
                    }
                });
            }
        });
    }

```

捕获到的堆栈如下,可以看到都已经被 `at com.wanjian.cockroach.Cockroach$1.run(Cockroach.java:47)` 拦截，APP没有任何影响，没有闪退，也没有重启进程

```java

02-16 09:58:00.660 21199-21199/wj.com.fuck E/AndroidRuntime: --->CockroachException:Thread[main,5,main]<---
                                                             java.lang.RuntimeException: click exception...
                                                                 at wj.com.fuck.MainActivity$3.onClick(MainActivity.java:53)
                                                                 at android.view.View.performClick(View.java:4909)
                                                                 at android.view.View$PerformClick.run(View.java:20390)
                                                                 at android.os.Handler.handleCallback(Handler.java:815)
                                                                 at android.os.Handler.dispatchMessage(Handler.java:104)
                                                                 at android.os.Looper.loop(Looper.java:194)
                                                                 at com.wanjian.cockroach.Cockroach$1.run(Cockroach.java:47)
                                                                 at android.os.Handler.handleCallback(Handler.java:815)
                                                                 at android.os.Handler.dispatchMessage(Handler.java:104)
                                                                 at android.os.Looper.loop(Looper.java:194)
                                                                 at android.app.ActivityThread.main(ActivityThread.java:5826)
                                                                 at java.lang.reflect.Method.invoke(Native Method)
                                                                 at java.lang.reflect.Method.invoke(Method.java:372)
                                                                 at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:1009)
                                                                 at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:804)
02-16 09:58:12.401 21199-21199/wj.com.fuck E/AndroidRuntime: --->CockroachException:Thread[main,5,main]<---
                                                             java.lang.RuntimeException: handler exception...
                                                                 at wj.com.fuck.MainActivity$4$1.run(MainActivity.java:63)
                                                                 at android.os.Handler.handleCallback(Handler.java:815)
                                                                 at android.os.Handler.dispatchMessage(Handler.java:104)
                                                                 at android.os.Looper.loop(Looper.java:194)
                                                                 at com.wanjian.cockroach.Cockroach$1.run(Cockroach.java:47)
                                                                 at android.os.Handler.handleCallback(Handler.java:815)
                                                                 at android.os.Handler.dispatchMessage(Handler.java:104)
                                                                 at android.os.Looper.loop(Looper.java:194)
                                                                 at android.app.ActivityThread.main(ActivityThread.java:5826)
                                                                 at java.lang.reflect.Method.invoke(Native Method)
                                                                 at java.lang.reflect.Method.invoke(Method.java:372)
                                                                 at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:1009)
                                                                 at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:804)
02-16 09:58:13.241 21199-21199/wj.com.fuck E/AndroidRuntime: --->CockroachException:Thread[Thread-26326,5,main]<---
                                                             java.lang.RuntimeException: new thread exception...
                                                                 at wj.com.fuck.MainActivity$5$1.run(MainActivity.java:76)


```


当卸载`Cockroach`后再在click中抛出异常，日志如下

```java

02-16 09:59:01.251 21199-21199/wj.com.fuck E/AndroidRuntime: FATAL EXCEPTION: main
                                                             Process: wj.com.fuck, PID: 21199
                                                             java.lang.RuntimeException: click exception...
                                                                 at wj.com.fuck.MainActivity$3.onClick(MainActivity.java:53)
                                                                 at android.view.View.performClick(View.java:4909)
                                                                 at android.view.View$PerformClick.run(View.java:20390)
                                                                 at android.os.Handler.handleCallback(Handler.java:815)
                                                                 at android.os.Handler.dispatchMessage(Handler.java:104)
                                                                 at android.os.Looper.loop(Looper.java:194)
                                                                 at android.app.ActivityThread.main(ActivityThread.java:5826)
                                                                 at java.lang.reflect.Method.invoke(Native Method)
                                                                 at java.lang.reflect.Method.invoke(Method.java:372)
                                                                 at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:1009)
                                                                 at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:804)


```             
          
 可以看到 ` at com.wanjian.cockroach.Cockroach$1.run(Cockroach.java:47)` 没有拦截，并且APP crash了。



### 注意
 
* 当主线程或子线程抛出异常时都会调用exceptionHandler.handlerException(Thread thread, Throwable throwable)
     
* exceptionHandler.handlerException可能运行在非UI线程中。
    
* handlerException内部建议手动try{  你的异常处理逻辑  }catch(Throwable e){ } ，以防handlerException内部再次抛出异常，导致循环调用handlerException
    
* 若设置了Thread.setDefaultUncaughtExceptionHandler则可能无法捕获子线程异常。

虽然可以捕获到所有异常，但可能会导致一些莫名其妙的问题，比如view初始化时发生了异常，异常后面的代码得不到执行，虽然不
会导致app crash但view内部已经出现了问题，运行时就会出现很奇葩的现象。再比如activity声明周期方法中抛出了异常，则生
命周期就会不完整，从而导致各种奇葩的现象。

虽然会导致各种奇葩问题发生，但可以最大程度的保证APP正常运行，很多时候我们希望主线程即使抛出异常也不影响app的正常使用，比如我们
给某个view设置背景色时，由于view是null就会导致app crash，像这种问题我们更希望即使view没法设置颜色也不要crash，这
时Cockroach就可以满足你的需求。

handlerException(final Thread thread, final Throwable throwable)内部建议请求自己服务器决定该如何处理该异常，是
直接忽略还是杀死APP又或者其他操作。


Cockroach采用android标准API编写，无依赖，足够轻量，轻量到只有不到100行代码，一般不会存在兼容性问题，也不存在性能上的问题，可以兼容所有android版本。

已上传到jcenter， compile 'com.wanjian:cockroach:0.0.5'

效果视频  [http://weibo.com/tv/v/EvM57BR6O?fid=1034:40b2f631632f0cf2a096a09c65db89ad](http://weibo.com/tv/v/EvM57BR6O?fid=1034:40b2f631632f0cf2a096a09c65db89ad)



### 原理分析  

[原理分析](https://github.com/android-notes/Cockroach/blob/master/%E5%8E%9F%E7%90%86%E5%88%86%E6%9E%90.md)



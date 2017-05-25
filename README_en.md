
> Translate via google

## Cockroach

> Never crash Android

> Android development is most afraid of the crash, the test no problem, released on the crash, only through the emergency release hotfix to solve, but the time to prepare the hotfix may be very long, resulting in this time the user experience is very poor, android can pass Set Thread.setDefaultUncaughtExceptionHandler to catch all threads of the exception, but the main thread throws an exception will still cause the activity flashes, app process restart. Use Cockroach can guarantee that no matter how abnormal activities will not flash, app process will not restart.


### Manual

Custom Application inherited from the android application, and in the Application load, the sooner the better, you can in the Aplication onCreate initialization, of course, can be anywhere in the need (not necessarily in the main thread) loading, in any place Unloading. Can be loaded and unloaded multiple times.

E.g:

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

           //HandlerException internal advice manually try {your exception handling logic} catch (throwable e) {}, in case handlerException internally throws an exception again, causing the loop to call handlerException

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
uninstall Cockroach

```java

 Cockroach.uninstall();
 
```


### Test

After loading Cockroach, click on the view to throw the exception and throw an exception in the new Handler

```java


        final TextView textView = (TextView) findViewById(R.id.text);
        findViewById(R.id.install).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("Installed Cockroach");
                install();
            }
        });

        findViewById(R.id.uninstall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("UnInstalled Cockroach");
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

Capture the stack as follows, you can see that has been `at com.wanjian.cockroach.Cockroach$1.run(Cockroach.java:47)` Interception, APP no effect, no flashback, there is no restart process

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


When uninstalling `Cockroach` and then throw an exception in the click, the log is as follows

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
          
 can be seen ` at com.wanjian.cockroach.Cockroach$1.run(Cockroach.java:47)` no interception, and APP crash.


### Note
 
* An exceptionHandler.handlerException (Thread thread, Throwable throwable) is called when the main thread or child thread throws an exception.     
* ExceptionHandler.handlerException may run in a non-UI thread.
    
* HandlerException internal advice manually try {your exception handling logic} catch (throwable e) {}, in case handlerException internally throws an exception again, causing the loop to call handlerException
    
* If you set the Thread.setDefaultUncaughtExceptionHandler, you may not be able to catch a child thread exception.

Although you can catch all the exceptions, but may lead to some strange problems, such as view initialization occurred when the exception, abnormal code behind the implementation of the implementation, although not
Will lead to app crash But the view has been a problem inside the run, there will be very strange when the phenomenon.


Although it will lead to a variety of strange problems, but can maximize the normal operation of APP to ensure that many times we hope that the main thread even if the exception does not affect the normal use of the app, such as we
To set the background color for a view, because the view is null will lead to app crash, like this problem we hope that even if the view can not set the color and do not crash
When Cockroach can meet your needs.

HandlerException (final thread thread, final Throwable throwable) Internal advice Ask your server to decide how to handle the exception,
Directly ignore or kill APP or other operations.


Cockroach using the android standard API written, no reliance, light enough to light to only less than 100 lines of code, generally there will be no compatibility issues, there is no performance on the issue, can be compatible with all android version.

Has been uploaded to jcenter， compile 'com.wanjian:cockroach:0.0.5'

Effect video  [http://weibo.com/tv/v/EvM57BR6O?fid=1034:40b2f631632f0cf2a096a09c65db89ad](http://weibo.com/tv/v/EvM57BR6O?fid=1034:40b2f631632f0cf2a096a09c65db89ad)

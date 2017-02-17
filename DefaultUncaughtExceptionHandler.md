`Thread.setDefaultUncaughtExceptionHandler`

```java

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Thread.UncaughtExceptionHandler uncaughtExceptionHandler=Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log.e("AndroidRuntime","--->uncaughtException:"+t+"<---",e);
                uncaughtExceptionHandler.uncaughtException(t,e);//若不把异常交给默认的异常处理器处理会导致ANR，交给了就会导致crash
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

    }
}

```


可以看到主线程的异常被UncaughtExceptionHandler捕获到了，若不把异常交给默认的异常处理器处理会导致ANR，交给了就会导致crash
            
```java

02-17 09:36:00.152 19525-19525/wj.com.fuck E/AndroidRuntime: --->uncaughtException:Thread[main,5,main]<---
                                                             java.lang.RuntimeException: click exception...
                                                                 at wj.com.fuck.MainActivity$2.onClick(MainActivity.java:46)
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
02-17 09:36:00.152 19525-19525/wj.com.fuck E/AndroidRuntime: FATAL EXCEPTION: main
                                                             Process: wj.com.fuck, PID: 19525
                                                             java.lang.RuntimeException: click exception...
                                                                 at wj.com.fuck.MainActivity$2.onClick(MainActivity.java:46)
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
 
 
子线程抛出异常也可以被UncaughtExceptionHandler捕获到，即使不把异常交给默认的异常处理器处理也不会导致crash

```java

02-17 09:38:21.555 20825-20933/wj.com.fuck E/AndroidRuntime: --->uncaughtException:Thread[Thread-28481,5,main]<---
                                                             java.lang.RuntimeException: new thread exception...
                                                                 at wj.com.fuck.MainActivity$4$1.run(MainActivity.java:69)
02-17 09:38:21.555 20825-20933/wj.com.fuck E/AndroidRuntime: FATAL EXCEPTION: Thread-28481
                                                             Process: wj.com.fuck, PID: 20825
                                                             java.lang.RuntimeException: new thread exception...
                                                                 at wj.com.fuck.MainActivity$4$1.run(MainActivity.java:69)


```

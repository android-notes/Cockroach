# Cockroach

> 打不死的小强,永不crash的Android


# 使用方式

自定义Application继承自android的Application，并在Application中初始化，越早初始化越好，可以在Aplication的onCreate中初始化

# 初始化方式

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
                            Log.d("Cockroach", thread + "\n" + throwable.toString());
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


Cockroach采用android标准API编写，无依赖，足够轻量，轻量到只有50行代码，一般不会存在兼容性问题，可以兼容所有android版本

已上传到jcenter，明天(2017-2-15)就能用了 compile 'com.wanjian:cockroach:0.0.1'


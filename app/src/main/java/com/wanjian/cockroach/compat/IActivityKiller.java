package com.wanjian.cockroach.compat;

import android.os.Message;

/**
 * Created by wanjian on 2018/5/24.
 */

public interface IActivityKiller {

    void finishLaunchActivity(Message message);

    void finishResumeActivity(Message message);

    void finishPauseActivity(Message message);

    void finishStopActivity(Message message);


}

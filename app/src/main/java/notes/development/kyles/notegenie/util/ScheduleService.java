package notes.development.kyles.notegenie.util;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;

/**
 * This class is accessed by the ScheduleClient
 * Created by Kyle S on 3/30/2015.
 */
public class ScheduleService extends Service {
    //object that receives interactions from the client
    private final IBinder mBinder = new ServiceBinder();
    private static final String SCHEDULE_TAG = "NotifyScheduleService";

    public class ServiceBinder extends Binder {
        ScheduleService getService() {
            return ScheduleService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.w(SCHEDULE_TAG, "Received start id " + startId + ": " + intent);
        System.out.println("here");

        //service will continue running until it is explicitly stopped, so return sticky
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Show alarm for date user entered.
     * When the alarm is called, it will display a notification to the user in the notification bar.
     */
    public void setAlarm(Calendar calendar) {
        //start new thread to set alarm and push off tasks onto a new thread to free up UI to carry on responding
        new Alarm(this, calendar).run();
    }
}

package notes.development.kyles.notegenie.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import notes.development.kyles.notegenie.HomeScreen;

import java.util.Calendar;

/**
 * Set an alarm for the date passed into the constructor
 * When the alarm is raised it will start the NotifyService
 * This uses the android build in alarm manager *NOTE* if the phone is turned off this alarm will be cancelled
 * This will run on it's own thread.
 *
 * Created by Kyle S on 3/30/2015.
 */
public class Alarm implements Runnable{
    //variable to store date selected for the alarm to run
    private final Calendar date;
    //android system alarm manager
    private final AlarmManager alarmManager;
    //context to retrieve the alarm manager from
    private final Context context;

    //constructor to assign variables
    public Alarm(Context context, Calendar date) {
        this.context = context;
        this.date = date;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public void run() {
        //Request to start service when system date has reached alarm date
        //call NotifyService class to pop up notification and handle action when notification clicked
        Intent intent = new Intent(context, NotifyService.class);
        intent.putExtra(NotifyService.INTENT_NOTIFY, true);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        HomeScreen.setPendingIntent(pendingIntent);

        //set alarm - NOTE THAT ALARM WILL BE LOST IF THE DEVICE IS RESTARTED
        alarmManager.set(AlarmManager.RTC, date.getTimeInMillis(), pendingIntent);
    }
}

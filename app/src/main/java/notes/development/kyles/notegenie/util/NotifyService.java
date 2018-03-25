package notes.development.kyles.notegenie.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import notes.development.kyles.notegenie.NotifyScreen;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.provider.Settings;

import notes.development.kyles.notegenie.HomeScreen;
import notes.development.kyles.notegenie.R;

/**
 * This service is started when an Alarm has been raised
 * Display notification in device's notification bar for the user to click on
 * When user clicks notification, a new activity is called that will display the notes relating to the reminder
 *
 * Created by Kyle S on 3/30/2015.
 */
public class NotifyService extends Service{
    public class ServiceBinder extends Binder {
        NotifyService getService() {
            return NotifyService.this;
        }
    }

    //object that receives interactions from client
    private final IBinder mBinder = new ServiceBinder();
    //unique ID to identify notification
    public static int NOTIFICATION_ID = 1;
    //name of intent extra used to identify if service was started
    public static final String INTENT_NOTIFY = "notes.development.kyles.notegenie.INTENT_NOTIFY";
    //system notification manager
    private static NotificationManager notificationManager;
    private static final String NOTIFY_SERVICE_TAG = "NotifyService";
    private static final String LOCAL_SERVICE_TAG = "LocalScheduleService";
    static PendingIntent notifyIntent;

    @Override
    public void onCreate() {
        Log.w(NOTIFY_SERVICE_TAG, "Notification Manager Created");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(LOCAL_SERVICE_TAG, "Received start id " + startId + ": " + intent);

        //if service was started by Alarm intent, then show notification
        if(intent.getBooleanExtra(INTENT_NOTIFY, false))
            showNotification();

        //whether service has stopped or not is irrelevant as the notification has been displayed to the user
        return START_NOT_STICKY;
    }

    /**
     * Method called to create notification and display it in the notification bar
     */
    private void showNotification() {
        //get notification ringtone preferences for sound of ringtone
        SharedPreferences ringtone = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String setRingtone = ringtone.getString("notification_sound", "default");
        Boolean vibrationEnabled = ringtone.getBoolean("notification_vibrate", true);
        Uri notificationSound = Settings.System.DEFAULT_NOTIFICATION_URI;
        long[] vibrationPattern;

        //Prepare intent to be triggered if the notification is selected
        Intent notifyScreen = new Intent(getApplicationContext(), NotifyScreen.class);
        notifyIntent = PendingIntent.getActivity(this, 0, notifyScreen, 0);

        //user chose futurestic notification sound
        if(setRingtone.equals("future")){
            notificationSound = Uri.parse("android.resource://notes.development.kyles.notegenie/" + R.raw.perfect_notification);
        }

        //user chose droid notification sound
        if(setRingtone.equals("droid")){
            notificationSound = Uri.parse("android.resource://notes.development.kyles.notegenie/" + R.raw.droid);
        }

        //user chose galaxy notification sound
        if(setRingtone.equals("galaxy")){
            notificationSound = Uri.parse("android.resource://notes.development.kyles.notegenie/" + R.raw.galaxy);
        }

        //user chose instrumental notification sound
        if(setRingtone.equals("piano")){
            notificationSound = Uri.parse("android.resource://notes.development.kyles.notegenie/" + R.raw.instrumental);
        }

        //user enabled vibration so set pattern
        if(vibrationEnabled)
            vibrationPattern = new long[] {200, 600, 200, 200, 600};
        else
            vibrationPattern = new long[]{};

        //user chose no notification sound
        if(setRingtone.equals("none")) {
            //notification builder
            Notification notification = new Notification.Builder(this)
                    //notification icon
                    .setSmallIcon(R.drawable.notification_icon)

                    //notification title
                    .setContentTitle("NoteGenie Reminder")

                    //notification text
                    .setContentText("Tap to view your notes.")

                    //notification expandable expandable text which appears when notification is expanded
                    //only for devices with Android 4.1 and above
                    //notification expandable text is the name of the note
                    .setStyle(new Notification.BigTextStyle().bigText(HomeScreen.getNotificationName()))

                    //vibrate the device
                    .setVibrate(vibrationPattern)

                    //start NotifyScreen activity when user presses notification
                    .setContentIntent(notifyIntent).build();

            //hide notification after it has been selected
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            //issue notification
            notificationManager.notify(NOTIFICATION_ID, notification);

            //Stop service when notification is finished
            stopSelf();
        }

        //user set notification sound
        else {
            //notification builder
            Notification notification = new Notification.Builder(this)
                    //notification icon
                    .setSmallIcon(R.drawable.notification_icon)

                    //notification title
                    .setContentTitle("NoteGenie Reminder")

                    //notification text
                    .setContentText("Tap to view your notes.")

                    //notification expandable expandable text which appears when notification is expanded
                    //only for devices with Android 4.1 and above
                    //notification expandable text is the name of the note
                    .setStyle(new Notification.BigTextStyle().bigText(HomeScreen.getNotificationName()))

                    //vibrate the device
                    .setVibrate(vibrationPattern)

                    //notification sound
                    .setSound(notificationSound)

                    //start NotifyScreen activity when user presses notification
                    .setContentIntent(notifyIntent).build();

            //hide notification after it has been selected
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            //issue notification
            notificationManager.notify(NOTIFICATION_ID, notification);

            //Stop service when notification is finished
            stopSelf();
        }
    }
}

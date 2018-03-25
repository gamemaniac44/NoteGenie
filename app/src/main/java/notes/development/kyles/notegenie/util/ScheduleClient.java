package notes.development.kyles.notegenie.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import java.util.Calendar;

/**
 * Created by Kyle S on 3/30/2015.
 * Service client for setting up reminder/notification alarms and managing them
 */
public class ScheduleClient {
    //connection to service
    private ScheduleService mScheduleService;
    //context for starting service
    private Context mContext;
    //flag to tell if client is connected to service
    private boolean mScheduleBound;

    public ScheduleClient(Context context) {
        mContext = context;
    }

    /**
     * Method that when called will connect calling activity to the service for alarm manager
     * Calling activity will only call method when a notification is being set
     */
    public void bindScheduleService() {
        //establish connection with service
        mContext.bindService(new Intent(mContext, ScheduleService.class), mConnection, Context.BIND_AUTO_CREATE);
        mScheduleBound = true;
    }

    /**
     * Method called when the activity attempts to connect to the service.
     * If connection is successful, service object is instantiated so that methods can be called on it.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mScheduleService = ((ScheduleService.ServiceBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mScheduleService = null;
        }
    };

    /**
     * Tell ScheduleService to set an alarm for the date the user has set
     * Takes in the date to set the notification for
     */
    public void setAlarmForNotification(Calendar calendar) {
        mScheduleService.setAlarm(calendar);
    }

    /**
     * Method called when service call is finished to stop service by unbinding it
     * Releases the connection and prevents resource leak
     */
    public void unBindScheduleService() {
        //if the service is bound, unbind it
        if (mScheduleBound) {
            //Detach Scheduler from connection
            mContext.unbindService(mConnection);
            mScheduleBound = false;
        }
    }
}

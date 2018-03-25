package notes.development.kyles.notegenie;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import notes.development.kyles.notegenie.util.ViewPagerAdapter;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Intent;
import notes.development.kyles.notegenie.util.SlidingTabLayout;

import com.google.android.gms.common.AccountPicker;
import notes.development.kyles.notegenie.util.ScheduleClient;

import java.util.Calendar;

public class HomeScreen extends ActionBarActivity {

    /**
     * *********
     * Version 1.0 - 2/15/2015
     * Author:  Kyle Siler
     * Note that for this application to run, the device must have android 4.0 (Ice Cream Sandwich) or higher!
     * Home Screen of the application that is first displayed to the users and contains all of the notes in the database.
     * The Home Screen makes use of tabs to switch between subjects, notes, and reminders.
     * **********
     */

    private ActionBarDrawerToggle drawerToggle;
    private String sendEmail;
    private static String reminderName;
    private static String notifyNoteName;
    private static final int REQUEST_EMAIL_CODE = 1;
    private static ScheduleClient scheduleClient;
    public static final String ACCOUNT_TAG = "User Account Emails";
    AlertDialog emailAlertDialog;
    AlertDialog aboutDialog;
    static PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);

        ListView drawerList;
        ArrayAdapter<String> drawerAdapter;
        DrawerLayout drawerLayout;

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //set up side navigation menu
        drawerList = (ListView)findViewById(R.id.navList);
        String[] menuOptions = {"Settings", "About"};
        drawerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menuOptions);
        drawerList.setAdapter(drawerAdapter);

        //set up listener for items in side navigation menu
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //user selected settings
                if(position == 0){
                    Intent settings = new Intent(getApplicationContext(), Settings.class);
                    startActivity(settings);
                }
                if(position == 1){
                    AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(HomeScreen.this);
                    aboutBuilder.setTitle("About Note Genie:");
                    aboutBuilder.setMessage(R.string.aboutApplication);
                    aboutBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    aboutDialog = aboutBuilder.create();
                    aboutDialog.show();
                }
            }
        });

        //add toggle switch in the action bar
        final String activityTitle;
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        activityTitle = getTitle().toString();
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            //called when the drawer is completely open
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Menu");
                invalidateOptionsMenu();
            }

            //called when the drawer is completely closed
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getSupportActionBar().setTitle(activityTitle);
                invalidateOptionsMenu();
            }
        };
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(drawerToggle);

        //Declare Tab Page Viewer Variables
        ViewPager pager;
        ViewPagerAdapter adapter;
        SlidingTabLayout tabs;
        CharSequence tabTitles[] = {"Subjects", "Notes", "Reminders"};
        int numTabs = 3;

        //Create The ViewPagerAdapter and Pass Fragment Manager, Titles fot the Tabs, and Number Of Tabs.
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(), tabTitles, numTabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        //Assign the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.homeScreenTabs);
        //Tabs are Fixed So They Are Spaced Evenly in Available Width
        tabs.setDistributeEvenly(true);

        // Set Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        //Set ViewPager for SlidingTabsLayout
        tabs.setViewPager(pager);

        //search Intent
        Intent searchIntent = getIntent();

        //method call to determine if application has been run for first time to allow user choose email to send notes to
        checkAppFirstRun();

        //execute this code if the user is performing a search
        if (searchIntent.getAction() != null) {
            if (searchIntent.getAction().equals(Intent.ACTION_SEARCH))
            {
                //get search query
                String query = searchIntent.getStringExtra(SearchManager.QUERY);

                //start searchResults activity to display search results
                Intent searchResults = new Intent(getApplicationContext(), SearchResults.class);
                searchResults.putExtra("searchQuery", query);
                startActivity(searchResults);
            }
        }

        scheduleClient = new ScheduleClient(this);
        scheduleClient.bindScheduleService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_screen, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (id == R.id.search) {
            onSearchRequested();
            return true;
        }

        if (id == R.id.createNote) {
            Intent createNoteIntent = new Intent(getApplicationContext(), CreateNote.class);
            startActivity(createNoteIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public void checkAppFirstRun() {
        //use shared preferences to determine if application is run
        //NOTE:  THIS PREFERENCE WILL NOT CHANGE WHEN THE APP IS UPDATED.  CHANGED ONLY WHEN THERE IS A COMPLETE NEW INSTALL/REINSTALL
        boolean appFirstRun = getSharedPreferences("notes.development.kyles.notegenie", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (appFirstRun) {

            //create and show dialog notifying the user that they need to select an email account to use notegenie.
            //NOTE:  THE USER MUST SELECT AN EMAIL AND CANNOT CANCEL THE DIALOG
            AlertDialog.Builder emailDialogBuilder = new AlertDialog.Builder(this);
            emailDialogBuilder.setTitle("Email Settings");
            emailDialogBuilder.setCancelable(false);
            emailDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    //use account manager to get the user's email accounts on their device
                    AccountManager accountManager = AccountManager.get(getApplicationContext());
                    Account[] emailAccounts = accountManager.getAccountsByType(null);
                    for (Account account : emailAccounts) {
                        //log user's emails accounts for debugging purposes
                        Log.d(ACCOUNT_TAG, "account:  " + account.name + ":" + account.type);
                    }
                    try {
                        Intent emailPickerIntent = AccountPicker.newChooseAccountIntent(null, null,
                                new String[] {"com.google", "com.google.android.legacyimap", "com.android.email"},
                                false, null, null, null, null);
                        startActivityForResult(emailPickerIntent, REQUEST_EMAIL_CODE);
                    } catch (ActivityNotFoundException e) {
                        //application had an error so kill the application
                        Toast.makeText(getApplicationContext(), "Note Genie Had an Error:  " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), "Please contact support", Toast.LENGTH_LONG).show();
                        int pid = android.os.Process.myPid();
                        android.os.Process.killProcess(pid);
                    }

                }
            });
            emailAlertDialog = emailDialogBuilder.create();
            emailAlertDialog.setMessage("Note Genie needs your email account to send notes to whenever you choose to email a note." +
                    "This can be changed anytime in the note settings.  Select 'OK' to choose the email account you want to use for " +
                    "NoteGenie.");
            emailAlertDialog.show();
        }
        //if the app has been run first time, then set boolean to signify that the application has now been run a first time
        getSharedPreferences("notes.development.kyles.notegenie", MODE_PRIVATE).edit().putBoolean("isFirstRun", false).apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EMAIL_CODE && resultCode == RESULT_OK) {
            sendEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            Toast.makeText(getApplicationContext(), "Email Configured", Toast.LENGTH_SHORT).show();
        }

        //send email address will be sent to the NotesTab so the email functionality can access sending email address
        if (sendEmail != null) {
            //put email address into shared preference to be accessed throughout application
            SharedPreferences emailPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor emailEditor = emailPrefs.edit();
            emailEditor.putString("email_send", sendEmail);
            emailEditor.apply();
        }
    }

    //method implemented to prevent window leak error with alert dialogue in the HomeScreen, RemindersTab, and NoteScreen
    //DO NOT REMOVE METHOD OR WINDOW LEAK WILL OCCUR!
    protected void onStop() {
        RemindersTab remindersTab = new RemindersTab();
        if (remindersTab.reminderOptionAlert != null) {
            remindersTab.reminderOptionAlert.dismiss();
        }

        if (this.emailAlertDialog != null)
            emailAlertDialog.dismiss();

        //also stop connection to the notification service to prevent activity from leaking into the system
        if (scheduleClient != null) {
            //unbind from service to stop connection
            scheduleClient.unBindScheduleService();
        }
        super.onStop();
    }

    public static void setNotification(int notifyYear, int notifyMonth, int notifyDay, int notifyHour, int notifyMinute) {
        //increment notification id each time method is called so all notifications have a unique id
        //NotifyService.NOTIFICATION_ID++;

        //Create new service client and bind activity to alarm service

        //create new calendar to set the date user has chosen in system alarm manager
        Calendar calendar = Calendar.getInstance();
        calendar.set(notifyYear, notifyMonth, notifyDay);
        calendar.set(Calendar.HOUR_OF_DAY, notifyHour);
        calendar.set(Calendar.MINUTE, notifyMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        //call method in ScheduleClient to set an alarm for that date
        scheduleClient.setAlarmForNotification(calendar);
    }

    public static void setNotificationName(String notifyName) {
        reminderName = notifyName;
    }

    public static String getNotificationName() {
        return reminderName;
    }

    public static void setNotificationNoteName(String noteName) { notifyNoteName = noteName; }

    public static String getNotifyNoteName() { return notifyNoteName; }

    public static void setPendingIntent(PendingIntent intent)
    {
        pendingIntent = intent;
    }

    public static void cancelAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
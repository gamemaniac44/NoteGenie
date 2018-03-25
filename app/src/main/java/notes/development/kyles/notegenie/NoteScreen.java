package notes.development.kyles.notegenie;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import notes.development.kyles.notegenie.util.Database;

import java.util.Calendar;


public class NoteScreen extends ActionBarActivity {
    private ActionBarDrawerToggle drawerToggle;
    private int reminderYear;
    private int reminderMonth;
    private int reminderDay;
    private int reminderHour;
    private int reminderMinute;
    private String noteName;
    private String reminderName;
    AlertDialog createReminderPrompt;
    AlertDialog aboutDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_screen);

        ListView drawerList;
        ArrayAdapter<String> drawerAdapter;
        DrawerLayout drawerLayout;
        String subjectName;

        //keep the screen on until the activity is destroyed so the screen does not turn off on the user while they are editing their note or using voice typing
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //set header of screen to be name of the note.  (passed from other activity)
        Intent noteData = getIntent();
        noteName = noteData.getExtras().getString("Note Name");

        //get subjectName the note is associated with for setting title of activity
        Database.DatabaseOpenHelper helper = new Database.DatabaseOpenHelper(getApplicationContext());
        subjectName = helper.getNoteSubject(noteName);

        //set title of activity to be the name of the subject notes
        getSupportActionBar().setTitle(subjectName + " Notes");

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
                    //update note data before going to settings
                    updateNoteData();
                    Toast.makeText(getApplicationContext(), "note content updated", Toast.LENGTH_LONG).show();

                    //go to settings
                    Intent settings = new Intent(getApplicationContext(), Settings.class);
                    startActivity(settings);
                }
                if(position == 1){
                    AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(NoteScreen.this);
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
        activityTitle = subjectName + " Notes";
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

        //find and set the header text view to be the name of the note
        TextView noteHeaderView = (TextView) findViewById(R.id.noteEditHeader);
        noteHeaderView.setText(noteName);

        //check to see if there is already note data for the selected note
        if (helper.getNoteText(noteName) != null) {
            //find and set the edit text box to have data from the database
            EditText noteText = (EditText) findViewById(R.id.noteEditBox);
            noteText.setText(Database.noteText);
        }

        //there is no note data so the user is inserting new data into the note
        else {
            //call method to update note data in the database with data the user has entered
            updateNoteData();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note_screen, menu);
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

        if (id == R.id.createReminder) {
            //if the user pressed the back button on the device, then update the database with the
            //note data they have entered
            updateNoteData();

            //create a reminder
            promptReminder(noteName);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        //if the user pressed the back button on the device, then update the database with the
        //note data they have entered
        updateNoteData();

        //also check to see if they should be prompted to create a reminder based on the text in the note
        //create instance of database helper to get data from database
        Database.DatabaseOpenHelper helper;
        helper = new Database.DatabaseOpenHelper(getApplicationContext());

        //verify that database reminder check returned true and if so, then call method to display reminder
        if (helper.checkNoteForReminder(Database.noteText))
            promptReminder(noteName);

        //go back to the HomeScreen
        else {
            Intent HomeScreen = new Intent(getApplicationContext(), HomeScreen.class);
            startActivity(HomeScreen);
        }
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

    /*
     * Method will call method from database to update note text with the data the user has entered
     * in the note text box.
     */
    public void updateNoteData() {
        Database.DatabaseOpenHelper helper;
        helper = new Database.DatabaseOpenHelper(getApplicationContext());

        //find and set the edit text box to have data from the database
        EditText noteText = (EditText) findViewById(R.id.noteEditBox);
        String noteData = noteText.getText().toString();

        //find and set the header text view to be the name of the note
        TextView noteHeaderView = (TextView) findViewById(R.id.noteEditHeader);
        String noteName = noteHeaderView.getText().toString();

        helper.updateNoteText(noteName, noteData);
    }

    /*
     * Method will display a dialogue box to the user to create a reminder.  If they hit cancel then
     * the reminder will not be set.
     */
    public void promptReminder(final String noteName) {
        //create confirm dialogue box to make sure that user wants to create a reminder
        AlertDialog.Builder createReminderDialogBuilder = new AlertDialog.Builder(NoteScreen.this);
        createReminderDialogBuilder.setCancelable(false);

        //no button of dialogue box
        createReminderDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //if no button was clicked then return to the HomeScreen
                dialog.dismiss();
                //go back to the HomeScreen
                Intent HomeScreen = new Intent(getApplicationContext(), HomeScreen.class);
                startActivity(HomeScreen);
            }
        });

        //yes button of dialogue box
        createReminderDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //find and set the header text view to be the name of the note

                //if yes button was clicked then display create reminder dialog box
                dialog.dismiss();

                //show Dialog so user can set name for reminder
                AlertDialog.Builder setReminderName = new AlertDialog.Builder(NoteScreen.this);
                setReminderName.setTitle("Reminder Name:");
                setReminderName.setMessage("Type in what you want your reminder to be called:");
                setReminderName.setCancelable(false);

                //set EditText view to get user input for reminder name
                final EditText reminderNameData = new EditText(NoteScreen.this);
                setReminderName.setView(reminderNameData);

                //call method in HomeScreen to set name of reminder then show DatePicker Dialog so user can select date for reminder
                setReminderName.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //only create a reminder when the user has typed in a reminder name, they cannot create a blank reminder
                        reminderName = reminderNameData.getText().toString();
                        if (reminderName.equals(""))
                            Toast.makeText(getApplicationContext(), "You must enter a reminder name to create a reminder", Toast.LENGTH_LONG).show();
                        else {

                            HomeScreen.setNotificationName(reminderName);

                            //method call to send noteName to notification for displaying notes when
                            //notification is clicked by user
                            HomeScreen.setNotificationNoteName(noteName);

                            //initialize date picker values to current date
                            Calendar currentDate = Calendar.getInstance();
                            reminderYear = currentDate.get(Calendar.YEAR);
                            reminderMonth = currentDate.get(Calendar.MONTH);
                            reminderDay = currentDate.get(Calendar.DAY_OF_MONTH);

                            //show Date Picker Dialog so user can select date for reminder
                            DatePickerDialog setReminderDate = new DatePickerDialog(NoteScreen.this, datePickerListener, reminderYear, reminderMonth, reminderDay);
                            setReminderDate.setTitle("Reminder Date:");
                            setReminderDate.setMessage("Set the date for your reminder:");
                            setReminderDate.setCancelable(false);
                            setReminderDate.show();
                        }
                    }
                });
                setReminderName.show();
            }
        });

        //create dialogue, set dialogue box title, and show it
        createReminderPrompt = createReminderDialogBuilder.create();
        createReminderPrompt.setTitle("Create a New Reminder?");
        createReminderPrompt.setMessage("Hey There!  Based on the content in your note, it seems that you have " +
                "something big coming up that you are going to have to prepare for.  I can help you study in advance by " +
                "creating a reminder for you to remind you to study and/or help you prepare!  Do you want to create a reminder?");
        createReminderPrompt.show();
    }

    /**
     * Method executed when the user has set a date.
     * Will display another dialogue for user to set time and then call setNotification in HomeScreen to set reminder.
     */
    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            //to avoid values being set twice
            if(view.isShown()) {
                //set date values
                reminderYear = year;
                reminderMonth = monthOfYear;
                reminderDay = dayOfMonth;

                //initialize time picker value to current time
                Calendar currentTime = Calendar.getInstance();
                reminderHour = currentTime.get(Calendar.HOUR_OF_DAY);
                reminderMinute = currentTime.get(Calendar.MINUTE);
            }

            //when the user has selected a date for the reminder, show Time Picker Dialog so user can select time for reminder
            //and store the time the user entered into reminderHour and reminderMinute variables to be passed to setNotification
            //method in HomeScreen to set reminder
            TimePickerDialog setReminderTime = new TimePickerDialog(NoteScreen.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    //to avoid method and values being set/called twice
                    if(view.isShown()) {
                        //store the time the user has entered into variables to be passed to setNotification method in HomeScreen to set reminder
                        reminderHour = hourOfDay;
                        reminderMinute = minute;

                        //method call to set notification reminder
                        HomeScreen.setNotification(reminderYear, reminderMonth, reminderDay, reminderHour, reminderMinute);

                        //insert reminder date and time into the database
                        Database.DatabaseOpenHelper helper;
                        helper = new Database.DatabaseOpenHelper(getApplicationContext());
                        helper.insertReminder(noteName, reminderName, reminderYear, reminderMonth, reminderDay, reminderHour, reminderMinute);

                        //go back to the HomeScreen
                        Intent HomeScreen = new Intent(getApplicationContext(), HomeScreen.class);
                        startActivity(HomeScreen);

                        //Display Message to user that alarm has been set
                        Toast.makeText(getApplicationContext(), "Reminder Set", Toast.LENGTH_LONG).show();
                    }
                }
            }, reminderHour, reminderMinute, false);
            setReminderTime.setTitle("Reminder Time:");
            setReminderTime.setMessage("Set the time for your reminder");
            setReminderTime.setCancelable(false);
            setReminderTime.show();
        }
    };
}
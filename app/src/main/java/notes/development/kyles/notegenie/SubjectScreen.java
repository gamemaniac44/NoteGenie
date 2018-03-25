package notes.development.kyles.notegenie;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;
import notes.development.kyles.notegenie.util.Database;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class SubjectScreen extends ActionBarActivity {
    private ActionBarDrawerToggle drawerToggle;
    private ArrayList<String> subjectNotes;
    private ArrayList<String> subjectReminders = new ArrayList<>();
    private ArrayAdapter<String> subjectRemindersAdapter;
    private ArrayAdapter<String> subjectNotesAdapter;
    private String sendEmail;
    private String sendText;
    private String noteName;
    private String subject;
    private String reminderName;
    private String newReminderName;
    private int reminderYear;
    private int reminderMonth;
    private int reminderDay;
    private int reminderHour;
    private int reminderMinute;
    ListView subjectNotesList;
    ListView subjectRemindersList;
    AlertDialog reminderOptionAlert;
    AlertDialog editReminderAlert;
    AlertDialog aboutDialog;
    private String[] reminderDialogTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_screen);

        ListView drawerList;
        ArrayAdapter<String> drawerAdapter;
        DrawerLayout drawerLayout;

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //get subject name from home screen and set as title of activity
        Intent intent = getIntent();
        subject = intent.getExtras().getString("subject");
        getSupportActionBar().setTitle(subject);

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
                    //go to settings screen
                    Intent settings = new Intent(getApplicationContext(), Settings.class);
                    startActivity(settings);
                }
                if(position == 1){
                    AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(SubjectScreen.this);
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
        activityTitle = subject;
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

        //method calls to display notes and reminders associated with the subject
        displayNotes(subject);
        displayReminders(subject);
    }

    /*
     * Method will display all of the notes in the database pertaining to the subject passed in through
     * the subject parameter.
     */
    public void displayNotes(final String subject)
    {
        ArrayAdapter<String> noteListAdapter;
        subjectNotesList = (ListView) findViewById(R.id.subjectNotesList);

        //create instance of database helper to get data from database
        final Database.DatabaseOpenHelper helper;

        //database helper to get data from database
        helper = new Database.DatabaseOpenHelper(SubjectScreen.this);

        subjectNotes = helper.getSubjectNotes(subject);

        //do not add blank elements to a list, occurs when user creates a subject with no notes
        for(int i = 0; i < subjectNotes.size(); i++){
            if(subjectNotes.get(i).equals(""))
                subjectNotes.remove(i);
        }

        //check if there were any notes under the subject and if so then display them on the screen
        if (!subjectNotes.isEmpty()) {
            //noteList to display note results from the database
            noteListAdapter = new ArrayAdapter <> (SubjectScreen.this, android.R.layout.simple_list_item_1, subjectNotes);
            subjectNotesList.setAdapter(noteListAdapter);
        }

        //there were no notes so display an error message
        if(subjectNotes.isEmpty()) {
            //display error in note results list
            subjectNotes.add("No Notes To Display At This Time");
        }

        subjectNotesAdapter = new ArrayAdapter <> (SubjectScreen.this, android.R.layout.simple_list_item_1, subjectNotes);
        subjectNotesList.setAdapter(subjectNotesAdapter);

        //options for note options dialogue box displayed when user long presses a note
        final CharSequence[] noteOptions = {"Delete", "Email", "Edit"};

        //set on item long click listener where long clicks are used when the user wants to delete or edit a subject
        subjectNotesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //get title of note that was selected
                //do not allow options if "No Notes To Display At This Time" is displayed
                if (!parent.getItemAtPosition(position).toString().equals("No Notes To Display At This Time")) {

                    noteName = (String) parent.getItemAtPosition(position);

                    //if a note was long pressed then display a dialogue box with list asking if they want to delete, email, or edit note
                    //Initialize dialogue box
                    AlertDialog.Builder noteSelected = new AlertDialog.Builder(SubjectScreen.this);

                    //set title of dialogue box
                    noteSelected.setTitle("Note Options for " + noteName);

                    noteSelected.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //if cancel button was clicked then cancel dialog
                            dialog.dismiss();
                        }
                    });

                    //set dialogue options and assign them with the click listener
                    noteSelected.setItems(noteOptions, new DialogInterface.OnClickListener() {

                        //click listener to check which option is selected
                        @Override
                        public void onClick(DialogInterface dialog, int option) {
                            //user has selected to delete a note
                            if (option == 0) {
                                Toast.makeText(SubjectScreen.this, "Note Deleted", Toast.LENGTH_LONG).show();

                                //get reminder the note is associated with from the database
                                String reminderName = helper.getReminderName(noteName);

                                //remove reminder associated with deleted note from the database and update the reminder list
                                helper.deleteReminder(reminderName);

                                //remove selected note from the database and update the note list
                                helper.deleteNote(noteName);

                                //inform the note list of the changes
                                updateNotesResultsList(subject);

                                //inform the reminders list of the changes
                                updateRemindersResultsList(subject);
                            }

                            //user has selected to email a note
                            if (option == 1) {
                                //get email address to send notes to, if it is null, then email functionality is disabled
                                SharedPreferences emailPrefs = PreferenceManager.getDefaultSharedPreferences(SubjectScreen.this);
                                if (emailPrefs != null) {
                                    //get note text from database to pass as email body
                                    sendText = helper.getNoteText(noteName);

                                    sendEmail = emailPrefs.getString("email_send", "email");
                                    if (!sendEmail.equals("email")) {
                                        //method call to email select note
                                        emailNote(sendEmail, noteName, sendText);
                                    } else {
                                        //disable email functionality
                                        Toast.makeText(SubjectScreen.this, "Email has not been configured!", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    //disable email functionality
                                    Toast.makeText(SubjectScreen.this, "Email has not been configured!", Toast.LENGTH_LONG).show();
                                }
                            }

                            //user has selected to edit a note
                            if (option == 2) {
                                //method call to pass data into note edit screen for when the edit note option is selected
                                editViewNote(noteName);
                            }
                        }
                    });

                    AlertDialog noteOptionAlert = noteSelected.create();

                    //display dialogue box
                    noteOptionAlert.show();
                }
                return true;
            }
        });

        //set listener for item clicks.  When the user clicks a note, they will be taken to the note's contents where they can edit/see the note.
        subjectNotesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //do not allow for item clicks if "No Results Were Found" is displayed
                if (!parent.getItemAtPosition(position).equals("No Notes To Display At This Time")) {
                    //get name of note to pass into note screen to act as a header
                    final String noteName = (String) parent.getItemAtPosition(position);

                    //method call to pass data into note edit screen for when a note is pressed
                    editViewNote(noteName);
                }
            }
        });
    }

    /*
     * Method will display all of the reminders in the database pertaining to the subject passed in through
     * the subject parameter.
     */
    public void displayReminders(final String subject) {
        subjectRemindersList = (ListView) findViewById(R.id.subjectRemindersList);

        //create instance of database helper to get data from database
        final Database.DatabaseOpenHelper helper;

        //database helper to get data from database
        helper = new Database.DatabaseOpenHelper(SubjectScreen.this);

        //get all of the notes linked to subject from the database
        subjectNotes = helper.getSubjectNotes(subject);

        //subjectReminders.add("test");

        //get reminders associated with all notes in the subject
        for(int i = 0; i < subjectNotes.size(); i++) {
            if(helper.getSubjectReminders(subjectNotes.get(i)) != null)
                subjectReminders.add(helper.getSubjectReminders(subjectNotes.get(i)));
        }

        //check if there were any reminders under the subject and if so then display them on the screen
        if (!subjectReminders.isEmpty()) {
            //reminderList to display reminder results from the database
            subjectRemindersAdapter = new ArrayAdapter<>(SubjectScreen.this, android.R.layout.simple_list_item_1, subjectReminders);
            subjectRemindersList.setAdapter(subjectRemindersAdapter);
        }

        //there were no reminders so display an error message
        if (subjectReminders.isEmpty()) {
            //display error in reminder results list
            subjectReminders.add("No Reminders To Display At This Time");
            subjectRemindersAdapter = new ArrayAdapter<>(SubjectScreen.this, android.R.layout.simple_list_item_1, subjectReminders);
            subjectRemindersList.setAdapter(subjectRemindersAdapter);
        }

        //options for reminder options dialogue box displayed when user long presses a reminder
        final CharSequence[] reminderOptions = {"Delete", "Edit"};

        //set listener for item clicks.  When the user clicks a reminder, a dialogue box will appear that will let
        //them edit or delete a reminder
        subjectRemindersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //get name of note to set as title of reminders options dialogue box
                reminderName = (String) parent.getItemAtPosition(position);
                reminderDialogTitle = reminderName.split("\n");

                if(!parent.getItemAtPosition(position).toString().equals("No Reminders To Display At This Time")) {

                    //create dialogue box to allow user to edit and delete a reminder
                    AlertDialog.Builder reminderDialogBuilder = new AlertDialog.Builder(SubjectScreen.this);

                    reminderDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //if cancel button was clicked then cancel dialog
                            dialog.dismiss();
                        }
                    });

                    //set dialogue options and assign them with the click listener
                    reminderDialogBuilder.setItems(reminderOptions, new DialogInterface.OnClickListener() {

                        //click listener to check which option is selected
                        @Override
                        public void onClick(DialogInterface dialog, int option) {
                            //user has selected to delete a reminder
                            if (option == 0) {
                                Toast.makeText(SubjectScreen.this, "Reminder Deleted", Toast.LENGTH_LONG).show();

                                //create instance of database helper to get data from database
                                Database.DatabaseOpenHelper helper;
                                helper = new Database.DatabaseOpenHelper(SubjectScreen.this);

                                //delete reminder from the database and update reminder list
                                helper.deleteReminder(reminderDialogTitle[0]);

                                //inform the notes list of the changes
                                updateNotesResultsList(subject);

                                //inform the reminders list of the changes
                                updateRemindersResultsList(subject);

                                //delete reminder from notification manager so notification will not display
                                HomeScreen.cancelAlarm(SubjectScreen.this);
                            }

                            //user has selected to edit a reminder
                            if (option == 1) {
                                //create dialogue box for user to edit selected reminder reminder
                                AlertDialog.Builder createEditReminderDialogBuilder = new AlertDialog.Builder(SubjectScreen.this);
                                createEditReminderDialogBuilder.setTitle("New Reminder Name:");
                                createEditReminderDialogBuilder.setMessage("Type a new name for the reminder.");
                                createEditReminderDialogBuilder.setCancelable(false);

                                //set EditText view to get user input for new reminder name
                                final EditText newReminderNameData = new EditText(SubjectScreen.this);
                                newReminderNameData.setText(reminderName);
                                createEditReminderDialogBuilder.setView(newReminderNameData);

                                createEditReminderDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        newReminderName = newReminderNameData.getText().toString();
                                        if (newReminderName.equals(""))
                                            Toast.makeText(SubjectScreen.this, "You must enter a reminder name to edit the reminder", Toast.LENGTH_LONG).show();
                                        else {
                                            dialog.dismiss();

                                            HomeScreen.setNotificationName(newReminderName);

                                            //get the note name associated with the reminder for getting the time and date data of the note from database
                                            noteName = helper.getNoteName(reminderName);

                                            //method call to initialize reminder time and date pickers
                                            initializeReminder(SubjectScreen.this, noteName);

                                            //method call to send noteName to notification for displaying notes when
                                            //notification is clicked by user
                                            HomeScreen.setNotificationNoteName(noteName);

                                            //initialize date picker values to the old reminder date
                                            Calendar oldReminderDate = Calendar.getInstance();
                                            oldReminderDate.set(reminderYear, reminderMonth, reminderDay);

                                            //show Date Picker Dialog so user can select date for new reminder
                                            DatePickerDialog setReminderDate = new DatePickerDialog(SubjectScreen.this, datePickerListener, reminderYear, reminderMonth, reminderDay);
                                            setReminderDate.setTitle("Reminder Date:");
                                            setReminderDate.setMessage("Set a new date for the reminder:");
                                            setReminderDate.setCancelable(false);
                                            setReminderDate.show();
                                        }
                                    }
                                });

                                editReminderAlert = createEditReminderDialogBuilder.create();
                                editReminderAlert.show();

                            }

                        }

                    });
                    reminderOptionAlert = reminderDialogBuilder.create();
                    reminderOptionAlert.setTitle("Reminder Options for " + reminderName);

                    //display dialogue box
                    reminderOptionAlert.show();
                }
            }
        });
    }

    /**
     * Method executed when the user has set a date.
     * Will display another dialogue for user to set time and then call setNotification in HomeScreen to set reminder.
     */
    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            //to avoid values being set twice
            if (view.isShown()) {
                //set date values
                reminderYear = year;
                reminderMonth = monthOfYear;
                reminderDay = dayOfMonth;

                //initialize time picker value to the old reminder time
                Calendar oldReminderTime = Calendar.getInstance();
                oldReminderTime.set(Calendar.HOUR_OF_DAY, reminderHour);
                oldReminderTime.set(Calendar.MINUTE, reminderMinute);

                //when the user has selected a date for the reminder, show Time Picker Dialog so user can select time for reminder
                //and store the time the user entered into reminderHour and reminderMinute variables to be passed to setNotification
                //method in HomeScreen to set reminder
                final TimePickerDialog setReminderTime = new TimePickerDialog(SubjectScreen.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        //to avoid method and values being set/called twice
                        if (view.isShown()) {
                            //store the time the user has entered into variables to be passed to setNotification method in HomeScreen to set reminder
                            reminderHour = hourOfDay;
                            reminderMinute = minute;

                            //remove current set notification time and replace with new one
                            HomeScreen.cancelAlarm(SubjectScreen.this);
                            HomeScreen.setNotification(reminderYear, reminderMonth, reminderDay, reminderHour, reminderMinute);

                            //update reminder date and time into the database
                            Database.DatabaseOpenHelper helper;
                            helper = new Database.DatabaseOpenHelper(SubjectScreen.this);
                            helper.updateReminder(noteName, reminderName, newReminderName, reminderYear, reminderMonth, reminderDay, reminderHour, reminderMinute);

                            //update remindersList with changes
                            updateRemindersResultsList(subject);

                            Toast.makeText(SubjectScreen.this, "Reminder Updated", Toast.LENGTH_LONG).show();
                        }
                    }
                }, reminderHour, reminderMinute, false);
                setReminderTime.setTitle("Reminder Time:");
                setReminderTime.setMessage("Set a new time for the reminder");
                setReminderTime.setCancelable(false);
                setReminderTime.show();
            }
        }
    };

    @Override
    public void onBackPressed()
    {
        //when back button is pressed, update all lists on the HomeScreen
        SubjectTab.updateSubjectList();
        NotesTab.updateNoteList();

        //implemented to avoid crash with null view in Reminders Tab when updating reminders list
        //DO NOT REMOVE
        RemindersTab remindersTab = new RemindersTab();
        remindersTab.updateReminderList(getApplicationContext());

        //go back to the HomeScreen
        Intent HomeScreen = new Intent(getApplicationContext(), HomeScreen.class);
        startActivity(HomeScreen);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_subject_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return drawerToggle.onOptionsItemSelected(item) || drawerToggle.onOptionsItemSelected(item);
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

    public void updateNotesResultsList(String subject)
    {
        //create instance of database helper to get data from database
        Database.DatabaseOpenHelper helper = new Database.DatabaseOpenHelper(this);

        subjectNotesAdapter.clear();
        //search the subjects in the database for the query the user has typed
        subjectNotes = helper.getSubjectNotes(subject);

        //check if there were any notes under the subject and if so then display them on the screen
        if (!subjectNotes.isEmpty()) {
            //noteList to display note results from the database
            subjectNotesAdapter = new ArrayAdapter <> (SubjectScreen.this, android.R.layout.simple_list_item_1, subjectNotes);
            subjectNotesList.setAdapter(subjectNotesAdapter);
        }

        //there were no notes so display an error message
        if(subjectNotes.isEmpty()) {
            //display error in note results list
            subjectNotes.add("No Notes To Display At This Time");
            subjectNotesAdapter = new ArrayAdapter <> (SubjectScreen.this, android.R.layout.simple_list_item_1, subjectNotes);
            subjectNotesList.setAdapter(subjectNotesAdapter);
        }
    }

    public void updateRemindersResultsList(String subject)
    {
        //create instance of database helper to get data from database
        Database.DatabaseOpenHelper helper = new Database.DatabaseOpenHelper(getApplicationContext());

        subjectRemindersAdapter.clear();

        ArrayList<String> notes;
        notes = helper.getSubjectNotes(subject);

        ArrayList<String> reminders = new ArrayList<>();
        for(int i =0; i < notes.size(); i++) {
            if (!helper.getReminderName(notes.get(i)).equals(""))
                reminders.add(helper.getReminderName(notes.get(i)));
        }

        if (!reminders.isEmpty()) {
            subjectRemindersAdapter = new ArrayAdapter <> (SubjectScreen.this, android.R.layout.simple_list_item_1, reminders);
            subjectRemindersList.setAdapter(subjectRemindersAdapter);
        }

        if (reminders.isEmpty()) {
            //display error in reminder results list
            reminders.add("No Reminders To Display At This Time");
            subjectRemindersAdapter = new ArrayAdapter <> (SubjectScreen.this, android.R.layout.simple_list_item_1, reminders);
            subjectRemindersList.setAdapter(subjectRemindersAdapter);
        }
    }

    /*
     * Method executed when the user has selected a note or long pressed a note and selected the option to edit a note.
     * Call the NoteScreen activity which will display the note stored in the database to the user.
     */
    public void editViewNote(String noteName)
    {
        //call note screen activity and pass name of note so user can edit/see contents of note that is selected from list
        Intent displayNote = new Intent(getApplicationContext(), NoteScreen.class);
        displayNote.putExtra("Note Name", noteName);
        startActivity(displayNote);
    }

    /*
     * Method is called when users presses the email note option from dialogue box.
     * This method will call an implicit email intent to email the selected note to the user's email address by using
     * any mail application installed on the device.
     */
    public void emailNote(String sendEmail, String noteName, String noteText)
    {
        //create an implicit intent to email the note to the user's email address or the address they specify in the mail app
        Intent emailNoteIntent = new Intent();
        emailNoteIntent.setAction(Intent.ACTION_SEND);

        //Recipient of the email, recipient is app user by default
        emailNoteIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{sendEmail});

        //subject of the email
        emailNoteIntent.putExtra(Intent.EXTRA_SUBJECT, "Note Genie:  " + noteName + " notes");

        //body of the email
        emailNoteIntent.putExtra(Intent.EXTRA_TEXT, noteText + "\n" + "\n" + "\n" + "Powered by Note Genie \nCopyright 2015 All Rights Reserved");

        //set text type of the email for personalization
        emailNoteIntent.setType("message/rfc822");

        //try to send the email using mail client on phone
        //if no mail client is installed then notify user with toast error message
        try {
            startActivity(Intent.createChooser(emailNoteIntent, "Send Note via:"));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "Can't send mail.  There are no mail application installed on device.", Toast.LENGTH_LONG).show();
        }
    }

    public void initializeReminder(Context context, String noteName)
    {
        //create instance of database helper to get data from database
        Database.DatabaseOpenHelper helper = new Database.DatabaseOpenHelper((context));

        ArrayList<String> reminderDateTime;
        reminderDateTime = helper.getReminderDateTime(noteName);

        //get and assign data from array list
        String reminderMonthData = reminderDateTime.get(0);
        String reminderDayData= reminderDateTime.get(1);
        String reminderYearData = reminderDateTime.get(2);
        String reminderHourData = reminderDateTime.get(3);
        String reminderMinuteData = reminderDateTime.get(4);

        //for converting string month to int value for the calendar
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        reminderMonth = Arrays.asList(months).indexOf(reminderMonthData);
        reminderDay = Integer.valueOf(reminderDayData);
        reminderYear = Integer.valueOf(reminderYearData);
        reminderHour = Integer.valueOf(reminderHourData);
        reminderMinute = Integer.valueOf(reminderMinuteData);
    }
}
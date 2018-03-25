package notes.development.kyles.notegenie;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
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
import android.content.Intent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import notes.development.kyles.notegenie.util.Database;

public class SearchResults extends ActionBarActivity {
    private ActionBarDrawerToggle drawerToggle;
    private ArrayAdapter<String> subjectSearchAdapter;
    private ArrayList<String> subjectSearchResults;
    private ArrayAdapter<String> noteSearchAdapter;
    private ArrayList<String> noteSearchResults;
    private ArrayAdapter<String> reminderSearchAdapter;
    private ArrayList<String> reminderSearchResults;
    private String subjectName;
    private ArrayList<String> subjectNotes;
    private String searchQuery;
    private String subjectReminders;
    private String sendEmail;
    private String sendText;
    private String noteName;
    private String[] reminderDialogTitle;
    private String newReminderName;
    private int reminderYear;
    private int reminderMonth;
    private int reminderDay;
    private int reminderHour;
    private int reminderMinute;
    private Set<String> subjectSet;
    ListView subjectResultsList;
    ListView noteResultsList;
    ListView reminderResultsList;
    AlertDialog reminderOptionAlert;
    AlertDialog editReminderAlert;
    AlertDialog aboutDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_results);

        ListView drawerList;
        ArrayAdapter<String> drawerAdapter;
        DrawerLayout drawerLayout;

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //set title of activity
        getSupportActionBar().setTitle("Search Results");

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
                    AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(SearchResults.this);
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
        activityTitle = "Search Results";
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

        //get search query from home screen and call method to search notes for term/terms user entered
        Intent intent = getIntent();
        searchQuery = intent.getExtras().getString("searchQuery");

        searchDatabaseSubjects(searchQuery);
        searchDatabaseNoteText(searchQuery);
        searchDatabaseReminders(searchQuery);
    }
    /*
     * Method executed when the user is searching for key term or set of words
     * Searches the subjects in the database and returns any results found that contain
     * the keyword(s) the user is searching for.
     * "No Results Were Found" is displayed if there are no results returned from the database search query.
     */
    public void searchDatabaseSubjects(final String searchQuery) {
        subjectResultsList = (ListView) findViewById(R.id.subjectResultsList);

        //create database helper for searching database
        Database.DatabaseOpenHelper helper = new Database.DatabaseOpenHelper(this);

        //search the subjects in the database for the query the user has typed
        subjectSearchResults = helper.getSubjectSearchName(searchQuery);

        //remove any duplicate items from list by adding to set and then adding back to subjects ArrayList
        subjectSet = new HashSet<>();
        subjectSet.addAll(subjectSearchResults);

        //check if there were any subject results and if so then display them on the screen
        if (!subjectSearchResults.isEmpty()) {
            //subjectList to display subject results from the database
            subjectSearchResults.clear();
            subjectSearchResults.addAll(subjectSet);
            subjectSearchAdapter = new ArrayAdapter <> (SearchResults.this, android.R.layout.simple_list_item_1, subjectSearchResults);
            subjectResultsList.setAdapter(subjectSearchAdapter);
        }

        //there were no results so display an error message
        if(subjectSearchResults.isEmpty()) {
            //display error in subject results list
            subjectSearchResults.add("No Results Were Found");
        }

        subjectSearchAdapter = new ArrayAdapter <> (SearchResults.this, android.R.layout.simple_list_item_1, subjectSearchResults);
        subjectResultsList.setAdapter(subjectSearchAdapter);

        //options for subject options dialogue box displayed when user long presses a subject
        final CharSequence[] subjectOptions = {"Delete", "Edit"};

        //set on item long click listener where long clicks are used when the user wants to delete or edit a subject
        subjectResultsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //get title of subject that was selected
                //do not allow options if "No Results Were Found" is displayed
                if(!parent.getItemAtPosition(position).toString().equals("No Results Were Found")){

                    subjectName = (String) parent.getItemAtPosition(position);

                    //if a subject was long pressed then display a dialogue box with list asking if they want to delete or edit the subject
                    //Initialize dialogue box
                    final AlertDialog.Builder subjectSelected = new AlertDialog.Builder(SearchResults.this);

                    //set title of dialogue box
                    subjectSelected.setTitle("Options for Subject:  " + subjectName);

                    subjectSelected.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

                    subjectSelected.setItems(subjectOptions, new DialogInterface.OnClickListener() {

                        //click listener to check which option is selected
                        @Override
                        public void onClick(DialogInterface dialog, int option) {
                            //user has selected to delete a subject
                            if (option == 0) {
                                Toast.makeText(SearchResults.this, "Subject Deleted", Toast.LENGTH_LONG).show();

                                //create instance of database helper to get data from database
                                Database.DatabaseOpenHelper helper;
                                helper = new Database.DatabaseOpenHelper(SearchResults.this);

                                //get the notes the subject is associated with from the database
                                subjectNotes = helper.getSubjectNotes(subjectName);

                                //remove any duplicate items from list by adding to set and then adding back to subjects ArrayList
                                subjectSet = new HashSet<>();
                                subjectSet.addAll(subjectSearchResults);

                                //delete all the reminders associated with all notes under the subject
                                for (int i = 0; i < subjectNotes.size(); i++) {
                                    //remove reminder associated with each note under the subject from the database and update the reminder list
                                    subjectReminders = helper.getReminderName(subjectNotes.get(i));
                                    helper.deleteReminder(subjectReminders);

                                    //inform the reminders results list of the changes
                                    updateRemindersResultsList(searchQuery);
                                }

                                //remove selected subject from the database and update the subject list
                                helper.deleteSubject(subjectName);

                                //inform the subject results list of the changes
                                updateSubjectsResultsList(searchQuery);

                                //inform the note results list of the changes
                                updateNotesResultsList(searchQuery);
                            }

                            //user has selected to edit a subject
                            if (option == 1) {
                                //show Dialog so user can can change name of subject
                                AlertDialog.Builder editSubjectName = new AlertDialog.Builder(SearchResults.this);
                                editSubjectName.setTitle("Subject Name:");
                                editSubjectName.setMessage("Type in a new name for the subject:");

                                //set EditText view to get user input for new subject name
                                final EditText newSubjectNameData = new EditText(SearchResults.this);
                                newSubjectNameData.setText(subjectName);
                                editSubjectName.setView(newSubjectNameData);

                                //if user pressed "cancel" then dismiss the dialogue and do NOT update the subject name
                                editSubjectName.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });

                                //call database method to update current subject name with the new subject name
                                editSubjectName.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        //create instance of database helper to get data from database
                                        Database.DatabaseOpenHelper helper;
                                        helper = new Database.DatabaseOpenHelper(SearchResults.this);

                                        String newSubjectName = newSubjectNameData.getText().toString();

                                        //update current subject name in database with new subject name
                                        helper.updateSubjectName(subjectName, newSubjectName);

                                        //update subject list with new subject name
                                        updateSubjectsResultsList(searchQuery);
                                    }
                                });

                                AlertDialog editSubjectBuilder = editSubjectName.create();

                                //display edit subject dialogue
                                editSubjectBuilder.show();
                            }
                        }
                    });

                    //show subject options dialogue
                    AlertDialog subjectOptionsDialog = subjectSelected.create();
                    subjectOptionsDialog.show();
                }
                return true;
            }
        });

        //set listener for item clicks.  When the user clicks a subject, they will be taken to the SubjectScreen activity
        //where they can view all of the notes associated with a subject
        subjectResultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //get title of subject that was selected
                //do not allow for item clicks if "No Results Were Found" is displayed
                if (!parent.getItemAtPosition(position).toString().equals("No Results Were Found")) {
                    subjectName = (String) parent.getItemAtPosition(position);

                    //call SubjectScreen activity
                    Intent SubjectScreen = new Intent(getApplicationContext(), SubjectScreen.class);
                    SubjectScreen.putExtra("subject", subjectName);
                    startActivity(SubjectScreen);
                }
            }
        });

    }

    /*
     * Method executed when the user is searching for key term or set of words
     * Searches the note text and names in the database and returns any results found that contain
     * the keyword(s) the user is searching for.
     * "No Results Were Found" is displayed if there are no results returned from the database search query.
     */
    public void searchDatabaseNoteText(final String searchQuery) {
        ArrayList<String> noteSearchName;
        noteResultsList = (ListView) findViewById(R.id.noteTextResultsList);

        //create database helper for searching database
        final Database.DatabaseOpenHelper helper = new Database.DatabaseOpenHelper(this);

        //search the note text and names in the database for the query the user has typed
        noteSearchResults = helper.getNoteSearchText(searchQuery);
        noteSearchName = helper.getNoteSearchName(searchQuery);

        //check if there were any note text results and if so then display them on the screen
        if (!noteSearchResults.isEmpty()) {
            //noteList to display note results from the database
            noteSearchAdapter = new ArrayAdapter <> (SearchResults.this, android.R.layout.simple_list_item_1, noteSearchResults);
            noteResultsList.setAdapter(noteSearchAdapter);
        }

        //since the search will also search note names based on the query the user entered,
        //add the names of the notes to the list if any results were found
        if (!noteSearchName.isEmpty()) {
            for(int i = 0; i < noteSearchName.size(); i++)
            {
                //ONLY ADD THE NOTE IF THE RESULT LIST DOES NOT ALREADY CONTAIN IT
                if(!noteSearchResults.contains(noteSearchName.get(i))) {
                    noteSearchResults.add(noteSearchName.get(i));
                }
            }
        }

        //there were no results so display an error message
        if((noteSearchName.isEmpty()) && (noteSearchResults.isEmpty())) {
            //display error in note results list
            noteSearchResults.add("No Results Were Found");
        }

        noteSearchAdapter = new ArrayAdapter <> (SearchResults.this, android.R.layout.simple_list_item_1, noteSearchResults);
        noteResultsList.setAdapter(noteSearchAdapter);

        //options for note options dialogue box displayed when user long presses a note
        final CharSequence[] noteOptions = {"Delete", "Email", "Edit"};

        //set on item long click listener where long clicks are used when the user wants to delete or edit a subject
        noteResultsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //get title of note that was selected
                //do not allow options if "No Results Were Found" is displayed
                if (!parent.getItemAtPosition(position).toString().equals("No Results Were Found")) {

                    noteName = (String) parent.getItemAtPosition(position);

                    //if a note was long pressed then display a dialogue box with list asking if they want to delete, email, or edit note
                    //Initialize dialogue box
                    AlertDialog.Builder noteSelected = new AlertDialog.Builder(SearchResults.this);

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
                                Toast.makeText(SearchResults.this, "Note Deleted", Toast.LENGTH_LONG).show();

                                //get reminder the note is associated with from the database
                                String reminderName = helper.getReminderName(noteName);

                                //remove reminder associated with deleted note from the database and update the reminder list
                                helper.deleteReminder(reminderName);

                                //remove selected note from the database and update the note list
                                helper.deleteNote(noteName);

                                //inform the subject list of the changes
                                updateSubjectsResultsList(searchQuery);

                                //inform the note list of the changes
                                updateNotesResultsList(searchQuery);

                                //inform the reminders list of the changes
                                updateRemindersResultsList(searchQuery);
                            }

                            //user has selected to email a note
                            if (option == 1) {
                                //get email address to send notes to, if it is null, then email functionality is disabled
                                SharedPreferences emailPrefs = PreferenceManager.getDefaultSharedPreferences(SearchResults.this);
                                if (emailPrefs != null) {
                                    //get note text from database to pass as email body
                                    sendText = helper.getNoteText(noteName);

                                    sendEmail = emailPrefs.getString("email_send", "email");
                                    if (!sendEmail.equals("email")) {
                                        //method call to email select note
                                        emailNote(sendEmail, noteName, sendText);
                                    } else {
                                        //disable email functionality
                                        Toast.makeText(SearchResults.this, "Email has not been configured!", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    //disable email functionality
                                    Toast.makeText(SearchResults.this, "Email has not been configured!", Toast.LENGTH_LONG).show();
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
        noteResultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //do not allow for item clicks if "No Results Were Found" is displayed
                if (!parent.getItemAtPosition(position).toString().equals("No Results Were Found")) {
                    //get name of note to pass into note screen to act as a header
                    final String noteName = (String) parent.getItemAtPosition(position);

                    //method call to pass data into note edit screen for when a note is pressed
                    editViewNote(noteName);
                }
            }
        });
    }

    /*
     * Method executed when the user is searching for key term or set of words
     * Searches the reminders in the database and returns any results found that contain
     * the keyword(s) the user is searching for.
     * "No Results Were Found" is displayed if there are no results returned from the database search query.
     */
    public void searchDatabaseReminders(final String searchQuery) {
        reminderResultsList = (ListView) findViewById(R.id.reminderResultsList);

        //create database helper for searching database
        final Database.DatabaseOpenHelper helper = new Database.DatabaseOpenHelper(this);

        //search the reminders in the database for the query the user has typed
        reminderSearchResults = helper.getReminderSearchName(searchQuery);

        //check if there were any reminder results and if so then display them on the screen
        if (!reminderSearchResults.isEmpty()) {
            //reminderList to display reminder results from the database
            reminderSearchAdapter = new ArrayAdapter <> (SearchResults.this, android.R.layout.simple_list_item_1, reminderSearchResults);
            reminderResultsList.setAdapter(reminderSearchAdapter);
        }

        //there were no results so display an error message
        if(reminderSearchResults.isEmpty()) {
            //display error in reminder results list
            reminderSearchResults.add("No Results Were Found");
        }

        reminderSearchAdapter = new ArrayAdapter <> (SearchResults.this, android.R.layout.simple_list_item_1, reminderSearchResults);
        reminderResultsList.setAdapter(reminderSearchAdapter);

        //options for reminder options dialogue box displayed when user long presses a reminder
        final CharSequence[] reminderOptions = {"Delete", "Edit"};

        //set listener for item clicks.  When the user clicks a reminder, a dialogue box will appear that will let
        //them edit or delete a reminder
        reminderResultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //get name of note to set as title of reminders options dialogue box
                //do not allow options if "No Results Were Found" is displayed
                if (!parent.getItemAtPosition(position).toString().equals("No Results Were Found")) {
                    final String reminderData = (String) parent.getItemAtPosition(position);
                    reminderDialogTitle = reminderData.split("\n");

                    //create dialogue box to allow user to edit and delete a reminder
                    AlertDialog.Builder reminderDialogBuilder = new AlertDialog.Builder(SearchResults.this);

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
                                Toast.makeText(SearchResults.this, "Reminder Deleted", Toast.LENGTH_LONG).show();

                                //create instance of database helper to get data from database
                                Database.DatabaseOpenHelper helper;
                                helper = new Database.DatabaseOpenHelper(SearchResults.this);

                                //delete reminder from the database and update reminder list
                                helper.deleteReminder(reminderDialogTitle[0]);

                                //inform the reminders list of the changes
                                updateRemindersResultsList(searchQuery);

                                //inform the subject list of the changes
                                updateSubjectsResultsList(searchQuery);

                                //inform the notes list of the changes
                                updateNotesResultsList(searchQuery);

                                //delete reminder from notification manager so notification will not display
                                HomeScreen.cancelAlarm(getApplicationContext());
                            }

                            //user has selected to edit a reminder
                            if (option == 1) {
                                //create dialogue box for user to edit selected reminder reminder
                                AlertDialog.Builder createEditReminderDialogBuilder = new AlertDialog.Builder(SearchResults.this);
                                createEditReminderDialogBuilder.setTitle("New Reminder Name:");
                                createEditReminderDialogBuilder.setMessage("Type a new name for the reminder.");
                                createEditReminderDialogBuilder.setCancelable(false);

                                //set EditText view to get user input for new reminder name
                                final EditText newReminderNameData = new EditText(SearchResults.this);
                                newReminderNameData.setText(reminderDialogTitle[0]);
                                createEditReminderDialogBuilder.setView(newReminderNameData);

                                createEditReminderDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        newReminderName = newReminderNameData.getText().toString();
                                        if (newReminderName.equals(""))
                                            Toast.makeText(SearchResults.this, "You must enter a reminder name to edit the reminder", Toast.LENGTH_LONG).show();
                                        else {
                                            dialog.dismiss();

                                            HomeScreen.setNotificationName(newReminderName);

                                            //get the note name associated with the reminder for getting the time and date data of the note from database
                                            noteName = helper.getNoteName(reminderDialogTitle[0]);

                                            //method call to initialize reminder time and date pickers
                                            initializeReminder(SearchResults.this, noteName);

                                            //method call to send noteName to notification for displaying notes when
                                            //notification is clicked by user
                                            HomeScreen.setNotificationNoteName(noteName);

                                            //initialize date picker values to the old reminder date
                                            Calendar oldReminderDate = Calendar.getInstance();
                                            oldReminderDate.set(reminderYear, reminderMonth, reminderDay);

                                            //show Date Picker Dialog so user can select date for new reminder
                                            DatePickerDialog setReminderDate = new DatePickerDialog(SearchResults.this, datePickerListener, reminderYear, reminderMonth, reminderDay);
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
                    reminderOptionAlert.setTitle("Reminder Options for " + reminderDialogTitle[0]);

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
                final TimePickerDialog setReminderTime = new TimePickerDialog(SearchResults.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        //to avoid method and values being set/called twice
                        if (view.isShown()) {
                            //store the time the user has entered into variables to be passed to setNotification method in HomeScreen to set reminder
                            reminderHour = hourOfDay;
                            reminderMinute = minute;

                            //remove current set notification time and replace with new one
                            HomeScreen.cancelAlarm(SearchResults.this);
                            HomeScreen.setNotification(reminderYear, reminderMonth, reminderDay, reminderHour, reminderMinute);

                            //update reminder date and time into the database
                            Database.DatabaseOpenHelper helper;
                            helper = new Database.DatabaseOpenHelper(SearchResults.this);
                            helper.updateReminder(noteName, reminderDialogTitle[0], newReminderName, reminderYear, reminderMonth, reminderDay, reminderHour, reminderMinute);

                            //update remindersList with changes
                            updateRemindersResultsList(searchQuery);

                            Toast.makeText(SearchResults.this, "Reminder Updated", Toast.LENGTH_LONG).show();
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
    public void onBackPressed() {
        //go back to the HomeScreen
        Intent HomeScreen = new Intent(getApplicationContext(), HomeScreen.class);
        startActivity(HomeScreen);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_results, menu);
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

    public void updateSubjectsResultsList(String searchQuery)
    {
        //create database helper for searching database
        Database.DatabaseOpenHelper helper = new Database.DatabaseOpenHelper(this);

        subjectSearchAdapter.clear();
        //search the subjects in the database for the query the user has typed
        subjectSearchResults = helper.getSubjectSearchName(searchQuery);

        //check if there were any subject results and if so then display them on the screen
        if (!subjectSearchResults.isEmpty()) {
            //subjectList to display subject results from the database
            subjectSearchResults.clear();
            subjectSearchResults.addAll(subjectSet);
            subjectSearchAdapter = new ArrayAdapter <> (SearchResults.this, android.R.layout.simple_list_item_1, subjectSearchResults);
            subjectResultsList.setAdapter(subjectSearchAdapter);
        }
    }

    public void updateNotesResultsList(String searchQuery)
    {
        //create database helper for searching database
        Database.DatabaseOpenHelper helper = new Database.DatabaseOpenHelper(this);

        noteSearchAdapter.clear();
        //search the subjects in the database for the query the user has typed
        noteSearchResults = helper.getNoteSearchName(searchQuery);

        //check if there were any note results and if so then display them on the screen
        if (!noteSearchResults.isEmpty()) {
            //notesList to display note results from the database
            noteSearchAdapter = new ArrayAdapter<>(SearchResults.this, android.R.layout.simple_list_item_1, noteSearchResults);
            noteResultsList.setAdapter(noteSearchAdapter);
        }
    }

    public void updateRemindersResultsList(String searchQuery)
    {
        //create database helper for searching database
        Database.DatabaseOpenHelper helper = new Database.DatabaseOpenHelper(this);

        reminderSearchAdapter.clear();
        //search the reminders in the database for the query the user has typed
        reminderSearchResults = helper.getReminderSearchName(searchQuery);

        //check if there were any reminder results and if so then display them on the screen
        if (!reminderSearchResults.isEmpty()) {
            //reminderList to display reminder results from the database
            reminderSearchAdapter = new ArrayAdapter <> (SearchResults.this, android.R.layout.simple_list_item_1, reminderSearchResults);
            reminderResultsList.setAdapter(reminderSearchAdapter);
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

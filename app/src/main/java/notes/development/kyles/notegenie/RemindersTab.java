package notes.development.kyles.notegenie;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;
import notes.development.kyles.notegenie.util.Database;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class RemindersTab extends Fragment {
    static View rootView;
    AlertDialog reminderOptionAlert;
    AlertDialog editReminderAlert;
    static ArrayAdapter<String> remindersAdapter;
    static ListView remindersList;
    private String newReminderName;
    private String noteName;
    private String[] reminderDialogTitle;
    private int reminderYear;
    private int reminderMonth;
    private int reminderDay;
    private int reminderHour;
    private int reminderMinute;

    //Get the subjects fragment to display subjects when the "subjects" tab is selected on the home screen.

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_reminders, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //create instance of database helper to get data from database
        final Database.DatabaseOpenHelper helper;

        //database helper to get data from database
        helper = new Database.DatabaseOpenHelper(getActivity());

        //remindersList to display reminders from the database
        remindersList = (ListView) rootView.findViewById(R.id.remindersList);

        //create and set the reminders list to display the user's reminders
        ArrayList<String> reminders;
        reminders = helper.getReminders();

        //if there are no reminders, display message
        if(reminders.isEmpty())
            reminders.add("You have no reminders yet.  Let's get started, tap the alarm icon in the upper right corner when editing/creating a note to create a reminder.");

        remindersAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, reminders);
        remindersList.setAdapter(remindersAdapter);

        //options for reminder options dialogue box displayed when user long presses a reminder
        final CharSequence[] reminderOptions = {"Delete", "Edit"};

        //set listener for item clicks.  When the user clicks a reminder, a dialogue box will appear that will let
        //them edit or delete a reminder
        remindersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(!parent.getItemAtPosition(position).equals("You have no reminders yet.  Let's get started, tap the alarm icon in the upper right corner when editing/creating a note to create a reminder.")) {
                    //get name of note to set as title of reminders options dialogue box
                    final String reminderData = (String) parent.getItemAtPosition(position);
                    reminderDialogTitle = reminderData.split("\n");

                    //create dialogue box to allow user to edit and delete a reminder
                    AlertDialog.Builder reminderDialogBuilder = new AlertDialog.Builder(getActivity());

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
                                Toast.makeText(getActivity(), "Reminder Deleted", Toast.LENGTH_LONG).show();

                                //create instance of database helper to get data from database
                                Database.DatabaseOpenHelper helper;
                                helper = new Database.DatabaseOpenHelper(getActivity());

                                //delete reminder from the database and update reminder list
                                helper.deleteReminder(reminderDialogTitle[0]);

                                //inform the reminders list of the changes
                                updateReminderList(getActivity().getApplicationContext());

                                //inform the subject list of the changes
                                SubjectTab.updateSubjectList();

                                //inform the notes list of the changes
                                NotesTab.updateNoteList();

                                //delete reminder from notification manager so notification will not display
                                HomeScreen.cancelAlarm(getActivity().getApplicationContext());
                            }

                            //user has selected to edit a reminder
                            if (option == 1) {
                                //create dialogue box for user to edit selected reminder reminder
                                AlertDialog.Builder createEditReminderDialogBuilder = new AlertDialog.Builder(getActivity());
                                createEditReminderDialogBuilder.setTitle("New Reminder Name:");
                                createEditReminderDialogBuilder.setMessage("Type a new name for the reminder.");
                                createEditReminderDialogBuilder.setCancelable(false);

                                //set EditText view to get user input for new reminder name
                                final EditText newReminderNameData = new EditText(getActivity());
                                newReminderNameData.setText(reminderDialogTitle[0]);
                                createEditReminderDialogBuilder.setView(newReminderNameData);

                                createEditReminderDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        newReminderName = newReminderNameData.getText().toString();
                                        if (newReminderName.equals(""))
                                            Toast.makeText(getActivity().getApplicationContext(), "You must enter a reminder name to edit the reminder", Toast.LENGTH_LONG).show();
                                        else {
                                            dialog.dismiss();

                                            HomeScreen.setNotificationName(newReminderName);

                                            //get the note name associated with the reminder for getting the time and date data of the note from database
                                            noteName = helper.getNoteName(reminderDialogTitle[0]);

                                            //method call to initialize reminder time and date pickers
                                            initializeReminder(getActivity().getApplicationContext(), noteName);

                                            //method call to send noteName to notification for displaying notes when
                                            //notification is clicked by user
                                            HomeScreen.setNotificationNoteName(noteName);

                                            //initialize date picker values to the old reminder date
                                            Calendar oldReminderDate = Calendar.getInstance();
                                            oldReminderDate.set(reminderYear, reminderMonth, reminderDay);

                                            //show Date Picker Dialog so user can select date for new reminder
                                            DatePickerDialog setReminderDate = new DatePickerDialog(getActivity(), datePickerListener, reminderYear, reminderMonth, reminderDay);
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
                final TimePickerDialog setReminderTime = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        //to avoid method and values being set/called twice
                        if (view.isShown()) {
                            //store the time the user has entered into variables to be passed to setNotification method in HomeScreen to set reminder
                            reminderHour = hourOfDay;
                            reminderMinute = minute;

                            //remove current set notification time and replace with new one
                            HomeScreen.cancelAlarm(getActivity().getApplicationContext());
                            HomeScreen.setNotification(reminderYear, reminderMonth, reminderDay, reminderHour, reminderMinute);

                            //update reminder date and time into the database
                            Database.DatabaseOpenHelper helper;
                            helper = new Database.DatabaseOpenHelper(getActivity().getApplicationContext());
                            helper.updateReminder(noteName, reminderDialogTitle[0], newReminderName, reminderYear, reminderMonth, reminderDay, reminderHour, reminderMinute);

                            //update remindersList with changes
                            updateReminderList(getActivity().getApplicationContext());

                            Toast.makeText(getActivity().getApplicationContext(), "Reminder Updated", Toast.LENGTH_LONG).show();
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

    public static void updateReminderList(Context context)
    {
        //create instance of database helper to get data from database
        Database.DatabaseOpenHelper helper;
        helper = new Database.DatabaseOpenHelper(context);

        ArrayList <String> newReminders;
        newReminders = helper.getReminders();

        //if there are no notes, display message
        if(newReminders.isEmpty())
            newReminders.add("You have no reminders yet.  Let's get started, tap the alarm icon in the upper right corner when editing/creating a note to create a reminder.");

        if (remindersAdapter != null) {
            remindersAdapter.clear();
            remindersAdapter.addAll(newReminders);
            ((BaseAdapter)remindersList.getAdapter()).notifyDataSetChanged();
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

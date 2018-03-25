package notes.development.kyles.notegenie;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.SharedPreferences;
import notes.development.kyles.notegenie.util.Database;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import android.widget.AdapterView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;
import android.content.Intent;


public class NotesTab extends Fragment {
    static View rootView;
    String sendEmail;
    String sendText;
    static ArrayAdapter <String> noteAdapter;
    static ListView noteList;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Get the subjects fragment to display subjects when the "subjects" tab is selected on the home screen.
        rootView = inflater.inflate(R.layout.fragment_notes, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //create instance of database helper to get data from database
        final Database.DatabaseOpenHelper helper;

        //database helper to get data from database
        helper = new Database.DatabaseOpenHelper(getActivity());

        //options for note options dialogue box displayed when user long presses a note
        final CharSequence[] noteOptions = {"Delete", "Email", "Edit"};

        //noteList to display notes from the database
        noteList = (ListView) rootView.findViewById(R.id.noteList);

        //create and set the note list to display the user's notes
        ArrayList <String> notes;
        notes = helper.getNotes();

        //Do not add a note with blank data to the list if the user chose not to create a note with a subject
        for(int i = 0; i < notes.size(); i++){
            if(notes.get(i).equals(""))
                notes.remove(i);
        }

        //if there are no notes, display message
        if(notes.isEmpty())
            notes.add("You have no notes yet.  Let's get started, tap the create note icon in the upper right corner to create a note.");

        noteAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, notes);
        noteList.setAdapter(noteAdapter);

        //set listener for item Long clicks.  Long clicks are used when the user wants to delete, email, or edit a note.
        noteList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(!parent.getItemAtPosition(position).equals("You have no notes yet.  Let's get started, tap the create note icon in the upper right corner to create a note.")) {
                    //get title of note that was selected
                    final String noteName = (String) parent.getItemAtPosition(position);

                    //if a note was long pressed then display a dialogue box with list asking if they want to delete, email, or edit note
                    //Initialize dialogue box
                    AlertDialog.Builder noteSelected = new AlertDialog.Builder(getActivity());

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
                                Toast.makeText(getActivity(), "Note Deleted", Toast.LENGTH_LONG).show();

                                //create instance of database helper to get data from database
                                Database.DatabaseOpenHelper helper;
                                helper = new Database.DatabaseOpenHelper(getActivity());

                                //get reminder the note is associated with from the database
                                String reminderName = helper.getReminderName(noteName);

                                //remove reminder associated with deleted note from the database and update the reminder list
                                helper.deleteReminder(reminderName);

                                //remove selected note from the database and update the note list
                                helper.deleteNote(noteName);

                                //inform the note list of the changes
                                updateNoteList();

                                //inform the subject list of the changes
                                SubjectTab.updateSubjectList();

                                //inform the reminders list of the changes
                                RemindersTab.updateReminderList(getActivity().getApplicationContext());
                            }

                            //user has selected to email a note
                            if (option == 1) {
                                //get email address to send notes to, if it is null, then email functionality is disabled
                                SharedPreferences emailPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                                if (emailPrefs != null) {
                                    //get note text from database to pass as email body
                                    sendText = helper.getNoteText(noteName);

                                    sendEmail = emailPrefs.getString("email_send", "email");
                                    if (!sendEmail.equals("email")) {
                                        //method call to email select note
                                        emailNote(sendEmail, noteName, sendText);
                                    } else {
                                        //disable email functionality
                                        Toast.makeText(getActivity(), "Email has not been configured!", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    //disable email functionality
                                    Toast.makeText(getActivity(), "Email has not been configured!", Toast.LENGTH_LONG).show();
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
        noteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(!parent.getItemAtPosition(position).equals("You have no notes yet.  Let's get started, tap the create note icon in the upper right corner to create a note.")) {
                    //get name of note to pass into note screen to act as a header
                    final String noteName = (String) parent.getItemAtPosition(position);

                    //method call to pass data into note edit screen for when a note is pressed
                    editViewNote(noteName);
                }
            }
        });
    }

    /*
     * Method executed when the user has selected a note or long pressed a note and selected the option to edit a note.
     * Call the NoteScreen activity which will display the note stored in the database to the user.
     */
    public void editViewNote(String noteName)
    {
        //call note screen activity and pass name of note so user can edit/see contents of note that is selected from list
        Intent displayNote = new Intent(getActivity(), NoteScreen.class);
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
            Toast.makeText(getActivity(), "Can't send mail.  There are no mail application installed on device.", Toast.LENGTH_LONG).show();
        }
    }

    public static void updateNoteList()
    {
        //create instance of database helper to get data from database
        Database.DatabaseOpenHelper helper;
        Activity NotesTab = (Activity) rootView.getContext();
        helper = new Database.DatabaseOpenHelper(NotesTab);

        ArrayList <String> newNotes;
        newNotes = helper.getNotes();

        //if there are no notes, display message
        if(newNotes.isEmpty())
            newNotes.add("You have no notes yet.  Let's get started, tap the create note icon in the upper right corner to create a note.");

        if (noteAdapter != null) {
            noteAdapter.clear();
            noteAdapter.addAll(newNotes);
            ((BaseAdapter) noteList.getAdapter()).notifyDataSetChanged();
        }
    }
}
package notes.development.kyles.notegenie;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import notes.development.kyles.notegenie.util.Database;
import notes.development.kyles.notegenie.util.Database.DatabaseOpenHelper;

public class SubjectTab extends Fragment {
    static View rootView;
    static ArrayAdapter <String> subjectAdapter;
    static ListView subjectList;
    private String subjectName;
    private String subjectReminders;
    private ArrayList<String> subjectNotes;

    //Get the subjects fragment to display subjects when the "subjects" tab is selected on the home screen.
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_subjects, container, false);
        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Set<String> subjectSet;

        //create instance of database helper to get data from database
        DatabaseOpenHelper helper;

        helper = new DatabaseOpenHelper(getActivity());
        subjectList = (ListView) rootView.findViewById(R.id.subjectList);

        //options for subject options dialogue box displayed when user long presses a subject
        final CharSequence[] subjectOptions = {"Delete", "Edit"};

        ArrayList <String> subjects;
        subjects = helper.getNoteSubjects();

        //if there are no subjects, display message
        if(subjects.isEmpty())
            subjects.add("You have no subjects yet.  Let's get started, tap the create note icon in the upper right corner to create a subject.");

        //remove any duplicate items from list by adding to set and then adding back to subjects ArrayList
        subjectSet = new HashSet<>();
        subjectSet.addAll(subjects);
        subjects.clear();
        subjects.addAll(subjectSet);

        subjectAdapter = new ArrayAdapter <> (getActivity(), android.R.layout.simple_list_item_1, subjects);
        subjectList.setAdapter(subjectAdapter);

        //set listener for item Long clicks.  Long clicks are used when the user wants to delete or edit a subject
        subjectList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(!parent.getItemAtPosition(position).equals("You have no subjects yet.  Let's get started, tap the create note icon in the upper right corner to create a subject.")) {
                    //get title of subject that was selected
                    subjectName = (String) parent.getItemAtPosition(position);

                    //if a subject was long pressed then display a dialogue box with list asking if they want to delete or edit the subject
                    //Initialize dialogue box
                    final AlertDialog.Builder subjectSelected = new AlertDialog.Builder(getActivity());

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
                                Toast.makeText(getActivity(), "Subject Deleted", Toast.LENGTH_LONG).show();

                                //create instance of database helper to get data from database
                                Database.DatabaseOpenHelper helper;
                                helper = new Database.DatabaseOpenHelper(getActivity());

                                //get the notes the subject is associated with from the database
                                subjectNotes = helper.getSubjectNotes(subjectName);

                                //delete all the reminders associated with all notes under the subject
                                for (int i = 0; i < subjectNotes.size(); i++) {
                                    //remove reminder associated with each note under the subject from the database and update the reminder list
                                    subjectReminders = helper.getReminderName(subjectNotes.get(i));
                                    helper.deleteReminder(subjectReminders);
                                }

                                //remove selected subject from the database and update the subject list
                                helper.deleteSubject(subjectName);

                                //inform the subject list of the changes
                                updateSubjectList();

                                //inform the note list of the changes
                                NotesTab.updateNoteList();

                                //inform the reminders list of the changes
                                RemindersTab.updateReminderList(getActivity().getApplicationContext());
                            }

                            //user has selected to edit a subject
                            if (option == 1) {
                                //show Dialog so user can can change name of subject
                                AlertDialog.Builder editSubjectName = new AlertDialog.Builder(getActivity());
                                editSubjectName.setTitle("Subject Name:");
                                editSubjectName.setMessage("Type in a new name for the subject:");

                                //set EditText view to get user input for new subject name
                                final EditText newSubjectNameData = new EditText(getActivity());
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
                                        DatabaseOpenHelper helper;
                                        helper = new DatabaseOpenHelper(getActivity());

                                        String newSubjectName = newSubjectNameData.getText().toString();

                                        //update current subject name in database with new subject name
                                        helper.updateSubjectName(subjectName, newSubjectName);

                                        //update subject list with new note name
                                        updateSubjectList();
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
        subjectList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!parent.getItemAtPosition(position).equals("You have no subjects yet.  Let's get started, tap the create note icon in the upper right corner to create a subject.")) {
                    //get title of subject that was selected
                    subjectName = (String) parent.getItemAtPosition(position);

                    //call SubjectScreen activity
                    Intent SubjectScreen = new Intent(getActivity(), SubjectScreen.class);
                    SubjectScreen.putExtra("subject", subjectName);
                    startActivity(SubjectScreen);
                }
            }
        });
    }

    public static void updateSubjectList()
    {
        //create instance of database helper to get data from database
        DatabaseOpenHelper helper;
        Activity SubjectTab = (Activity) rootView.getContext();
        helper = new DatabaseOpenHelper(SubjectTab);

        ArrayList <String> newSubjects;
        newSubjects = helper.getNoteSubjects();

        //if there are no notes, display message
        if(newSubjects.isEmpty())
            newSubjects.add("You have no subjects yet.  Let's get started, tap the create note icon in the upper right corner to create a note.");

        if (subjectAdapter != null) {
            subjectAdapter.clear();
            subjectAdapter.addAll(newSubjects);
            ((BaseAdapter) subjectList.getAdapter()).notifyDataSetChanged();
        }
    }
}

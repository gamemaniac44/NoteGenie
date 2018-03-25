package notes.development.kyles.notegenie.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database{

    private static final String TAG = "Notes Database";

    //columns of the database
    public static final String COL_SUBJECT = "SUBJECT";
    public static final String COL_NOTENAME = "NOTENAME";
    public static final String COL_NOTETEXT = "NOTETEXT";
    public static final String COL_REMINDER_NAME = "REMINDER_NAME";
    public static final String COL_REMINDER_DATE = "REMINDER_DATE";
    public static final String COL_REMINDER_TIME = "REMINDER_TIME";

    //name of the database
    private static final String DATABASE_NAME = "NOTES_DATA";

    //table names
    private static final String FTS_VIRTUAL_NOTES_TABLE = "NOTES";
    private static final String FTS_VIRTUAL_REMINDERS_TABLE = "REMINDERS";

    //database version for upgrading
    private static final int DATABASE_VERSION = 1;

    //String variable to hold text of note for setting edit note text box anc checking for reminders
    public static String noteText;

    //String variables to hold data for reminders
    public static String reminderData;
    public static String reminderName;
    public static String reminderDate;
    public static String reminderTime;

    //ArrayList to hold reminder keywords for prompting user to create a reminder
    public static List<String> reminderKeys = Arrays.asList("quiz", "test", "remember", "study", "monday",
            "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "due", "project", "presentation");

    private static DatabaseOpenHelper mDatabaseOpenHelper;

    public Database(Context context) {
        mDatabaseOpenHelper = new DatabaseOpenHelper(context);
    }

    public static class DatabaseOpenHelper extends SQLiteOpenHelper{

        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;

        String CREATE_NOTES_TABLE = "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_NOTES_TABLE + " USING fts3 (" +
                COL_SUBJECT + " text, " +
                COL_NOTENAME + " text PRIMARY KEY, " +
                COL_NOTETEXT + " text);";

        String CREATE_REMINDERS_TABLE = "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_REMINDERS_TABLE + " USING fts3 (" +
                COL_NOTENAME + " text, " +
                COL_REMINDER_NAME + " text, " +
                COL_REMINDER_DATE + " text, " +
                COL_REMINDER_TIME + " text, " +
                " FOREIGN KEY (" + COL_NOTENAME + ") REFERENCES " + FTS_VIRTUAL_NOTES_TABLE + "(" + COL_NOTENAME + ") " +
                " ON DELETE CASCADE);";

        /*
         * Helper to aid in upgrading and maintenance of database when updates to the application are deployed.
         */
        public DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        /*
         * This method is executed when the database is first created, often when the application is first installed on the phone. (non-Javadoc)
         * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;

            //create the virtual tables in the database
            mDatabase.execSQL(CREATE_NOTES_TABLE);
            mDatabase.execSQL(CREATE_REMINDERS_TABLE);

            //insert sample data into the virtual table
            insertData(mDatabase);
        }

        /*
         * This method is executed when the database needs to be upgraded to a new version.
         * This often occurs when future updates are deployed to the application.
         * Checks the old version with the new version.  (non-Javadoc)
         * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_NOTES_TABLE + FTS_VIRTUAL_REMINDERS_TABLE);
            onCreate(db);
        }

        @Override
        //enables foreign keys for delting data in database
        public void onConfigure(SQLiteDatabase db) {
            db.setForeignKeyConstraintsEnabled(true);
        }

        /*
         * This method takes in parameters from calling activity that contain the name of the note, subject name, as well
         * as the content of the note.
         * Method when called, will insert all of the note data into the database.
         */
        public void insertNote(String noteName, String subjectName, String noteText) {
            SQLiteDatabase db = this.getReadableDatabase();

            ContentValues newValues = new ContentValues();

            //assign values to database rows
            newValues.put(COL_SUBJECT, subjectName);
            newValues.put(COL_NOTENAME, noteName);
            newValues.put(COL_NOTETEXT, noteText);

            db.insert(FTS_VIRTUAL_NOTES_TABLE, null, newValues);
            System.out.println("New Note Inserted Into Database With Values:"
                + "\nSubject Name:  " + subjectName
                + "\nNote Name:  " + noteName
                + "\nNote Text:  " + noteText);
        }

        /*
         * This method takes in parameters from calling activity that contain the name of the reminder, reminder date,
         * and reminder time.
         * Method when called, will insert all of the reminder data into the database.
         * REMINDERS WILL ONLY BE INSERTED INTO THE DATABASE IF THE USER HAS SET ONE
         */
        public void insertReminder(String noteName, String reminderName, int reminderYear, int reminderMonth, int reminderDay, int reminderHour, int reminderMinute) {
            SQLiteDatabase db = this.getReadableDatabase();

            ContentValues newValues = new ContentValues();

            String AM_PM;
            String hour;
            String minute;

            //convert int month values to string months
            String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
            String month = months[reminderMonth];

            //convert int day value to string day
            String day = String.valueOf(reminderDay);

            //convert int year value to string year
            String year = String.valueOf(reminderYear);

            //create formatted date string for insertion into database
            String date = month + " " + day + ", " + year;

            //convert int hour value to string based on 12 hour time with AM and PM
            if (reminderHour >= 12) {
                hour = String.valueOf(reminderHour - 12);
                AM_PM = "PM";
            }
            else {
                hour = String.valueOf(reminderHour);
                AM_PM = "AM";
            }

            //if the reminder minute time is less than 10 (for example 3:03) then add a 0 in front of minute value
            //to avoid reminder list showing 3:3 as the time
            if(reminderMinute < 10){
                minute = String.valueOf(reminderMinute);
                minute = "0" + minute;
            }

            else
                minute = String.valueOf(reminderMinute);

            //create formatted time string for insertion into database
            String time = hour + ":" + minute + " " + AM_PM;

            //assign values to database rows
            newValues.put(COL_NOTENAME, noteName);
            newValues.put(COL_REMINDER_NAME, reminderName);
            newValues.put(COL_REMINDER_DATE, date);
            newValues.put(COL_REMINDER_TIME, time);

            db.insert(FTS_VIRTUAL_REMINDERS_TABLE, null, newValues);

            System.out.println("New Reminder Inserted Into Database With Values:"
                    + "\nNote Name:  " + noteName
                    + "\nReminder Name:  " + reminderName
                    + "\nReminder Date:  " + date
                    + "\nReminder Time:  " + time);
        }

        /*
         * This method inserts data into the database.
         */
        void insertData(SQLiteDatabase mDatabase)
        {
            /*
            ContentValues cv = new ContentValues();

            cv.put(COL_SUBJECT, "csci");
            cv.put(COL_NOTENAME, "Operating Systems Boot");
            cv.put(COL_NOTETEXT, "This is the Operating Systems Note");
            mDatabase.insert(FTS_VIRTUAL_NOTES_TABLE, null, cv);
            cv.clear();
            cv.put(COL_NOTENAME, "Operating Systems Boot");
            cv.put(COL_REMINDER_NAME, "OS quiz on Monday!");
            cv.put(COL_REMINDER_DATE, "March 16, 2015");
            cv.put(COL_REMINDER_TIME, "1:00 PM");
            mDatabase.insert(FTS_VIRTUAL_REMINDERS_TABLE, null, cv);



            cv.clear();
            cv.put(COL_SUBJECT, "English");
            cv.put(COL_NOTENAME, "English Day 1");
            cv.put(COL_NOTETEXT, "This is the English Day 1 Note");
            mDatabase.insert(FTS_VIRTUAL_NOTES_TABLE, null, cv);
            cv.clear();
            cv.put(COL_NOTENAME, "English Day 1");
            cv.put(COL_REMINDER_NAME, "English Test Next Week!");
            cv.put(COL_REMINDER_DATE, "February 3, 2015");
            cv.put(COL_REMINDER_TIME, "2:00 PM");
            mDatabase.insert(FTS_VIRTUAL_REMINDERS_TABLE, null, cv);

            cv.clear();
            cv.put(COL_SUBJECT, "Math");
            cv.put(COL_NOTENAME, "PreCalc - Functions");
            cv.put(COL_NOTETEXT, "This is the PreCalc Notes");
            mDatabase.insert(FTS_VIRTUAL_NOTES_TABLE, null, cv);
            cv.clear();
            cv.put(COL_NOTENAME, "PreCalc - Functions");
            cv.put(COL_REMINDER_NAME, "Math quiz next class");
            cv.put(COL_REMINDER_DATE, "March 10, 2015");
            cv.put(COL_REMINDER_TIME, "11:00 AM");
            mDatabase.insert(FTS_VIRTUAL_REMINDERS_TABLE, null, cv);

            cv.clear();
            cv.put(COL_SUBJECT, "Business");
            cv.put(COL_NOTENAME, "Marketing - Advertising Companies");
            cv.put(COL_NOTETEXT, "This is the Marketing Note");
            mDatabase.insert(FTS_VIRTUAL_NOTES_TABLE, null, cv);
            cv.clear();
            cv.put(COL_NOTENAME, "Marketing - Advertising Companies");
            cv.put(COL_REMINDER_NAME, "Business quiz to study for");
            cv.put(COL_REMINDER_DATE, "March 9, 2015");
            cv.put(COL_REMINDER_TIME, "1:00 PM");
            mDatabase.insert(FTS_VIRTUAL_REMINDERS_TABLE, null, cv);

            cv.clear();
            cv.put(COL_SUBJECT, "SOC");
            cv.put(COL_NOTENAME, "SOC Testing");
            cv.put(COL_NOTETEXT, "This is the SOC note");
            mDatabase.insert(FTS_VIRTUAL_NOTES_TABLE, null, cv);
            cv.clear();
            cv.put(COL_NOTENAME, "SOC Testing");
            cv.put(COL_REMINDER_NAME, "SOC presentation due on Tuesday");
            cv.put(COL_REMINDER_DATE, "March 13, 2015");
            cv.put(COL_REMINDER_TIME, "10:00 AM");
            mDatabase.insert(FTS_VIRTUAL_REMINDERS_TABLE, null, cv);

            cv.clear();
            cv.put(COL_SUBJECT, "Databases");
            cv.put(COL_NOTENAME, "Android - SQL Database");
            cv.put(COL_NOTETEXT, "This is the SQL Android Databases note");
            mDatabase.insert(FTS_VIRTUAL_NOTES_TABLE, null, cv);
            cv.clear();
            cv.put(COL_NOTENAME, "Android - SQL Database");
            cv.put(COL_REMINDER_NAME, "Database Seminar's next Monday, Study!");
            cv.put(COL_REMINDER_DATE, "April 3, 2015");
            cv.put(COL_REMINDER_TIME, "3:00 PM");
            mDatabase.insert(FTS_VIRTUAL_REMINDERS_TABLE, null, cv);

            cv.clear();
            cv.put(COL_SUBJECT, "History");
            cv.put(COL_NOTENAME, "World War II");
            cv.put(COL_NOTETEXT, "This is the World War II note");
            mDatabase.insert(FTS_VIRTUAL_NOTES_TABLE, null, cv);
            cv.clear();
            cv.put(COL_NOTENAME, "World War II");
            cv.put(COL_REMINDER_NAME, "History Final on Friday to study for!");
            cv.put(COL_REMINDER_DATE, "March 27, 2015");
            cv.put(COL_REMINDER_TIME, "2:00 PM");
            mDatabase.insert(FTS_VIRTUAL_REMINDERS_TABLE, null, cv);

            cv.clear();
            cv.put(COL_SUBJECT, "Android");
            cv.put(COL_NOTENAME, "Android - Notification Manager");
            cv.put(COL_NOTETEXT, "This is the Notification Manager Note");
            mDatabase.insert(FTS_VIRTUAL_NOTES_TABLE, null, cv);
            cv.clear();
            cv.put(COL_NOTENAME, "Android - Notification Manager");
            cv.put(COL_REMINDER_NAME, "Android project due on Monday!");
            cv.put(COL_REMINDER_DATE, "March 16, 2015");
            cv.put(COL_REMINDER_TIME, "3:00 PM");
            mDatabase.insert(FTS_VIRTUAL_REMINDERS_TABLE, null, cv);

            cv.clear();
            cv.put(COL_SUBJECT, "Communication");
            cv.put(COL_NOTENAME, "Oral Comm - Public Speaking");
            cv.put(COL_NOTETEXT, "THis is the Public Speaking Note");
            mDatabase.insert(FTS_VIRTUAL_NOTES_TABLE, null, cv);
            cv.clear();
            cv.put(COL_NOTENAME, "Oral Comm - Public Speaking");
            cv.put(COL_REMINDER_NAME, "Quiz on Monday!");
            cv.put(COL_REMINDER_DATE, "March 24, 2015");
            cv.put(COL_REMINDER_TIME, "12:00 PM");
            mDatabase.insert(FTS_VIRTUAL_REMINDERS_TABLE, null, cv);

            cv.clear();
            cv.put(COL_SUBJECT, "Chemistry");
            cv.put(COL_NOTENAME, "CHEM - Reactions");
            cv.put(COL_NOTETEXT, "This is the CHEM Note");
            mDatabase.insert(FTS_VIRTUAL_NOTES_TABLE, null, cv);
            cv.clear();
            cv.put(COL_NOTENAME, "CHEM - Reactions");
            cv.put(COL_REMINDER_NAME, "Lab on Tuesday!");
            cv.put(COL_REMINDER_DATE, "March 17, 2015");
            cv.put(COL_REMINDER_TIME, "5:00 PM");
            mDatabase.insert(FTS_VIRTUAL_REMINDERS_TABLE, null, cv);*/
        }

        /*
         * This method will update the name of the current subject name in the database with a new subject
         * name that the user has entered in the dialogue box.
         * The newNoteName parameter holds the data the user has entered
         */
        public void updateSubjectName(String oldSubjectName, String newSubjectName)
        {
            SQLiteDatabase db = this.getReadableDatabase();
            String formattedCheck;

            ContentValues cv = new ContentValues();
            cv.put(COL_SUBJECT, newSubjectName);

            //update the subject name with the new name the user typed in
            //CHECKS TO PREVENT update command FROM CRASHING APP IF OLD SUBJECT NAME HAS AN APOSTROPHE (')
            //DO NOT REMOVE!!!
            if (oldSubjectName.contains("'")) {
                formattedCheck = oldSubjectName.replaceAll("'", "''");
                db.update(FTS_VIRTUAL_NOTES_TABLE, cv, COL_SUBJECT + "='" + formattedCheck + "'", null);
            }
            else
                db.update(FTS_VIRTUAL_NOTES_TABLE, cv, COL_SUBJECT + "='" + oldSubjectName + "'", null);

            db.close();
            System.out.println("Subject Name Updated in Notes Table");
        }

        /*
         * This method will update the note data in the database with the text the user has typed
         * into the note text box on the edit note or new note screen
         * The noteData parameter holds the data the user has entered for insertion into the database.
         */
        public void updateNoteText(String noteName, String noteData)
        {
            SQLiteDatabase db = this.getReadableDatabase();
            String formattedCheck;

            ContentValues cv = new ContentValues();
            cv.put(COL_NOTENAME, noteName);
            cv.put(COL_NOTETEXT, noteData);

            //update string with note data whenever the database is updated
            noteText = noteData;

            //update the note text with the data the user typed in
            //CHECKS TO PREVENT update command FROM CRASHING APP IF NOTE NAME HAS AN APOSTROPHE (')
            //DO NOT REMOVE!!!
            if (noteName.contains("'")) {
                formattedCheck = noteName.replaceAll("'", "''");
                db.update(FTS_VIRTUAL_NOTES_TABLE, cv, COL_NOTENAME + "='" + formattedCheck + "'", null);
            }
            else
                db.update(FTS_VIRTUAL_NOTES_TABLE, cv, COL_NOTENAME + "='" + noteName + "'", null);

            db.close();
            System.out.println("Database Updated");
        }

        /*
         * This method takes in parameters from calling activity that contain the name of the reminder, reminder date,
         * and reminder time.
         * Method when called, will insert all of the reminder data into the database.
         * REMINDERS WILL ONLY BE INSERTED INTO THE DATABASE IF THE USER HAS SET ONE
         */
        public void updateReminder(String noteName, String oldReminderName, String newReminderName, int reminderYear, int reminderMonth, int reminderDay, int reminderHour, int reminderMinute) {
            SQLiteDatabase db = this.getReadableDatabase();
            String formattedCheck;

            ContentValues newValues = new ContentValues();

            String AM_PM;
            String hour;
            String minute;

            //convert int month values to string months
            String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
            String month = months[reminderMonth];

            //convert int day value to string day
            String day = String.valueOf(reminderDay);

            //convert int year value to string year
            String year = String.valueOf(reminderYear);

            //create formatted date string for insertion of updated time into database
            String date = month + " " + day + ", " + year;

            //convert int hour value to string based on 12 hour time with AM and PM
            if (reminderHour >= 12) {
                hour = String.valueOf(reminderHour - 12);
                AM_PM = "PM";
            }
            else {
                hour = String.valueOf(reminderHour);
                AM_PM = "AM";
            }

            //if the reminder minute time is less than 10 (for example 3:03) then add a 0 in front of minute value
            //to avoid reminder list showing 3:3 as the time
            if(reminderMinute < 10){
                minute = String.valueOf(reminderMinute);
                minute = "0" + minute;
            }

            else
                minute = String.valueOf(reminderMinute);

            //create formatted time string for insertion into database
            String time = hour + ":" + minute + " " + AM_PM;

            //assign values to database rows
            newValues.put(COL_NOTENAME, noteName);
            newValues.put(COL_REMINDER_NAME, newReminderName);
            newValues.put(COL_REMINDER_DATE, date);
            newValues.put(COL_REMINDER_TIME, time);

            //update reminder data in database
            //CHECKS TO PREVENT update command FROM CRASHING APP IF OLD REMINDER NAME HAS AN APOSTROPHE (')
            //DO NOT REMOVE!!!
            if (oldReminderName.contains("'")) {
                formattedCheck = oldReminderName.replaceAll("'", "''");
                db.update(FTS_VIRTUAL_REMINDERS_TABLE, newValues, COL_REMINDER_NAME + "='" + formattedCheck + "'", null);
            }
            else
                db.update(FTS_VIRTUAL_REMINDERS_TABLE, newValues, COL_REMINDER_NAME + "='" + oldReminderName + "'", null);

            db.close();
            System.out.println("Reminder Updated for Note:  " + noteName);
        }

        /*
         * This method will delete the subject the user has selected in the database
         */
        public void deleteSubject(String subject)
        {
            SQLiteDatabase db = this.getReadableDatabase();

            //CHECK TO PREVENT delete command FROM CRASHING APP IF SUBJECT HAS AN APOSTROPHE (')
            //DO NOT REMOVE!!!
            if (subject.contains("'")) {
                subject = subject.replaceAll("'", "''");
            }

            db.delete(FTS_VIRTUAL_NOTES_TABLE, COL_SUBJECT + "='" + subject + "'", null);
            db.close();
        }

        /*
        * This method will delete the note the user has selected in the database
        */
        public void deleteNote(String noteName)
        {
            SQLiteDatabase db = this.getReadableDatabase();

            //CHECK TO PREVENT delete command FROM CRASHING APP IF NOTE NAME HAS AN APOSTROPHE (')
            //DO NOT REMOVE!!!
            if (noteName.contains("'")) {
                noteName = noteName.replaceAll("'", "''");
            }

            db.delete(FTS_VIRTUAL_NOTES_TABLE, COL_NOTENAME + "='" + noteName + "'", null);
            db.close();
        }

        /*
         * This method will delete the reminder the user has selected in the database
         */
        public void deleteReminder(String reminderName)
        {
            SQLiteDatabase db = this.getReadableDatabase();

            //CHECK TO PREVENT delete command FROM CRASHING APP IF REMINDER HAS AN APOSTROPHE (')
            //DO NOT REMOVE!!!
            if (reminderName.contains("'")) {
                reminderName = reminderName.replaceAll("'", "''");
            }

            db.delete(FTS_VIRTUAL_REMINDERS_TABLE, COL_REMINDER_NAME + "='" + reminderName + "'", null);
            db.close();
        }


        /*
         * This method is used to populate the list view in the subjects tab of the home screen with all of the subjects the user has.
         * Goes through only the subject column in the database and adds all subjects to the subject list.
         */
        public ArrayList<String> getNoteSubjects()
        {
            SQLiteDatabase db = this.getReadableDatabase();

            //search the whole notes table in the database
            Cursor cursor = db.rawQuery("SELECT * FROM " + FTS_VIRTUAL_NOTES_TABLE, null);

            //initialize note list to hold query results
            ArrayList<String> subjects = new ArrayList<>(cursor.getCount());

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                subjects.add(cursor.getString(cursor.getColumnIndex(COL_SUBJECT)));
                cursor.moveToNext();
            }
            cursor.close();

            return subjects;
        }

        /*
         * This method is used to populate the list view in the notes tab of the home screen with all of the notes the user has.
         * Note that only the name of the note will be displayed, not the note text!
         * Goes through only the note name column in the database and adds all notes to the note list.
         */
        public ArrayList<String> getNotes()
        {
            SQLiteDatabase db = this.getReadableDatabase();

            //search the whole notes table in the database
            Cursor cursor = db.rawQuery("SELECT * FROM " + FTS_VIRTUAL_NOTES_TABLE, null);

            //initialize note list to hold query results
            ArrayList<String> notes = new ArrayList<>(cursor.getCount());

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                notes.add(cursor.getString(cursor.getColumnIndex(COL_NOTENAME)));
                cursor.moveToNext();
            }
            cursor.close();

            return notes;
        }

        /*
         * This method is used to get the text of the note the user has selected.
         * The parameter noteName holds the name of the note so the database can look for the text associated
         * with a note by its name.  The method returns the note text or an empty string if there is no text
         * associated with a note.
        */
        public String getNoteText(String noteName)
        {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor;
            String formattedCheck;

            //search all of note text in the database
            //CHECK TO PREVENT note name query FROM CRASHING APP IF NOTE NAME HAS AN APOSTROPHE (')
            //DO NOT REMOVE!!!
            if (noteName.contains("'")) {
                formattedCheck = noteName.replaceAll("'", "''");
                cursor = db.rawQuery("select * FROM NOTES WHERE NOTES MATCH 'NOTENAME:"+formattedCheck+"'", null);
            }
            else
                cursor = db.rawQuery("select * FROM NOTES WHERE NOTES MATCH 'NOTENAME:"+noteName+"'", null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                noteText = cursor.getString(cursor.getColumnIndex(COL_NOTETEXT));
                cursor.moveToNext();
            }
            cursor.close();

            return noteText;
        }

        /*
        * This method is used to populate the list view in the reminders tab of the home screen with all of the reminders the user has.
        * The name of the reminder as well as its time and date are displayed in the list view.
        * Goes through the REMINDER_NAME column, REMINDER_DATE column, and the REMINDER_TIME column in the database
        * and adds all reminders to the reminder list.
        */
        public ArrayList<String> getReminders()
        {
            SQLiteDatabase db = this.getReadableDatabase();

            //search the whole notes table in the database for reminders
            Cursor cursor = db.rawQuery("SELECT * FROM " + FTS_VIRTUAL_REMINDERS_TABLE, null);

            //initialize list and string to hold query results
            ArrayList<String> reminders = new ArrayList<>(cursor.getCount());

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                reminderName = cursor.getString(cursor.getColumnIndex(COL_REMINDER_NAME));
                reminderDate = cursor.getString(cursor.getColumnIndex(COL_REMINDER_DATE));
                reminderTime = cursor.getString(cursor.getColumnIndex(COL_REMINDER_TIME));
                reminderData = reminderName + "\n" + reminderDate + " " + reminderTime;

                reminders.add(reminderData);
                cursor.moveToNext();
            }

            cursor.close();

            return reminders;
        }

        /*
         * This method is used to check the text of the note for certain keywords as defined in the reminderKeys list.
         * If two or more matches are found between the note text and reminderKeys, then the method will return
         * true to display a dialogue box to create a reminder.
         */
        public boolean checkNoteForReminder(String noteText)
        {
            //flags to determine if more than two keywords have been found to prompt for a reminder
            boolean reminderPrompt = false;
            //need to have at least two matches to prompt for a reminder
            int matchCount = 0;

            //go through note text to see if there are two or more words in the note that match the keys
            //note text is split into a list of values for comparison against the reminder keys
            //regardless of case (non case sensitive)
            String noteData = noteText.toLowerCase().trim();
            String[] matchWords = noteData.split("[, .]", 0);
            for(int i = 0; i < matchWords.length; i++)
            {
                if(reminderKeys.contains(matchWords[i])){
                    matchCount ++;
                }
            }

            //only prompt for reminder if there were 2 or more matches
            if (matchCount >= 2)
                reminderPrompt = true;

            return reminderPrompt;
        }

        public ArrayList<String> getSubjectSearchName(String query)
        {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor;
            String formattedCheck;

            ArrayList<String> subjectResults = new ArrayList<>();

            //CHECK TO PREVENT SEARCH QUERY FROM CRASHING APP IF USER ENTERS AN APOSTROPHE (')
            //DO NOT REMOVE!!!
            if (query.contains("'")) {
                formattedCheck = query.replaceAll("'", "''");
                cursor = db.rawQuery("select * FROM NOTES WHERE NOTES MATCH 'SUBJECT:"+formattedCheck+"'", null);
            }
            else
                cursor = db.rawQuery("select * FROM NOTES WHERE NOTES MATCH 'SUBJECT:"+query+"'", null);

            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                String subjectResultsName = cursor.getString(cursor.getColumnIndex(COL_SUBJECT));
                subjectResults.add(subjectResultsName);
                cursor.moveToNext();
            }

            cursor.close();

            return subjectResults;
        }

        public ArrayList<String> getNoteSearchText(String query)
        {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor;
            String formattedCheck;

            ArrayList<String> noteTextResults = new ArrayList<>();

            //CHECK TO PREVENT SEARCH QUERY FROM CRASHING APP IF USER ENTERS AN APOSTROPHE (')
            //DO NOT REMOVE!!!
            if (query.contains("'")) {
                formattedCheck = query.replaceAll("'", "''");
                cursor = db.rawQuery("select * FROM NOTES WHERE NOTES MATCH 'NOTETEXT:"+formattedCheck+"'", null);
            }
            else
                cursor = db.rawQuery("select * FROM NOTES WHERE NOTES MATCH 'NOTETEXT:"+query+"'", null);

            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                String noteResultsName = cursor.getString(cursor.getColumnIndex(COL_NOTENAME));
                noteTextResults.add(noteResultsName);
                cursor.moveToNext();
            }

            cursor.close();

            return noteTextResults;
        }

        public ArrayList<String> getNoteSearchName(String query)
        {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor;
            String formattedCheck;

            ArrayList<String> noteNameResults = new ArrayList<>();

            //CHECK TO PREVENT SEARCH QUERY FROM CRASHING APP IF USER ENTERS AN APOSTROPHE (')
            //DO NOT REMOVE!!!
            if (query.contains("'")) {
                formattedCheck = query.replaceAll("'", "''");
                cursor = db.rawQuery("select * FROM NOTES WHERE NOTES MATCH 'NOTENAME:"+formattedCheck+"'", null);
            }
            else
                cursor = db.rawQuery("select * FROM NOTES WHERE NOTES MATCH 'NOTENAME:"+query+"'", null);

            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                String noteName = cursor.getString(cursor.getColumnIndex(COL_NOTENAME));
                noteNameResults.add(noteName);
                cursor.moveToNext();
            }

            cursor.close();

            return noteNameResults;
        }

        public ArrayList<String> getReminderSearchName(String query)
        {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor;
            String formattedCheck;

            ArrayList<String> reminderResults = new ArrayList<>();

            //CHECK TO PREVENT SEARCH QUERY FROM CRASHING APP IF USER ENTERS AN APOSTROPHE (')
            //DO NOT REMOVE!!!
            if (query.contains("'")) {
                formattedCheck = query.replaceAll("'", "''");
                cursor = db.rawQuery("select * FROM REMINDERS WHERE REMINDERS MATCH 'REMINDER_NAME:"+formattedCheck+"'", null);
            }
            else
                cursor = db.rawQuery("select * FROM REMINDERS WHERE REMINDERS MATCH 'REMINDER_NAME:"+query+"'", null);

            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                reminderName = cursor.getString(cursor.getColumnIndex(COL_REMINDER_NAME));
                reminderDate = cursor.getString(cursor.getColumnIndex(COL_REMINDER_DATE));
                reminderTime = cursor.getString(cursor.getColumnIndex(COL_REMINDER_TIME));
                reminderData = reminderName + "\n" + reminderDate + " " + reminderTime;

                reminderResults.add(reminderData);
                cursor.moveToNext();
            }

            cursor.close();

            return reminderResults;
        }

        public ArrayList<String> getSubjectNotes(String subject)
        {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor;
            String formattedCheck;
            ArrayList<String> subjectNotesResults = new ArrayList<>();

            //CHECK TO PREVENT subject query FROM CRASHING APP IF SUBJECT HAS AN APOSTROPHE (')
            //DO NOT REMOVE!!!
            if (subject.contains("'")) {
                formattedCheck = subject.replaceAll("'", "''");
                cursor = db.rawQuery("select * FROM NOTES WHERE NOTES MATCH 'SUBJECT:"+formattedCheck+"'", null);
            }
            else
                cursor = db.rawQuery("select * FROM NOTES WHERE NOTES MATCH 'SUBJECT:"+subject+"'", null);

            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                String subjectNotes = cursor.getString(cursor.getColumnIndex(COL_NOTENAME));
                subjectNotesResults.add(subjectNotes);
                cursor.moveToNext();
            }

            cursor.close();

            return subjectNotesResults;
        }

        public String getNoteSubject(String noteName)
        {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor;
            String formattedCheck;
            String subjectName = "";

            //CHECK TO PREVENT note name query FROM CRASHING APP IF NOTE NAME HAS AN APOSTROPHE (')
            //DO NOT REMOVE!!!
            if (noteName.contains("'")) {
                formattedCheck = noteName.replaceAll("'", "''");
                cursor = db.rawQuery("select * FROM NOTES WHERE NOTES MATCH 'NOTENAME:"+formattedCheck+"'", null);
            }
            else
                cursor = db.rawQuery("select * FROM NOTES WHERE NOTES MATCH 'NOTENAME:"+noteName+"'", null);

            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                subjectName = cursor.getString(cursor.getColumnIndex(COL_SUBJECT));
                cursor.moveToNext();
            }

            cursor.close();

            return subjectName;
        }

        public String getReminderName(String noteName)
        {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor;
            String formattedCheck;
            String reminderName = "";

            //select the reminder name the note name is associated with
            //CHECK TO PREVENT note name query FROM CRASHING APP IF NOTE NAME HAS AN APOSTROPHE (')
            //DO NOT REMOVE!!!
            if (noteName.contains("'")) {
                formattedCheck = noteName.replaceAll("'", "''");
                cursor = db.rawQuery("select * FROM REMINDERS WHERE REMINDERS MATCH 'NOTENAME:"+formattedCheck+"'", null);
            }
            else
                cursor = db.rawQuery("select * FROM REMINDERS WHERE REMINDERS MATCH 'NOTENAME:"+noteName+"'", null);

            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                reminderName = cursor.getString(cursor.getColumnIndex(COL_REMINDER_NAME));
                cursor.moveToNext();
            }

            cursor.close();

            return reminderName;
        }

        public String getSubjectReminders(String noteName)
        {
            String reminderNameData;
            String reminderDateData;
            String reminderTimeData;
            String reminder = null;
            String formattedCheck;

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor;

            if(noteName.contains("'")){
                formattedCheck = noteName.replaceAll("'", "''");
                cursor = db.rawQuery("select * FROM REMINDERS WHERE REMINDERS MATCH 'NOTENAME:"+formattedCheck+"'", null);
            }
            else
                cursor = db.rawQuery("select * FROM REMINDERS WHERE REMINDERS MATCH 'NOTENAME:"+noteName+"'", null);

            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                reminderNameData = cursor.getString(cursor.getColumnIndex(COL_REMINDER_NAME));
                reminderDateData = cursor.getString(cursor.getColumnIndex(COL_REMINDER_DATE));
                reminderTimeData = cursor.getString(cursor.getColumnIndex(COL_REMINDER_TIME));
                reminder = reminderNameData + "\n" + reminderDateData + " " + reminderTimeData;
                cursor.moveToNext();
            }

            cursor.close();

            return reminder;
        }

        public ArrayList<String> getReminderDateTime(String noteName)
        {
            String reminderDate = "";
            String reminderMonth;
            String reminderDay;
            String reminderYear;
            String reminderHour;
            String reminderMinute;
            int reminderHourFormat;

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor;
            String formattedCheck;

            ArrayList<String> reminderData = new ArrayList<>();
            String[] reminderDateSplit;
            String[] reminderTimeSplit;

            //CHECKS TO PREVENT query command FROM CRASHING APP IF NOTE NAME HAS AN APOSTROPHE (')
            //DO NOT REMOVE!!!
            if (noteName.contains("'")) {
                formattedCheck = noteName.replaceAll("'", "''");
                //select the reminder name the note name is associated with
                cursor = db.rawQuery("select * FROM REMINDERS WHERE REMINDERS MATCH 'NOTENAME:" + formattedCheck + "'", null);
            }
            else
                //select the reminder name the note name is associated with
                cursor = db.rawQuery("select * FROM REMINDERS WHERE REMINDERS MATCH 'NOTENAME:"+noteName+"'", null);

            //get data from database
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                reminderDate = cursor.getString(cursor.getColumnIndex(COL_REMINDER_DATE));
                reminderTime= cursor.getString(cursor.getColumnIndex(COL_REMINDER_TIME));
                cursor.moveToNext();
            }
            cursor.close();

            //split date and time data to remove any formatting from the data and time of the reminder in the database
            reminderDateSplit = reminderDate.split("\\s*(=>|,|\\s)\\s*");
            reminderTimeSplit = reminderTime.split("[: ]");

            if(reminderTimeSplit[2].equals("PM"))
            {
                //convert to 24 hour time for AM and PM values on time picker
                reminderHourFormat = Integer.valueOf(reminderTimeSplit[0]);
                reminderHour = String.valueOf(reminderHourFormat + 12);
            }
            else{
                reminderHour = reminderTimeSplit[0];
            }

            //put data into array list for passing to calling activity to get data and time values
            reminderMonth = reminderDateSplit[0];
            reminderDay = reminderDateSplit[1];
            reminderYear = reminderDateSplit[2];
            reminderMinute = reminderTimeSplit[1];
            reminderData.add(0, reminderMonth);
            reminderData.add(1, reminderDay);
            reminderData.add(2, reminderYear);
            reminderData.add(3, reminderHour);
            reminderData.add(4, reminderMinute);

            return reminderData;
        }

        public String getNoteName(String reminderName)
        {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor;
            String formattedCheck;
            String noteName = "";

            //CHECK TO PREVENT QUERY FROM CRASHING APP IF THE REMINDER NAME HAS AN APOSTROPHE (')
            //DO NOT REMOVE!!!
            if (reminderName.contains("'")) {
                formattedCheck = reminderName.replaceAll("'", "''");
                cursor = db.rawQuery("select * FROM REMINDERS WHERE REMINDERS MATCH 'REMINDER_NAME:"+formattedCheck+"'", null);
            }
            else
                cursor = db.rawQuery("select * FROM REMINDERS WHERE REMINDERS MATCH 'REMINDER_NAME:"+reminderName+"'", null);

            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                noteName = cursor.getString(cursor.getColumnIndex(COL_NOTENAME));
                cursor.moveToNext();
            }

            cursor.close();

            return noteName;
        }
    }
}
package notes.development.kyles.notegenie;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import notes.development.kyles.notegenie.util.Database;


public class NotifyScreen extends ActionBarActivity {

    private ActionBarDrawerToggle drawerToggle;
    AlertDialog aboutDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notify_screen);

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
        getSupportActionBar().setTitle("Reminder Notes");

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
                    //go to settings
                    Intent settings = new Intent(getApplicationContext(), Settings.class);
                    startActivity(settings);
                }
                if(position == 1){
                    AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(NotifyScreen.this);
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
        activityTitle = "Reminder Notes";
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

        //get noteName from HomeScreen method so notification knows which note to look
        //for in the database for showing notes to user when notification is selected
        String noteName = HomeScreen.getNotifyNoteName();

        Toast.makeText(getApplicationContext(), "Displaying Note", Toast.LENGTH_LONG).show();

        //find and set the header text view to be the name of the note
        TextView noteHeaderView = (TextView) findViewById(R.id.noteNotifyNameHeader);
        noteHeaderView.setText(noteName);

        //create instance of database helper to get data from database
        Database.DatabaseOpenHelper helper;
        helper = new Database.DatabaseOpenHelper(getApplicationContext());

        //get note data for the selected note
        helper.getNoteText(noteName);

        //find and set the note text view to have the text of the note from the database
        TextView noteText = (TextView) findViewById(R.id.noteNotifyText);
        noteText.setText(Database.noteText);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notify_screen, menu);
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
    public void onBackPressed() {
        //if the user pressed the back button on the device, then go back to the HomeScreen
        Intent HomeScreen = new Intent(getApplicationContext(), HomeScreen.class);
        startActivity(HomeScreen);
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
}

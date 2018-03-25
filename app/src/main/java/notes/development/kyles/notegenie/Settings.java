package notes.development.kyles.notegenie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class Settings extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolbar_preferences);

        //display toolbar
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.settings_icon);

        //display settings fragment as main content
        getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
        if((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            //Activity was brought to front and not created
            //Thus finishing this will display the last viewed activity
            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home){
            //go back to Home Screen
            Toast.makeText(getApplicationContext(), "Settings Saved", Toast.LENGTH_LONG).show();
            Intent HomeScreen = new Intent(getApplicationContext(), HomeScreen.class);
            startActivity(HomeScreen);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //go back to Home Screen
        Toast.makeText(getApplicationContext(), "Settings Saved", Toast.LENGTH_LONG).show();
        Intent HomeScreen = new Intent(getApplicationContext(), HomeScreen.class);
        startActivity(HomeScreen);
    }
}

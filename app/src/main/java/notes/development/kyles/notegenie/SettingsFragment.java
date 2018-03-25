package notes.development.kyles.notegenie;

import android.preference.PreferenceFragment;
import android.os.Bundle;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load application preferences from XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}

package notes.development.kyles.notegenie.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import notes.development.kyles.notegenie.NotesTab;
import notes.development.kyles.notegenie.RemindersTab;
import notes.development.kyles.notegenie.SubjectTab;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    //Tab titles
    CharSequence tabTitles[];
    //Number of tabs
    int numTabs;

    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm,CharSequence mTabTitles[], int mNumOfTabs) {
        super(fm);

        this.tabTitles = mTabTitles;
        this.numTabs = mNumOfTabs;

    }

    //This method returns the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {

        SubjectTab subjectTab;
        NotesTab notesTab;
        RemindersTab remindersTab;

        //First Tab
        if(position == 0)
        {
            //Subjects Tab
            subjectTab = new SubjectTab();
            return subjectTab;
        }
        //Second Tab
        else if(position == 1)
        {
            //Notes Tab
            notesTab = new NotesTab();
            return notesTab;
        }
        //Third Tab
        else
            //Reminders Tab
            remindersTab = new RemindersTab();
            return remindersTab;
    }

    // This method returns the titles for the Tabs in the Tab Strip
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    // This method returns the Number of tabs for the tabs Strip
    @Override
    public int getCount() {
        return numTabs;
    }
}
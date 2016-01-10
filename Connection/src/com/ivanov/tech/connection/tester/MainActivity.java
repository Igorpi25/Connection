package com.ivanov.tech.connection.tester;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ivanov.tech.connection.R;
import com.ivanov.tech.connection.R.id;
import com.ivanov.tech.connection.R.layout;

/**
 * Created by Игорь on 15.01.15.
 */
public class MainActivity extends SherlockFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        
        changeFragment(0);
    }


    private void changeFragment(int position) {
        switch (position) {
            case 0:
                showFragment(new FragmentConnectionTester());
                break;
        }
    }

    private void showFragment(Fragment currentFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_container, currentFragment)
                .commit();
    }

    
}

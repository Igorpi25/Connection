package com.ivanov.tech.connection.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ivanov.tech.connection.Connection;
import com.ivanov.tech.connection.R;

/**
 * Created by Игорь on 15.01.15.
 */
public class ActivityDemo extends AppCompatActivity {

	private static final String TAG=Connection.class.getSimpleName();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        
        showFragment(new FragmentDemo());
    }

    private void showFragment(Fragment currentFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_container, currentFragment)
                .commit();
    }

    
}

package com.ivanov.tech.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class FragmentNoConnection extends DialogFragment {

    private static final String TAG=Connection.class.getSimpleName();
    TextView edittext_message;
    Button button_confirm,button_cancel;
    View layout_dimming;

    FragmentNoConnectionEventListener fragmentNoConnectionEventListener;

    public static FragmentNoConnection newInstance(FragmentNoConnectionEventListener listener) {
    	FragmentNoConnection f = new FragmentNoConnection();
        f.fragmentNoConnectionEventListener =listener;
        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver();
    }
    


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        view = inflater.inflate(R.layout.fragment_no_connection, container, false);
        edittext_message = (TextView) view.findViewById(R.id.fragment_connection_server_textview_message);
        button_confirm = (Button) view.findViewById(R.id.fragment_connection_button_confirm);
        button_cancel = (Button) view.findViewById(R.id.fragment_connection_button_cancel);
        layout_dimming=view.findViewById(R.id.fragment_connection_layout_dimming);

        button_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
                fragmentNoConnectionEventListener.onCanceled();
            }
        });


        layout_dimming.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    getFragmentManager().popBackStack();
                    fragmentNoConnectionEventListener.onCanceled();
                }
                return true;
            }
        });

        return view;
    }

    private void registerReceiver()
    {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(reciever, filter);
    }

    private void unregisterReceiver()
    {
        getActivity().unregisterReceiver(reciever);
    }

    final BroadcastReceiver reciever = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
            {
                Log.d ( TAG, "handling event: ConnectivityManager.CONNECTIVITY_ACTION action: "+action );

                if(Connection.isOnline(getActivity())){
                    getFragmentManager().popBackStack();
                    fragmentNoConnectionEventListener.onInternetIsAvailable();
                }
            }
        }
    };

    public interface FragmentNoConnectionEventListener {
        public void onInternetIsAvailable();
        public void onCanceled();
    }
}
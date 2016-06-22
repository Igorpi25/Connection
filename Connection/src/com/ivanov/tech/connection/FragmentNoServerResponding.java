package com.ivanov.tech.connection;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class FragmentNoServerResponding extends DialogFragment {

    private static final String TAG=Connection.class.getSimpleName();

    TextView textview_message;
    Button button_retry;
    View layout_dimming;

    FragmentNoServerRespondingEventListener fragmentNoServerRespondingEventListener;

    public static FragmentNoServerResponding newInstance(FragmentNoServerRespondingEventListener listener) {
        FragmentNoServerResponding f = new FragmentNoServerResponding();
        f.fragmentNoServerRespondingEventListener =listener;
        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        view = inflater.inflate(R.layout.fragment_no_server_responding, container, false);
        textview_message = (TextView) view.findViewById(R.id.fragment_connection_server_textview_message);
        button_retry = (Button) view.findViewById(R.id.fragment_connection_server_button_retry);
        layout_dimming=view.findViewById(R.id.fragment_connection_server_layout_dimming);

        button_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
                fragmentNoServerRespondingEventListener.onRetry();
            }
        });


        layout_dimming.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    getFragmentManager().popBackStack();
                    fragmentNoServerRespondingEventListener.onCancel();
                }
                return true;
            }
        });

        return view;
    }


    public interface FragmentNoServerRespondingEventListener {
        public void onRetry();
        public void onCancel();
    }
}
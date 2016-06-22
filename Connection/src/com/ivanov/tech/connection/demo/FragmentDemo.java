package com.ivanov.tech.connection.demo;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ivanov.tech.connection.Connection;
import com.ivanov.tech.connection.R;
import com.ivanov.tech.connection.Connection.ProtocolListener;

/**
 * Created by Igor on 09.05.15.
 */
public class FragmentDemo extends DialogFragment implements OnClickListener {


    public static final String TAG = Connection.class.getSimpleName();
    
	
    TextView textview_response;
    EditText edittext_server_url;
    Button button_request,button_check;
    View layout_dimming;

    public static FragmentDemo newInstance() {
    	FragmentDemo f = new FragmentDemo();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        view = inflater.inflate(R.layout.fragment_demo, container, false);
                
        edittext_server_url=(EditText)view.findViewById(R.id.fragment_tester_edittext_server_url);
        
        button_check = (Button) view.findViewById(R.id.fragment_tester_button_check);
        button_check.setOnClickListener(this);
        	        
        button_request = (Button) view.findViewById(R.id.fragment_tester_button_request);
        button_request.setOnClickListener(this);

        textview_response=(TextView)view.findViewById(R.id.fragment_tester_textview_response);
        
        return view;
    }

    @Override
	public void onClick(View v) {
		textview_response.setText("checkConnection...");
		
		if (v.getId()==button_check.getId()){
			
			Connection.protocolConnection(getActivity(), getFragmentManager(),R.id.main_container, new ProtocolListener(){
				@Override
				public void isCompleted() {					
					textview_response.setText("ProtocolConnection completed. You are connected to Internet; ");
				}

				@Override
				public void onCanceled() {
					textview_response.setText("ProtocolConnection interrupted");
				}
			});
			
		}
		
		if (v.getId()==button_request.getId()){
			
			String server_url=getEditTextValue();
			
			Connection.protocolServerResponding(getActivity(), server_url, getFragmentManager(),R.id.main_container, new ProtocolListener(){
				@Override
				public void isCompleted() {					
					textview_response.setText("ProtocolServerResponding completed. You are connected to Internet and server responding");								
					
				}
				@Override
				public void onCanceled() {
					textview_response.setText("ProtocolServerResponding interrupted");
				}
			});
						
		}
	}
     
    String getEditTextValue(){
    	return edittext_server_url.getText().toString();
    }
}

package com.ivanov.tech.connection.tester;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ivanov.tech.connection.Connection;
import com.ivanov.tech.connection.R;
import com.ivanov.tech.connection.Connection.Status;

/**
 * Created by Igor on 09.05.15.
 */
public class FragmentConnectionTester extends SherlockDialogFragment implements OnClickListener {


    public static final String TAG = FragmentConnectionTester.class
            .getSimpleName();
    
	
    TextView textview_response;
    EditText edittext_server_url;
    Button button_request,button_check;
    View layout_dimming;

    public static FragmentConnectionTester newInstance() {
    	FragmentConnectionTester f = new FragmentConnectionTester();
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
        view = inflater.inflate(R.layout.fragment_tester, container, false);
                
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
			
			Connection.checkConnection(getActivity(), getFragmentManager(),R.id.main_container, new Status(){
				@Override
				public void isConnected() {					
					textview_response.setText("onConnected");
				}

				@Override
				public void onCanceled() {
					textview_response.setText("onCanceled");
				}
			});
			
		}
		
		if (v.getId()==button_request.getId()){
			
			Connection.checkConnection(getActivity(), getFragmentManager(),R.id.main_container, new Status(){
				@Override
				public void isConnected() {					
					textview_response.setText("onConnected, doing request...");								
					doRequest();
				}
				@Override
				public void onCanceled() {
					textview_response.setText("onCanceled");
				}
			});
						
		}
	}
     
    void doRequest(){
    	
    	String tag_stringRequest = "tag_stringRequest";
    	 
    	final ProgressDialog pDialog = new ProgressDialog(getActivity());
    	pDialog.setMessage("Waiting for server response...");
    	pDialog.show();     
    	    	
    	String server_url=edittext_server_url.getText().toString();
    	    	
    	StringRequest stringRequest = new StringRequest(Method.GET,
    					server_url,
    	                new Response.Listener<String>() {
    	 
    	                    @Override
    	                    public void onResponse(String response) {
    	                        Log.d(TAG, response);
    	                        
    	                        pDialog.hide();
    	                        
    	                        textview_response.setText(response);
    	                    }
    	                }, new Response.ErrorListener() {
    	 
    	                    @Override
    	                    public void onErrorResponse(VolleyError error) {
    	                        VolleyLog.d(TAG, "Error: " + error.getMessage());
    	                        
    	                        pDialog.hide();
    	                        
    	                        textview_response.setText(error.toString());
    	                    }
    	                });
    	 
    	    	
    	stringRequest.setTag(tag_stringRequest);
    	Volley.newRequestQueue(this.getActivity().getApplicationContext()).add(stringRequest);
    	    
    }
	
}

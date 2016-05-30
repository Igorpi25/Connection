package com.ivanov.tech.connection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.StringRequest;

public class Connection {

    private final static String TAG=Connection.class.getSimpleName();//Used in logs

    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
        
    public static void protocolConnection(final Context context, final FragmentManager fragmentManager, final int container,final ProtocolListener listener){
    	
        if(isOnline(context)){  
        	listener.isCompleted();
            return;
        }
        
        createFragmentNoConnection(context,fragmentManager,container,listener);
    }
    
    public static void protocolServerResponding(final Context context, final String url, final FragmentManager fragmentManager, final int container,final ProtocolListener listener){
    	
    	//protocol-connection must be completed, before start protocol-server-responding
    	protocolConnection(context,fragmentManager,container,new ProtocolListener(){

			@Override
			public void isCompleted() {
				//protocol-connection completed, now it have to check the server's responding
				doRequestToServer(context,url,fragmentManager,container,listener);
			}

			@Override
			public void onCanceled() {
				listener.onCanceled();
			}
			
        });
    	
    }

//-----------------Methods to Create fragment and show it in container-------------------------------------------

    public static void createFragmentNoServerResponding(final Context context, final String url, final FragmentManager fragmentManager, final int container,final ProtocolListener listener){

        try{
            if(fragmentManager.findFragmentByTag("ServerResponding").isVisible()){
                return;
            }else{
                throw (new NullPointerException());
            }
        }catch(NullPointerException e) {

            FragmentNoServerResponding connectionserverfragment = FragmentNoServerResponding.newInstance(new FragmentNoServerResponding.FragmentNoServerRespondingEventListener() {

                @Override
                public void onRetry() {
                	protocolServerResponding(context, url, fragmentManager,container, listener);
                }
                
                @Override
                public void onCancel() {
                	listener.onCanceled();
                }
            });

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(container, connectionserverfragment, "ServerResponding");
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.addToBackStack("ServerResponding");
            fragmentTransaction.commit();
        }
    }

    public static void createFragmentNoConnection(final Context context,final FragmentManager fragmentManager, final int container,final ProtocolListener listener){

        try{
            if(fragmentManager.findFragmentByTag("Connection").isVisible()){
                return;
            }else{
                throw (new NullPointerException());
            }
        }catch(NullPointerException e){

            FragmentNoConnection connectionfragment = FragmentNoConnection.newInstance(new FragmentNoConnection.FragmentNoConnectionEventListener(){
                @Override
                public void onInternetIsAvailable() {
                	listener.isCompleted();
                }

				@Override
				public void onCanceled() {
					listener.onCanceled();
				}

            });

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(container, connectionfragment, "Connection");
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.addToBackStack("Connection");
            fragmentTransaction.commit();
        }
    }

//-------------------------Http Get Request to the server to check if server available----------------------------------- 

    public static void doRequestToServer(final Context context, final String url, final FragmentManager fragmentManager, final int container,final ProtocolListener listener) {

        StringRequest stringrequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

        	@Override
			public void onResponse(String response) {

            	Log.d(TAG, "doRequestToServer onResponse response="+response);
            	
            	listener.isCompleted();
            }
			
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "doRequestToServer onErrorResponse error=" + error.toString());
                createFragmentNoServerResponding(context,url,fragmentManager,container,listener);

            }
        });
        

        String tag_stringrequest ="doRequestToServer_"+url;
    	
    	stringrequest.setTag(tag_stringrequest);
    	Volley.newRequestQueue(context.getApplicationContext()).add(stringrequest);
    
    }

//----------------------Listener used to make callback if protocol completed-----------------------------

    public interface ProtocolListener{
        public void isCompleted();
        public void onCanceled();
    }
}

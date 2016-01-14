package com.ivanov.tech.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class Connection {

	//Туора бардыбыт
    private final static String TAG="Connection";
    
    public final static String URL_TRANSPORT="http://";
    public final static String URL_DOMEN="192.168.0.100/"; //Your servers domen or ip 
    public final static String URL_VERSION="v1/"; 
    public final static String URL_SERVER=URL_TRANSPORT+URL_DOMEN+URL_VERSION;
    
    public static final String URL_WEBSOCKET = "ws://192.168.0.100:8001";//Used in websocket connection

    //You should write your own server URL here (url of php-script, e.g.)  
    private final static String serverTestUrl = URL_SERVER+"testconnection.php";

    public static void checkConnection(final Context context, final FragmentManager fragmentManager, final int container,final Status statusListener){

    	
        if(isOnline(context)){
            //doServerTestRequest(context,fragmentManager,container,statusListener);
            return;
        }
        
        createConnectionFragment(context,fragmentManager,container,statusListener);
    }

    public static void createConnectionServerFragment(final Context context,final FragmentManager fragmentManager, final int container,final Status statusListener){

        try{
            if(fragmentManager.findFragmentByTag("ServerResponding").isVisible()){
                return;
            }else{
                throw (new NullPointerException());
            }
        }catch(NullPointerException e) {

            ConnectionServerFragment connectionserverfragment = ConnectionServerFragment.newInstance(new ConnectionServerFragment.ServerRespondingStatus() {

                @Override
                public void onRetry() {
                    checkConnection(context, fragmentManager,container, statusListener);
                }
                
                @Override
                public void onCancel() {
                	statusListener.onCanceled();
                }
            });

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(container, connectionserverfragment, "ServerResponding");
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.addToBackStack("ServerResponding");
            fragmentTransaction.commit();
        }
    }

    public static void createConnectionFragment(final Context context,final FragmentManager fragmentManager, final int container,final Status statusListener){

        try{
            if(fragmentManager.findFragmentByTag("Connection").isVisible()){
                return;
            }else{
                throw (new NullPointerException());
            }
        }catch(NullPointerException e){

            ConnectionFragment connectionfragment = ConnectionFragment.newInstance(new ConnectionFragment.ConnectionStatus(){
                @Override
                public void onInternetIsAvailable() {
                    doServerTestRequest(context,fragmentManager,container,statusListener);
                }

				@Override
				public void onCanceled() {
					statusListener.onCanceled();
				}

            });

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(container, connectionfragment, "Connection");
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.addToBackStack("Connection");
            fragmentTransaction.commit();
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean doServerTestRequest(final Context context,final FragmentManager fragmentManager, final int container,final Status statusListener) {

        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(Request.Method.GET, serverTestUrl, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                // TODO Auto-generated method stub

                try{
                    if(response.getInt("connection")==1){
                        statusListener.isConnected();
                        //Log.d(TAG,"isServerResponding connected");
                    }else{
                        throw (new JSONException("server responded, but it\'s not available"));
                    }
                }catch (JSONException e){
                    createConnectionServerFragment(context,fragmentManager,container,statusListener);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "isServerResponding error=" + error.toString());
                createConnectionServerFragment(context,fragmentManager,container,statusListener);

            }
        });
        

        String tag_json_obj =TAG+"doServerTestRequest";
    	
    	jsonObjRequest.setTag(tag_json_obj);
    	Volley.newRequestQueue(context.getApplicationContext()).add(jsonObjRequest);
    
        return false;
    }

    public interface Status{
        public void isConnected();
        public void onCanceled();
    }

//--------------Service LastTime Preferences--------------------------------
	
    private static final String PREF = "Connection";    
    public static final String PREF_LAST_TIMESTAMP="PREF_LAST_TIMESTAMP";
    public static final long PREF_LAST_TIMESTAMP_DEFAULT=0;
    
    static private SharedPreferences preferences=null;
    
    public static void Initialize(Context context){
    	if(preferences==null){
    		preferences=context.getApplicationContext().getSharedPreferences(PREF, 0);
    	}
    }
    
    public static long getLastTimestamp(){		
  		return preferences.getLong(Connection.PREF_LAST_TIMESTAMP, Connection.PREF_LAST_TIMESTAMP_DEFAULT);
  	}
    
    public static void setLastTimestamp(long timestamp){  		
  			preferences.edit().putLong(Connection.PREF_LAST_TIMESTAMP, timestamp).commit();
  	}

}

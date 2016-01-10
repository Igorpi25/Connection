package com.ivanov.tech.connection.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;
import com.codebutler.android_websockets.WebSocketClient.Listener;
import com.ivanov.tech.connection.Connection;

public abstract class ConnectionService extends Service implements Listener{

	private static final String TAG = ConnectionService.class
            .getSimpleName();    
        
    private static final String JSON_LAST_TIMESTAMP="last_timestamp";
    
    protected WebSocketClient websocketclient=null;	
    
	protected int startId;
	protected int userid;//Потому что Session не доступен
	
	protected ArrayList<TransportBase> transports=createTransports();
	
	//You should create list of TransportBase objects here
	public abstract ArrayList<TransportBase> createTransports();
	
	public void onCreate() {
	    super.onCreate();
	    Log.d(TAG, "onCreate");
	    
	    //If Internet connection change
	    IntentFilter filter = new IntentFilter();
	    filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");	    
	    registerReceiver(receiver_CONNECTIVITY_CHANGE, filter);
	    
	    //Initialize shared preferences
	    Connection.Initialize(getApplicationContext());
	    
	    for(TransportBase transport : transports){
	    	transport.onConnectionServiceCreate();
	    }
	}
	
	public void onDestroy() {
	    super.onDestroy();
	    Log.d(TAG, "onDestroy");
	    unregisterReceiver(receiver_CONNECTIVITY_CHANGE);
	    
	    for(TransportBase transport : transports){
	    	transport.onConnectionServiceDestroy();
	    }
	}
		
    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    
    	Log.d(TAG, "onStartCommand");
    	
	    this.startId=startId;
	    
	    if(websocketclient==null){
	    	//Обязательный параметр. Required parameter. Без этого сервер не может нас идентифицировать
	    	userid=intent.getIntExtra("userid", -1);//Так как Session недоступен в Connection вынуждены так делать
		    create_websocketclient();	
	    }
	    
	    websocketclient.connect();
	    	    
	    JSONObject json=null;
	    int transport=intent.getIntExtra("transport",0);
	    
	    if( (intent.hasExtra("json")) ){
		    try {
				json=new JSONObject(intent.getStringExtra("json"));
				
			} catch (JSONException e) {
				Log.d(TAG, "onStartCommand JSONException e="+e);
			}
	    }
	    
	    if(transport!=0){
	    	
	    	for(TransportBase _transport : transports){
	    		_transport.onOutgoingMessage(transport, json);
	    	}
	    }
	    
	    return START_NOT_STICKY;
	}
    
    @Override
	public IBinder onBind(Intent intent) {
		return null;
	}
    
    public void create_websocketclient(){
    	Log.d(TAG, "createWebSocketClient");

    	ArrayList<BasicNameValuePair> headers=new ArrayList<BasicNameValuePair>();    	
    	BasicNameValuePair header_userid=new BasicNameValuePair("userid",String.valueOf(userid));    	
    	BasicNameValuePair last_timestamp=new BasicNameValuePair("last_timestamp",String.valueOf(Connection.getLastTimestamp()));
    	headers.add(header_userid);
    	headers.add(last_timestamp);
    	
    	websocketclient = new WebSocketClient(URI.create(Connection.URL_WEBSOCKET), this, headers);
    	
    	for(TransportBase transport : transports){
    		transport.websocketclient=websocketclient;
    	}
    }
    
//------------WebsocketClientListener------------------------
    
    @Override
    public void onCreate(WebSocketClient websocketclient) {
    	Log.d(TAG, "onCreate(WebSocketClient)");
    	
    	for(TransportBase transport : transports){
    		transport.onCreate(websocketclient);
    	}
    }
    
    public void onConnect() {
    	Log.d(TAG, "onConnect");
		
		ResetConnectAttempts();
		
		for(TransportBase transport : transports){
    		transport.onConnect();
    	}
    }

    @Override
    public void onMessage(String message) {
    	Log.d(TAG, "onMessage message="+message);  

        JSONObject json=null;
	    int transport=0;
        
        try {
        	json = new JSONObject(message);
        
	    	if(json.has(JSON_LAST_TIMESTAMP)){
	    		long last_timestamp=json.getLong(JSON_LAST_TIMESTAMP);
	    		//Сохраняем last_timestamp в секундах. Нужно для следующего подключения в header-е запроса
	    		Connection.setLastTimestamp(last_timestamp);
	    	}
	    	
	    	if(json.has("transport")){
	    		transport=json.getInt("transport");
	    	}
	    	
        }catch(JSONException e){
        	Log.d(TAG, "onMessage JSONException e="+e);
        }
        
        for(TransportBase _transport : transports){
    		_transport.onMessage(message);
    	}
        
        if(transport!=0){
	        for(TransportBase _transport : transports){
	        	_transport.onIncomingMessage(transport, json);
	    	}
        }
        
    }

    @Override
    public void onMessage(byte[] data) {
    	Log.d(TAG, "onMessage data");  
    	for(TransportBase transport : transports){
    		transport.onMessage(data);
    	}
    }

    @Override
    public void onDisconnect(int code, String reason) {
        Log.d(TAG, String.format("onDisconnect code=%d Reason=%s", code, reason));
                
        Reconnect();
        
        for(TransportBase transport : transports){
    		transport.onDisconnect(code,reason);
    	}
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "onError error="+error);
      
        Reconnect();
        
        for(TransportBase transport : transports){
    		transport.onError(error);
    	}
    }
    
//----------------Reconnecting---------------------------
    
    public int reconnect_attempts=0;
	public int max_reconnect_attempts=7;
	public boolean reconnect_attempt_waiting=false; 
	
    void Reconnect(){    
    	
    	if((reconnect_attempts>=max_reconnect_attempts)){
    		handler_reconnectevents.sendEmptyMessage(0);
    		
    		return;
    	}
    	
    	if(!reconnect_attempt_waiting){
    		
    		reconnect_attempt_waiting=true;
    		
    		reconnect_attempts++;
    		timer_reconnect.schedule(new ReconnectTask(), 6000);   	
    	}    	
    	
    }
    
    void ResetConnectAttempts(){
    	handler_reconnectevents.sendEmptyMessage(2);
    	reconnect_attempts=0;
    }
    
    private static Timer timer_reconnect = new Timer(); 
    
    private final Handler handler_reconnectevents = new Handler(){
    	
        @Override
        public void handleMessage(Message msg)
        {
        	switch(msg.what){
        		case 0:
        			onReconnectRefused();
            	return;
            	
        		case 1:
        			onReconnectAttempt(reconnect_attempts);
	        	return;
	        	
	        	case 2:
	        		onReconnected(reconnect_attempts);
	            return;	
        	}        	
        }
    };  
    
    private class ReconnectTask extends TimerTask { 
        
    	public void run() 
        {
        	reconnect_attempt_waiting=false;        	
        	handler_reconnectevents.sendEmptyMessage(1);
        	websocketclient.connect();	
        }
    }    
    
    private final BroadcastReceiver receiver_CONNECTIVITY_CHANGE = new BroadcastReceiver() {

    	   @Override
    	   public void onReceive(Context context, Intent intent) {

    		   String action = intent.getAction();
    	      
    		   if(action.equals("android.net.conn.CONNECTIVITY_CHANGE")){
    			   if(intent.getExtras()!=null) {
    	    	        NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
    	    	        if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTED) {
    	    	        	if( (!reconnect_attempt_waiting)&&(reconnect_attempts>=max_reconnect_attempts) )
    	    	        		websocketclient.connect();
    	    	        }
    			   }    	        
    	      }
    	   }
    };
 
//-----------------Reconnecting Events---------------------------
    
    protected void onReconnectRefused(){
    	//Toast.makeText(getApplicationContext(), "Lets-Race: Connect refused", Toast.LENGTH_SHORT).show();
    }
    
    protected void onReconnectAttempt(int attempt){
    	//Toast.makeText(getApplicationContext(), "Lets-Race: connect attempt "+connectAttempts, Toast.LENGTH_SHORT).show();
    }
    
    protected void onReconnected(int attempt){
    	//Toast.makeText(getApplicationContext(), "Lets-Race: CONNECTED on "+connectAttempts, Toast.LENGTH_LONG).show();
    }

	
}

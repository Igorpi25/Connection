package com.ivanov.tech.connection.service;

import org.json.JSONObject;

import com.codebutler.android_websockets.WebSocketClient;
import com.codebutler.android_websockets.WebSocketClient.Listener;
import com.codebutler.android_websockets.WebSocketClient.OutgoingListener;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

public abstract class TransportBase extends ContextWrapper implements Listener,OutgoingListener{

	private static final String TAG = TransportBase.class
            .getSimpleName();    
    
	public TransportBase(Context context) {
		super(context);	
	}
		
    public WebSocketClient websocketclient=null;	
	
//----------------Methods---------------------------
	
	public void sendMessage(int outgoing_failed_type, int message_id, JSONObject json){
		websocketclient.sendWithOutgoingProtocol(outgoing_failed_type, message_id, json.toString(), this);
	}

//------------ConnectionService------------------------
	
	public void onConnectionServiceCreate() {	    
	    Log.d(TAG, "onConnectionServiceCreate");	    
	}
	
	public void onConnectionServiceDestroy() {	   
	    Log.d(TAG, "onConnectionServiceDestroy");
	}

//------------TransportProtocol------------------
	
	public boolean onOutgoingMessage(int transport, JSONObject json){	
		Log.d(TAG, "onOutgoingMessage transport="+transport+" json="+json);
		return false;
	}
	
	public boolean onIncomingMessage(int transport, JSONObject json){
		Log.d(TAG, "onIncomingMessage transport="+transport+" json="+json);
		return false;
	}
	
	@Override
	public void onOutgoingFailed(int outgoing_failed_type, int message_id ) {
		Log.d(TAG, "onOutgoingFailed outgoing_failed_type="+outgoing_failed_type+" message_id="+message_id);
	}
		
//------------WebSocketClient.Listener------------------------
	    
	@Override
	public void onCreate(WebSocketClient websocketclient) {
		Log.d(TAG, "onConnect(WebSocketClient)");
	}
	
    public void onConnect() {
    	Log.d(TAG, "onConnect");
    }

    @Override
    public void onMessage(String message) {
    	Log.d(TAG, "onMessage message="+message);  
    }

    @Override
    public void onMessage(byte[] data) {
    	Log.d(TAG, "onMessage data");    		    	
    }

    @Override
    public void onDisconnect(int code, String reason) {
        Log.d(TAG, String.format("onDisconnect code=%d Reason=%s", code, reason));        
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "onError error="+error);      
    }

}

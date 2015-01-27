package fr.coyot.notifbus.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.net.http.AndroidHttpClient;
import android.util.Log;

public final class GetHTTP {

	public final static String getURL (String url){
		Log.d("Get URL", url);
		StringBuilder content = new StringBuilder();
		try
    	{
    		AndroidHttpClient hc = AndroidHttpClient.newInstance("Android");
    		HttpGet get = new HttpGet(url);
    		
    		HttpResponse response = hc.execute(get);

    		Log.d("Get URL", String.valueOf(response.getStatusLine().getStatusCode()));
    		
    		if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
    		{
    			InputStream str = response.getEntity().getContent();
    			BufferedReader reader = new BufferedReader(new InputStreamReader(str));
    	        String line;
    	        while ((line = reader.readLine()) != null) {
    	        	content.append(line);
    	        }
    		}
    		hc.close();
    	}catch(IOException e){
    		e.printStackTrace();
    	}
		return content.toString();
	}
}

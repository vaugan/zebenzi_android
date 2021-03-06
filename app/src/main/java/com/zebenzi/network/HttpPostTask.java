package com.zebenzi.network;

/**
 * Created by Vaugan.Nayagar on 2015/09/17.
 */

import android.content.Context;
import android.os.AsyncTask;

import com.zebenzi.ui.R;

//import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents an asynchronous http post task
 *
 */
public class HttpPostTask extends AsyncTask<Object, String, String> {

    private Context ctx;
    private IAsyncTaskListener listener;
    private boolean networkError = false;
    private String mUrl;
    private HashMap<String, String> mHeader = null;
    String mBody;


    public HttpPostTask(Context ctx, IAsyncTaskListener<String> listener) {
        this.ctx = ctx;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Object... params) {
        OutputStream os = null;
        HttpURLConnection conn = null;
        String resultToDisplay = "";
        BufferedWriter writer;

        mUrl = (String)params[0];
        mHeader = (HashMap)params[1];
        HttpContentTypes type = (HttpContentTypes)params[3];

        try {
            URL url = new URL(mUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(10000);
            conn.setRequestMethod(ctx.getString(R.string.api_rest_post));
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            //All headers should be passed in as a hashmap
            if (mHeader != null) {
                Iterator<String> it = mHeader.keySet().iterator();
                while(it.hasNext()){
                    String key = it.next();
                    conn.setRequestProperty(key, mHeader.get(key));
                }
            }
            conn.connect();

            os = conn.getOutputStream();
            //Build output string using body - params[2]
            switch(type){
                case X_WWW_FORM_URLENCODED:
                    writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    Map<String,String> listBody = (Map<String,String>)params[2];
                    mBody = getEncodedData(listBody);
                    break;
                case RAW:
                    writer = new BufferedWriter(new OutputStreamWriter(os));
                    JSONObject jsonBody = (JSONObject)params[2];
                    mBody = jsonBody.toString();
                    break;

                //Unsupported content types, so return null.
                case BINARY:
                case FORM_DATA:
                default:
                    System.out.println("Unsupported content type: "+type);
                    return null;
            }

            //Write params to output string
            writer.write(mBody);
            writer.flush();

            //If successful connection, read input stream, else read error stream
            if (conn.getResponseCode() / 100 == 2) { // 2xx code means success
                StringBuilder sb = new StringBuilder();
                String line = "";

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                resultToDisplay = sb.toString();

            } else {

                StringBuilder sb = new StringBuilder();
                String line = "";

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                resultToDisplay = sb.toString();

                System.out.println("Error = "+conn.getResponseCode());
                System.out.println("Error Stream = " + resultToDisplay);
            }
        }
        catch (SocketTimeoutException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        catch (Exception e) {
            networkError = true;
            e.printStackTrace();
            return e.getMessage();

        } finally {

            try {
                os.close();
                conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        //This will be either input stream or error stream
        return resultToDisplay;
    }


    @Override
    protected void onPostExecute(final String result) {
        listener.onAsyncTaskComplete(result, networkError);
    }

    @Override
    protected void onCancelled() {
        listener.onAsyncTaskCancelled();
    }

    private String getEncodedData(Map<String,String> data) {
        StringBuilder sb = new StringBuilder();
        for(String key : data.keySet()) {
            String value = null;
            try {
                value = URLEncoder.encode(data.get(key),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if(sb.length()>0)
                sb.append("&");

            sb.append(key + "=" + value);
        }
        return sb.toString();
    }
}

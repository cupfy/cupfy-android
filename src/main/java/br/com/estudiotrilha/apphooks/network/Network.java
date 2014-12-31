package br.com.estudiotrilha.apphooks.network;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.net.UnknownHostException;

/**
 * This class is responsible to communicate with API.
 */
public class Network
{
    public static final String TAG = "NETWORK";

    private Delegate delegateReceiver;

    public final static int GET 	= 0;
    public final static int POST 	= 1;

    public static class Status
    {
        public boolean hasInternet = false;
        public HttpClientHelper httpClientHelper = null;
        public HttpResponse response = null;
        public String result = null;
        public String value = null;
    }

    /**
     * Creates a new request to the API.
     * @param client The object that stores all the required information for the request.
     * @param method Whether is GET or POST. Use the static values provided on this class.
     * @param delegate The callback function.
     */
    public static final void newRequest(HttpClientHelper client, int method, Delegate delegate)
    {
        new APIRequest(client, method, delegate).execute("");
    }

    public static class APIRequest extends AsyncTask<String, String, String>
    {
        private HttpClientHelper client = null;
        private int	method = 0;

        /*
         * 0: GET
         * 1: POST
         */
        private Delegate delegateReceiver		= null;
        private HttpResponse response			= null;
        private String result				    = null;
        private String value					= null;
        private boolean hasInternet				= true;

        public APIRequest(HttpClientHelper client, int method, Delegate delegateReceiver)
        {
            this.client 			= client;
            this.method 			= method;
            this.delegateReceiver 	= delegateReceiver;
        }

        @Override
        protected String doInBackground(String... data)
        {
            try
            {
                switch(this.method)
                {
                    case GET:
                        this.response = client.executeGet();
                        break;
                    case POST:
                        this.response = client.executePost();
                        break;
                    default:
                        this.response = client.executeGet();
                }

                HttpEntity entity = response.getEntity();

                this.result = EntityUtils.toString(entity);
            }
            catch (UnknownHostException e) { e.printStackTrace(); }
            catch(Exception e) { e.printStackTrace(); }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(this.response == null)
            {
                this.hasInternet = false;
            }

            try
            {
                Log.d(TAG,  ((this.method == GET) ? "[GET] " : "[POST] ") +
                        "URI: " + client.getURI() + "\nRESPONSE: " + response.getStatusLine().toString());
                Log.d(TAG, "POST: "+ client.postParams().toString());
            } catch(NullPointerException e) {
                Log.d(TAG, "NO INTERNET CONNECTION!");
            }

            Network.Status status = new Network.Status();

            status.hasInternet = this.hasInternet;
            status.httpClientHelper = this.client;
            status.response = this.response;
            status.result = this.result;
            status.value = this.value;

            if(delegateReceiver != null)
            {
                delegateReceiver.requestResults(status);
            }
        }
    }
}
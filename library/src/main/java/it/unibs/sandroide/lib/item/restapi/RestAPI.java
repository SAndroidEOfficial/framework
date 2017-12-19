/**
 * Copyright (c) 2016 University of Brescia, Alessandra Flammini, All rights reserved.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package it.unibs.sandroide.lib.item.restapi;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import it.unibs.sandroide.lib.BLEContext;

import static android.content.Context.MODE_PRIVATE;

public class RestAPI {
    private SharedPreferences sharedPref;
    private Context context;
    private String apiName;
    private Map<String, OnApiCallListener> mapListeners;
    private JSONObject api,              // contains api description from JSON
            apiConfig = null; // contains configuration parameteters: app id, session token, ecc...
    boolean reloadSharedPrefOnNextCall = false;
    public final static String HAS_EXTRA_LABEL = "hasExtra";

    public RestAPI(Context ctx, String name, JSONObject config) {
        mapListeners = new HashMap<>();

        context = ctx;
        apiName = name;
        api = BLEContext.findRestApi(name);
        sharedPref = context.getSharedPreferences(apiName, MODE_PRIVATE);
        apiConfig = reloadConfigFromSharedPref();

        // overwrite with passed starting configuration
        Iterator<String> keys = config.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            try {
                apiConfig.put(k, config.optString(k, ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        sharedPref.edit().putString("apiConfig", apiConfig.toString()).apply();
    }

    public void authenticate() {
        reloadSharedPrefOnNextCall = true;
        Intent intent = new Intent(context, Oauth2Activity.class);
        intent.putExtra(HAS_EXTRA_LABEL, true);
        intent.putExtra("sharedPrefId",apiName);
        intent.putExtra("callId","auth");
        intent.putExtra("api",api.toString());
        context.startActivity(intent);
    }

    public void setOnApiCallListener(String callId, OnApiCallListener listener) {
        mapListeners.put(callId, listener);
    }

    public void runApiCall(String callId, JSONObject params) {
        if (mapListeners.containsKey(callId)) {
            runApiCall(callId, params, mapListeners.get(callId));
        } else {
            throw new RuntimeException(String.format("You must first set Listener for call \"%s\"", callId));
        }
    }

    public void runApiCall(String callId, JSONObject params, OnApiCallListener listener) {
        if (reloadSharedPrefOnNextCall) reloadConfigFromSharedPref();

        // we create a task because network operations must be performed in background, not UI thread
        new submitApiTask().execute(callId, params, listener);
    }

    protected class submitApiTask extends AsyncTask<Object, Void, JSONObject> {

        private OnApiCallListener listener = null;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected JSONObject doInBackground(Object... params) {
            if (params.length == 3) {
                String callId = (String) params[0];
                JSONObject callParams = (JSONObject) params[1];
                listener = (OnApiCallListener) params[2];

                return prepareAndRunApiMethod(callId, callParams);
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            int status = -1;
            if (result != null) {
                status = result.optInt("status", 400);
                String bodyResult = result.optString("body", "");
                listener.onResult(status, bodyResult);
            } else {
                throw new RuntimeException("Api Response result not implemented!");
            }
        }
    }

    protected JSONObject prepareAndRunApiMethod(String apiCallId, JSONObject methodParameters) {
        // we add api bundleObj parameters to method parameters
        Iterator<String> keys = apiConfig.keys();
        methodParameters = methodParameters == null ? new JSONObject() : methodParameters;
        while (keys.hasNext()) {
            String k = keys.next();
            try {
                methodParameters.put(k, apiConfig.optString(k, ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return submitApiRequest(apiCallId, methodParameters);
    }

    protected JSONObject submitApiRequest(String apiCallId, JSONObject methodParameters) {
        JSONObject ret = new JSONObject();
        try {
            StringBuilder total = new StringBuilder();

            // replace methodparameters
            JSONObject apiCall = new JSONObject(replaceCallParameters(api.getJSONObject(apiCallId).toString(), methodParameters));

            // configure url connection
            URL url = new URL(apiCall.getString("url"));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(apiCall.optString("method", "GET"));

            // set headers
            JSONObject headers = apiCall.optJSONObject("headers");
            if (headers != null) {
                Iterator<String> keys = headers.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    urlConnection.setRequestProperty(k, headers.optString(k, ""));
                }
            }

            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                ret.put("status", urlConnection.getResponseCode())
                        .put("body", total.toString());
            } catch (FileNotFoundException e) {
                ret.put("status", urlConnection.getResponseCode())
                        .put("body", urlConnection.getResponseMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return ret;
    }

    protected static String replaceCallParameters(String old, JSONObject params) {
        params = params == null ? new JSONObject() : params;
        Iterator<String> keys = params.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            old = old.replaceAll("\\[" + k + "\\]", params.optString(k, ""));
        }
        return old;
    }

    protected JSONObject reloadConfigFromSharedPref() {
        try {
            apiConfig = new JSONObject(sharedPref.getString("apiConfig", "{}"));
            reloadSharedPrefOnNextCall = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return apiConfig;
    }
}

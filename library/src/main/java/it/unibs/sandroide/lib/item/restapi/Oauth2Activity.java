/**
 * Copyright (c) 2016 University of Brescia, Alessandra Flammini, All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package it.unibs.sandroide.lib.item.restapi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Set;

public class Oauth2Activity extends Activity {
    WebView webView;
    Context context = this;
    ProgressDialog progressBar;

    SharedPreferences sharedPref;
    String sharedPrefId;
    private String callId;
    private JSONObject apiConfig = new JSONObject();
    private JSONObject api = new JSONObject();

    @SuppressLint({ "NewApi", "NewApi", "NewApi" })
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Bundle b = getIntent().getExtras();
        sharedPrefId = b.getString("sharedPrefId","api-id-or-RestItem-id");
        callId = b.getString("callId","action-id");
        sharedPref = context.getSharedPreferences(sharedPrefId, MODE_PRIVATE);
        try {
            apiConfig = new JSONObject(sharedPref.getString("apiConfig", "{}"));
            api = new JSONObject(b.getString("api","{}")); // sharedPref.getString("api", "{}")
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new loginTask().execute();
    }

    private void authPageWithWebview() {
        try {
            progressBar = new ProgressDialog(context);
            progressBar.setCancelable(true);
            progressBar.setMessage("Loading...");
            progressBar.show();

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    String finalToken = view.getUrl().replace("#","");
                    Uri newurl = Uri.parse(finalToken);
                    Set<String> paramNames = newurl.getQueryParameterNames();
                    String access_token="";

                    // TODO: I parametri della query string risultante devono essere memorizzati nelle shared preferences all'interno di un jsonobject
                    // che ha come chiave il nome della API (es. fitbitAPI.access_token)
                    for (String key: paramNames) {
                        try {
                            apiConfig.put(callId+"."+key,newurl.getQueryParameter(key));
                        } catch (JSONException e) {
                        }
                        switch(key) {
                            case "access_token":
                                access_token = newurl.getQueryParameter(key);
                                break;
                            default: break;
                        }
                    }

                    if (progressBar.isShowing()) {
                        progressBar.dismiss();
                    }

                    // access_token is standard in Oauth2 RFC, thus it is always present when authentication succeeds
                    if (access_token.length()>0) {
                        sharedPref.edit().putString("apiConfig",apiConfig.toString()).commit();
                        new isLoggedInTask().execute();
                    }
                }
            });


            String fitUrl = replaceCallParameters(api.getJSONObject(callId).getString("url"),apiConfig);
            webView.loadUrl(fitUrl);

            webView.requestFocus(View.FOCUS_DOWN);
            webView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    switch (arg1.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_UP:
                            if (!arg0.hasFocus()) {
                                arg0.requestFocus();
                            }
                            break;
                    }
                    return false;
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    protected String replaceCallParameters(String old, JSONObject params){
        params = params==null?new JSONObject():params;
        Iterator<String> keys = params.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            old = old.replaceAll("\\["+k+"\\]",params.optString(k,""));
        }
        return old;
    }


    public class loginTask extends AsyncTask<JSONObject, Void, JSONObject> {

        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(context, "Please wait",
                    "Loading please wait..", true);
            progressDialog.setCancelable(true);

        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            return null;
            //return submitApiRequest();
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (result==null)
                authPageWithWebview();
            /*if (result==null)
                authPageWithChromeTabs();*/ // chrome tabs auth currently does not work
            progressDialog.dismiss();
        }
    }


    public class isLoggedInTask extends AsyncTask<JSONObject, Void, JSONObject> {

        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            /*progressDialog = ProgressDialog.show(context, "Please wait",
                    "Loading please wait..", true);
            progressDialog.setCancelable(true);
*/
        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            return null;
            //return submitApiRequest();
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            /*//progressDialog.dismiss();
            Intent intent = new Intent(Oauth2Activity.this,
                    MainActivity.class);
            // intent.putExtra() // here we can return the result (auth success or failure), which should invoke eventual failed api calls, invoked before authentication
            startActivity(intent);*/
            finish();
        }
    }

    // TODO: we need to switch to use chrome tabs instead of webview
    private void authPageWithChromeTabs() {
        //String fitUrl = String.format("https://www.fitbit.com/oauth2/authorize?response_type=token&client_id=%s&redirect_uri=%s&expires_in=%d&scope=%s", oauth2_clientid, URLEncoder.encode(redirect_uri,"UTF-8"),expires_in,"activity%20heartrate%20location%20nutrition%20profile%20settings%20sleep%20social%20weight");
        /*String fitUrl = String.format("https://www.fitbit.com/oauth2/authorize?response_type=token&client_id=%s&redirect_uri=&expires_in=%d&scope=%s",oauth2_clientid,expires_in,"activity%20heartrate%20location%20nutrition%20profile%20settings%20sleep%20social%20weight");
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(fitUrl));*/

    }

/*
    private JSONObject submitApiRequest() {
        JSONObject obj = null;
        try {
            URL url = new URL("https://api.fitbit.com/1/user/-/profile.json");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization",String.format("Bearer %s",access_token));
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                }
                obj = new JSONObject(total.toString());
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return obj;
    }*/




    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack() == true) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

}

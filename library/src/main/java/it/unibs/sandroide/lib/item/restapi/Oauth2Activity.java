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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.customtabs.CustomTabsIntent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Set;

public class Oauth2Activity extends Activity {
    private Context context = this;

    private SharedPreferences sharedPref;
    private String callId;
    private JSONObject apiConfig = new JSONObject();
    private JSONObject api = new JSONObject();
    private String fitUrl;
    private final static String CALLBACK_URL = "sandroide-oauth://";
    private boolean authPageLaunched;

    @SuppressLint({"NewApi", "NewApi", "NewApi"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authPageLaunched = false;
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        if (getIntent().hasExtra(RestAPI.HAS_EXTRA_LABEL)) {
            Bundle b = getIntent().getExtras();

            String sharedPrefId = b.getString("sharedPrefId", "api-id-or-RestItem-id");
            callId = b.getString("callId", "action-id");
            sharedPref = context.getSharedPreferences(sharedPrefId, MODE_PRIVATE);
            try {
                apiConfig = new JSONObject(sharedPref.getString("apiConfig", "{}"));
                api = new JSONObject(b.getString("api", "{}")); // sharedPref.getString("api", "{}")
            } catch (JSONException e) {
                e.printStackTrace();
            }

            authPageWithChromeTabs();
        } else
            finish();
    }


    protected String replaceCallParameters(String old, JSONObject params) {
        params = params == null ? new JSONObject() : params;
        Iterator<String> keys = params.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            old = old.replaceAll("\\[" + k + "\\]", params.optString(k, ""));
        }
        return old;
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Se l'activity si sta per chiudere allora ha visualizzato la pagina di auth
        authPageLaunched = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Se la pagina auth l'ho visualizzata ma non ho ricevuto alcun intent -> l'utente ha schiacciato il tasto back senza fare autorizzazione.
        //-> chiudo l'activity
        if (authPageLaunched && getIntent().getData() == null) {
            finish();
        }
    }

    private void authPageWithChromeTabs() {
        try {
            fitUrl = replaceCallParameters(api.getJSONObject(callId).getString("url"), apiConfig);
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(context, Uri.parse(fitUrl));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_VIEW)) {

            Uri uriData = intent.getData();
            if (uriData != null && uriData.toString().startsWith(CALLBACK_URL)) {
                String urlCallback = uriData.toString();
                String finalToken = urlCallback.replace("#", "");
                Uri newurl = Uri.parse(finalToken);
                Set<String> paramNames = newurl.getQueryParameterNames();
                String access_token = "";
                // TODO: I parametri della query string risultante devono essere memorizzati nelle shared preferences all'interno di un jsonobject
                // che ha come chiave il nome della API (es. fitbitAPI.access_token)
                for (String key : paramNames) {
                    try {
                        apiConfig.put(callId + "." + key, newurl.getQueryParameter(key));
                    } catch (JSONException e) {
                    }

                    if (key.equals("access_token")) {
                        access_token = newurl.getQueryParameter(key);
                        // access_token is standard in Oauth2 RFC, thus it is always present when authentication succeeds
                        if (access_token.length() > 0) {
                            sharedPref.edit().putString("apiConfig", apiConfig.toString()).apply();
                        }
                        break;
                    } else if (key.equals("error_description")) {
                        //In caso di errore rimuoviamo le info di configurazione salvate
                        sharedPref.edit().remove("apiConfig").apply();
                        break;
                    }

                }
            }

            setResult(RESULT_OK, intent);
        }
        finish();
    }



}

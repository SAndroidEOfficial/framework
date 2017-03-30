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

package it.unibs.sandroide.test;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import it.unibs.sandroide.R;
import it.unibs.sandroide.lib.activities.SandroideBaseActivity;
import it.unibs.sandroide.lib.item.restapi.OnApiCallListener;
import it.unibs.sandroide.lib.item.restapi.RestAPI;

public class MainActivityFitbitApi extends SandroideBaseActivity {
    RestAPI fitbitAPI,openweatherAPI,ilmeteoitAPI,facebookAPI;

    Button btnFitbitLogin, btnFitbitSteps, btnFitbitOther, btnOpenWeather, btnIlMeteoDaily, btnIlMeteoHourly, btnFacebook, btnTwitter, btnStackoverflow,btnMyFacebook;
    TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitbit);

        try {
            fitbitAPI = new RestAPI(this,"api_fitbit_v1",new JSONObject()
                    .put("oauth2_clientid", "228CLH")
                    .put("redirect_uri", URLEncoder.encode("https://www.example.com/index.html?","UTF-8"))
                    .put("expires_in", 604800)
                    .put("scope", "activity%20heartrate%20location%20nutrition%20profile%20settings%20sleep%20social%20weight"));

            facebookAPI = new RestAPI(this,"api_facebook",new JSONObject()
                    .put("client_id", "190680884759825")
                    .put("redirect_uri", URLEncoder.encode("http://es3.unibs.it/SAndroidE/?","UTF-8")));


            // api specification taken from  http://openweathermap.org/appid
            openweatherAPI = new RestAPI(this,"api_open_weather",new JSONObject()
                    .put("apikey", "70270028f45e80cccde15ab6029a1fc3"));

            // api specification taken from http://www.ilmeteo.it/portale/dati-meteo-xml
            ilmeteoitAPI = new RestAPI(this,"api_il_meteo_it_roma",new JSONObject());

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Qui creo un bottone per avviare il workflow di autenticazione, ma in teoria tale workflow dovrebbe essere
        // chiamato in automatico ogni qualvolta un metodo della api chiamato, restituisca "401 Unauthorized user"
        // o cmq il mess di errore che indica che ci si deve autenticare
        btnFitbitLogin = (Button) findViewById(R.id.buttonLogin);
        btnFitbitSteps = (Button) findViewById(R.id.buttonSteps);
        btnFitbitOther = (Button) findViewById(R.id.buttonOtherCall);
        btnOpenWeather = (Button) findViewById(R.id.buttonOpenWeather);
        btnIlMeteoDaily = (Button) findViewById(R.id.buttonilMeteoDaily);
        btnIlMeteoHourly = (Button) findViewById(R.id.buttonilMeteoHourly);
        btnFacebook = (Button) findViewById(R.id.buttonFacebook);
        btnMyFacebook = (Button) findViewById(R.id.buttonMyFacebook);
        btnTwitter = (Button) findViewById(R.id.buttonTwitter);
        btnStackoverflow = (Button) findViewById(R.id.buttonStackoverflow);
        tvResult = (TextView) findViewById(R.id.tvApiResult);

        btnFitbitLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fitbitAPI.authenticate();
            }
        });


        btnFitbitSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    fitbitAPI.runApiCall("daily_activity_summary", new JSONObject().put("user-id", "-").put("date", "2017-03-01"), new OnApiCallListener() {
                        @Override
                        public void onResult(int statusCode, String body) {
                            callListener(body);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // another way to call the API, set the listener once, then call it later
        fitbitAPI.setOnApiCallListener("heartrate",new OnApiCallListener() {
            @Override
            public void onResult(int statusCode, String body) {
                callListener(body);
            }
        });

        btnFitbitOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    fitbitAPI.runApiCall("heartrate", new JSONObject()
                            .put("user-id", "-")
                            .put("date", "2017-02-01")
                            .put("period", "1m"));

                 } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        btnOpenWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    openweatherAPI.runApiCall("getByCity", new JSONObject()
                            .put("cityid", "524901"), new OnApiCallListener() {
                        @Override
                        public void onResult(int statusCode, String body) {
                            callListener(body);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        btnIlMeteoDaily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ilmeteoitAPI.runApiCall("daily", new JSONObject(), new OnApiCallListener() {
                    @Override
                    public void onResult(int statusCode, String body) {
                        callListener(body);
                    }
                });
            }
        });
        btnIlMeteoHourly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ilmeteoitAPI.runApiCall("hourly", new JSONObject(), new OnApiCallListener() {
                    @Override
                    public void onResult(int statusCode, String body) {
                        callListener(body);
                    }
                });
            }
        });

        btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    facebookAPI.runApiCall("userfeed", new JSONObject().put("user_id","Metallica"), new OnApiCallListener() {
                        @Override
                        public void onResult(int statusCode, String body) {
                            callListener(body);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        btnMyFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookAPI.runApiCall("me", new JSONObject(), new OnApiCallListener() {
                    @Override
                    public void onResult(int statusCode, String body) {
                        try {
                            JSONObject me = new JSONObject(body);
                            facebookAPI.runApiCall("userfeed", new JSONObject().put("user_id",me.get("id")), new OnApiCallListener() {
                                @Override
                                public void onResult(int statusCode, String body) {
                                    callListener(body);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    // listener is called, when the api call succeeds
    protected void callListener(String result) {
        tvResult.setText(result);
        Log.i("MainActivityFitbitApi",result);
        // il risultato può poi essere parsato come json, o altro a seconda del suo formato
        //obj = new JSONObject(total.toString());
    }



}

package es.sancho.david.forecast;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by David on 12/12/2014.
 *
 * @author David
 */
public class WeatherConn {

    public static final String TAG = WeatherConn.class.getSimpleName();

    public static final String LONDON = "London";
    public static final String MADRID = "Madrid";

    public static final String[][] TRANSLATOR = {
            {"500", "light rain"},
            {"501", "moderate rain"},
            {"502", "heavy intensity rain"},
            {"503", "very heavy rain"},
            {"504", "extreme rain"},
            {"511", "freezing rain"},
            {"520", "light intensity shower rain"},
            {"521", "shower rain"},
            {"522", "heavy intensity shower rain"},
            {"531", "ragged shower rain"},
            {"701", "mist"},
            {"711", "smoke"},
            {"721", "haze"},
            {"731", "sand, dust whirls"},
            {"741", "fog"},
            {"751", "sand"},
            {"761", "dust"},
            {"762", "volcanic ash"},
            {"771", "squalls"},
            {"781", "tornado"}
    };

    public static String getWeatherString(String city) {
        //These two need to be declared outside the try/catch
        //so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        //Will contain the raw JSON response as a string.
        String forecastJsonStr = null;
        //Construct the URL for the OpenWeather Map query
        //Possible parameters are avaiable at OWM's forecast API page, at http://openweathermap.org/API#forecast
        URL url = null;
        try {
            String urlString = "http://api.openweathermap.org/data/2.5/forecast/daily?q=Madrid&mode=json&units=metric&cnt=7";
            url = new URL(urlString);
            //Create the request to OpenWeather Map, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            //Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                //Nothing to do.
                return "";
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                //Since it's JSON, adding a new line isn't necessary (it won't affect parsing)
                //But it does make debugginga *lot* easier if youpr into ut the completed
                //buffer for debugging.
                buffer.append(line);
                buffer.append("\n");
            }
            if (buffer.length() == 0) {
                //Stream was empty. No point in parsing.
                return "";
            }
            forecastJsonStr = buffer.toString();
        } catch (MalformedURLException | ProtocolException e) {
            Log.e(TAG, "Error", e);
        } catch (IOException e) {
            Log.e(TAG, "Error", e);
            //If the code didn't successfully get the weather data,there's no point in attemping to parse it.
            return "";
        } catch(Throwable t) {
            Log.e(TAG, "Error", t);
            return "";
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Errorclosingstream", e);
                }
            }

        }
        return forecastJsonStr == null ? "" : forecastJsonStr;
    }

    public String getTranslation(String code) {
        for (String[] translation : TRANSLATOR) {
            if(translation.length > 1) {
                String codeAux = translation[0];
                if(codeAux.equals(code)) {
                    return translation[1];
                }
            }
        }
        return code;
    }



}

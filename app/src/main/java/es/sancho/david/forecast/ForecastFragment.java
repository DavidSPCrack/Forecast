package es.sancho.david.forecast;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by usuario.apellido on 12/12/2014.
 *
 * @author David
 */
public class ForecastFragment extends Fragment {

    private final static String TAG = ForecastFragment.class.getSimpleName();

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView lista = (ListView) rootView.findViewById(R.id.ListViewForeCast);
        //String[] tiempos = getResources().getStringArray(R.array.tiempos);

        FetchWeatherTask fetch = new FetchWeatherTask();
        fetch.execute();
        String result = null;
        try {
            result = fetch.get();
        } catch (Throwable t) {
            Log.e(TAG, "Error", t);
            result = "";
        }
        Log.v(TAG, result);

        String[] tiempos = new String[0];
        try {
            if (!result.isEmpty())
                tiempos = getWeatherDataFromJson(result, 7);
        } catch (JSONException e) {
            tiempos = new String[0];
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, tiempos);
        lista.setAdapter(adapter);

        return rootView;
    }

    /*
    * The date/time conversion code is going to be moved outside the asynctask later,
    * so for convenience we're breaking it out into its own method now.
    */
    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);
        StringBuilder sb = new StringBuilder();
        sb.append(roundedHigh);
        sb.append("/");
        sb.append(roundedLow);
        return sb.toString();
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy: constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {
        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DATETIME = "dt";
        final String OWM_DESCRIPTION = "main";
        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
        String[] resultStrs = new String[numDays];
        for (int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;
            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);
            // The date/time is returned as a long. We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime = dayForecast.getLong(OWM_DATETIME);
            day = getReadableDateString(dateTime);
            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            // Temperatures are in a child object called "temp". Try not to name variables
            // "temp" when working with temperature. It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);
            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }
        return resultStrs;
    }

    public static class FetchWeatherTask extends AsyncTask<String, Integer, String> {

        private final static String TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String doInBackground(String[] params) {
            String city = params.length > 0 ? params[0] : WeatherConn.MADRID;
            String weatherString = WeatherConn.getWeatherString(city);
            return weatherString;
        }
    }
}


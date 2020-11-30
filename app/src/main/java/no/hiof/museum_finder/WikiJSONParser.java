package no.hiof.museum_finder;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;


public class WikiJSONParser {

    /**
     * Methoad that replaces white space in a string with underscores
     * @param title - The museum title to be parsed
     * @return - The parsed title
     */
    private String parseTitle(String title) {
        return title.replace(" ", "_");
    }

    /**
     * A method that utilizes Volley to parse data from a WikiMedia response based on a Museum title.
     * Firstly is parses the title to fit the WikiMedia request URL (Munch musem = Munch_museum), sets up
     * a JsonObjectRequest (Volley) and sets request method to GET. OnResponse (if the request has response)
     * parse the data to retrieve the intro section for the requested WikiPedia page.
     * @param title - Museum title to be parsed
     * @param requestQueue - The RequestQueue where the request should be performed
     * @param description - The TextView to display the parsed data (Museum description)
     */
    public void parseWikiData(String title, RequestQueue requestQueue, TextView description) {
        String urlTitle = parseTitle(title);

        String downloadUrl = "https://en.wikipedia.org/w/api.php?action=query&format=json&titles=" + urlTitle + "&prop=extracts&exintro&explaintext";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, downloadUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject wikidata = response.getJSONObject("query").getJSONObject("pages");
                    Iterator<String> keys = wikidata.keys();
                    String key = "";

                    while(keys.hasNext()) {
                        key = keys.next();
                    }

                    String intro = wikidata.getJSONObject(key).getString("extract");

                    description.setText(!intro.isEmpty() ? intro : "The retrieved description is empty");

                } catch (JSONException e) {
                    e.printStackTrace();
                    description.setText(R.string.no_description);
                    System.out.println("ERROR I ADAPTER");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(request);
    }




}

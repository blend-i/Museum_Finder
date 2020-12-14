package no.hiof.museum_finder;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NearbySearchJSONParser2 {

    /**
     * Method that handles parsing of a single museum object, retrieved from a Nearby Search
     * Request in the HomeFragment. Gets name, latitude and longitude, photo, rating, id and
     * opening hours from the search request and puts the data in a HashMap.
     *
     * @param jsonObject - Museum JSONObject to be parsed
     * @return - dataList (HashMap containing name of the museum, latitude, longitude, photo, rating,
     * id and opening hours of the museum)
     */
    private HashMap<String, String> parseMuseumJSONObject(JSONObject jsonObject) {
        HashMap<String, String> dataList = new HashMap<>();

        try {
            String name = jsonObject.getString("name");
            String latitude = jsonObject.getJSONObject("geometry")
                    .getJSONObject("location").getString("lat");
            String longitude = jsonObject.getJSONObject("geometry")
                    .getJSONObject("location").getString("lng");

            if (jsonObject.has("photos")) {
                String photo = jsonObject.getJSONArray("photos").getJSONObject(0).getString("photo_reference");
                dataList.put("photo", photo);
            } else {
                dataList.put("photo", "0");
            }

            if (jsonObject.has("rating")) {
                String rating = jsonObject.getString("rating");
                dataList.put("rating", rating);
            } else {
                dataList.put("rating", "0");
            }

            String placeId = jsonObject.getString("place_id");

            if (!jsonObject.has("opening_hours")) {
                dataList.put("open", "false");
            } else {
                String open = jsonObject.getJSONObject("opening_hours").getString("open_now");
                dataList.put("open", open);
            }

            dataList.put("name", name);
            dataList.put("lat", latitude);
            dataList.put("lng", longitude);
            dataList.put("placeId", placeId);

        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
        return dataList;
    }


    /**
     * Method that iterates the result of a Nearby Search Request, for every museum
     * object, pass it to parseMuseumJSONObject to retrive the specified values of
     * the museum and add the data to a List of Hashmaps.
     *
     * @param jsonArray - JSONArray retrieved from Nearby Search Request
     * @return - Return the dataList (List containing HashMaps (each HashMap represents a museum object)
     */
    private List<HashMap<String, String>> parseMuseumJSONArray(JSONArray jsonArray) {
        List<HashMap<String, String>> datalist = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                HashMap<String, String> data = parseMuseumJSONObject((JSONObject) jsonArray.get(i));
                datalist.add(data);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
        return datalist;
    }


    /**
     * Method that retrieves the "results" from the nearby search request (JSONArray of museum objects
     * retrieved from request) and passes it to the parseMuseumJSONArray (which utilizes parseMuseumJSONObject)
     * to create the the List of Hashmaps, which is further used to create museums objects in HomeFragment which
     * are put on an ArrayList and then put in the museum rescycler adapter.
     * (Museums within the specified radius of the device in use)
     *
     * @param jsonObject - JSONObject retrieved from a Nearby Search Request
     * @return - Parsed museum data (List of HashMaps)
     */
    public List<HashMap<String, String>> parseResult(JSONObject jsonObject) {
        JSONArray jsonArray = null;
        try {
            jsonArray = jsonObject.getJSONArray("results");
            System.out.println("JSON ARRAY: " + jsonArray);
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
        return parseMuseumJSONArray(jsonArray);
    }
}
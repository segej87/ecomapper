package com.gmail.jonsege.androiddatacollection;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jon on 9/24/16.
 */

class Record {

    //region Class Variables

    /**
     * Tag for the class
     */
    private final static String TAG = "record";

    /**
     * Context creating the record
     */
    private final Context context;

    /**
     * Variables for the record
     */
    final String type;
    Double[] coords = new Double[3];
    String photoPath;
    Map<String, Object> props = new HashMap<>();

    //endregion

    //region Initialization

    Record(Context context, String type, Double[] coords, String photo, Map<String, Object> props){
        this.context = context;
        this.type = type;
        this.coords = coords;
        this.props = props;
        this.photoPath = photo;
    }

    //endregion

    //region Data I/O

    /**
    * A method to encode the record in JSON format
    * */
    @SuppressWarnings("unchecked") JSONObject recordJsonEncode () {
        // The JSON object to return
        JSONObject recordJsonOut = new JSONObject();

        // A JSON object for storing properties
        JSONObject propsJsonOut = new JSONObject();
        try {

            // Create the geometry key and define its type
            JSONObject geomJsonOut = new JSONObject();
            geomJsonOut.put("type", type);

            // Create a JSON object with the record's coordinates
            JSONArray coordsJsonOut = new JSONArray();
            for (int c = 0; c < coords.length; c++) {
                coordsJsonOut.put(c,coords[c]);
            }

            // Add coordinates to the geometry
            geomJsonOut.put("coordinates", coordsJsonOut);

            // Loop through the record's properties keys and add to a JSON object
            for (String k : props.keySet()) {
                if (props.get(k) instanceof List) {
                    JSONArray arrAdd = new JSONArray();
                    for (String i : (List<String>) props.get(k)) {
                        arrAdd.put(i);
                    }
                    propsJsonOut.put(k, arrAdd);
                } else {
                    propsJsonOut.put(k, props.get(k));
                }
            }

            // Add the keys to the feature array
            recordJsonOut.put("geometry", geomJsonOut);
            recordJsonOut.put("type", "Feature");
            recordJsonOut.put("properties", propsJsonOut);

            return recordJsonOut;
        } catch (JSONException e) {
            Log.i(TAG,context.getString(R.string.json_encode_error,e.getLocalizedMessage()));
        }

        return recordJsonOut;
    }

    //endregion

}

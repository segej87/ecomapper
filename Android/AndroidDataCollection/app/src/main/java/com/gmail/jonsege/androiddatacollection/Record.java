package com.gmail.jonsege.androiddatacollection;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created for the Kora project by jon on 9/24/16.
 */

class Record {

    //region Class Variables

    /**
     * Tag for the class
     */
    private final static String TAG = "record";

    /**
     * Variables for the record
     */
    private final String type;
    Double[] coords = new Double[3];

    // The path to the record's media on the local filesystem
    final String photoPath;
    Map<String, Object> props = new HashMap<>();

    //endregion

    //region Initialization

    Record(String type, Double[] coords, String photo, Map<String, Object> props){
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

            // If the record is a photo, add the blob filepath.
            if (props.get("datatype").equals("photo")) {
                String blobPath = UserVars.blobRootString +
                        UserVars.UUID + "/" +
                        photoPath.substring(photoPath.lastIndexOf('/') + 1);

                propsJsonOut.put("filepath", blobPath);
            }

            // Add the keys to the feature array
            recordJsonOut.put("geometry", geomJsonOut);
            recordJsonOut.put("type", "Feature");
            recordJsonOut.put("properties", propsJsonOut);

            return recordJsonOut;
        } catch (JSONException e) {
            Log.e(TAG, "Error encoding record to JSON: " + e.getLocalizedMessage());
        }

        return recordJsonOut;
    }

    //endregion

}

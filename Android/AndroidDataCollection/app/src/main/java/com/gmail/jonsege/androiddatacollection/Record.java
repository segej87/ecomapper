package com.gmail.jonsege.androiddatacollection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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

    protected Double[] coords = new Double[3];
    protected String photoPath;
    protected Map<String, Object> props = new HashMap<>();

    //endregion

    //region Initialization

    Record(Double[] coords, String photo, Map<String, Object> props){
        this.coords = coords;
        this.props = props;
        this.photoPath = photo;
    }

    //endregion

    //region Data I/O

    /*
    * A method to encode the record in JSON format
    * */
    JSONObject recordJsonEncode () {
        JSONObject recordJsonOut = new JSONObject();

        JSONObject propsJsonOut = new JSONObject();
        try {
            // Write the "geometry" key of the feature
            JSONObject geomJsonOut = new JSONObject();
            geomJsonOut.put("type", "Point");

            JSONArray coordsJsonOut = new JSONArray();
            for (double c : coords) {
                coordsJsonOut.put(c);
            }
            geomJsonOut.put("coordinates", coordsJsonOut);

            // Write the properties array
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
            System.out.println("Error writing record to JSON: " + e.getLocalizedMessage());
        }

        return recordJsonOut;
    }

    //endregion

    //region Getters and Setters



    //endregion

}

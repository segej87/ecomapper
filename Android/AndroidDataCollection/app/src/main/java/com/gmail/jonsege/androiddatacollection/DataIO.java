package com.gmail.jonsege.androiddatacollection;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jon Sege on 10/1/2016.
 */

final class DataIO {

    //region Class Variables

    /**
     * Tag for this class
     */
    private final static String TAG = "data_io";

    //endregion

    //region Login

    /**
     * Saves the user ID to a shared preferences file, or clears it on logout
     * @param context context
     * @param uuid UserID
     * @return report
     */
    static String saveLogin(Context context, String uuid) {
        String errorString;

        try {

            //Try to create an editor for the login preferences file
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.login_save_file),0);
            SharedPreferences.Editor editor = sharedPref.edit();

            // If the uuid is the logout flag, clear the user ID from the preferences file.
            // Otherwise, put the new uuid in the preferences file.
            if (uuid.equals(context.getString(R.string.logout_flag))) {

                // Get the old user ID to log.
                String oldUUID = loadLogin(context);

                // Remove the user ID from the preferences file.
                editor.remove(context.getString(R.string.uuid_save_key));
                editor.apply();

                // Return a report
                return context.getString(R.string.io_success) + ": " + oldUUID.replace(context.getString(R.string.io_success) + ": ","");
            } else {

                // Put the new uuid into the preferences file.
                editor.putString(context.getString(R.string.uuid_save_key), uuid);
                editor.apply();

                // Return a report
                return context.getString(R.string.io_success) + ": " + uuid;
            }
        } catch (Exception e) {
            errorString = e.getLocalizedMessage();
        }

        // Return a report
        return context.getString(R.string.save_login_failure,errorString);
    }

    /**
     * Loads the user ID from a shared preferences file
     * @param context context
     * @return report
     */
    static String loadLogin(Context context) {
        String errorString;

        try {

            // Try to read the saved user ID from the login preferences file.
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.login_save_file),0);
            String defaultUUID = "";
            String savedUUID = sharedPref.getString(context.getString(R.string.uuid_save_key), defaultUUID);

            // Return a report.
            return context.getString(R.string.io_success) + ": " + savedUUID;
        } catch (Exception e) {
            errorString = e.getLocalizedMessage();
        }

        // Return a report.
        return context.getString(R.string.load_login_failure,errorString);
    }

    //endregion

    //region User Variables

    static boolean meshUserVars(Context context, String result) {
        try {

            // First, try to load saved user variables, if they exist.
            String loadResult = loadUserVars(context);

            // If a saved UserVars file could not be loaded, pass the context and
            // json result directly to the set list method.
            if (!loadResult.contains(context.getString(R.string.io_success))) {
                setLists(context, result);
                return true;
            }

            // Try to decode the list server's response as a JSON string.
            JSONObject jObject = new JSONObject(result);

            // Add access levels to User Variables only if they aren't already there.
            JSONArray accessArray = jObject.getJSONArray("institutions");
            for (int i = 0; i < accessArray.length(); i++) {
                if (!UserVars.AccessLevels.contains(accessArray.getString(i))) {
                    UserVars.AccessLevels.add(accessArray.getString(i));
                }
            }

            // For lists pulled from the server, tag each as server and 0 count.
            Object[] addArray = createUserVarAddArray("Server");

            // Put values from each JSON Array into the appropriate maps.
            JSONArray tagsArray = jObject.getJSONArray("tags");
            for (int i = 0; i < tagsArray.length(); i++) {
                UserVars.Tags.put(tagsArray.getString(i),addArray);
            }

            JSONArray specArray = jObject.getJSONArray("species");
            for (int i = 0; i < specArray.length(); i++) {
                UserVars.Species.put(specArray.getString(i),addArray);
            }

            JSONArray unitArray = jObject.getJSONArray("units");
            for (int i = 0; i < unitArray.length(); i++) {
                UserVars.Units.put(unitArray.getString(i),addArray);
            }

            return true;
        } catch (JSONException e) {

            // Log the error.
            Log.i(TAG,context.getString(R.string.json_decode_error,e.getLocalizedMessage()));
        }

        return false;
    }

    /**
     * Saves the user variables to a shared preferences file
     * @param context context
     * @return report
     */
    static String saveUserVars(Context context) {
        String errorString;

        // The output stream for saving
        FileOutputStream os;

        // Set up the UserVars as a JSON Object to write
        JSONObject jsonObject = new JSONObject();
        try {

            // For string objects, put in JSON Object as is
            jsonObject.put("UUID", UserVars.UUID);
            jsonObject.put("UName", UserVars.UName);
            jsonObject.put("UserVarsSaveFileName", UserVars.UserVarsSaveFileName);
            jsonObject.put("RecordsSaveFileName",UserVars.RecordsSaveFileName);
            jsonObject.put("MediasSaveFileName",UserVars.MediasSaveFileName);
            jsonObject.put("AccessLevels",new JSONArray(UserVars.AccessLevels));
            jsonObject.put("AccessDefaults",new JSONArray(UserVars.AccessDefaults));
            jsonObject.put("TagsDefaults",new JSONArray(UserVars.TagsDefaults));
            if (UserVars.SpecDefault == null) {
                jsonObject.put("SpecDefault", "");
            } else {
                jsonObject.put("SpecDefault", UserVars.SpecDefault);
            }

            if (UserVars.UnitsDefault == null) {
                jsonObject.put("UnitsDefault", "");
            } else {
                jsonObject.put("UnitsDefault", UserVars.UnitsDefault);
            }

            // For maps, loop to encode each
            JSONObject jTags = encodeJavaMaps(context, UserVars.Tags);
            jsonObject.put("Tags",jTags);
            JSONObject jSpec = encodeJavaMaps(context, UserVars.Species);
            jsonObject.put("Species",jSpec);
            JSONObject jUnit = encodeJavaMaps(context, UserVars.Units);
            jsonObject.put("Units",jUnit);
        } catch (JSONException e) {
            Log.i(TAG,context.getString(R.string.json_encode_error,e.getLocalizedMessage()));
        }

        // Write the JSON object to the output file
        try {
            // Open the output stream and write the object's data.
            os = context.openFileOutput(UserVars.UserVarsSaveFileName,Context.MODE_PRIVATE);
            os.write(jsonObject.toString().getBytes());

            // Close the output stream
            os.close();

            Log.i(TAG,context.getString(R.string.user_vars_save_success,jsonObject.toString(),UserVars.UserVarsSaveFileName));

            // Return a report.
            return context.getString(R.string.io_success) + ": " + UserVars.UserVarsSaveFileName;
        } catch (Exception e) {
            errorString = e.getLocalizedMessage();
        }

        // Return a report.
        return context.getString(R.string.save_user_vars_failure,errorString);
    }

    /**
     * Loads the user variables from as hared preferences file
     * @param context context
     * @return report
     */
    static String loadUserVars(Context context) {
        String errorString;

        // A JSON object to store data from the file.
        JSONObject jResult;

        // The result string builder.
        StringBuilder result = new StringBuilder();

        // The file with saved user variables.
        File uVarFile = new File(context.getFilesDir(),UserVars.UserVarsSaveFileName);

        try {

            // Try to read the user variables file.
            BufferedReader br = new BufferedReader(new FileReader(uVarFile));
            String line;

            // Loop through the read lines and build the result string.
            while ((line = br.readLine()) != null) {
                result.append(line);
                result.append('\n');
            }
            br.close();

            // Create a JSON Object from the result string.
            jResult = new JSONObject(result.toString());

            // Set the string user variables by pulling objects from the result JSON object.
            UserVars.UName = jResult.get("UName").toString();

            UserVars.RecordsSaveFileName = jResult.get("RecordsSaveFileName").toString();

            UserVars.MediasSaveFileName = jResult.get("MediasSaveFileName").toString();

            // Add the access levels to the default values.
            JSONArray alJArray = (JSONArray) jResult.get("AccessLevels");
            List<String> alList = new ArrayList<>();
            for (int i=0; i<alJArray.length(); i++) {
                alList.add(alJArray.get(i).toString());
            }
            UserVars.AccessLevels = alList;

            // Set the list user variables by first decoding them.
            JSONObject tagsIn = (JSONObject) jResult.get("Tags");
            UserVars.Tags = decodeJsonLists(context, tagsIn);

            JSONObject specIn = (JSONObject) jResult.get("Species");
            UserVars.Species = decodeJsonLists(context, specIn);

            JSONObject unitIn = (JSONObject) jResult.get("Units");
            UserVars.Units = decodeJsonLists(context, unitIn);

            // Set the defaults.
            JSONArray aldJArray = (JSONArray) jResult.get("AccessDefaults");
            for (int i=0; i < aldJArray.length(); i++) {
                if (!UserVars.AccessDefaults.contains(aldJArray.getString(i)))
                    UserVars.AccessDefaults.add(aldJArray.getString(i));
            }

            JSONArray tdJArray = (JSONArray) jResult.get("TagsDefaults");
            for (int i=0; i < tdJArray.length(); i++) {
                if (!UserVars.TagsDefaults.contains(tdJArray.getString(i)))
                    UserVars.TagsDefaults.add(tdJArray.getString(i));
            }

            UserVars.SpecDefault = jResult.getString("SpecDefault");
            if (UserVars.SpecDefault.equals("")) UserVars.SpecDefault = null;

            UserVars.UnitsDefault = jResult.getString("UnitsDefault");
            if (UserVars.UnitsDefault.equals("")) UserVars.UnitsDefault = null;

            // Return a report.
            return context.getString(R.string.io_success);
        } catch (Exception e) {
            errorString = e.getLocalizedMessage();
        }

        // Return a report.
        return context.getString(R.string.load_user_vars_failure, errorString);
    }

    /**
     * Updates and save user variables when a new record is added, or a record is modified
     * @param context
     *      The calling context
     * @param record
     *      The record to check for user variable updates
     * @return
     *      Returns true if the user variables were updated successfully
     */
    @SuppressWarnings("unchecked") static boolean addUserVars(Context context, Record record) {
        Map<String, Object> props = record.props;

        String dt = props.get("datatype").toString();

        Object[] addArray = createUserVarAddArray("Local");

        try {
            List<String> newTags = (List<String>) props.get("tags");

            for (String t : newTags) {
                if (!UserVars.Tags.keySet().contains(t)) {
                    UserVars.Tags.put(t, addArray);
                    Log.i(TAG, context.getString(R.string.add_new_user_var_entry, "tag", t));
                } else {
                    String tagFlag = UserVars.Tags.get(t)[0].toString();
                    if (tagFlag.equals("Local")) {
                        int oldCount = (int) UserVars.Tags.get(t)[1];
                        int newCount = oldCount + 1;

                        Object[] setArray = new Object[2];
                        setArray[0] = "Local";
                        setArray[1] = newCount;

                        UserVars.Tags.put(t, setArray);
                    }
                }
            }

            if (dt.equals("meas")) {
                String newSpec = props.get("species").toString();

                if (!UserVars.Species.keySet().contains(newSpec)) {
                    UserVars.Species.put(newSpec, addArray);
                    Log.i(TAG, context.getString(R.string.add_new_user_var_entry, "species", newSpec));
                } else {
                    String specFlag = UserVars.Species.get(newSpec)[0].toString();
                    if (specFlag.equals("Local")) {
                        int oldCount2 = (int) UserVars.Species.get(newSpec)[1];
                        int newCount2 = oldCount2 + 1;

                        Object[] setArray2 = new Object[2];
                        setArray2[0] = "Local";
                        setArray2[1] = newCount2;

                        UserVars.Species.put(newSpec, setArray2);
                    }
                }

                String newUnit = props.get("units").toString();

                if (!UserVars.Units.keySet().contains(newUnit)) {
                    UserVars.Units.put(newUnit, addArray);
                    Log.i(TAG, context.getString(R.string.add_new_user_var_entry, "units", newUnit));
                } else {
                    String unitFlag = UserVars.Units.get(newUnit)[0].toString();
                    if (unitFlag.equals("Local")) {
                        int oldCount3 = (int) UserVars.Units.get(newUnit)[1];
                        int newCount3 = oldCount3 + 1;

                        Object[] setArray3 = new Object[2];
                        setArray3[0] = "Local";
                        setArray3[1] = newCount3;

                        UserVars.Units.put(newUnit, setArray3);
                    }
                }
            }

            saveUserVars(context);
            return true;
        } catch (Exception e) {
            Log.i(TAG, context.getString(R.string.general_error_prefix, e.getLocalizedMessage()));
        }

        return false;
    }

    /**
     * Updates and save user variables when a record is removed
     * @param context
     *      The calling context
     * @param record
     *      The record that is being removed
     * @return
     *      Returns true if the user variables were updated successfully
     */
    @SuppressWarnings("unchecked") static boolean removeUserVars(Context context, Record record) {

        Map<String, Object> props = record.props;

        String dt = props.get("datatype").toString();

        try {
            List<String> newTags = (List<String>) props.get("tags");

            for (String t : newTags) {
                if (!UserVars.Tags.keySet().contains(t)) {
                    Log.i(TAG, context.getString(R.string.general_error_report));
                } else {
                    if (UserVars.Tags.get(t)[0].toString().equals("Local")) {
                        int oldCount = (int) UserVars.Tags.get(t)[1];
                        if (oldCount == 1) {
                            UserVars.Tags.remove(t);
                        } else {
                            int newCount = oldCount - 1;

                            Object[] newArray = new Object[2];
                            newArray[0] = "Local";
                            newArray[1] = newCount;

                            UserVars.Tags.put(t, newArray);
                        }
                    }
                }
            }

            if (dt.equals("meas")) {
                String newSpec = props.get("species").toString();

                if (!UserVars.Species.keySet().contains(newSpec)) {
                    Log.i(TAG, context.getString(R.string.general_error_report));
                } else {
                    if (UserVars.Species.get(newSpec)[0].toString().equals("Local")) {
                        int oldCount2 = (int) UserVars.Species.get(newSpec)[1];
                        if (oldCount2 == 1) {
                            UserVars.Species.remove(newSpec);
                        } else {
                            int newCount2 = oldCount2 - 1;

                            Object[] newArray2 = new Object[2];
                            newArray2[0] = "Local";
                            newArray2[1] = newCount2;

                            UserVars.Species.put(newSpec, newArray2);
                        }
                    }
                }

                String newUnit = props.get("units").toString();

                if (!UserVars.Units.keySet().contains(newUnit)) {
                    Log.i(TAG, context.getString(R.string.general_error_report));
                } else {
                    if (UserVars.Units.get(newUnit)[0].toString().equals("Local")) {
                        int oldCount3 = (int) UserVars.Units.get(newUnit)[1];
                        if (oldCount3 == 1) {
                            UserVars.Units.remove(newUnit);
                        } else {
                            int newCount3 = oldCount3 - 1;

                            Object[] newArray3 = new Object[2];
                            newArray3[0] = "Local";
                            newArray3[1] = newCount3;

                            UserVars.Units.put(newUnit, newArray3);
                        }
                    }
                }
            }

            saveUserVars(context);
            return true;
        } catch (Exception e) {
            Log.i(TAG, context.getString(R.string.general_error_prefix, e.getLocalizedMessage()));
        }

        return false;
    }

    /**
     * Resets the user variables in the current application context to their default values.
     * @param context context
     * @return report
     */
    @NonNull
    static String resetUserVars(Context context) {
        UserVars.UUID = "testing";
        UserVars.UName = "Testing";
        UserVars.UserVarsSaveFileName = "UserVars-testing";
        UserVars.RecordsSaveFileName = "Records-testing";
        UserVars.MediasSaveFileName = "Medias-testing";
        UserVars.AccessLevels = new ArrayList<String>() {{
            add("Public");
            add("Private");
        }};
        UserVars.Tags = new HashMap<>();
        UserVars.Species = new HashMap<>();
        UserVars.Units = new HashMap<>();
        UserVars.AccessDefaults = new ArrayList<>();
        UserVars.TagsDefaults = new ArrayList<>();
        UserVars.SpecDefault = null;
        UserVars.UnitsDefault = null;

        // Return a report.
        return context.getString(R.string.io_success);
    }

    //endregion

    //region Records

    /**
     * Saves records as JSON data to a file in the application's data folder
     * @param context context
     * @param records records
     * @return report
     */
    static String saveRecords(Context context, List<Record> records) {

        // Report an error by default.
        String result = context.getString(R.string.save_record_failure);

        // The output stream to save data to the file.
        FileOutputStream os;

        // The JSON object to write to the file.
        JSONObject recordsJObject = new JSONObject();

        try {

            // Loop through the records and put each JSON encoded record in a JSON array.
            JSONArray recordsJArray = new JSONArray();
            for (Record r : records) {
                recordsJArray.put(r.recordJsonEncode());
            }

            // Log that n records are being saved.
            Log.i(TAG,context.getString(R.string.save_record_report,recordsJArray.length()));

            // Put the records array into the JSON Object to write.
            recordsJObject.put("type", "FeatureCollection");
            recordsJObject.put("features", recordsJArray);
        } catch (Exception e) {
            result += e.getLocalizedMessage();
        }

        // Write the JSON object to the output file
        try {

            // Try to write the JSON Object's data to the output stream.
            os = context.openFileOutput(UserVars.RecordsSaveFileName,Context.MODE_PRIVATE);
            os.write(recordsJObject.toString().getBytes());
            os.close();

            // Return a report.
            return context.getString(R.string.io_success) + ": " + recordsJObject.toString();
        } catch (Exception e) {
            result += e.getLocalizedMessage();
        }

        // Return a report.
        return result;
    }

    static JSONObject loadFullRecordsFile(Context context) {
        // A JSON Object to decode data from the file.
        JSONObject jResult = null;

        // The file containing the record data.
        File recFile = new File(context.getFilesDir(),UserVars.RecordsSaveFileName);

        // A string to read data from the file.
        StringBuilder dataString = new StringBuilder();

        try {

            // Try to read the file using a buffered reader.
            BufferedReader br = new BufferedReader(new FileReader(recFile));
            String line;

            // Loop through the reader's data and build the result string.
            while ((line = br.readLine()) != null) {
                dataString.append(line);
                dataString.append('\n');
            }
            br.close();

            // Decode the result string using a JSON Object.
            jResult = new JSONObject(dataString.toString());
        } catch (Exception e) {

            // Log an error.
            Log.i(TAG,context.getString(R.string.load_record_failure,e.getLocalizedMessage()));
        }

        return jResult;
    }

    /**
     * Loads records from JSON data from af ile in the application's data folder
     * @param context context
     * @return records
     */
    static List<Record> loadRecords(Context context) {

        // The list to hold records to return.
        List<Record> recordsOut = new ArrayList<>();

        // Try to load the records from the save file.
        JSONObject jResult = loadFullRecordsFile(context);

        if (jResult != null) {
            try {

                // Read the records from the JSON Object as a JSON Array.
                JSONArray recJArray = (JSONArray) jResult.get("features");

                // Log that n records are being loaded
                Log.i(TAG, context.getString(R.string.load_record_report, recJArray.length()));

                // Loop through the records in the features array.
                for (int i = 0; i < recJArray.length(); i++) {
                    JSONObject rec = (JSONObject) recJArray.get(i);

                    // Read the geometry key from the record.
                    JSONObject geom = (JSONObject) rec.get("geometry");

                    // Get the record type.
                    String type = geom.get("type").toString();

                    // Read the coordinates from the geometry object and add to the coordinates array.
                    JSONArray coords = (JSONArray) geom.get("coordinates");
                    Double[] coordsOut = new Double[coords.length()];
                    for (int j = 0; j < coords.length(); j++) {
                        coordsOut[j] = Double.valueOf(coords.get(j).toString());
                    }

                    // Read the properties of the record as a JSON Object.
                    JSONObject props = (JSONObject) rec.get("properties");

                    // Iterate over the JSON Object's keyset
                    Iterator<String> iter = props.keys();
                    Map<String, Object> propsOut = new HashMap<>();
                    while (iter.hasNext()) {
                        String k = iter.next();

                        // Get the property using the current keyset value.
                        Object prop = props.get(k);

                        if (prop instanceof JSONArray) {

                            // If the property is a JSON array, put the property into a List.
                            List<String> propArray = new ArrayList<>();
                            for (int l = 0; l < ((JSONArray) prop).length(); l++) {
                                propArray.add(((JSONArray) prop).get(l).toString());
                            }
                            propsOut.put(k, propArray);
                        } else {

                            propsOut.put(k, prop);
                        }
                    }

                    // Finish the iteration
                    iter.remove();

                    String photoPath = null;
                    // Get the photo filepath (if the datatype is photo).
                    if (propsOut.get("datatype").equals("photo")) {
                        photoPath = propsOut.get("filepath").toString();
                    }

                    // Add the decoded record to the list to return.
                    recordsOut.add(new Record(type, coordsOut, photoPath, propsOut));
                }
            } catch (Exception e) {

                // Log an error.
                Log.i(TAG, context.getString(R.string.load_record_failure, e.getLocalizedMessage()));
            }
        }

        // Return the records list.
        return recordsOut;
    }

    static boolean deleteRecord(Context context, Record record) {
        MyApplication app = (MyApplication) context.getApplicationContext();

        boolean deleted = app.deleteRecord(record);

        if (deleted) {
            boolean updated = removeUserVars(context, record);

            if (updated) {
                String saveResult = saveRecords(context, app.getRecords());

                if (saveResult.contains(context.getString(R.string.io_success))) {
                    return true;
                }
            }
        }

        return false;
    }

    //endregion

    //region Medias

    //endregion

    //region Helper Methods

    /**
     * Encodes Java Maps as JSON Objects
     * @param context context
     * @param inMap map
     * @return JSONObject
     */
    private static JSONObject encodeJavaMaps(Context context, Map<String, Object[]> inMap) {

        // Initialize the JSON Object to return.
        JSONObject jObj = new JSONObject();

        try {
            // Loop through the keys in the map.
            for (String k : inMap.keySet()) {
                List<Object> varsOut = new ArrayList<>();

                // Get value for the current key as an Object array.
                Object[] uVarsVals = inMap.get(k);

                // Add all objects from the array as a list.
                varsOut.addAll(Arrays.asList(uVarsVals));

                // Put the list into a JSON Array and add it to the JSON Object to return.
                jObj.put(k, new JSONArray(varsOut));
            }
        } catch (JSONException e) {

            // Log an error.
            Log.i(TAG,context.getString(R.string.json_encode_error,e.getLocalizedMessage()));
        }

        // Return the JSON Object (or null if there was an error).
        return jObj;
    }

    /**
     * Decodes JSON Objects to Java Maps
     * @param context context
     * @param inObj JSONObject
     * @return Map
     */
    @Nullable
    private static Map<String, Object[]> decodeJsonLists(Context context, JSONObject inObj) {

        // Initialize the Map to return.
        Map<String, Object[]> outMap = new HashMap<>();

        try {

            // Iterate over the JSON Object's keyset
            Iterator<String> iter = inObj.keys();
            while (iter.hasNext()) {
                String k = iter.next();

                // Read the value for the current key as a JSON Array.
                JSONArray kJArray = (JSONArray) inObj.get(k);

                // Put the each value from the JSON Array into a new Object array.
                int kJLength = kJArray.length();
                Object[] kVal = new Object[kJLength];
                for (int i=0; i<kJLength; i++) kVal[i] = kJArray.get(i);

                // Put the object array into the map for the current key.
                outMap.put(k,kVal);
            }
        } catch (Exception e) {

            // Log that an error occurred.
            Log.i(TAG,context.getString(R.string.json_decode_error,e.getLocalizedMessage()));
        }

        // Return the Map (or null if an error occurred).
        return outMap;
    }

    @org.jetbrains.annotations.Contract(pure = true)
    private static Object[] createUserVarAddArray(String mode) {
        Object[] addArray = new Object[2];
        addArray[0] = mode;
        if (mode.equals("Server")) {
            addArray[1] = 0;
        } else if (mode.equals("Local")) {
            addArray[1] = 1;
        }
        return addArray;
    }

    //endregion

    //region Server Ops

    /**
     * Retrieves lists from the server
     * @param context context
     * @param uuid UserID
     * @return response
     */
    static String retrieveLists (Context context, String uuid) {
        String response;

        // The URL for the PHP script to retrieve lists from the server.
        final String listsURL = context.getString(R.string.php_server_root) + context.getString(R.string.php_get_lists);

        // Parameters for the server request.
        Map<String,Object> pars = new LinkedHashMap<>();
        pars.put("GUID", uuid);

        try {
            StringBuilder postData = new StringBuilder();

            // Loop through the server request parameters.
            for (Map.Entry<String,Object> par : pars.entrySet()) {

                // Append & before adding another parameter if necessary
                if (postData.length() != 0) postData.append('&');

                // Append the parameter name and set it equal to the parameter value.
                postData.append(URLEncoder.encode(par.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(par.getValue()), "UTF-8"));
            }

            // Create a data array of the request string.
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            //TODO: Check for internet connection. If not return error.

            // Create a connection to the list retrieve URL.
            URL url = new URL(listsURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Set up the request to the connection.
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty( "Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            // Get the input stream from the server response.
            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            // Read the server's response into a string builder.
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;)
                sb.append((char)c);
            response = sb.toString();

            // Disconnect.
            conn.disconnect();

        } catch (Exception e) {

            // Fill the response string with the error message.
            response = context.getString(R.string.server_connection_error, e.getLocalizedMessage());
        }

        // Log the server's response.
        Log.i(TAG,context.getString(R.string.list_server_response,response));

        // Return a response.
        return response;
    }

    /**
     * Fill User Variables with lists pulled down from the server
     * @param context context
     * @param result result
     * @return boolean
     */
    static boolean setLists (Context context, String result) {
        try {

            // Try to decode the list server's response as a JSON string.
            JSONObject jObject = new JSONObject(result);

            // Add access level's to the default values in the User Variables
            JSONArray accessArray = jObject.getJSONArray("institutions");
            for (int i = 0; i < accessArray.length(); i++) {
                if (!UserVars.AccessLevels.contains(accessArray.getString(i))) {
                    UserVars.AccessLevels.add(accessArray.getString(i));
                }
            }

            // For lists pulled from the server, tag each as server and 0 count.
            Object[] addArray = createUserVarAddArray("Server");

            // Put values from each JSON Array into the appropriate maps.
            JSONArray tagsArray = jObject.getJSONArray("tags");
            for (int i = 0; i < tagsArray.length(); i++) {
                UserVars.Tags.put(tagsArray.getString(i),addArray);
            }

            JSONArray specArray = jObject.getJSONArray("species");
            for (int i = 0; i < specArray.length(); i++) {
                UserVars.Species.put(specArray.getString(i),addArray);
            }

            JSONArray unitArray = jObject.getJSONArray("units");
            for (int i = 0; i < unitArray.length(); i++) {
                UserVars.Units.put(unitArray.getString(i),addArray);
            }

            return true;
        } catch (JSONException e) {

            // Log the error.
            Log.i(TAG,context.getString(R.string.json_decode_error,e.getLocalizedMessage()));
        }

        return false;
    }

    static boolean attemptSync(Context context) {
        String response;

        // The URL for the PHP script to retrieve lists from the server.
        final String addURL = context.getString(R.string.php_server_root) + context.getString(R.string.php_add_records);

        JSONObject jsonObject = loadFullRecordsFile(context);

        // Parameters for the server request.
        Map<String,Object> pars = new LinkedHashMap<>();
        pars.put("GUID", UserVars.UUID);
        pars.put("geojson", jsonObject.toString().replace("\\/","/"));
        System.out.println("Using json string: " + pars.get("geojson"));

        try {
            StringBuilder postData = new StringBuilder();

            // Loop through the server request parameters.
            for (Map.Entry<String,Object> par : pars.entrySet()) {

                // Append & before adding another parameter if necessary
                if (postData.length() != 0) postData.append('&');

                // Append the parameter name and set it equal to the parameter value.
                postData.append(URLEncoder.encode(par.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(par.getValue()), "UTF-8"));
            }

            // Create a data array of the request string.
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            //TODO: Check for internet connection. If not return error.

            // Create a connection to the list retrieve URL.
            URL url = new URL(addURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Set up the request to the connection.
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty( "Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            // Get the input stream from the server response.
            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            // Read the server's response into a string builder.
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;)
                sb.append((char)c);
            response = sb.toString();

            // Disconnect.
            conn.disconnect();

        } catch (Exception e) {

            // Fill the response string with the error message.
            response = context.getString(R.string.server_connection_error, e.getLocalizedMessage());
        }

        // Log the server's response.
        Log.i(TAG,context.getString(R.string.list_server_response,response));

        return response.contains("Success!");
    }

    //endregion

}

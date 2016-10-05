package com.gmail.jonsege.androiddatacollection;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by jonse on 10/1/2016.
 */

public final class DataIO extends AppCompatActivity {

    //region Login

    public static String saveLogin(Context context, String uuid) {
        String errorString = new String();

        try {
            SharedPreferences sharedPref = ((Activity) context).getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(context.getString(R.string.uuid_save_key), uuid);
            editor.apply();

            return context.getString(R.string.io_success) + ": " + uuid;
        } catch (Exception e) {
            errorString = e.getLocalizedMessage();
        }

        return context.getString(R.string.save_login_failure) + ": " + errorString;
    }

    public static String loadLogin(Context context) {
        String errorString = new String();

        try {
            SharedPreferences sharedPref = ((Activity) context).getPreferences(Context.MODE_PRIVATE);
            String defaultUUID = "";
            String savedUUID = sharedPref.getString(context.getString(R.string.uuid_save_key), defaultUUID);

            return context.getString(R.string.io_success) + ": " + savedUUID;
        } catch (Exception e) {
            errorString = e.getLocalizedMessage();
        }

        return context.getString(R.string.load_login_failure) + ": " + errorString;
    }

    //endregion

    //region User Variables

    public static String saveUserVars(Context context) {
        String errorString = new String();
        FileOutputStream os;

        // Set up the UserVars as a json array to write
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("UUID", UserVars.UUID);
            jsonObject.put("UName", UserVars.UName);
            jsonObject.put("UserVarsSaveFileName", UserVars.UserVarsSaveFileName);
            jsonObject.put("RecordsSaveFileName",UserVars.RecordsSaveFileName);
            jsonObject.put("MediasSaveFileName",UserVars.MediasSaveFileName);
            jsonObject.put("AccessLevels",new JSONArray(UserVars.AccessLevels));
            JSONObject jTags = encodeJsonLists(context, UserVars.Tags);
            jsonObject.put("Tags",jTags);
            JSONObject jSpec = encodeJsonLists(context, UserVars.Species);
            jsonObject.put("Species",jSpec);
            JSONObject jUnit = encodeJsonLists(context, UserVars.Units);
            jsonObject.put("Units",jUnit);
        } catch (JSONException e) {
            System.out.println(context.getString(R.string.json_encode_error) +
                    ": " + e.getLocalizedMessage());
        }

        // Write the JSON object to the output file
        try {
            os = context.openFileOutput(UserVars.UserVarsSaveFileName,Context.MODE_PRIVATE);
            os.write(jsonObject.toString().getBytes());
            os.close();

            return context.getString(R.string.io_success) + ": " + jsonObject.toString();
        } catch (Exception e) {
            errorString = e.getLocalizedMessage();
        }

        return context.getString(R.string.save_user_vars_failure) + ": " + errorString;
    }

    public static String loadUserVars(Context context) {
        String errorString = new String();
        JSONObject jResult;
        StringBuilder result = new StringBuilder();

        File uVarFile = new File(context.getFilesDir(),UserVars.UserVarsSaveFileName);

        try {
            BufferedReader br = new BufferedReader(new FileReader(uVarFile));
            String line;

            while ((line = br.readLine()) != null) {
                result.append(line);
                result.append('\n');
            }
            br.close();

            jResult = new JSONObject(result.toString());
            UserVars.UName = jResult.get("UName").toString();

            UserVars.RecordsSaveFileName = jResult.get("RecordsSaveFileName").toString();

            UserVars.MediasSaveFileName = jResult.get("MediasSaveFileName").toString();

            JSONArray alJArray = (JSONArray) jResult.get("AccessLevels");
            List<String> alList = new ArrayList<>();
            for (int i=0; i<alJArray.length(); i++) {
                alList.add(alJArray.get(i).toString());
            }
            UserVars.AccessLevels = alList;

            JSONObject tagsIn = (JSONObject) jResult.get("Tags");
            UserVars.Tags = decodeJsonLists(context, tagsIn);

            JSONObject specIn = (JSONObject) jResult.get("Species");
            UserVars.Species = decodeJsonLists(context, specIn);

            JSONObject unitIn = (JSONObject) jResult.get("Units");
            UserVars.Units = decodeJsonLists(context, unitIn);

            return context.getString(R.string.io_success);
        } catch (Exception e) {
            errorString = e.getLocalizedMessage();
        }

        return context.getString(R.string.load_user_vars_failure) + ": " + errorString;
    }

    public static void clearUserVars() {
        UserVars.UUID = new String();
        UserVars.UName = new String();
        UserVars.UserVarsSaveFileName = new String();
        UserVars.RecordsSaveFileName = new String();
        UserVars.MediasSaveFileName = new String();
        UserVars.AccessLevels = new ArrayList<String>() {{
            add("Public");
            add("Private");
        }};
        UserVars.Tags = new HashMap<>();
        UserVars.Species = new HashMap<>();
        UserVars.Units = new HashMap<>();
        System.out.println(UserVars.UUID);
        System.out.println(UserVars.UName);
    }

    //endregion

    //region Records

    public static String saveRecords(Context context, List<Record> records) {
        String result = context.getString(R.string.save_record_failure);
        FileOutputStream os;

        JSONObject recordsJObject = new JSONObject();

        try {
            JSONArray recordsJArray = new JSONArray();
            for (Record r : records) {
                recordsJArray.put(r.recordJsonEncode());
            }
            recordsJObject.put("type", "FeatureCollection");
            recordsJObject.put("features", recordsJArray);
        } catch (Exception e) {
            result += e.getLocalizedMessage();
        }

        // Write the JSON object to the output file
        try {
            os = context.openFileOutput(UserVars.RecordsSaveFileName,Context.MODE_PRIVATE);
            os.write(recordsJObject.toString().getBytes());
            os.close();

            return context.getString(R.string.io_success) + ": " + recordsJObject.toString();
        } catch (Exception e) {
            result += e.getLocalizedMessage();
        }

        return result;
    }

    public static List<Record> loadRecords(Context context) {
        List<Record> recordsOut = new ArrayList<>();
        String result = context.getString(R.string.load_record_failure);
        JSONObject jResult;
        StringBuilder dataString = new StringBuilder();

        File recFile = new File(context.getFilesDir(),UserVars.RecordsSaveFileName);

        try {
            BufferedReader br = new BufferedReader(new FileReader(recFile));
            String line;

            while ((line = br.readLine()) != null) {
                dataString.append(line);
                dataString.append('\n');
            }
            br.close();

            jResult = new JSONObject(dataString.toString());

            JSONArray recJArray = (JSONArray) jResult.get("features");

            for (int i=0; i<recJArray.length(); i++) {
                JSONObject rec = (JSONObject) recJArray.get(i);
                JSONObject geom = (JSONObject) rec.get("geometry");
                JSONArray coords = (JSONArray) geom.get("coordinates");
                Double[] coordsOut = new Double[coords.length()];
                for (i=0; i<coords.length(); i++) {
                    coordsOut[i] = (Double) coords.get(i);
                }

                JSONObject props = (JSONObject) rec.get("properties");
                Iterator<String> iter = props.keys();
                Map<String, Object> propsOut = new HashMap<>();
                while (iter.hasNext()) {
                    String k = iter.next();
                    Object prop = props.get(k);
                    if (prop instanceof JSONArray) {
                        List<String> propArray = new ArrayList<>();
                        for (i=0; i<((JSONArray) prop).length(); i++) {
                            propArray.add(((JSONArray) prop).get(i).toString());
                        }
                        String[] propStringArray = new String[((JSONArray) prop).length()];
                        propsOut.put(k, propArray.toArray(propStringArray));
                    } else {
                        propsOut.put(k, prop);
                    }
                }

                recordsOut.add(new Record(coordsOut, null, propsOut));
            }
        } catch (Exception e) {
            System.out.println(context.getString(R.string.load_record_failure) + e.getLocalizedMessage());
        }

        return recordsOut;
    }

    //endregion

    //region Medias

    //endregion

    //region Helper Methods

    private static JSONObject encodeJsonLists(Context context, Map<String, Object[]> inMap) {
        try {
            JSONObject jObj = new JSONObject();
            for (String k : inMap.keySet()) {
                List<Object> varsOut = new ArrayList<>();
                Object[] uVarsVals = inMap.get(k);
                for (Object v : uVarsVals) {
                    varsOut.add(v);
                }
                jObj.put(k, new JSONArray(varsOut));
            }

            return jObj;
        } catch (JSONException e) {
            System.out.println(context.getString(R.string.json_encode_error) + ": " + e.getLocalizedMessage());
        }

        return null;
    }

    private static Map<String, Object[]> decodeJsonLists(Context context, JSONObject inObj) {
        Map<String, Object[]> outMap = new HashMap<>();
        try {
            Iterator<String> iter = inObj.keys();
            while (iter.hasNext()) {
                String k = iter.next();
                JSONArray kJArray = (JSONArray) inObj.get(k);
                Object[] kVal = new Object[2];
                for (int i=0; i<2; i++) kVal[i] = kJArray.get(i);
                outMap.put(k,kVal);
            }
            return outMap;
        } catch (Exception e) {
            System.out.println(context.getString(R.string.json_decode_error) + ": " + e.getLocalizedMessage());
        }

        return null;
    }

    //endregion

}

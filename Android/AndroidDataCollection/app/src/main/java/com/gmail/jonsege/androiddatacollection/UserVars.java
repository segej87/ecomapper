package com.gmail.jonsege.androiddatacollection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jon on 9/23/16.
 */

public class UserVars {
    // The user ID of the current user
    static String UUID = "testing";

    // The username of the current user
    static String UName = "Testing";

    // Filename to save these user variables
    static String UserVarsSaveFileName = "UserVars-testing";

    // Filename to save records
    static String RecordsSaveFileName = "Records-testing";

    // Filename to save media
    static String MediasSaveFileName = "Medias-testing";

    // An array of access levels, with the built-in public and private options
    static List<String> AccessLevels = new ArrayList<String>() {{
        add("Public");
        add("Private");
    }};

    // New key-value pairs object for tags
    static Map<String, Object[]> Tags = new HashMap<>();

    // New key-value pairs object for species
    static Map<String, Object[]> Species = new HashMap<>();

    // New key-value pairs object for units
    static Map<String, Object[]> Units = new HashMap<>();

}

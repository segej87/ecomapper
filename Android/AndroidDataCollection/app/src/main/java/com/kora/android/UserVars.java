package com.kora.android;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Created for the Kora project by jon on 9/23/16.
 */

class UserVars {

    /**
     * The user ID of the current user
     */
    static String UUID;

    /**
     * The username of the current user
     */
    static String UName;

    /**
     * Filename to save these user variables
     */
    static String UserVarsSaveFileName;

    /**
     * Filename to save records
     */
    static String RecordsSaveFileName;

    /**
     * A map linking blob urls to local urls
     */
    static final Map<String, String> Medias = new HashMap<>();

    /**
     * A list of media marked for upload
     */
    static final List<String> MarkedMedia = new ArrayList<>();

    /**
     * An array of access levels, with the built-in public and private options
     */
    static List<String> AccessLevels = new ArrayList<String>() {{
        add("Public");
        add("Private");
    }};

    /**
     * New key-value pairs object for tags
     */
    static Map<String, Object[]> Tags = new HashMap<>();

    /**
     * New key-value pairs object for species
     */
    static Map<String, Object[]> Species = new HashMap<>();

    /**
     * New key-value pairs object for units
     */
    static Map<String, Object[]> Units = new HashMap<>();

    /**
     * Defaults
     */
    static List<String> AccessDefaults = new ArrayList<>();
    static List<String> TagsDefaults = new ArrayList<>();
    static String SpecDefault = null;
    static String UnitsDefault = null;

    /**
     * Public strings
     */
    static final String blobRootString = "https://ecomapper.blob.core.windows.net/";
}
package com.gmail.jonsege.androiddatacollection;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jon on 9/23/16.
 */

public class UserVars {
    static String uuid = new String();

    static String uName = new String();

    static List<String> AccessLevels = new ArrayList<String>() {{
        add("Public");
        add("Private");
    }};

    static Map<String, Object[]> Tags = new HashMap<String, Object[]>();

    static Map<String, Object[]> Species = new HashMap<String, Object[]>();

    static Map<String, Object[]> Units = new HashMap<String, Object[]>();

}

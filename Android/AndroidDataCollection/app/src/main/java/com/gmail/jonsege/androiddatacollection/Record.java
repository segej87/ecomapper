package com.gmail.jonsege.androiddatacollection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jon on 9/24/16.
 */

public class Record {

    //region Class Variables

    Double[] coords = new Double[3];
    String photoPath = new String();
    Map<String, Object> props = new HashMap<String, Object>();

    //endregion

    //region Initialization

    public Record(Double[] coords, String photo, Map<String, Object> props){
        this.coords = coords;
        this.props = props;
        this.photoPath = photo;
    }

    //endregion

}

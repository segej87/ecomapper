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
    BitmapFactory photo = new BitmapFactory();
    Map<String, Object> props = new HashMap<String, Object>();

    //endregion

    //region Initialization

    public Record(Double[] coords, BitmapFactory photo, Map<String, Object> props){
        this.coords = coords;
        this.photo = photo;
        this.props = props;
    }

    //endregion

}

package com.gmail.jonsege.androiddatacollection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by jon on 9/27/16.
 */

public class NotebookArrayAdapter extends ArrayAdapter<Record> {
    //region Class Variables

    // The context for the adapter.
    private final Context context;

    // The data source for the adapter.
    private final List<Record> records;

    //endregion

    //region Initialization

    // Adapter's constructor.
    public NotebookArrayAdapter(Context context, List<Record> values) {
        super(context, -1, values);
        this.context = context;
        this.records = values;
    }

    //endregion

    //region Adapter methods

    // Get and populate the adapter's listview row.
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Use the custom notebook row layout.
        View rowView = inflater.inflate(R.layout.notebook_row, parent, false);

        // Get properties from the datasource.
        Map<String,Object> props = records.get(position).props;

        // Get the current record type from the datasource.
        String dt = props.get("datatype").toString();

        // Fill the first line with the record's name.
        TextView titleView = (TextView) rowView.findViewById(R.id.firstLine);
        titleView.setText(props.get("name").toString());

        // Fill the image view with the record's photo or with defaults.
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

        // If the photo path is not null or blank, get it at attempt to set the image view.
        String photoPath = records.get(position).photoPath;
        if (photoPath != null && photoPath != "") {
            File imgFile = new File(records.get(position).photoPath);

            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
            }
        } else {
            int resource = 0;
            if (dt == "meas") resource = R.mipmap.meas_image;
            if (dt == "note") resource = R.mipmap.note_image;

            imageView.setImageResource(resource);
        }

        return rowView;
    }

    //endregion
}

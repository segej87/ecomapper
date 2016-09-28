package com.gmail.jonsege.androiddatacollection;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by jon on 9/27/16.
 */

public class NotebookArrayAdapter extends ArrayAdapter<Record> {
    private final Context context;
    private final List<Record> records;

    public NotebookArrayAdapter(Context context, List<Record> values) {
        super(context, -1, values);
        this.context = context;
        this.records = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.notebook_row, parent, false);
        TextView titleView = (TextView) rowView.findViewById(R.id.firstLine);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        titleView.setText(records.get(position).props.get("name").toString());

        return rowView;
    }
}

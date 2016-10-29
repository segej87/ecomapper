package com.gmail.jonsege.androiddatacollection;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created for the Kora project by jonse on 10/25/2016.
 */
public class LocationOverrideFragment extends DialogFragment {

    public interface LocationOverrideListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    private LocationOverrideListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (LocationOverrideListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Verify that the host activity implements the callback interface
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                // Instantiate the NoticeDialogListener so we can send events to the host
                mListener = (LocationOverrideListener) activity;
            } catch (ClassCastException e) {
                // The activity doesn't implement the interface, throw exception
                throw new ClassCastException(activity.toString()
                        + " must implement NoticeDialogListener");
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle b = getArguments();
        String numMin = b.getString("NUMMIN");

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.old_location_error_message, numMin))
                .setPositiveButton(R.string.old_location_use_option, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(LocationOverrideFragment.this);
                    }
                })
                .setNegativeButton(R.string.old_location_wait_option, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(LocationOverrideFragment.this);
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}

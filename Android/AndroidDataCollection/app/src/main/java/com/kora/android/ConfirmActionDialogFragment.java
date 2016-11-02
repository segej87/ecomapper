package com.kora.android;

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
 * Created for the Kora project by jonse on 11/1/2016.
 */

public class ConfirmActionDialogFragment extends DialogFragment {

    public interface ConfirmActionListener {
        void onConfirmPositiveClick(DialogFragment d);
        void onConfirmNegativeClick();
    }

    private ConfirmActionListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ConfirmActionListener) context;
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
                mListener = (ConfirmActionListener) activity;
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
        String message = b.getString("MESSAGE");
        String positiveString = b.getString("POSSTRING");
        String negativeString = b.getString("NEGSTRING");

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton(positiveString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onConfirmPositiveClick(ConfirmActionDialogFragment.this);
                    }
                })
                .setNegativeButton(negativeString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onConfirmNegativeClick();
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}

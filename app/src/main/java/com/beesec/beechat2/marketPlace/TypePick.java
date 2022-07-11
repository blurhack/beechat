package com.beesec.beechat2.marketPlace;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.beesec.beechat2.R;


@SuppressWarnings("ALL")
public class TypePick extends DialogFragment {

    int position = 0;
    @SuppressWarnings("EmptyMethod")
    public interface SingleChoiceListener{
        void onPositiveButtonClicked(String[] list, int position);
        void onNegativeButtonClicked();

    }

    SingleChoiceListener listener;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (SingleChoiceListener) context;
        } catch (Exception e) {
            throw new ClassCastException(requireActivity().toString()+" SingleChoiceListener must implemented");
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] list= requireActivity().getResources().getStringArray(R.array.type);
        builder.setTitle("Select product type")
                .setSingleChoiceItems(list, position, (dialog, which) -> position = which).setPositiveButton("Ok", (dialog, which) -> listener.onPositiveButtonClicked(list,position)).setNegativeButton("Cancel", (dialog, which) -> listener.onNegativeButtonClicked());
        return builder.create();
    }
}
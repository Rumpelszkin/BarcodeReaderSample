package com.varvet.barcodereadersample;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.varvet.barcodereadersample.barcode.BarcodeCaptureActivity;

public class MenuDialogFragment extends DialogFragment {

        public MenuDialogFragment(){}

        public static MenuDialogFragment newInstance(String title){
            MenuDialogFragment frag = new MenuDialogFragment();
            Bundle args = new Bundle();
            args.putString("title", title);
            frag.setArguments(args);
            return frag;
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu_dialog, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        Button buttonScanKey = (Button) view.findViewById(R.id.generateKey);
        Button buttonGenerateKey = (Button) view.findViewById(R.id.scanKey);

        buttonGenerateKey.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){

                Intent i = new Intent(getContext(), Main2Activity.class);
                getActivity().startActivityForResult(i,2);
                dismiss();
            }}
        );

        buttonScanKey.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){

                Intent i = new Intent(getContext(), BarcodeCaptureActivity.class);
                getActivity().startActivityForResult(i,1);
                dismiss();
            }
        }
        );


        getDialog().setTitle("Choose one!");

      }



}

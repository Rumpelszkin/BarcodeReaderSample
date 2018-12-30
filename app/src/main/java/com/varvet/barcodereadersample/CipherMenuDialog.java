package com.varvet.barcodereadersample;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;

public class CipherMenuDialog extends DialogFragment {


    String name;
    String key;


    public CipherMenuDialog(


    ){}

    public CipherMenuDialog newInstance(String title){
        CipherMenuDialog frag = new CipherMenuDialog();
        Bundle myArgs = getArguments();

        myArgs.putString("title", title);
        frag.setArguments(myArgs);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            Bundle myArgs = getArguments();
            name = myArgs.getString("name");
            key = myArgs.getString("key");

        }

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){


        return inflater.inflate(R.layout.fragment_menu_cipher,container);
    }


    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);

        Button buttonENCODE = (Button) view.findViewById(R.id.encode);
        Button buttonDECODE = (Button) view.findViewById(R.id.decode);




        buttonENCODE.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(getContext(), name + " " +key,Toast.LENGTH_SHORT).show();
                ((MainActivity)getActivity()).getUri();
            }
        });

        buttonDECODE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), name + " " +key,Toast.LENGTH_SHORT).show();
                //odszyfrowanie
            }
        });

    }

}

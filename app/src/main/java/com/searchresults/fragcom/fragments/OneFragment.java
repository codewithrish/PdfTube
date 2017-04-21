package com.searchresults.fragcom.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.searchresults.fragcom.MainActivity;
import com.searchresults.fragcom.R;
import com.searchresults.fragcom.TextChangedEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by risha on 4/17/2017.
 */

public class OneFragment extends Fragment{

    private TextView tv;
    private Button btn;
    private EditText editText;

    public OneFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_one, container, false);
        tv = (TextView) view.findViewById(R.id.textView1);
        btn = (Button) view.findViewById(R.id.button1);
        editText = (EditText) view.findViewById(R.id.newText);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //EventBus bus = EventBus.getDefault();
                String s = editText.getText().toString();
                if(TextUtils.isEmpty(s)) {
                    Toast.makeText(getContext(), "Enter Text", Toast.LENGTH_SHORT).show();
                }
                else {
                    //bus.post(new TextChangedEvent(s));
                }
            }
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_one, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

}

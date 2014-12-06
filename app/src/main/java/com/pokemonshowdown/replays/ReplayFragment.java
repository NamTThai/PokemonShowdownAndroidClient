package com.pokemonshowdown.replays;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pokemonshowdown.app.R;

import org.jsoup.nodes.Element;

public class ReplayFragment extends android.support.v4.app.Fragment implements View.OnClickListener{

    // TODO: Rename and change types and number of parameters
    public static ReplayFragment newInstance() {
        ReplayFragment fragment = new ReplayFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ReplayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_replay, container, false);
        v.setFocusableInTouchMode(true);
        Button submit = (Button) v.findViewById(R.id.submit);
        submit.setOnClickListener(this);
        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void getReplay(View view) {
        EditText mEditText = (EditText) view.findViewById(R.id.replayInput);
        //Log.i("Connecting to Showdown", "Getting replay of id " + mEditText.getText());
        RetrieveReplayTask getReplayTask = new RetrieveReplayTask(){

            //Called when async finishes task
            @Override
            public void OnResponseRecived(Object obj) {
                Element battleLog = (Element) obj;
                //If replay was not retrieved correctly
                if(battleLog == null){
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Error fetching replay", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                //Just test code for debugger to confirm replay retreived correctly
                Replay replay = new Replay(battleLog.html());
                Log.i("Replay Test", replay.player1);
                Log.i("Replay Test", replay.player2);
                for(String p : replay.p1Pokemon){
                    Log.i("Replay Test 1", p);
                }
                for(String p : replay.p2Pokemon){
                    Log.i("Replay Test 2", p);
                }
                replay.Play();

            }
        };
        getReplayTask.execute("http://pokemonshowdown.com/replay/exeggutorboldalakazam");
    }


    @Override
    public void onClick(View view) {
                getReplay(view);
    }
}

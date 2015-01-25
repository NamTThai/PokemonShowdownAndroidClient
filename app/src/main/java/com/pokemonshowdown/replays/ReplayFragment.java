package com.pokemonshowdown.replays;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pokemonshowdown.app.BattleFragment;
import com.pokemonshowdown.app.R;
import com.pokemonshowdown.data.BattleFieldData;

public class ReplayFragment extends android.support.v4.app.Fragment implements View.OnClickListener{

    public ProgressDialog mWaitingDialog;

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

    public void showReplay(View view) {
        EditText mEditText = (EditText) view.findViewById(R.id.replayInput);

        RetrieveReplayTask getReplayTask = new RetrieveReplayTask(){
            public void OnResponseRecived(Object obj) {
                String battleLog = (String) obj;
                //If replay was not retrieved correctly
                if(battleLog == null){
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Error fetching replay", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                setUpReplayBattle(battleLog);
            }
        };
        getReplayTask.execute("http://pokemonshowdown.com/replay/exeggutorboldalakazam");
    }

    @Override
    public void onClick(View view) {
        showReplay(view);
    }

    private void setUpReplayBattle(String replayString){

        //Create BattleFragment (Basically the same thing as watching replays)
        BattleFragment replayBattleFragment;
        replayBattleFragment = BattleFragment.newInstance("replay");
        Replay currentReplay = new Replay(replayString, replayBattleFragment);
        BattleFieldData fieldData = BattleFieldData.get(getActivity().getApplicationContext());
        fieldData.joinRoom("replay",false);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragmentContainer, replayBattleFragment).commit();
        currentReplay.Play();
    }
}
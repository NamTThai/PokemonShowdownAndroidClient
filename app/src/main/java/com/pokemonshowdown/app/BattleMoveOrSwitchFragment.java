package com.pokemonshowdown.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pokemonshowdown.data.ServerRequest;

/**
 * Created by clement-b on 11/12/2014.
 */
public class BattleMoveOrSwitchFragment extends Fragment {
    public final static String ACTIONIDTAG = "ACTIONIDTAG";
    public final static String MOVETAG = "MOVETAG";
    public final static String ROOMIDTAG = "ROOMIDTAG";

    private ServerRequest serverRequest;
    private String roomId;
    private int actionId;


    public static final BattleMoveOrSwitchFragment newInstance(ServerRequest serverRequest, int actionId, String roomId) {
        BattleMoveOrSwitchFragment fragment = new BattleMoveOrSwitchFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(MOVETAG, serverRequest);
        bundle.putSerializable(ROOMIDTAG, roomId);
        bundle.putSerializable(ACTIONIDTAG, actionId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionId = (Integer) getArguments().get(ACTIONIDTAG);
        roomId = (String) getArguments().get(ROOMIDTAG);
        serverRequest = (ServerRequest) getArguments().get(MOVETAG);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_battle_move_or_switch, parent, false);

        TextView switchTextView = (TextView) view.findViewById(R.id.battle_switch_textview);
        TextView attackTextView = (TextView) view.findViewById(R.id.battle_attack_textview);

        attackTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BattleMoveFragment fragment = BattleMoveFragment.newInstance(serverRequest, actionId, roomId);

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.action_fragment_container, fragment, "")
                        .commit();
            }
        });

        switchTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BattleSwitchFragment fragment = BattleSwitchFragment.newInstance(serverRequest, actionId, roomId);

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.action_fragment_container, fragment, "")
                        .commit();
            }
        });


        return view;
    }


}

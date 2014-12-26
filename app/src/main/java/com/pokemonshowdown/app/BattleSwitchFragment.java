package com.pokemonshowdown.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pokemonshowdown.data.MyApplication;
import com.pokemonshowdown.data.PokemonInfo;
import com.pokemonshowdown.data.ServerRequest;

public class BattleSwitchFragment extends Fragment {
    public final static String ACTIONIDTAG = "ACTIONIDTAG";
    public final static String MOVETAG = "MOVETAG";
    public final static String ROOMIDTAG = "ROOMIDTAG";
    public final static String TEAMPREVIEWTAG = "TEAMPREVIEWTAG";

    private ServerRequest serverRequest;
    private String roomId;
    private int actionId;
    private boolean isTeamPreview;

    public static BattleSwitchFragment newInstance(ServerRequest serverRequest, int actionId, String roomId, boolean teamPreview) {
        BattleSwitchFragment fragment = new BattleSwitchFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(MOVETAG, serverRequest);
        bundle.putSerializable(ROOMIDTAG, roomId);
        bundle.putSerializable(ACTIONIDTAG, actionId);
        bundle.putSerializable(TEAMPREVIEWTAG, teamPreview);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionId = (Integer) getArguments().get(ACTIONIDTAG);
        roomId = (String) getArguments().get(ROOMIDTAG);
        serverRequest = (ServerRequest) getArguments().get(MOVETAG);
        isTeamPreview = (Boolean) getArguments().get(TEAMPREVIEWTAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_battle_select_switch, parent, false);

        int currentPokemon = 1;
        for (final PokemonInfo pokemonInfo : serverRequest.getPkmTeam()) {
            TextView pokemonTextView = null;
            switch (currentPokemon) {
                case 1:
                    pokemonTextView = (TextView) view.findViewById(R.id.pokemon1_name);
                    break;
                case 2:
                    pokemonTextView = (TextView) view.findViewById(R.id.pokemon2_name);
                    break;
                case 3:
                    pokemonTextView = (TextView) view.findViewById(R.id.pokemon3_name);
                    break;
                case 4:
                    pokemonTextView = (TextView) view.findViewById(R.id.pokemon4_name);
                    break;
                case 5:
                    pokemonTextView = (TextView) view.findViewById(R.id.pokemon5_name);
                    break;
                case 6:
                    pokemonTextView = (TextView) view.findViewById(R.id.pokemon6_name);
                    break;
            }
            pokemonTextView.setEnabled(!pokemonInfo.isActive());
            pokemonTextView.setText(pokemonInfo.getName());
            final int id = currentPokemon;
            pokemonTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isTeamPreview) {
                        MyApplication.getMyApplication().sendClientMessage(roomId + "|/team " + id + "|" + serverRequest.getRqId());
                    } else {
                        MyApplication.getMyApplication().sendClientMessage(roomId + "|/choose switch " + id + "|" + serverRequest.getRqId());
                    }
                    getActivity().getSupportFragmentManager().beginTransaction().remove(BattleSwitchFragment.this).commit();
                }
            });

            currentPokemon++;
        }


        return view;
    }


}

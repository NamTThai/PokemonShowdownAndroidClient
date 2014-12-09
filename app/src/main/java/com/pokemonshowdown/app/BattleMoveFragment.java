package com.pokemonshowdown.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pokemonshowdown.data.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BattleMoveFragment extends Fragment {
    public BattleMoveFragment() {
        super();
    }

    public final static String MOVETAG = "MOVETAG";
    public final static String ROOMIDTAG = "ROOMIDTAG";

    private JSONObject reqJsonObject;
    private String roomId;

    public static final BattleMoveFragment newInstance(JSONObject requestJson, String roomId) {
        BattleMoveFragment fragment = new BattleMoveFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(MOVETAG, requestJson.toString());
        bundle.putSerializable(ROOMIDTAG, roomId);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            reqJsonObject = new JSONObject((String) getArguments().get(MOVETAG));
        } catch (JSONException e) {
            // TODO
        }
        roomId = (String) getArguments().get(ROOMIDTAG);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_battle_active_moves, parent, false);

        try {
            //todo think on multiples battles (2v2 etc...)
            final int rqId = reqJsonObject.getInt("rqid");
            JSONArray activeMovesArray = reqJsonObject.getJSONArray("active");

            for (int i = 0; i < activeMovesArray.length(); i++) {
                JSONArray movesArray = activeMovesArray.getJSONObject(i).getJSONArray("moves");
                for (int j = 0; j < movesArray.length(); j++) {
                    JSONObject move = movesArray.getJSONObject(j);
                    final String moveName = move.getString("move");
                    final String moveId = move.getString("id");
                    final boolean isDisabled = move.getBoolean("disabled");
                    final int pp = move.getInt("pp");
                    final int maxPP = move.getInt("maxpp");

                    TextView moveTextView = null;

                    switch (j) {
                        case 0:
                            moveTextView = (TextView) view.findViewById(R.id.teambuilder_move1_name);
                            break;
                        case 1:
                            moveTextView = (TextView) view.findViewById(R.id.teambuilder_move2_name);
                            break;
                        case 2:
                            moveTextView = (TextView) view.findViewById(R.id.teambuilder_move3_name);
                            break;
                        case 3:
                            moveTextView = (TextView) view.findViewById(R.id.teambuilder_move4_name);
                            break;
                    }

                    if (moveTextView != null) {
                        moveTextView.setText(moveName);
                        moveTextView.setEnabled(!isDisabled);
                        moveTextView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                MyApplication.getMyApplication().sendClientMessage(roomId + "|/choose move " + moveName + "|" + rqId);
                                getActivity().getSupportFragmentManager().beginTransaction().remove(BattleMoveFragment.this).commit();
                            }
                        });
                    }


                }
            }


        } catch (JSONException e) {
            // TODO
        }

        return view;
    }

}

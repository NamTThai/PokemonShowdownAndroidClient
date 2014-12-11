package com.pokemonshowdown.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pokemonshowdown.data.MyApplication;
import com.pokemonshowdown.data.ServerRequest;

public class BattleMoveFragment extends Fragment {
    public BattleMoveFragment() {
        super();
    }

    public final static String ACTIONIDTAG = "ACTIONIDTAG";
    public final static String MOVETAG = "MOVETAG";
    public final static String ROOMIDTAG = "ROOMIDTAG";

    private ServerRequest serverRequest;
    private String roomId;
    private int actionId;

    public static final BattleMoveFragment newInstance(ServerRequest serverRequest, int actionId, String roomId) {
        BattleMoveFragment fragment = new BattleMoveFragment();
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
        View view = inflater.inflate(R.layout.fragment_battle_select_moves, parent, false);

        ServerRequest.ActiveMoveInfo moves = serverRequest.getMovesToDo().get(actionId);
        int moveId = 1;
        for (final ServerRequest.MoveInfo moveInfo : moves.getAvailableMoves()) {
            TextView moveTextView = null;
            TextView ppTextView = null;

            switch (moveId) {
                case 1:
                    moveTextView = (TextView) view.findViewById(R.id.active_move1_name);
                    ppTextView = (TextView) view.findViewById(R.id.active_move1_pp);
                    break;

                case 2:
                    moveTextView = (TextView) view.findViewById(R.id.active_move2_name);
                    ppTextView = (TextView) view.findViewById(R.id.active_move2_pp);
                    break;

                case 3:
                    moveTextView = (TextView) view.findViewById(R.id.active_move3_name);
                    ppTextView = (TextView) view.findViewById(R.id.active_move3_pp);
                    break;

                case 4:
                    moveTextView = (TextView) view.findViewById(R.id.active_move4_name);
                    ppTextView = (TextView) view.findViewById(R.id.active_move4_pp);
                    break;

            }

            if (moveTextView != null && ppTextView != null) {
                moveTextView.setText(moveInfo.getMoveName());
                ppTextView.setText(moveInfo.getPp() + "/" + moveInfo.getMaxPp());
                moveTextView.setEnabled(!moveInfo.isDisabled());
                moveTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MyApplication.getMyApplication().sendClientMessage(roomId + "|/choose move " + moveInfo.getMoveName() + "|" + serverRequest.getRqId());
                        getActivity().getSupportFragmentManager().beginTransaction().remove(BattleMoveFragment.this).commit();
                    }
                });
            }
            moveId++;
        }


        return view;
    }

}

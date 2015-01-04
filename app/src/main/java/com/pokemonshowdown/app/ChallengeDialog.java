package com.pokemonshowdown.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.MyApplication;
import com.pokemonshowdown.data.PokemonTeam;

import java.util.Arrays;

/**
 * Created by clement on 04/01/2015.
 */
public class ChallengeDialog extends DialogFragment {
    public static final String CTAG = ChallengeDialog.class.getName();
    public static final String CHALLENGER_TAG = "CHALLENGER_TAG";
    public static final String FORMAT_TAG = "FORMAT_TAG";
    public final static String RANDOM_TEAM_NAME = "Random Team";


    private String mChallengerName;
    private String mFormatId;
    private BattleFieldData.Format mFormat;


    public static ChallengeDialog newInstance(String challenger, String format) {
        ChallengeDialog fragment = new ChallengeDialog();
        Bundle args = new Bundle();
        args.putString(CHALLENGER_TAG, challenger);
        args.putString(FORMAT_TAG, format);

        fragment.setArguments(args);
        return fragment;
    }

    public ChallengeDialog() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getArguments() != null) {
            mChallengerName = getArguments().getString(CHALLENGER_TAG);
            mFormatId = getArguments().getString(FORMAT_TAG);
            mFormat = BattleFieldData.get(getActivity()).getFormatUsingId(mFormatId);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_challenge, container);


        TextView challengerTextView = (TextView) view.findViewById(R.id.challenger_name);
        challengerTextView.setText(mChallengerName);

        TextView formatTextView = (TextView) view.findViewById(R.id.challenger_format);
        formatTextView.setText(mFormat.getName());

        final Spinner teamSpinner = (Spinner) view.findViewById(R.id.challenger_spinner);
        if (mFormat.isRandomFormat()) {
            PokemonTeamListArrayAdapter randomTeamAdapter = new PokemonTeamListArrayAdapter(getActivity(), Arrays.asList(new PokemonTeam(RANDOM_TEAM_NAME)));
            teamSpinner.setAdapter(randomTeamAdapter);
        } else {
            if (PokemonTeam.getPokemonTeamList().isEmpty()) {
                ArrayAdapter noTeamsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.empty_team_list_filler));
                teamSpinner.setAdapter(noTeamsAdapter);
            } else {
                PokemonTeamListArrayAdapter pokemonTeamListArrayAdapter = new PokemonTeamListArrayAdapter(getActivity(), PokemonTeam.getPokemonTeamList());
                teamSpinner.setAdapter(pokemonTeamListArrayAdapter);
            }
        }

        TextView acceptChallengeTextView = (TextView) view.findViewById(R.id.accept_challenge);
        acceptChallengeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFormat.isRandomFormat()) {
                    MyApplication.getMyApplication().sendClientMessage("|/utm");
                    MyApplication.getMyApplication().sendClientMessage("|/accept " + mChallengerName);
                } else {
                    if (PokemonTeam.getPokemonTeamList().isEmpty()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(R.string.error_dialog_title);
                        builder.setIcon(android.R.drawable.ic_dialog_alert);
                        builder.setMessage(R.string.no_teams);
                        final AlertDialog alert = builder.create();
                        getActivity().runOnUiThread(new java.lang.Runnable() {
                            public void run() {
                                alert.show();
                            }
                        });
                    } else {
                        PokemonTeam selectedTeam = (PokemonTeam) teamSpinner.getSelectedItem();
                        MyApplication.getMyApplication().sendClientMessage("|/utm " + selectedTeam.exportForVerification());
                        MyApplication.getMyApplication().sendClientMessage("|/accept " + mChallengerName);
                    }
                }

                ChallengeDialog.this.dismiss();
            }
        });


        TextView rejectChallengeTextView = (TextView) view.findViewById(R.id.reject_challenge);
        rejectChallengeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApplication.getMyApplication().sendClientMessage("|/reject " + mChallengerName);
                ChallengeDialog.this.dismiss();
            }
        });


        return view;

    }


}

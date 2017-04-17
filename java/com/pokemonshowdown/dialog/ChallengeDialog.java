package com.pokemonshowdown.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.pokemonshowdown.R;
import com.pokemonshowdown.adapter.PokemonTeamListArrayAdapter;
import com.pokemonshowdown.adapter.PokemonTeamSpinnerAdapter;
import com.pokemonshowdown.application.MyApplication;
import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.PokemonTeam;

import java.util.ArrayList;
import java.util.Arrays;

public class ChallengeDialog extends DialogFragment {
    public static final String CTAG = ChallengeDialog.class.getName();
    public static final String CHALLENGER_TAG = "CHALLENGER_TAG";
    public static final String FORMAT_TAG = "FORMAT_TAG";

    public final static String RANDOM_TEAM_NAME = "Random Team";


    private String mChallengerName;
    private String mFormatId;
    private boolean mChallenged;
    private BattleFieldData.Format mFormat;
    private PokemonTeamSpinnerAdapter mRandomTeamAdapter;
    private ArrayAdapter<String> mNoTeamsAdapter;
    private PokemonTeamSpinnerAdapter mPokemonTeamListArrayAdapter;
    private boolean challengeAccepted;

    public ChallengeDialog() {

    }

    public static ChallengeDialog newInstance(String challenger, String format) {
        ChallengeDialog fragment = new ChallengeDialog();
        Bundle args = new Bundle();
        args.putString(CHALLENGER_TAG, challenger);
        args.putString(FORMAT_TAG, format);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (!challengeAccepted && mChallenged) {
            //dialog has been cancelled, we reject
            MyApplication.getMyApplication().sendClientMessage("|/reject " + mChallengerName);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        challengeAccepted = false;
        if (getArguments() != null) {
            mChallengerName = getArguments().getString(CHALLENGER_TAG);
            mFormatId = getArguments().getString(FORMAT_TAG);

            // if format is != null, we are getting challenged
            // otehrwise, we challenge someone
            mChallenged = (mFormatId != null);
            mFormat = BattleFieldData.get(getActivity()).getFormatUsingId(mFormatId);

            // team adapters
            mNoTeamsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.empty_team_list_filler));
            //mRandomTeamAdapter = new PokemonTeamListArrayAdapter(getActivity(), Arrays.asList(new PokemonTeam(RANDOM_TEAM_NAME)));
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_challenge, container);


        TextView challengerTextView = (TextView) view.findViewById(R.id.challenger_name);
        if (mChallenged) {
            challengerTextView.setText(String.format(getResources().getString(R.string.challenged_by), mChallengerName));
        } else {
            challengerTextView.setText(String.format(getResources().getString(R.string.challenging), mChallengerName));
        }

        final Spinner teamSpinner = (Spinner) view.findViewById(R.id.challenger_team_spinner);

        Spinner formatSpinner = (Spinner) view.findViewById(R.id.challenger_format);

        if (mChallenged) {
            //if we are challenged, we fix the spiner to the challenge format
            ArrayList<String> mFormatList = new ArrayList<>();
            mFormatList.add(mFormat.getName());
            ArrayAdapter formatAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mFormatList);
            formatSpinner.setAdapter(formatAdapter);
            formatSpinner.setEnabled(false);
        } else {
            // else we load the spiners with all the formats
            ArrayList<String> mFormatList = new ArrayList<>();

            ArrayList<BattleFieldData.FormatType> formatTypes = BattleFieldData.get(getActivity()).getFormatTypes();
            for (BattleFieldData.FormatType formatType : formatTypes) {
                ArrayList<String> result = formatType.getSearchableFormatList();
                for (String name : result) {
                    mFormatList.add(name);
                }
            }

            ArrayAdapter formatAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mFormatList);
            formatSpinner.setAdapter(formatAdapter);
            formatSpinner.setSelection(0);
            mFormat = BattleFieldData.get(getActivity()).getFormat((String) formatSpinner.getSelectedItem());
            formatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String currentFormatString = parent.getItemAtPosition(position)
                            .toString();
                    BattleFieldData.Format currentFormat;
                    if (currentFormatString != null) {
                        currentFormat = BattleFieldData.get(getActivity()).getFormat(currentFormatString);
                        mFormat = currentFormat;
                        if (currentFormat != null) {
                            if (currentFormat.isRandomFormat()) {
                                //teamSpinner.setAdapter(mRandomTeamAdapter);
                                teamSpinner.setEnabled(false);
                            } else {
                                if (PokemonTeam.getPokemonTeamList(getContext()).size() > 0) {
                                    ArrayList<PokemonTeam> formatTeams = new ArrayList<PokemonTeam>();

                                    for (int i = 0; i < PokemonTeam.getPokemonTeamList(getContext()).size(); i++) {
                                        if (PokemonTeam.getPokemonTeamList(getContext()).get(i).getTier().equals(currentFormatString)) {
                                            formatTeams.add(PokemonTeam.getPokemonTeamList(getContext()).get(i));
                                        }
                                    }

                                    if (formatTeams.size() == 0) {
                                        teamSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                                                getActivity().getResources().getStringArray(R.array.empty_team_list_filler)));
                                        teamSpinner.setEnabled(false);
                                        return;
                                    } else {
                                        teamSpinner.setAdapter(new PokemonTeamSpinnerAdapter(getActivity(), formatTeams));
                                        teamSpinner.setEnabled(true);
                                    }
                                } else {
                                    //there are no teams, we fill the spinner with a filler item an disable it
                                    teamSpinner.setAdapter(mNoTeamsAdapter);
                                    teamSpinner.setEnabled(false);
                                }
                            }
                        }

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    return;
                }
            });
        }


        if (mFormat.isRandomFormat()) {
            PokemonTeamListArrayAdapter randomTeamAdapter = new PokemonTeamListArrayAdapter(getActivity(), Arrays.asList(new PokemonTeam(RANDOM_TEAM_NAME)));
            teamSpinner.setAdapter(randomTeamAdapter);
        } else {
            PokemonTeam.loadPokemonTeams(getContext());
            if (PokemonTeam.getPokemonTeamList(getContext()).isEmpty()) {
                ArrayAdapter noTeamsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.empty_team_list_filler));
                teamSpinner.setAdapter(noTeamsAdapter);
            } else {
                PokemonTeamListArrayAdapter pokemonTeamListArrayAdapter = new PokemonTeamListArrayAdapter(getActivity(), PokemonTeam.getPokemonTeamList(getContext()));
                teamSpinner.setAdapter(pokemonTeamListArrayAdapter);
            }
        }

        TextView acceptChallengeTextView = (TextView) view.findViewById(R.id.accept_challenge);
        if (mChallenged) {
            //we are getting challenged
            acceptChallengeTextView.setText(R.string.accept_challenge);
        } else {
            // we are challenging
            acceptChallengeTextView.setText(R.string.challenge_battle);
        }
        acceptChallengeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mChallenged) {
                    if (mFormat.isRandomFormat()) {
                        MyApplication.getMyApplication().sendClientMessage("|/utm");
                        MyApplication.getMyApplication().sendClientMessage("|/accept " + mChallengerName);
                    } else {
                        if (PokemonTeam.getPokemonTeamList(getContext()).isEmpty()) {
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
                    challengeAccepted = true;
                } else {
                    //we need to send the team for verification
                    Object pokemonTeamObject = teamSpinner.getSelectedItem();
                    // if we have no teams
                    if (!(pokemonTeamObject instanceof PokemonTeam)) {
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
                        return;
                    }
                    PokemonTeam pokemonTeam = (PokemonTeam) pokemonTeamObject;
                    String teamVerificationString = pokemonTeam.exportForVerification();
                    MyApplication.getMyApplication().sendClientMessage("|/utm " + teamVerificationString);
                    MyApplication.getMyApplication().sendClientMessage("|/challenge " + mChallengerName + ", " + MyApplication.toId(mFormat.getName()));
                }
                ChallengeDialog.this.dismiss();
            }
        });


        TextView rejectChallengeTextView = (TextView) view.findViewById(R.id.reject_challenge);
        if (mChallenged) {
            //we are getting challenged
            rejectChallengeTextView.setText(R.string.reject_challenge);
        } else {
            // we are challenging
            rejectChallengeTextView.setText(R.string.cancel);
        }

        rejectChallengeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mChallenged) {
                    MyApplication.getMyApplication().sendClientMessage("|/reject " + mChallengerName);
                }
                ChallengeDialog.this.dismiss();
            }
        });


        return view;

    }


}

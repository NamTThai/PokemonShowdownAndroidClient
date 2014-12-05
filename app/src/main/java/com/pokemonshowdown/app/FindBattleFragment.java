package com.pokemonshowdown.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.MyApplication;
import com.pokemonshowdown.data.PokemonTeam;

import java.util.ArrayList;
import java.util.Arrays;


public class FindBattleFragment extends Fragment {
    public final static String FTAG = FindBattleFragment.class.getName();

    private ProgressDialog mWaitingDialog;
    private ArrayList<String> mFormatList;
    private Spinner mPokemonTeamSpinner;

    private PokemonTeamListArrayAdapter mRandomTeamAdapter;
    private ArrayAdapter<String> mNoTeamsAdapter;
    private PokemonTeamListArrayAdapter mPokemonTeamListArrayAdapter;

    public static FindBattleFragment newInstance() {
        FindBattleFragment fragment = new FindBattleFragment();

        return fragment;
    }

    public FindBattleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_battle, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setAvailableFormat();
        mWaitingDialog = new ProgressDialog(getActivity());

        TextView findBattle = (TextView) view.findViewById(R.id.find_battle);
        findBattle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.still_in_development)
                        .create().show();
            }
        });

        TextView watchBattle = (TextView) view.findViewById(R.id.watch_battle);
        watchBattle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.getMyApplication().sendClientMessage("|/cmd roomlist");
                mWaitingDialog.setMessage("Downloading list of matches");
                mWaitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mWaitingDialog.setCancelable(true);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWaitingDialog.show();
                    }
                });
            }
        });

        mPokemonTeamSpinner = (Spinner) view.findViewById(R.id.teams_spinner);

        mNoTeamsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.empty_team_list_filler));
        mRandomTeamAdapter = new PokemonTeamListArrayAdapter(getActivity(), Arrays.asList(new PokemonTeam[]{new PokemonTeam("Random Team")}));
    }

    @Override
    public void onResume() {
        super.onResume();
        PokemonTeam.loadPokemonTeams(getActivity());
        if (PokemonTeam.getPokemonTeamList().size() > 0) {
            mPokemonTeamListArrayAdapter = new PokemonTeamListArrayAdapter(getActivity(), PokemonTeam.getPokemonTeamList());
            mPokemonTeamSpinner.setAdapter(mPokemonTeamListArrayAdapter);
            mPokemonTeamSpinner.setEnabled(true);
        } else {
            //there are no teams, we fill the spinner with a filler item an disable it
            mPokemonTeamSpinner.setAdapter(mNoTeamsAdapter);
            mPokemonTeamSpinner.setEnabled(false);
        }
    }

    public void setAvailableFormat() {
        View v = getView();
        if (v == null) {
            return;
        }

        mFormatList = new ArrayList<>();

        ArrayList<BattleFieldData.FormatType> formatTypes = BattleFieldData.get(getActivity()).getFormatTypes();
        for (BattleFieldData.FormatType formatType : formatTypes) {
            ArrayList<String> result = formatType.getSearchableFormatList();
            for (String name : result) {
                mFormatList.add(name);
            }
        }

        final ListView listView = (ListView) v.findViewById(R.id.available_formats);
        listView.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.fragment_user_list, mFormatList));
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.requestFocusFromTouch();
        listView.setItemChecked(0, true);
        BattleFieldData.get(getActivity()).setCurrentFormat(0);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BattleFieldData.get(getActivity()).setCurrentFormat(position);
                String currentFormatString = (String) listView.getItemAtPosition(position);
                BattleFieldData.Format currentFormat = null;
                if (currentFormatString != null) {
                    ArrayList<BattleFieldData.FormatType> formatTypes = BattleFieldData.get(getActivity()).getFormatTypes();
                    for (BattleFieldData.FormatType formatType : formatTypes) {
                        ArrayList<BattleFieldData.Format> result = formatType.getFormatList();
                        for (BattleFieldData.Format format : result) {
                            if (format.getName().equals(currentFormatString)) {
                                currentFormat = format;
                            }
                        }
                    }

                    if (currentFormat != null) {
                        if (currentFormat.isRandomFormat()) {
                            mPokemonTeamSpinner.setAdapter(mRandomTeamAdapter);
                            mPokemonTeamSpinner.setEnabled(false);
                        } else {
                            if (PokemonTeam.getPokemonTeamList().size() > 0) {
                                int currentSelectedTeam = mPokemonTeamSpinner.getSelectedItemPosition();
                                mPokemonTeamSpinner.setAdapter(mPokemonTeamListArrayAdapter);
                                mPokemonTeamSpinner.setEnabled(true);
                                int newSelectedTeam = -1;
                                for (int i = 0; i < PokemonTeam.getPokemonTeamList().size(); i++) {
                                    if (PokemonTeam.getPokemonTeamList().get(i).getTier().equals(currentFormatString)) {
                                        newSelectedTeam = i;
                                        break;
                                    }
                                }
                                if (newSelectedTeam > -1) {
                                    mPokemonTeamSpinner.setSelection(newSelectedTeam);
                                } else {
                                    mPokemonTeamSpinner.setSelection(currentSelectedTeam);
                                }
                            } else {
                                //there are no teams, we fill the spinner with a filler item an disable it
                                mPokemonTeamSpinner.setAdapter(mNoTeamsAdapter);
                                mPokemonTeamSpinner.setEnabled(false);
                            }
                        }
                    }
                }
            }
        });
    }

    public void dismissWaitingDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWaitingDialog.dismiss();
            }
        });

    }

}

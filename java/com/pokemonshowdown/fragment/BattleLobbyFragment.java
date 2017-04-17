package com.pokemonshowdown.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.pokemonshowdown.R;
import com.pokemonshowdown.activity.ContainerActivity;
import com.pokemonshowdown.adapter.PokemonTeamSpinnerAdapter;
import com.pokemonshowdown.application.MyApplication;
import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.Onboarding;
import com.pokemonshowdown.data.PokemonTeam;
import com.pokemonshowdown.dialog.ChallengeDialog;
import com.pokemonshowdown.dialog.OnboardingDialog;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by McBeengs on 23/10/2016.
 */

public class BattleLobbyFragment extends BaseFragment {

    public static int requestingRoomIndex = -1;
    private boolean isSearching = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_battle_lobby, container, false);
    }

    @Override
    public void onViewCreated(final View mView, @Nullable Bundle savedInstanceState) {
        LinearLayout userLayout = (LinearLayout) mView.findViewById(R.id.user_layout);
        final EditText userText = (EditText) mView.findViewById(R.id.user_text);
        final Spinner formatsSpinner = (Spinner) mView.findViewById(R.id.formats_spinner);
        final Spinner teamSpinner = (Spinner) mView.findViewById(R.id.teams_spinner);
        final Button findButton = (Button) mView.findViewById(R.id.find_button);

        String temp = "";
        if (getArguments() != null) {
            temp = getArguments().getString("format");
        }

        final String format = temp;
        if (format.equals("normal")) {
            userLayout.setVisibility(View.GONE);
        } else {
            userText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() > 0) {
                        findButton.setEnabled(true);
                    } else {
                        findButton.setEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        //Populate formats spinner with webservice response
        final ArrayList<String> mFormatList = new ArrayList<>();

        ArrayList<BattleFieldData.FormatType> formatTypes = BattleFieldData.get(mView.getContext()).getFormatTypes();
        for (BattleFieldData.FormatType formatType : formatTypes) {
            ArrayList<String> result = formatType.getSearchableFormatList();
            for (String name : result) {
                mFormatList.add(name);
            }
        }

        ArrayAdapter<String> formatsAdapter = new ArrayAdapter<>(mView.getContext(), R.layout.fragment_simple_list_row, mFormatList);

        formatsSpinner.setAdapter(formatsAdapter);

        //Create and set custom adapter to teams spinner
        final PokemonTeamSpinnerAdapter mRandomTeamAdapter = new PokemonTeamSpinnerAdapter(mView.getContext(),
                Arrays.asList(new PokemonTeam(ChallengeDialog.RANDOM_TEAM_NAME)));
        teamSpinner.setAdapter(mRandomTeamAdapter);

        formatsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mFormatList.size() == 0) {
                    //can happen when no internet
                    return;
                }
                String currentFormatString = (String) formatsSpinner.getItemAtPosition(position);
                BattleFieldData.Format currentFormat;
                if (currentFormatString != null) {
                    currentFormat = BattleFieldData.get(mView.getContext()).getFormat(currentFormatString);
                    if (currentFormat != null) {
                        if (currentFormat.isRandomFormat()) {
                            teamSpinner.setAdapter(mRandomTeamAdapter);
                            teamSpinner.setEnabled(false);
                            if (format.equals("challenge") && !userText.getText().toString().isEmpty()) {
                                findButton.setEnabled(true);
                            } else if (format.equals("normal")) {
                                findButton.setEnabled(true);
                            } else {
                                findButton.setEnabled(false);
                            }
                        } else {
                            if (PokemonTeam.getPokemonTeamList(getContext()) != null && PokemonTeam.getPokemonTeamList(getContext()).size() > 0) {
                                ArrayList<PokemonTeam> formatTeams = new ArrayList<>();

                                for (int i = 0; i < PokemonTeam.getPokemonTeamList(getContext()).size(); i++) {
                                    if (PokemonTeam.getPokemonTeamList(getContext()).get(i).getTier().equals(currentFormatString)) {
                                        formatTeams.add(PokemonTeam.getPokemonTeamList(getContext()).get(i));
                                    }
                                }

                                if (formatTeams.size() == 0) {
                                    teamSpinner.setAdapter(new ArrayAdapter<>(mView.getContext(), android.R.layout.simple_spinner_item,
                                            mView.getResources().getStringArray(R.array.empty_team_list_filler)));
                                    teamSpinner.setEnabled(false);
                                    findButton.setEnabled(false);
                                    findButton.setEnabled(false);
                                    return;
                                } else {
                                    teamSpinner.setAdapter(new PokemonTeamSpinnerAdapter(mView.getContext(), formatTeams));
                                    teamSpinner.setEnabled(true);
                                }

                                if (format.equals("challenge") && !userText.getText().toString().isEmpty()) {
                                    findButton.setEnabled(true);
                                } else if (format.equals("normal")) {
                                    findButton.setEnabled(true);
                                } else {
                                    findButton.setEnabled(false);
                                }
                            } else {
                                //there are no teams, we fill the spinner with a filler item an disable it
                                teamSpinner.setAdapter(new ArrayAdapter<>(mView.getContext(), android.R.layout.simple_spinner_item,
                                        mView.getResources().getStringArray(R.array.empty_team_list_filler)));
                                teamSpinner.setEnabled(false);
                                findButton.setEnabled(false);
                            }
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //first need to check if the user is logged in
                Onboarding onboarding = Onboarding.get(mView.getContext().getApplicationContext());
                if (!onboarding.isSignedIn()) {
                    FragmentManager fm = ((FragmentActivity) mView.getContext()).getSupportFragmentManager();
                    OnboardingDialog fragment = new OnboardingDialog();
                    fragment.show(fm, OnboardingDialog.OTAG);
                    return;
                }

                if (!ContainerActivity.lastRoomIdCreated.isEmpty()) {
                    new AlertDialog.Builder(getContext()).setMessage("Unfortunately, the app currently supports only one battle" +
                            " per time.").setPositiveButton("Ok", null).show();
                    return;
                }

                String currentFormatString = formatsSpinner.getSelectedItem().toString();
                if (format.equals("challenge")) {
                    Toast.makeText(mView.getContext(), R.string.request_sent, Toast.LENGTH_SHORT).show();
                    String toChallenge = userText.getText().toString();

                    // todo check if user exists
                    if (currentFormatString != null) {
                        BattleFieldData.Format currentFormat = BattleFieldData.get(mView.getContext()).getFormat(currentFormatString);
                        if (currentFormat.isRandomFormat()) {
                            // we send /utm only
                            MyApplication.getMyApplication().sendClientMessage("|/utm");
                            MyApplication.getMyApplication().sendClientMessage("|/challenge " + toChallenge + ", " + MyApplication.toId(currentFormatString));
                            requestingRoomIndex = MainScreenFragment.TABS_HOLDER_ACCESSOR.getTabIndex();
                        } else {
                            //we need to send the team for verification
                            Object pokemonTeamObject = teamSpinner.getSelectedItem();
                            // if we have no teams
                            if (!(pokemonTeamObject instanceof PokemonTeam)) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(mView.getContext());
                                builder.setTitle(R.string.error_dialog_title);
                                builder.setIcon(android.R.drawable.ic_dialog_alert);
                                builder.setMessage(R.string.no_teams);
                                final AlertDialog alert = builder.create();
                                alert.show();
                                requestingRoomIndex = -1;
                                return;
                            }
                            PokemonTeam pokemonTeam = (PokemonTeam) pokemonTeamObject;
                            String teamVerificationString = pokemonTeam.exportForVerification();
                            MyApplication.getMyApplication().sendClientMessage("|/utm " + teamVerificationString);
                            MyApplication.getMyApplication().sendClientMessage("|/challenge " + toChallenge + ", " + MyApplication.toId(currentFormatString));
                            requestingRoomIndex = MainScreenFragment.TABS_HOLDER_ACCESSOR.getTabIndex();
                        }
                    }
                } else {
                    BattleFieldData.Format currentFormat = BattleFieldData.get(getActivity()).getFormat(currentFormatString);
                    if (currentFormat.isRandomFormat()) {
                        // we send /utm only
                        MyApplication.getMyApplication().sendClientMessage("|/utm");
                        MyApplication.getMyApplication().sendClientMessage("|/search " + MyApplication.toId(currentFormatString));
                        requestingRoomIndex = MainScreenFragment.TABS_HOLDER_ACCESSOR.getTabIndex();
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
                        MyApplication.getMyApplication().sendClientMessage("|/search " + MyApplication.toId(currentFormatString));
                        requestingRoomIndex = MainScreenFragment.TABS_HOLDER_ACCESSOR.getTabIndex();
                    }
                }

            }
        });
    }
}

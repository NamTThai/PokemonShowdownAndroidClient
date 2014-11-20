package com.pokemonshowdown.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import java.util.Timer;
import java.util.TimerTask;


public class FindBattleFragment extends Fragment {
    public final static String FTAG = FindBattleFragment.class.getName();

    private ProgressDialog waitingDialog;
    private Timer disableTimer = null;
    private ArrayList<String> mFormatList;
    private TextView watchBattle;
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
        waitingDialog = new ProgressDialog(getActivity());

        Spinner spin = (Spinner) view.findViewById(R.id.teams_spinner);
        PokemonTeamListArrayAdapter pokemonTeamListArrayAdapter = new PokemonTeamListArrayAdapter(getActivity(), PokemonTeam.getPokemonTeamList());
        //spin.setAdapter(pokemonTeamListArrayAdapter);

        TextView findBattle = (TextView) view.findViewById(R.id.find_battle);
        findBattle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.still_in_development)
                        .create().show();

                /*
                waitingDialog.setCancelable(true);
                waitingDialog.setMessage("Finding battle");
                waitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                waitingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        MyApplication.getMyApplication().sendClientMessage("|/cancelsearch");
                    }
                });
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        waitingDialog.show();
                    }
                });
                 */
            }
        });
        watchBattle = (TextView) view.findViewById(R.id.watch_battle);
        watchBattle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableTimer = new Timer();
                disableTimer.schedule(new TimerTask() {
                                         @Override
                                         public void run() {
                                             dismissWaitingDialog();
                                         }
                                     }, 5000);

                MyApplication.getMyApplication().sendClientMessage("|/cmd roomlist");
                waitingDialog.setMessage("Downloading list of matches");
                waitingDialog.setCancelable(true);
                waitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                waitingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        // nothing
                    }
                });

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        watchBattle.setEnabled(false);
                        waitingDialog.show();
                    }
                });
            }
        });
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
            }
        });
    }

    public void dismissWaitingDialog() {
        if(disableTimer != null) {
            disableTimer.cancel();
            disableTimer = null;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                waitingDialog.dismiss();
                watchBattle.setEnabled(true);
            }
        });

    }

}

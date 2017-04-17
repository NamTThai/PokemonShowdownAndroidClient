package com.pokemonshowdown.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.pokemonshowdown.R;
import com.pokemonshowdown.application.MyApplication;
import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.Onboarding;
import com.pokemonshowdown.dialog.OnboardingDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by McBeengs on 23/10/2016.
 */

public class WatchBattleFragment extends BaseFragment {

    public static int requestingRoomIndex = -1;
    public static WatchFragmentAccessor ACCESSOR;
    private View mView;
    private ProgressDialog mWaitingDialog;
    private ListView mListView;
    private String selectedKey;
    private String selectedValue;
    private Button findButton;

    public static WatchBattleFragment newInstance() {
        return new WatchBattleFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ACCESSOR = new WatchFragmentAccessor();
        return inflater.inflate(R.layout.fragment_watch_battle_lobby, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mView = view;
        TextView roomTitle = (TextView) mView.findViewById(R.id.room_title);
        final Spinner formatsSpinner = (Spinner) mView.findViewById(R.id.formats_spinner);
        mListView = (ListView) mView.findViewById(R.id.battles_list_view);
        findButton = (Button) mView.findViewById(R.id.find_button);

        //Populate formats spinner with webservice response
        final ArrayList<String> mFormatList = new ArrayList<>();

        ArrayList<BattleFieldData.FormatType> formatTypes = BattleFieldData.get(mView.getContext()).getFormatTypes();
        for (BattleFieldData.FormatType formatType : formatTypes) {
            ArrayList<String> result = formatType.getSearchableFormatList();
            for (String name : result) {
                mFormatList.add(name);
            }
        }

        ArrayAdapter<String> formatsAdapter = new ArrayAdapter<>(mView.getContext(), R.layout.fragment_user_list, mFormatList);
        formatsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatsSpinner.setAdapter(formatsAdapter);

        formatsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MyApplication.getMyApplication().sendClientMessage("|/cmd roomlist");

                //Alert dialog that hides the loading
                mWaitingDialog = new ProgressDialog(mView.getContext());
                mWaitingDialog.setMessage(mView.getResources().getString(R.string.download_matches_inprogress));
                mWaitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mWaitingDialog.setCancelable(true);

                ((Activity) mView.getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWaitingDialog.show();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void battlesListViewUpdate() {
        if (!dismissWaitingDialog()) {
            return;
        }

        HashMap<String, String> battleList = BattleFieldData.get(mView.getContext()).getAvailableWatchBattleList();
        if (battleList.isEmpty()) {
            new AlertDialog.Builder(mView.getContext())
                    .setMessage(R.string.no_available_battle)
                    .create()
                    .show();
            return;
        }
        final String[] key = new String[battleList.size()];
        final String[] value = new String[battleList.size()];
        int count = 0;
        Set<String> iterators = battleList.keySet();
        for (String iterator : iterators) {
            key[count] = iterator;
            value[count] = battleList.get(iterator);
            count++;
        }

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

                BattleFieldData.get(mView.getContext()).joinRoom(selectedKey, true);
                requestingRoomIndex = MainScreenFragment.TABS_HOLDER_ACCESSOR.getTabIndex();
            }
        });

        mListView.setAdapter(new ArrayAdapter<String>(mListView.getContext(), R.layout.fragment_simple_list_row, value));
        mListView.setItemChecked(0, true);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedKey = key[i];
                selectedValue = value[i];
                mView.findViewById(R.id.find_button).setEnabled(true);
            }
        });
    }

    private boolean dismissWaitingDialog() {
        if (mWaitingDialog.isShowing()) {
            ((Activity) mView.getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWaitingDialog.dismiss();
                }
            });
            return true;
        } else {
            return false;
        }
    }

    public class WatchFragmentAccessor {

        public void fireBattlesListViewUpdate() {
            battlesListViewUpdate();
        }

        public void fireDismissWaitingDialog() {
            dismissWaitingDialog();
        }
    }
}

package com.pokemonshowdown.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Created by thain on 7/22/14.
 */

public class AbilityDialog extends DialogFragment {
    public static final String ATAG = "ABILITY_DIALOG";

    private String[] mAbilityList;

    public AbilityDialog() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_ability, container);

        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.pokemon_ability_list);
        radioGroup.removeAllViews();
        mAbilityList = getArguments().getStringArray("AbilityList");
        int selectedAbility = getArguments().getInt("SelectedAbility");
        for (int i=0; i < mAbilityList.length; i++) {
            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setText(mAbilityList[i]);
            radioButton.setPadding(0, 0, 48, 0);
            if (i == selectedAbility) {
                radioButton.setChecked(true);
            }
            radioGroup.addView(radioButton);
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                PokemonFragment pokemonFragment = (PokemonFragment) fm.findFragmentByTag(PokemonFragment.PokemonTAG);
                pokemonFragment.setAbility(group.indexOfChild(group.findViewById(checkedId)));
                getDialog().dismiss();
            }
        });

        return view;
    }
}
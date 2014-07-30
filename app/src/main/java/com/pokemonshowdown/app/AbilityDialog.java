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

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by thain on 7/22/14.
 */

public class AbilityDialog extends DialogFragment {
    public static final String ATAG = "ABILITY_DIALOG";

    private HashMap<String, String> mAbilityList;

    public AbilityDialog() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_ability, container);

        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.pokemon_ability_list);
        radioGroup.removeAllViews();
        mAbilityList = (HashMap<String, String>) getArguments().getSerializable("AbilityList");
        String selectedAbility = getArguments().getString("SelectedAbility");
        Iterator<String> keys = mAbilityList.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setText(mAbilityList.get(key));
            radioButton.setPadding(0, 0, 48, 0);
            radioButton.setTag(key);
            if (key.equals(selectedAbility)) {
                radioButton.setChecked(true);
            }
            radioGroup.addView(radioButton);
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                PokemonFragment pokemonFragment = (PokemonFragment) fm.findFragmentByTag(PokemonFragment.PokemonTAG);
                pokemonFragment.getPokemon().setAbility((String) group.findViewById(checkedId).getTag());
                pokemonFragment.setAbilityString(pokemonFragment.getPokemon().getAbility());
                getDialog().dismiss();
            }
        });

        return view;
    }
}
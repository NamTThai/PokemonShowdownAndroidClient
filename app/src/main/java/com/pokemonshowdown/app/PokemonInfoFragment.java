package com.pokemonshowdown.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pokemonshowdown.data.ItemDex;
import com.pokemonshowdown.data.MoveDex;
import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.data.PokemonInfo;

import java.util.HashMap;
import java.util.Set;

public class PokemonInfoFragment extends DialogFragment {
    public final static String PTAG = PokemonInfoFragment.class.getName();
    public final static String POKEMON_INFO = "PokemonInfo";
    public final static String SWITCH = "Switch";

    private PokemonInfo mPokemonInfo;
    private boolean mSwitch;

    public static PokemonInfoFragment newInstance(PokemonInfo pkm, boolean switchPkm) {
        PokemonInfoFragment fragment = new PokemonInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(POKEMON_INFO, pkm);
        args.putBoolean(SWITCH, switchPkm);
        fragment.setArguments(args);
        return fragment;
    }

    public PokemonInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPokemonInfo = (PokemonInfo) getArguments().getSerializable(POKEMON_INFO);
        mSwitch = getArguments().getBoolean(SWITCH);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pokemon_info, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int[] typeIcon = mPokemonInfo.getTypeIcon();
        int type2;
        if (typeIcon.length == 2) {
            type2 = typeIcon[1];
        } else {
            type2 = 0;
        }

        TextView pokemonName = (TextView) view.findViewById(R.id.pokemon_name);
        pokemonName.setText(mPokemonInfo.getName());
        pokemonName.setCompoundDrawablesWithIntrinsicBounds(mPokemonInfo.getIcon(getActivity()), 0, typeIcon[0], 0);
        pokemonName.setCompoundDrawablePadding(8);

        TextView pokemonLevelGender = (TextView) view.findViewById(R.id.pokemon_level_gender);
        pokemonLevelGender.setText("Lv " + mPokemonInfo.getLevel());
        pokemonLevelGender.setCompoundDrawablesWithIntrinsicBounds(type2, 0, Pokemon.getGenderIcon(mPokemonInfo.getGender()), 0);
        pokemonLevelGender.setCompoundDrawablePadding(8);

        ImageView pokemonView = (ImageView) view.findViewById(R.id.pokemon_view);
        pokemonView.setImageResource(mPokemonInfo.getSprite(getActivity()));

        TextView pokemonStats = (TextView) view.findViewById(R.id.stats);
        pokemonStats.setText(getStatsString());

        TextView pokemonAbility = (TextView) view.findViewById(R.id.stats_abilities);
        pokemonAbility.setText(mPokemonInfo.getAbility());

        TextView nature = (TextView) view.findViewById(R.id.nature);
        if (mPokemonInfo.getNature() != null) {
            nature.setText(mPokemonInfo.getNature());
        } else {
            nature.setVisibility(View.GONE);
        }

        TextView item = (TextView) view.findViewById(R.id.item);
        if (mPokemonInfo.getItem(getActivity()) != null) {
            item.setText(mPokemonInfo.getItem(getActivity()));
            item.setCompoundDrawablesWithIntrinsicBounds(ItemDex.getItemIcon(getActivity(), mPokemonInfo.getItem(getActivity())), 0, 0, 0);
        } else {
            item.setVisibility(View.GONE);
        }
        
        TextView status = (TextView) view.findViewById(R.id.status);
        if (mPokemonInfo.getStatus() != null) {
            setStatus(status, mPokemonInfo.getStatus());
        } else {
            status.setVisibility(View.GONE);
        }

        TextView hp = (TextView) view.findViewById(R.id.hp);
        hp.setText(Integer.toString(mPokemonInfo.getHp()));

        ProgressBar hpBar = (ProgressBar) view.findViewById(R.id.bar_hp);
        hpBar.setProgress(mPokemonInfo.getHp());

        HashMap<String, Integer> moves = mPokemonInfo.getMoves();
        Set<String> moveSets = moves.keySet();
        String[] moveNames = moveSets.toArray(new String[moveSets.size()]);
        if (moveNames.length < 4) {
            view.findViewById(R.id.move4).setVisibility(View.GONE);
        } else {
            ((TextView) view.findViewById(R.id.move4_name)).setText(moveNames[3]);
            ((ImageView) view.findViewById(R.id.move4_type)).setImageResource(MoveDex.getMoveTypeIcon(getActivity(), moveNames[3]));
            ((TextView) view.findViewById(R.id.move4_pp)).setText(moves.get(moveNames[3]));
        }

        if (moveNames.length < 3) {
            view.findViewById(R.id.move3).setVisibility(View.GONE);
        } else {
            ((TextView) view.findViewById(R.id.move3_name)).setText(moveNames[2]);
            ((ImageView) view.findViewById(R.id.move3_type)).setImageResource(MoveDex.getMoveTypeIcon(getActivity(), moveNames[2]));
            ((TextView) view.findViewById(R.id.move3_pp)).setText(moves.get(moveNames[2]));
        }

        if (moveNames.length < 2) {
            view.findViewById(R.id.move2).setVisibility(View.GONE);
        } else {
            ((TextView) view.findViewById(R.id.move2_name)).setText(moveNames[1]);
            ((ImageView) view.findViewById(R.id.move2_type)).setImageResource(MoveDex.getMoveTypeIcon(getActivity(), moveNames[1]));
            ((TextView) view.findViewById(R.id.move2_pp)).setText(moves.get(moveNames[1]));
        }

        if (moveNames.length < 1) {
            view.findViewById(R.id.move1).setVisibility(View.GONE);
        } else {
            ((TextView) view.findViewById(R.id.move1_name)).setText(moveNames[0]);
            ((ImageView) view.findViewById(R.id.move1_type)).setImageResource(MoveDex.getMoveTypeIcon(getActivity(), moveNames[0]));
            ((TextView) view.findViewById(R.id.move1_pp)).setText(moves.get(moveNames[0]));
        }

        TextView switchPkm = (TextView) view.findViewById(R.id.switchPkm);
        if (mSwitch) {
            switchPkm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchPkm();
                }
            });
        } else {
            switchPkm.setVisibility(View.GONE);
        }
    }

    public String getStatsString() {
        int[] stats = mPokemonInfo.getStats();
        return ("Atk " + stats[0] + " / Def " + stats[1] + " / SpA " + stats[2] + " / SpD " + stats[3] + " / Spe " + stats[4]);
    }

    public void setStatus(TextView statusView, String status) {
        if (getView() == null) {
            return;
        }
        
        statusView.setText(status.toUpperCase());
        switch (status) {
            case "slp":
                statusView.setBackgroundResource(R.drawable.editable_frame_blackwhite);
                break;
            case "psn":
            case "tox":
                statusView.setBackgroundResource(R.drawable.editable_frame_light_purple);
                break;
            case "brn":
                statusView.setBackgroundResource(R.drawable.editable_frame_light_red);
                break;
            case "par":
                statusView.setBackgroundResource(R.drawable.editable_frame_light_orange);
                break;
            case "frz":
                statusView.setBackgroundResource(R.drawable.editable_frame);
                break;
            default:
                statusView.setBackgroundResource(R.drawable.editable_frame);
        }
        statusView.setPadding(2, 2, 2, 2);
    }

    private void switchPkm() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

}

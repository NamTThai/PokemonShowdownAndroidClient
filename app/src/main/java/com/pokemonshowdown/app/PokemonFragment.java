package com.pokemonshowdown.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.data.SearchableActivity;

import java.util.Arrays;
import java.util.Set;

public class PokemonFragment extends DialogFragment {
    public final static String PTAG = PokemonFragment.class.getName();
    private Pokemon mPokemon;

    public final static String POKEMON = "Pokemon";
    public final static String SEARCH = "Search";
    public final static String SEARCH_CODE = "Search Code";
    public final static String STAGES = "Stages";

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() instanceof TeamBuildingActivity) {
            TeamBuildingActivity parent = (TeamBuildingActivity) getActivity();
            parent.updateList();
        } else if (getActivity() instanceof DmgCalcActivity) {
            DmgCalcActivity parent = (DmgCalcActivity) getActivity();
            parent.updateDamage();
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPokemon = (Pokemon) getArguments().getSerializable(POKEMON);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pokemon, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView pokemonIcon = (ImageView) view.findViewById(R.id.pokemon_icon);
        pokemonIcon.setImageResource(getPokemon().getIcon());

        TextView pokemonName = (TextView) view.findViewById(R.id.pokemon_name);
        pokemonName.setText(getPokemon().getName());

        final ImageView pokemonView = (ImageView) view.findViewById(R.id.pokemon_view);
        pokemonView.setImageResource(getPokemon().getSprite());

        pokemonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPokemon().switchShiny(getActivity());
                pokemonView.setImageResource(getPokemon().getSprite());
            }
        });

        if (getArguments().getBoolean(SEARCH)) {
            addSearchWidget(view);
        }

        final TextView pokemonStats = (TextView) view.findViewById(R.id.stats);
        resetStatsString();
        pokemonStats.setBackgroundResource(R.drawable.editable_frame);
        pokemonStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                StatsDialog statsDialog = new StatsDialog();
                Bundle bundle = new Bundle();
                bundle.putIntArray(StatsDialog.ARGUMENT_STATS, getPokemon().getStats());
                bundle.putIntArray(StatsDialog.ARGUMENT_BASE_STATS, getPokemon().getBaseStats());
                bundle.putIntArray(StatsDialog.ARGUMENT_EV, getPokemon().getEVs());
                bundle.putIntArray(StatsDialog.ARGUMENT_IV, getPokemon().getIVs());
                bundle.putInt(StatsDialog.ARGUMENT_LEVEL, getPokemon().getLevel());
                bundle.putFloatArray(StatsDialog.ARGUMENT_NATURE_MULTIPLIER, getPokemon().getNatureMultiplier());
                bundle.putIntArray(StatsDialog.ARGUMENT_STAGES, getPokemon().getStages());

                if (getArguments().containsKey(STAGES)) {
                    bundle.putBoolean(StatsDialog.ARGUMENT_SHOW_STAGES, getArguments().getBoolean(STAGES));
                }

                statsDialog.setArguments(bundle);
                statsDialog.show(fm, StatsDialog.STAG);
            }
        });

        final TextView pokemonAbility = (TextView) view.findViewById(R.id.stats_abilities);
        pokemonAbility.setBackgroundResource(R.drawable.editable_frame);
        String abilities = getPokemon().getAbility();
        pokemonAbility.setText(abilities);
        pokemonAbility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Set<String> abilitySet = getPokemon().getAbilityList().keySet();
                final String[] abilityList = abilitySet.toArray(new String[abilitySet.size()]);
                final String[] abilityNames = new String[abilityList.length];
                String selectedAbilityTag = getPokemon().getAbilityTag();
                int selectedAbility = 0;
                for (int i = 0; i < abilityList.length; i++) {
                    abilityNames[i] = getPokemon().getAbilityList().get(abilityList[i]);
                    if (abilityList[i].equals(selectedAbilityTag)) {
                        selectedAbility = i;
                    }
                }
                Dialog dialog = new AlertDialog.Builder(getActivity())
                        .setSingleChoiceItems(abilityNames, selectedAbility, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newAbilityTag = abilityList[which];
                                getPokemon().setAbilityTag(newAbilityTag);
                                setAbilityString(getPokemon().getAbility());
                                dialog.dismiss();
                            }
                        })
                        .create();
                dialog.show();
            }
        });

        int[] typesIcon = getPokemon().getTypeIcon();
        ImageView type1 = (ImageView) view.findViewById(R.id.type_1);
        type1.setImageResource(typesIcon[0]);
        ImageView type2 = (ImageView) view.findViewById(R.id.type_2);
        if (typesIcon.length == 2) {
            type2.setImageResource(typesIcon[1]);
        } else {
            type2.setImageResource(0);
        }

        TextView level = (TextView) view.findViewById(R.id.level);
        level.setBackgroundResource(R.drawable.editable_frame);
        level.setText(Integer.toString(getPokemon().getLevel()));

        level.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                LevelDialog levelDialog = new LevelDialog();
                Bundle bundle = new Bundle();
                bundle.putInt("Level", getPokemon().getLevel());
                levelDialog.setArguments(bundle);
                levelDialog.show(fm, LevelDialog.LTAG);
            }
        });

        final ImageView gender = (ImageView) view.findViewById(R.id.gender);
        gender.setImageResource(getPokemon().getGenderIcon());
        gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getPokemon().isGenderAvailable()) {
                    getPokemon().switchGender(getActivity());
                    gender.setImageResource(getPokemon().getGenderIcon());
                    int pokemonSprite = getPokemon().getSprite();
                    pokemonView.setImageResource(pokemonSprite);
                }
            }
        });

        final TextView nature = (TextView) view.findViewById(R.id.nature);
        nature.setText(getPokemon().getNature());
        nature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedNature = Arrays.binarySearch(Pokemon.NATURES, getPokemon().getNature());
                Dialog dialog = new AlertDialog.Builder(getActivity())
                        .setSingleChoiceItems(Pokemon.NATURES_DETAILS, selectedNature, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newNature = Pokemon.NATURES[which];
                                nature.setText(newNature);
                                mPokemon.setNature(newNature);
                                mPokemon.setStats(getPokemon().calculateStats());
                                PokemonFragment.this.resetStatsString();
                                dialog.dismiss();
                            }
                        })
                        .create();
                dialog.show();
            }
        });

        int initialHPFraction = (int) (100 * getPokemon().getHP() / (double) getPokemon().calculateHP());
        final TextView initialHPText = (TextView) view.findViewById(R.id.initial_hp_text);
        initialHPText.setText(getResources().getString(R.string.initial_hp_dialog, initialHPFraction));

        final SeekBar initialHP = (SeekBar) view.findViewById(R.id.initial_hp);
        initialHP.setProgress(initialHPFraction);
        initialHP.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPokemon.setHP((int) Math.ceil(getPokemon().calculateHP() * (progress / 100.0)));
                initialHPText.setText(getResources().getString(R.string.initial_hp_dialog, progress));
                pokemonStats.setText(getStatsString());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        initialHP.setVisibility(getArguments().getBoolean(STAGES) ? View.VISIBLE : View.GONE);
        initialHPText.setVisibility(getArguments().getBoolean(STAGES) ? View.VISIBLE : View.GONE);
    }

    private void addSearchWidget(View view) {
        ImageButton imageButton = (ImageButton) view.findViewById(R.id.pokemon_fragment_functions);
        imageButton.setImageResource(R.drawable.ic_action_search);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFragment();
                Intent intent = new Intent(getActivity(), SearchableActivity.class);
                intent.putExtra("Search Type", SearchableActivity.REQUEST_CODE_SEARCH_POKEMON);
                getActivity().startActivityForResult(intent, getArguments().getInt(SEARCH_CODE));
            }
        });
    }

    public Pokemon getPokemon() {
        return mPokemon;
    }

    public void setAbilityString(String ability) {
        TextView pokemonAbility = (TextView) getView().findViewById(R.id.stats_abilities);
        pokemonAbility.setText(ability);
    }

    public String getStatsString() {
        if (getArguments().getBoolean(STAGES) && getPokemon().calculateHP() != getPokemon().getHP()) {
            return ("HP " + Integer.toString(getPokemon().getHP()) + " (" + getPokemon().calculateHP() + ")" + " / Atk " + Integer.toString(getPokemon().getBaseAtk()) + " / Def " + Integer.toString(getPokemon().getBaseDef()) + " / SpA " + Integer.toString(getPokemon().getBaseSpAtk()) + " / SpD " + Integer.toString(getPokemon().getBaseSpDef()) + " / Spe " + Integer.toString(getPokemon().getBaseSpd()));
        } else {
            return ("HP " + Integer.toString(getPokemon().getHP()) + " / Atk " + Integer.toString(getPokemon().getAtk()) + " / Def " + Integer.toString(getPokemon().getDef()) + " / SpA " + Integer.toString(getPokemon().getSpAtk()) + " / SpD " + Integer.toString(getPokemon().getSpDef()) + " / Spe " + Integer.toString(getPokemon().getSpd()));

        }
    }

    public void resetStatsString() {
        TextView pokemonStats = (TextView) getView().findViewById(R.id.stats);
        pokemonStats.setText(getStatsString());
    }

    public String getBaseStatsString() {
        return ("HP " + Integer.toString(getPokemon().getBaseHP()) + " / Atk " + Integer.toString(getPokemon().getBaseAtk()) + " / Def " + Integer.toString(getPokemon().getBaseDef()) + " / SpA " + Integer.toString(getPokemon().getBaseSpAtk()) + " / SpD " + Integer.toString(getPokemon().getBaseSpDef()) + " / Spe " + Integer.toString(getPokemon().getBaseSpd()));
    }

    public void resetBaseStatsString() {
        TextView pokemonStats = (TextView) getView().findViewById(R.id.stats);
        pokemonStats.setText(getBaseStatsString());
    }

    public void setLevelString(int lv) {
        TextView level = (TextView) getView().findViewById(R.id.level);
        level.setText(Integer.toString(lv));
    }

    private void closeFragment() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }


}

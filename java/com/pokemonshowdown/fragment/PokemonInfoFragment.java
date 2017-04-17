package com.pokemonshowdown.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pokemonshowdown.R;
import com.pokemonshowdown.data.ItemDex;
import com.pokemonshowdown.data.MoveDex;
import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.data.PokemonInfo;
import com.pokemonshowdown.data.RunWithNet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Set;

import me.grantland.widget.AutofitTextView;

public class PokemonInfoFragment extends DialogFragment {
    public final static String PTAG = PokemonInfoFragment.class.getName();
    public final static String POKEMON_INFO = "PokemonInfo";
    public final static String SWITCH = "Switch";
    public final static String OPPONENT = "Opponent";
    public final static String FRAGMENT_TAG = "Fragment Tag";
    public final static String ID = "Id";

    private PokemonInfo mPokemonInfo;
    private boolean mSwitch;
    private boolean mOpponent;
    private String mFragmentTag;
    private int mId;

    public PokemonInfoFragment() {
        // Required empty public constructor
    }

    public static PokemonInfoFragment newInstance(PokemonInfo pkm, boolean switchPkm, boolean opponent) {
        PokemonInfoFragment fragment = new PokemonInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(POKEMON_INFO, pkm);
        args.putBoolean(SWITCH, switchPkm);
        args.putBoolean(OPPONENT, opponent);
        fragment.setArguments(args);
        return fragment;
    }

    public static PokemonInfoFragment newInstance(PokemonInfo pkm, boolean switchPkm, boolean opponent, String tag, int id) {
        PokemonInfoFragment fragment = new PokemonInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(POKEMON_INFO, pkm);
        args.putBoolean(SWITCH, switchPkm);
        args.putBoolean(OPPONENT, opponent);
        args.putString(FRAGMENT_TAG, tag);
        args.putInt(ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
        mPokemonInfo = (PokemonInfo) getArguments().getSerializable(POKEMON_INFO);
        mSwitch = getArguments().getBoolean(SWITCH);
        mOpponent = getArguments().getBoolean(OPPONENT);
        mFragmentTag = getArguments().getString(FRAGMENT_TAG);
        mId = getArguments().getInt(ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pokemon_info, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int[] typeIcon = mPokemonInfo.getTypeIcon();

        TextView pokemonName = (TextView) view.findViewById(R.id.pokemon_name);
        pokemonName.setText(mPokemonInfo.getName());
        pokemonName.setCompoundDrawablesWithIntrinsicBounds(mPokemonInfo.getIcon(getActivity()), 0, 0, 0);
        pokemonName.setCompoundDrawablePadding(8);

        ImageView type1 = (ImageView) view.findViewById(R.id.type_1);
        type1.setImageResource(typeIcon[0]);
        ImageView type2 = (ImageView) view.findViewById(R.id.type_2);
        if (typeIcon.length > 1) {
            type2.setImageResource(typeIcon[1]);
        } else {
            type2.setVisibility(View.INVISIBLE);
        }

        TextView pokemonLevel = (TextView) view.findViewById(R.id.level);
        pokemonLevel.setText("Lv " + mPokemonInfo.getLevel());

        ImageView gender = (ImageView) view.findViewById(R.id.gender);
        gender.setImageResource(Pokemon.getGenderIcon(mPokemonInfo.getGender()));

        ImageView pokemonView = (ImageView) view.findViewById(R.id.pokemon_view);
        pokemonView.setImageResource(mPokemonInfo.getSprite(getActivity(), false));

        TextView statsLabel = (TextView) view.findViewById(R.id.stats_label);
        AutofitTextView pokemonStats = (AutofitTextView) view.findViewById(R.id.stats);
        if (!mOpponent) {
            pokemonStats.setText(getStatsString());
        } else {
            statsLabel.setText("Speed:");
            pokemonStats.setLines(2);
            pokemonStats.setGravity(View.TEXT_ALIGNMENT_CENTER);
            pokemonStats.setText(Pokemon.calculateSpd(mPokemonInfo.getStats()[4], 0, 0, mPokemonInfo.getLevel(), 0.9f) + " to "
                    + Pokemon.calculateSpd(mPokemonInfo.getStats()[4], 31, 252, mPokemonInfo.getLevel(), 1.1f) + ", with max EVs / IVs (Before items/abilities/modifiers)");
        }

        TextView pokemonAbility = (TextView) view.findViewById(R.id.ability);
        pokemonAbility.setText(mPokemonInfo.getAbilityName(getActivity()));

        TextView hp = (TextView) view.findViewById(R.id.hp);
        hp.setText(Integer.toString(mPokemonInfo.getHp()) + "%");

        ProgressBar hpBar = (ProgressBar) view.findViewById(R.id.bar_hp);
        hpBar.setProgress(mPokemonInfo.getHp());

//        TextView nature = (TextView) view.findViewById(R.id.nature);
//        if (mPokemonInfo.getNature() != null) {
//            nature.setText(mPokemonInfo.getNature());
//        } else {
//            nature.setVisibility(View.GONE);
//        }

        TextView itemLabel = (TextView) view.findViewById(R.id.item_label);
        TextView item = (TextView) view.findViewById(R.id.item_name);
        ImageView itemIcon = (ImageView) view.findViewById(R.id.item_icon);
        if (mPokemonInfo.getItemName(getActivity()) != null && !mPokemonInfo.getItemName(getActivity()).trim().isEmpty()) {
            item.setText(mPokemonInfo.getItemName(getActivity()));
            itemIcon.setImageResource(ItemDex.getItemIcon(getActivity(), mPokemonInfo.getItemName(getActivity())));
        } else {
            itemLabel.setVisibility(View.GONE);
            item.setVisibility(View.GONE);
            itemIcon.setVisibility(View.GONE);
        }

//        TextView status = (TextView) view.findViewById(R.id.status);
//        if (mPokemonInfo.getStatus() != null) {
//            setStatus(status, mPokemonInfo.getStatus());
//        } else {
//            status.setVisibility(View.GONE);
//        }
//
//        TextView hp = (TextView) view.findViewById(R.id.hp);
//        hp.setText(Integer.toString(mPokemonInfo.getHp()));
//
//        ProgressBar hpBar = (ProgressBar) view.findViewById(R.id.bar_hp);
//        hpBar.setProgress(mPokemonInfo.getHp());

        final LinearLayout[] moveViews = new LinearLayout[4];
        moveViews[0] = (LinearLayout) view.findViewById(R.id.active_move1);
        moveViews[1] = (LinearLayout) view.findViewById(R.id.active_move2);
        moveViews[2] = (LinearLayout) view.findViewById(R.id.active_move3);
        moveViews[3] = (LinearLayout) view.findViewById(R.id.active_move4);
        final AutofitTextView[] moveNames = new AutofitTextView[4];
        moveNames[0] = (AutofitTextView) view.findViewById(R.id.active_move1_name);
        moveNames[1] = (AutofitTextView) view.findViewById(R.id.active_move2_name);
        moveNames[2] = (AutofitTextView) view.findViewById(R.id.active_move3_name);
        moveNames[3] = (AutofitTextView) view.findViewById(R.id.active_move4_name);
        final TextView[] movePps = new TextView[4];
        movePps[0] = (TextView) view.findViewById(R.id.active_move1_pp);
        movePps[1] = (TextView) view.findViewById(R.id.active_move2_pp);
        movePps[2] = (TextView) view.findViewById(R.id.active_move3_pp);
        movePps[3] = (TextView) view.findViewById(R.id.active_move4_pp);
        final ImageView[] moveIcons = new ImageView[4];
        moveIcons[0] = (ImageView) view.findViewById(R.id.active_move1_icon);
        moveIcons[1] = (ImageView) view.findViewById(R.id.active_move2_icon);
        moveIcons[2] = (ImageView) view.findViewById(R.id.active_move3_icon);
        moveIcons[3] = (ImageView) view.findViewById(R.id.active_move4_icon);

        HashMap<String, Integer> moves = mPokemonInfo.getMoves();
        Set<String> moveSets = moves.keySet();
        String[] movesNames = moveSets.toArray(new String[moveSets.size()]);
        setupMovesView(movesNames, moveViews, moveNames, movePps, moveIcons);

        ImageView switchPkm = (ImageView) view.findViewById(R.id.switchPkm);
        if (mSwitch) {
            switchPkm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new RunWithNet() {
                        @Override
                        public void runWithNet() throws Exception {
                            switchPkm();
                        }
                    }.run();
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

    private void setupMovesView(final String[] moves, LinearLayout[] moveViews, AutofitTextView[] moveNames,
                                TextView[] movePps, ImageView[] moveIcons) {
        try {
            for (int i = 0; i < moves.length; i++) {
                final JSONObject moveJson = MoveDex.get(getContext()).getMoveJsonObject(moves[i]);

                moveNames[i].setText(moveJson.getString("name"));
                if (moveJson.optString("pp", "0").equals("0")) {
                    //sttruggle has noppinfo
                } else {
                    String maxPP = "" + (Integer.parseInt(moveJson.getString("pp"))
                            * 8 / 5);
                    movePps[i].setText(mPokemonInfo.getMoves().get(moveJson.getString("id")) + "/" + maxPP);
                }

                String type = MoveDex.get(getContext()).getMoveJsonObject(moveJson.getString("id")).getString("type");

                int typeIcon = getMoveIcon(type);
                moveIcons[i].setImageResource(typeIcon);

                String ability = mPokemonInfo.getAbilityName(getContext());

                // Account for all different move-type variations
                //{"num":332,"accuracy":true,"basePower":60,"category":"Physical","desc":"This move does not check accuracy.","shortDesc":"This move does not check accuracy.","id":"aerialace","isViable":true,"name":"Aerial Ace","pp":20,"priority":0,"flags":{"contact":1,"protect":1,"mirror":1,"distance":1},"secondary":false,"target":"any","type":"Flying","zMovePower":120,"contestType":"Cool"}
                if (moveJson.getString("name").contains("Judgment") && mPokemonInfo.getName()
                        .contains("Arceus") && mPokemonInfo.getItemName(getContext()).contains("Plate")) {
                    String arceus = mPokemonInfo.getName().substring(7, mPokemonInfo.getName().length());
                    moveViews[i].setBackgroundResource(getMoveBackground(arceus));
                    moveIcons[i].setImageResource(getMoveIcon(arceus.toLowerCase()));
                } else if (type.equals("Normal") && ability.equals("Aerilate") || ability.equals("Pixilate") || ability.equals("Galvanize") ||
                        ability.equals("Refrigerate")) {
                    switch (ability) {
                        case "Aerilate":
                            moveViews[i].setBackgroundResource(getMoveBackground("flying"));
                            moveIcons[i].setImageResource(getMoveIcon("flying"));
                            break;
                        case "Pixilate":
                            moveViews[i].setBackgroundResource(getMoveBackground("fairy"));
                            moveIcons[i].setImageResource(getMoveIcon("fairy"));
                            break;
                        case "Galvanize":
                            moveViews[i].setBackgroundResource(getMoveBackground("electric"));
                            moveIcons[i].setImageResource(getMoveIcon("electric"));
                            break;
                        case "Refrigerate":
                            moveViews[i].setBackgroundResource(getMoveBackground("ice"));
                            moveIcons[i].setImageResource(getMoveIcon("ice"));
                            break;
                    }
                } else if (ability.equals("Normalize")) {
                    moveViews[i].setBackgroundResource(getMoveBackground("normal"));
                    moveIcons[i].setImageResource(getMoveIcon("normal"));
                } else {
                    moveViews[i].setBackgroundResource(getMoveBackground(type));
                }

                if (moveJson.optBoolean("disabled", false)) {
                    moveViews[i].setOnClickListener(null);
                    moveViews[i].setBackgroundResource(R.drawable.uneditable_frame);
                }
            }
        } catch (final JSONException e) {
            new RunWithNet() {
                @Override
                public void runWithNet() throws Exception {
                    throw e;
                }
            }.run();
        }
    }

    private int getMoveIcon(String type) {
        int res = 0;

        switch (type.toLowerCase()) {
            case "bug":
                res = R.drawable.types_bug;
                break;
            case "dark":
                res = R.drawable.types_dark;
                break;
            case "dragon":
                res = R.drawable.types_dragon;
                break;
            case "electric":
                res = R.drawable.types_electric;
                break;
            case "fairy":
                res = R.drawable.types_fairy;
                break;
            case "fighting":
                res = R.drawable.types_fighting;
                break;
            case "fire":
                res = R.drawable.types_fire;
                break;
            case "flying":
                res = R.drawable.types_flying;
                break;
            case "ghost":
                res = R.drawable.types_ghost;
                break;
            case "grass":
                res = R.drawable.types_grass;
                break;
            case "ground":
                res = R.drawable.types_ground;
                break;
            case "ice":
                res = R.drawable.types_ice;
                break;
            case "normal":
                res = R.drawable.types_normal;
                break;
            case "poison":
                res = R.drawable.types_poison;
                break;
            case "psychic":
                res = R.drawable.types_psychic;
                break;
            case "rock":
                res = R.drawable.types_rock;
                break;
            case "steel":
                res = R.drawable.types_steel;
                break;
            case "water":
                res = R.drawable.types_water;
                break;
        }

        return res;
    }

    private int getMoveBackground(String type) {
        int res = 0;

        switch (type.toLowerCase()) {
            case "bug":
                res = R.drawable.button_attack_bug;
                break;
            case "dark":
                res = R.drawable.button_attack_dark;
                break;
            case "dragon":
                res = R.drawable.button_attack_dragon;
                break;
            case "electric":
                res = R.drawable.button_attack_electric;
                break;
            case "fairy":
                res = R.drawable.button_attack_fairy;
                break;
            case "fighting":
                res = R.drawable.button_attack_fighting;
                break;
            case "fire":
                res = R.drawable.button_attack_fire;
                break;
            case "flying":
                res = R.drawable.button_attack_flying;
                break;
            case "ghost":
                res = R.drawable.button_attack_ghost;
                break;
            case "grass":
                res = R.drawable.button_attack_grass;
                break;
            case "ground":
                res = R.drawable.button_attack_ground;
                break;
            case "ice":
                res = R.drawable.button_attack_ice;
                break;
            case "normal":
                res = R.drawable.button_attack_normal;
                break;
            case "poison":
                res = R.drawable.button_attack_poison;
                break;
            case "psychic":
                res = R.drawable.button_attack_psychic;
                break;
            case "rock":
                res = R.drawable.button_attack_rock;
                break;
            case "steel":
                res = R.drawable.button_attack_steel;
                break;
            case "water":
                res = R.drawable.button_attack_water;
                break;
        }

        return res;
    }

    private void switchPkm() throws JSONException {
        BattleFragment.RECEIVER.processSwitch(mId);
        this.dismiss();
    }
}

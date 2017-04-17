package com.pokemonshowdown.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pokemonshowdown.R;
import com.pokemonshowdown.application.MyApplication;
import com.pokemonshowdown.data.MoveDex;

import org.json.JSONException;
import org.json.JSONObject;

public class MoveInfoDialog extends DialogFragment {
    public final static String PTAG = MoveInfoDialog.class.getName();
    public final static String NAME = "Name";

    private String mName;

    public MoveInfoDialog() {
        // Required empty public constructor
    }

    public static MoveInfoDialog newInstance(String moveName) {
        MoveInfoDialog fragment = new MoveInfoDialog();
        Bundle args = new Bundle();
        args.putSerializable(NAME, moveName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
        mName = getArguments().getString(NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_move_info, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String move = MyApplication.toId(mName);
        if (move.startsWith("hiddenpower") || move.startsWith("return") || move.startsWith("frustration")
                || move.startsWith("gyroball") || move.startsWith("heavyslam")) {
            move = move.toLowerCase().replaceAll("[^a-z]", "");
            //dirty fix to remove numbers from base-power variant attacks
        }
        JSONObject moveJson = MoveDex.get(getContext()).getMoveJsonObject(MyApplication.toId(move));

        try {
            ((TextView) view.findViewById(R.id.move_name)).setText(moveJson.getString("name"));
            ((ImageView) view.findViewById(R.id.type)).setImageResource(getMoveIcon(moveJson.getString("type")));
            ((TextView) view.findViewById(R.id.base_power)).setText(moveJson.getString("basePower"));
            ((TextView) view.findViewById(R.id.accuracy)).setText(moveJson.getString("accuracy").equals("true") ? "-"
                    : moveJson.getString("accuracy") + "%");

            if (moveJson.getString("category").equals("Physical")) {
                ((ImageView) view.findViewById(R.id.category)).setImageResource(R.drawable.category_physical);
            } else if (moveJson.getString("category").equals("Special")) {
                ((ImageView) view.findViewById(R.id.category)).setImageResource(R.drawable.category_special);
            } else {
                ((ImageView) view.findViewById(R.id.category)).setImageResource(R.drawable.category_status);
                ((TextView) view.findViewById(R.id.base_power)).setText("-");
                if (moveJson.getString("accuracy").equals("true")) {
                    ((TextView) view.findViewById(R.id.accuracy)).setText("-");
                } else {
                    ((TextView) view.findViewById(R.id.accuracy)).setText(moveJson.getString("accuracy") + "%");
                }
            }

            TextView priority = (TextView) view.findViewById(R.id.priority);
            if (moveJson.getInt("priority") > 0) {
                priority.setVisibility(View.VISIBLE);
                priority.setText(moveJson.getString("shortDesc") + "(priority " + (moveJson.getInt("priority") > 0 ? "+" : "") + moveJson.getInt("priority") + ")");
            } else {
                priority.setVisibility(View.GONE);
            }

            ((TextView) view.findViewById(R.id.description)).setText(moveJson.getString("desc"));
        } catch (JSONException ex) {
            ex.printStackTrace();
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
}

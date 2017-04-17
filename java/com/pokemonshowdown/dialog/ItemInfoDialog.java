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
import com.pokemonshowdown.data.ItemDex;

public class ItemInfoDialog extends DialogFragment {
    public final static String PTAG = ItemInfoDialog.class.getName();
    public final static String NAME = "Name";
    public final static String DESC = "Description";

    private String mName;
    private String mDescription;

    public ItemInfoDialog() {
        // Required empty public constructor
    }

    public static ItemInfoDialog newInstance(String itemName, String itemDescription) {
        ItemInfoDialog fragment = new ItemInfoDialog();
        Bundle args = new Bundle();
        args.putSerializable(NAME, itemName);
        args.putSerializable(DESC, itemDescription);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
        mName = getArguments().getString(NAME);
        mDescription = getArguments().getString(DESC);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_item_info, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((ImageView) view.findViewById(R.id.icon)).setImageResource(ItemDex.getItemIcon(getContext(), mName));
        ((TextView) view.findViewById(R.id.item_name)).setText(mName);
        ((TextView) view.findViewById(R.id.description)).setText(mDescription);
    }
}

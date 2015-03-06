package com.pokemonshowdown.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.pokemonshowdown.data.MyApplication;
import com.pokemonshowdown.data.Onboarding;

public class UserDialog extends DialogFragment {
    public static final String UTAG = UserDialog.class.getName();

    public UserDialog() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_user, container);

        final Onboarding onboarding = Onboarding.get(getActivity().getApplicationContext());

        getDialog().setTitle(onboarding.getUsername());
        String avatarId = onboarding.getAvatar();
        int avatar = getActivity().getApplicationContext()
                .getResources().getIdentifier("avatar_" + avatarId, "drawable", getActivity().getApplicationContext().getPackageName());
        ((ImageView) view.findViewById(R.id.avatar)).setImageResource(avatar);

        view.findViewById(R.id.sign_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                onboarding.signingOut();
                MyApplication.getMyApplication().sendClientMessage("|/logout");
            }
        });

        return view;
    }
}
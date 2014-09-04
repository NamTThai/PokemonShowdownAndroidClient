package com.pokemonshowdown.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

public class SignInDialog extends DialogFragment {
    public static final String STAG = SignInDialog.class.getName();

    public SignInDialog() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_signin, container);

        final EditText password = (EditText) view.findViewById(R.id.password);

        TextView onboarding = (TextView) view.findViewById(R.id.onboarding);
        onboarding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        view.findViewById(R.id.new_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                OnboardingDialog fragment = new OnboardingDialog();
                fragment.show(fm, OnboardingDialog.OTAG);
            }
        });

        return view;
    }
}
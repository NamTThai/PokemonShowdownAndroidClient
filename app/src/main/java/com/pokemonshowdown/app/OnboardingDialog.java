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

import com.pokemonshowdown.data.MyApplication;
import com.pokemonshowdown.data.Onboarding;

public class OnboardingDialog extends DialogFragment {
    public static final String OTAG = OnboardingDialog.class.getName();

    public OnboardingDialog() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_onboarding, container);

        final EditText username = (EditText) view.findViewById(R.id.username);

        TextView onboarding = (TextView) view.findViewById(R.id.onboarding);
        onboarding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = username.getText().toString();
                if (name.equals("")) {
                    getDialog().dismiss();
                } else {
                    Onboarding onboarding = Onboarding.getWithApplicationContext(getActivity().getApplicationContext());
                    String assertion = onboarding.verifyUsernameRegistered(name);
                    if (assertion.equals(";")) {
                        getDialog().dismiss();
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        SignInDialog fragment = new SignInDialog();
                        Bundle bundle = new Bundle();
                        bundle.putString("username", name);
                        fragment.setArguments(bundle);
                        fragment.show(fm, SignInDialog.STAG);
                    } else {
                        MyApplication.getMyApplication().processGlobalMessage("|assertion|"+name+"|"+assertion);
                        getDialog().dismiss();
                    }
                }
            }
        });

        return view;
    }
}
package com.pokemonshowdown.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.pokemonshowdown.data.MyApplication;
import com.pokemonshowdown.data.Onboarding;

import org.json.JSONException;
import org.json.JSONObject;

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

        if (onboarding.isSignedIn() && !onboarding.isAccountRegistered()) {
            view.findViewById(R.id.register_account).setVisibility(View.VISIBLE);
            view.findViewById(R.id.register_account).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle("Account password");

                    final EditText input = new EditText(getActivity());
                    alert.setView(input);

                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String password = input.getText().toString();

                            String jsonString = onboarding.registerUser(password);
                            try {
                                JSONObject object = new JSONObject(jsonString);
                                if (object.has("assertion")) {
                                    // user successfully registered
                                    String assertion = object.getString("assertion");
                                    MyApplication.getMyApplication().processGlobalMessage("|assertion|" + onboarding.getUsername() + "|" + assertion);
                                    getDialog().dismiss();
                                } else if (object.has("actionerror")) {
                                    // error
                                    String errorString = object.getString("actionerror");
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setTitle(R.string.error_dialog_title);
                                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                                    builder.setMessage(errorString);
                                    final AlertDialog alert = builder.create();
                                    getActivity().runOnUiThread(new java.lang.Runnable() {
                                        public void run() {
                                            getDialog().dismiss();
                                            alert.show();
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                // todo error handling
                                getDialog().dismiss();
                            }

                        }
                    });

                    alert.show();
                }
            });
        } else {
            view.findViewById(R.id.register_account).setVisibility(View.GONE);
        }

        return view;
    }
}
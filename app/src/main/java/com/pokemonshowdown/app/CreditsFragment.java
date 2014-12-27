package com.pokemonshowdown.app;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pokemonshowdown.data.MyApplication;

public class CreditsFragment extends Fragment {
    public final static String CTAG = CreditsFragment.class.getName();
    public final static Integer[] ANDROID_AVATAR = {
            R.drawable.avatar_001,
            R.drawable.avatar_002
    };
    public final static String[][] ANDROID_CONTRIBUTOR = {
            {"RainFountain", "Nam Thai", "PS Android Creator"},
            {"TeTToN", "Clement", "Lead Contributor"}
    };
    public final static Integer[] PS_AVATAR = {
            R.drawable.avatar_003
    };
    public final static String[][] PS_CONTRIBUTOR = {
            {"Zarel", "Guangcong Luo", "Showdown Creator"}
    };

    public static CreditsFragment newInstance() {
        return new CreditsFragment();
    }

    public CreditsFragment() {

    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	} 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_credits, parent, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setFocusableInTouchMode(true);
        String numUsers = Integer.toString(MyApplication.getMyApplication().getUserCount());
        Spannable usersOnline = new SpannableString(numUsers + "\n users online");
        usersOnline.setSpan(new RelativeSizeSpan(2f), 0, numUsers.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        usersOnline.setSpan(new StyleSpan(Typeface.BOLD), 0, numUsers.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        usersOnline.setSpan(new RelativeSizeSpan(1.2f), numUsers.length(), usersOnline.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        usersOnline.setSpan(new StyleSpan(Typeface.ITALIC), numUsers.length(), usersOnline.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ((TextView) view.findViewById(R.id.users_online)).setText(usersOnline);
        String numBattles = Integer.toString(MyApplication.getMyApplication().getBattleCount());
        Spannable activeBattles = new SpannableString(numBattles + "\n active battles");
        activeBattles.setSpan(new RelativeSizeSpan(2f), 0, numBattles.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        activeBattles.setSpan(new StyleSpan(Typeface.BOLD), 0, numBattles.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        activeBattles.setSpan(new RelativeSizeSpan(1.2f), numBattles.length(), activeBattles.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        activeBattles.setSpan(new StyleSpan(Typeface.ITALIC), numBattles.length(), activeBattles.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ((TextView) view.findViewById(R.id.active_battle)).setText(activeBattles);

        ((ListView) view.findViewById(R.id.android_contributors))
                .setAdapter(new ContributorsArrayAdapter(getActivity(), ANDROID_AVATAR, true));
        ((ListView) view.findViewById(R.id.showdown_contributors))
                .setAdapter(new ContributorsArrayAdapter(getActivity(), PS_AVATAR, false));
    }
    
}

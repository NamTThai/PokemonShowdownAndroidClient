package com.pokemonshowdown.app;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pokemonshowdown.data.MyApplication;
import com.pokemonshowdown.data.Pokemon;
import com.pokemonshowdown.data.PokemonTeam;

import java.util.List;

public class CreditsFragment extends Fragment {
    public final static String CTAG = CreditsFragment.class.getName();

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
    }

    public class ContributorsArrayAdapter extends ArrayAdapter<String> {
        private Context mContext;
        private List<String> mContributorList;

        public ContributorsArrayAdapter(Context getContext, int parent, List<String> contributorList) {
            super(getContext, parent, R.id.team_nickname, contributorList);
            this.mContext = getContext;
            mContributorList = null; //userListData;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listwidget_teampreview, null);
            }
            PokemonTeam p = null; // mPokemonTeamList.get(position);

            TextView teamName = (TextView) convertView.findViewById(R.id.team_nickname);
            if (p.getTier().isEmpty()) {
                teamName.setText(p.getNickname());
            } else {
                teamName.setText(p.getNickname() + " (" + p.getTier() + ")");
            }

            LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.pokemon_small_icon_list);
            layout.removeAllViews();

            for (Pokemon pokemon : p.getPokemons()) {
                if (pokemon != null) {
                    ImageView image = new ImageView(getContext());
                    int smallIconId = pokemon.getIcon();
                    Drawable d = mContext.getResources().getDrawable(smallIconId);
                    image.setImageDrawable(d);
                    layout.addView(image);
                }
            }

            for (int i = 0; i < 6 - p.getPokemons().size(); i++) {
                ImageView image = new ImageView(getContext());
                Drawable d = mContext.getResources().getDrawable(R.drawable.smallicons_0);
                image.setImageDrawable(d);
                layout.addView(image);
            }
            return convertView;
        }
    }
}

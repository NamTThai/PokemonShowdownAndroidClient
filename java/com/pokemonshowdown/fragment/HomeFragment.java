package com.pokemonshowdown.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pokemonshowdown.R;
import com.pokemonshowdown.application.MyApplication;
import com.pokemonshowdown.data.Onboarding;

/**
 * Created by McBeengs on 22/10/2016.
 */

public class HomeFragment extends BaseFragment {

    public static final UsernameLogged USERNAME_LOGGED = new UsernameLogged();
    private static TextView loggedDisplay;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_screen, container, false);
    }

    @Override
    public void onViewCreated(final View mView, @Nullable Bundle savedInstanceState) {
        loggedDisplay = (TextView) mView.findViewById(R.id.logged_display);
        Onboarding onboarding = Onboarding.get(mView.getContext());
        if (onboarding.getUsername() != null && !onboarding.getUsername().isEmpty()) {
            loggedDisplay.setText("Logged as \"" + onboarding.getUsername() + "\"");
        }

//        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) mView.findViewById(R.id.main_screen_refresh);
//        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                //refresh users/battles counter and re-check internet connection
//                refreshLayout.setRefreshing(false);
//            }
//        });

        new Thread() {
            @Override
            public void run() {
                while (MyApplication.getMyApplication().getBattleCount() <= 0) {
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                final TextView battlesDisplay = (TextView) mView.findViewById(R.id.battles_display);
                final TextView usersDisplay = (TextView) mView.findViewById(R.id.users_display);

                battlesDisplay.post(new Runnable() {
                    @Override
                    public void run() {
                        battlesDisplay.setText(Integer.toString(MyApplication.getMyApplication().getBattleCount()));
                    }
                });

                usersDisplay.post(new Runnable() {
                    @Override
                    public void run() {
                        usersDisplay.setText(Integer.toString(MyApplication.getMyApplication().getUserCount()));
                    }
                });
            }
        }.start();

        final CardView news1 = (CardView) mView.findViewById(R.id.news_1);
        final CardView news2 = (CardView) mView.findViewById(R.id.news_2);
        final TextView hideNews = (TextView) mView.findViewById(R.id.hide_show_news);
        hideNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (news1.getVisibility() == View.VISIBLE) {
                    news1.setVisibility(View.GONE);
                    news2.setVisibility(View.GONE);
                    hideNews.setText("+");
                    news1.invalidate();
                    news2.invalidate();
                    hideNews.invalidate();
                } else {
                    news1.setVisibility(View.VISIBLE);
                    news2.setVisibility(View.VISIBLE);
                    hideNews.setText("-");
                    news1.invalidate();
                    news2.invalidate();
                    hideNews.invalidate();
                }
            }
        });
    }

    public static class UsernameLogged {

        public void setUsername(String name) {
            loggedDisplay.setText(Html.fromHtml("Logged as \"<b style='color: red'>" + name + "</b>\""));
            loggedDisplay.invalidate();
        }
    }
}

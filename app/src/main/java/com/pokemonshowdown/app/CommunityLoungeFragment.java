package com.pokemonshowdown.app;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


public class CommunityLoungeFragment extends android.support.v4.app.Fragment {
    private CommunityLoungePagerAdapter mCommunityLoungePagerAdapter;
    private ViewPager mViewPager;

    private static final String ROOM_LIST = "Room List";

    private ArrayList<String> mRoomList;

    public static CommunityLoungeFragment newInstance(ArrayList<String> roomList) {
        CommunityLoungeFragment fragment = new CommunityLoungeFragment();
        Bundle args = new Bundle();
        args.putSerializable(ROOM_LIST, roomList);
        fragment.setArguments(args);
        return fragment;
    }
    public CommunityLoungeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments().getSerializable("Room List") != null) {
            mRoomList = (ArrayList<String>) getArguments().getSerializable(ROOM_LIST);
        } else {
            mRoomList = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_community_lounge, container, false);
        v.setFocusableInTouchMode(true);
        mCommunityLoungePagerAdapter = new CommunityLoungePagerAdapter(getActivity().getSupportFragmentManager());
        mViewPager = (ViewPager) v.findViewById(R.id.community_pager);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        getActivity().getActionBar().setSelectedNavigationItem(position);
                    }
                });
        mViewPager.setAdapter(mCommunityLoungePagerAdapter);
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().removeAllTabs();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem addRoom = menu.add(R.string.bar_chatroom);
        addRoom.setIcon(R.drawable.ic_action_group);
        addRoom.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        addRoom.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return false;
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ActionBar actionBar = getActivity().getActionBar();

        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

            }
        };

        for(String room : mRoomList) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(room)
                            .setTabListener(tabListener)
            );
        }
    }

    public void processServerMessage(String roomId, String message) {
        if (mRoomList.indexOf(roomId) == mViewPager.getCurrentItem()) {
            
        }
    }

    private class CommunityLoungePagerAdapter extends FragmentStatePagerAdapter {
        public CommunityLoungePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return ChatRoomFragment.newInstance(mRoomList.get(i));
        }

        @Override
        public int getCount() {
            return mRoomList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mRoomList.get(position);
        }
    }

}

package com.pokemonshowdown.app;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.pokemonshowdown.data.CommunityLoungeData;
import com.pokemonshowdown.data.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class CommunityLoungeFragment extends android.support.v4.app.Fragment {
    public final static String CTAG = CommunityLoungeFragment.class.getName();
    private CommunityLoungePagerAdapter mCommunityLoungePagerAdapter;
    private ViewPager mViewPager;

    private ArrayList<String> mRoomList;

    public static CommunityLoungeFragment newInstance() {
        CommunityLoungeFragment fragment = new CommunityLoungeFragment();
        return fragment;
    }
    public CommunityLoungeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mRoomList = CommunityLoungeData.getWithApplicationContext(getActivity().getApplicationContext()).getRoomList();
        if (mRoomList.size() == 0) {
            CommunityLoungeData.getWithApplicationContext(getActivity().getApplicationContext()).joinRoom("lobby");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_community_lounge, container, false);
        v.setFocusableInTouchMode(true);
        mCommunityLoungePagerAdapter = new CommunityLoungePagerAdapter(getChildFragmentManager());
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
    public void onDetach() {
        super.onDetach();
        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().removeAllTabs();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem addRoom = menu.findItem(R.id.community_lounge);
        addRoom.setVisible(true);
        addRoom.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                generateRoomCategoryList();
                return true;
            }
        });
        MenuItem cancel = menu.findItem(R.id.cancel);
        cancel.setVisible(true);
        cancel.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                removeCurrentRoom();
                return true;
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
        int index = mRoomList.indexOf(roomId);
        ChatRoomFragment fragment = (ChatRoomFragment) getChildFragmentManager().findFragmentByTag("android:switcher:" + mViewPager.getId() + ":" + index);
        if (fragment != null) {
            fragment.processServerMessage(message);
        }
    }

    private void generateRoomCategoryList() {
        final HashMap<String, JSONArray> rooms = MyApplication.getMyApplication().getRoomCategoryList();
        Set<String> roomSet = rooms.keySet();
        final String[] roomCategoryNames = roomSet.toArray(new String[roomSet.size()]);
        final String[] roomCategories = new String[roomSet.size()];
        int count = 0;
        for (String room : roomSet) {
            roomCategories[count] = room.toUpperCase();
            count++;
        }
        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setItems(roomCategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String roomCategory = roomCategoryNames[which];
                        generateRoomList(rooms.get(roomCategory));
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void generateRoomList(JSONArray rooms) {
        try {
            String[] roomList = new String[rooms.length()];
            for (int i = 0; i < rooms.length(); i++) {
                JSONObject room = rooms.getJSONObject(i);
                roomList[i] = room.getString("title");
            }
            final String[] finalRoomList = roomList;

            Dialog dialog = new AlertDialog.Builder(getActivity())
                    .setItems(roomList, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String room = finalRoomList[which];
                            processNewRoomRequest(room);
                            dialog.dismiss();
                        }
                    })
                    .show();
        } catch (JSONException e) {
            Log.d(CTAG, e.toString());
        }
    }

    private void removeCurrentRoom() {
        ActionBar actionBar = getActivity().getActionBar();
        ActionBar.Tab tab = actionBar.getSelectedTab();
        String roomId = mRoomList.get(tab.getPosition());
        if (roomId.equals("lobby")) {
            return;
        }
        CommunityLoungeData.get(getActivity()).leaveRoom(roomId);
        mCommunityLoungePagerAdapter.notifyDataSetChanged();
        mViewPager.setAdapter(mCommunityLoungePagerAdapter);
        actionBar.removeTab(tab);
    }

    private void processNewRoomRequest(String room) {
        String roomId = MyApplication.toId(room);
        ActionBar actionBar = getActivity().getActionBar();
        if (mRoomList.contains(roomId)) {
            actionBar.setSelectedNavigationItem(mRoomList.indexOf(roomId));
        } else {
            CommunityLoungeData.getWithApplicationContext(getActivity().getApplicationContext()).joinRoom(roomId);
            mCommunityLoungePagerAdapter.notifyDataSetChanged();
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

            actionBar.addTab(
                    actionBar.newTab()
                            .setText(roomId)
                            .setTabListener(tabListener)
            );
            actionBar.setSelectedNavigationItem(mRoomList.indexOf(roomId));
        }
    }

    private class CommunityLoungePagerAdapter extends FragmentPagerAdapter {
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

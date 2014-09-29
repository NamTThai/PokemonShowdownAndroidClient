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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.pokemonshowdown.data.BattleFieldData;
import com.pokemonshowdown.data.CommunityLoungeData;
import com.pokemonshowdown.data.MyApplication;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class BattleFieldFragment extends Fragment {
    public final static String BTAG = BattleFieldFragment.class.getName();
    private BattleFieldPagerAdapter mBattleFieldPagerAdapter;
    private ViewPager mViewPager;

    private ArrayList<String> mRoomList;
    private int mPosition;

    public static BattleFieldFragment newInstance() {
        BattleFieldFragment fragment = new BattleFieldFragment();
        return fragment;
    }
    public BattleFieldFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mRoomList = BattleFieldData.getWithApplicationContext(getActivity().getApplicationContext()).getRoomList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_battle_field, container, false);
        mPosition = 0;

        v.setFocusableInTouchMode(true);
        mBattleFieldPagerAdapter = new BattleFieldPagerAdapter(getChildFragmentManager());
        mViewPager = (ViewPager) v.findViewById(R.id.battle_field_pager);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        getActivity().getActionBar().setSelectedNavigationItem(position);
                    }
                });
        mViewPager.setAdapter(mBattleFieldPagerAdapter);
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

        setAvailableFormat();

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

    public void setAvailableFormat() {
        if (mPosition == 0) {
            FindBattleFragment fragment = (FindBattleFragment) getChildFragmentManager().findFragmentByTag("android:switcher:" + mViewPager.getId() + ":" + 0);
            if (fragment != null) {
                fragment.setAvailableFormat();
            }
        }
    }

    public void processServerMessage(String roomId, String message) {
        int index = mRoomList.indexOf(roomId);
        ChatRoomFragment fragment = (ChatRoomFragment) getChildFragmentManager().findFragmentByTag("android:switcher:" + mViewPager.getId() + ":" + index);
        if (fragment != null) {
            fragment.processServerMessage(message);
        }
    }

    private void generateWatchBattleList() {
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
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void removeCurrentRoom() {
        ActionBar actionBar = getActivity().getActionBar();
        ActionBar.Tab tab = actionBar.getSelectedTab();
        String roomId = mRoomList.get(tab.getPosition());
        if (roomId.equals("global")) {
            return;
        }
        BattleFieldData.get(getActivity()).leaveRoom(roomId);
        mBattleFieldPagerAdapter.notifyDataSetChanged();
        mViewPager.setAdapter(mBattleFieldPagerAdapter);
        actionBar.removeTab(tab);
    }

    private void processNewRoomRequest(String room) {
        /*
        String roomId = MyApplication.getMyApplication().toId(room);
        ActionBar actionBar = getActivity().getActionBar();
        if (mRoomList.contains(roomId)) {
            actionBar.setSelectedNavigationItem(mRoomList.indexOf(roomId));
        } else {
            CommunityLoungeData.getWithApplicationContext(getActivity().getApplicationContext()).joinRoom(roomId);
            mBattleFieldPagerAdapter.notifyDataSetChanged();
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
        }*/
    }

    private class BattleFieldPagerAdapter extends FragmentPagerAdapter {
        public BattleFieldPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                return FindBattleFragment.newInstance();
            }
            ArrayList<Integer> roomType = BattleFieldData.get(getActivity()).getRoomType();
            if (roomType.get(i) == 0) {
                return FindBattleFragment.newInstance();
            } else {
                return FindBattleFragment.newInstance();
            }
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

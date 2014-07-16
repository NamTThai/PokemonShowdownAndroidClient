package com.pokemonshowdown.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class PokedexFragment extends Fragment {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		getActivity().getActionBar().setTitle(R.string.bar_pokedex);
	} 
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.fragment_pokedex, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){
		case android.R.id.home:
			if (NavUtils.getParentActivityName(getActivity()) != null)
				NavUtils.navigateUpFromSameTask(getActivity());
			return true;
		case R.id.menu_pokedex_search:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.activity_pokedex, parent, false);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		return v;
	}

}

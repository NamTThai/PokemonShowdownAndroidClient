package com.pokemonshowdown.app;

import android.support.v4.app.Fragment;

public class PokedexActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		// TODO Auto-generated method stub
		return new PokedexFragment();
	}

}

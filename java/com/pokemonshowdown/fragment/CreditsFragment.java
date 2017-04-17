package com.pokemonshowdown.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.pokemonshowdown.R;
import com.pokemonshowdown.application.MyApplication;

/**
 * Created by nunom on 13/02/2017.
 */

public class CreditsFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_credits, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SimpleDraweeView rainFountain = (SimpleDraweeView) view.findViewById(R.id.rainfountain);
        Uri uri = new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                .path(String.valueOf(getResources().getIdentifier("coder_rainfountain", "drawable", MyApplication.getMyApplication().getPackageName())))
                .build();
        rainFountain.setImageURI(uri);

        SimpleDraweeView teton = (SimpleDraweeView) view.findViewById(R.id.teton);
        uri = new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                .path(String.valueOf(getResources().getIdentifier("coder_teton", "drawable", MyApplication.getMyApplication().getPackageName())))
                .build();
        teton.setImageURI(uri);

        SimpleDraweeView mcbeengs = (SimpleDraweeView) view.findViewById(R.id.mcbeengs);
        uri = new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                .path(String.valueOf(getResources().getIdentifier("coder_mcbeengs", "drawable", MyApplication.getMyApplication().getPackageName())))
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build();
        mcbeengs.setController(controller);

        SimpleDraweeView lazloz = (SimpleDraweeView) view.findViewById(R.id.lazloz);
        uri = new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                .path(String.valueOf(getResources().getIdentifier("coder_lazloz", "drawable", MyApplication.getMyApplication().getPackageName())))
                .build();
        lazloz.setImageURI(uri);

        SimpleDraweeView zarel = (SimpleDraweeView) view.findViewById(R.id.zarel);
        uri = new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                .path(String.valueOf(getResources().getIdentifier("coder_zarel", "drawable", MyApplication.getMyApplication().getPackageName())))
                .build();
        zarel.setImageURI(uri);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }
}

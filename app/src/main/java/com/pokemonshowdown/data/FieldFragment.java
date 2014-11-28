package com.pokemonshowdown.data;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.pokemonshowdown.app.DmgCalcActivity;

/**
 * Created by lazkz on 27.11.2014.
 */
public class FieldFragment extends Fragment {

    private DmgCalcActivity.FieldConditionsListener mListener;

    public void setFieldConditionsListener(@Nullable DmgCalcActivity.FieldConditionsListener listener) {
        mListener = listener;
    }

    protected void sendUpdateToListeners(DmgCalcActivity.FieldConditions fieldCondition, boolean value) {
        if(mListener != null)  {
            mListener.onFieldConditionChanged(fieldCondition, value);
        }
    }
}

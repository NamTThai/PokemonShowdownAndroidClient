package com.pokemonshowdown.data;

import com.pokemonshowdown.app.BattleFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ServerRequest {
    private int rqId;
    private ArrayList<ActiveMoveInfo> movesToDo;
    private ArrayList<Boolean> forceSwitch;
    private ArrayList<PokemonInfo> pkmTeam;

    public ServerRequest(BattleFragment battleFragment, JSONObject request) throws JSONException {
        rqId = request.getInt("rqid");
        JSONObject side = request.getJSONObject("side");
        JSONArray team = side.getJSONArray("pokemon");
        for (int i = 0; i < team.length(); i++) {
            JSONObject info = team.getJSONObject(i);
            PokemonInfo pkm = BattleMessage.parsePokemonInfo(battleFragment, info);
            pkmTeam.add(pkm);
        }

        JSONArray active = side.getJSONArray("active");
        for (int i = 0; i < active.length(); i++) {
            ActiveMoveInfo info = new ActiveMoveInfo(active.getJSONObject(i));
            movesToDo.add(info);
        }

        JSONArray forceSwitchArray = side.getJSONArray("forceSwitch");
        for (int i = 0; i < forceSwitchArray.length(); i++) {
            forceSwitch.add(forceSwitchArray.getBoolean(i));
        }
    }

    private class ActiveMoveInfo {
        private ArrayList<MoveInfo> availableMoves;

        public ActiveMoveInfo(JSONObject activeArrayElement) throws JSONException {
            JSONArray moves = activeArrayElement.getJSONArray("moves");
            for (int i = 0; i < moves.length(); i++) {
                MoveInfo moveInfo = new MoveInfo(moves.getJSONObject(i));
                availableMoves.add(moveInfo);
            }
        }

    }

    private class MoveInfo {
        private String target;
        private boolean isDisabled;
        private int pp;
        private int maxPp;
        private String moveName;
        private String moveId;

        public MoveInfo(JSONObject moveElement) throws JSONException {
            setTarget(moveElement.getString("target"));
            setDisabled(moveElement.getBoolean("disabled"));
            setPp(moveElement.getInt("pp"));
            setMaxPp(moveElement.getInt("maxpp"));
            setMoveName(moveElement.getString("move"));
            setMoveId(moveElement.getString("id"));
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public boolean isDisabled() {
            return isDisabled;
        }

        public void setDisabled(boolean isDisabled) {
            this.isDisabled = isDisabled;
        }

        public int getPp() {
            return pp;
        }

        public void setPp(int pp) {
            this.pp = pp;
        }

        public int getMaxPp() {
            return maxPp;
        }

        public void setMaxPp(int maxPp) {
            this.maxPp = maxPp;
        }

        public String getMoveName() {
            return moveName;
        }

        public void setMoveName(String moveName) {
            this.moveName = moveName;
        }

        public String getMoveId() {
            return moveId;
        }

        public void setMoveId(String moveId) {
            this.moveId = moveId;
        }
    }


}

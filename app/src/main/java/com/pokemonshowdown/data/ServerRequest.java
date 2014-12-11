package com.pokemonshowdown.data;

import com.pokemonshowdown.app.BattleFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class ServerRequest implements Serializable {
    private int rqId;
    private ArrayList<ActiveMoveInfo> movesToDo = new ArrayList<>();
    private ArrayList<Boolean> forceSwitch = new ArrayList<>();
    private ArrayList<PokemonInfo> pkmTeam = new ArrayList<>();

    public ServerRequest(BattleFragment battleFragment, JSONObject request) throws JSONException {
        setRqId(request.getInt("rqid"));
        JSONObject side = request.getJSONObject("side");
        JSONArray team = side.getJSONArray("pokemon");
        for (int i = 0; i < team.length(); i++) {
            JSONObject info = team.getJSONObject(i);
            PokemonInfo pkm = BattleMessage.parsePokemonInfo(battleFragment, info);
            getPkmTeam().add(pkm);
        }

        JSONArray active = request.getJSONArray("active");
        for (int i = 0; i < active.length(); i++) {
            ActiveMoveInfo info = new ActiveMoveInfo(active.getJSONObject(i));
            getMovesToDo().add(info);
        }
        if (request.has("forceSwitch")) {
            JSONArray forceSwitchArray = request.getJSONArray("forceSwitch");
            for (int i = 0; i < forceSwitchArray.length(); i++) {
                getForceSwitch().add(forceSwitchArray.getBoolean(i));
            }
        }
    }

    public int getRqId() {
        return rqId;
    }

    public void setRqId(int rqId) {
        this.rqId = rqId;
    }

    public ArrayList<ActiveMoveInfo> getMovesToDo() {
        return movesToDo;
    }

    public void setMovesToDo(ArrayList<ActiveMoveInfo> movesToDo) {
        this.movesToDo = movesToDo;
    }

    public ArrayList<Boolean> getForceSwitch() {
        return forceSwitch;
    }

    public void setForceSwitch(ArrayList<Boolean> forceSwitch) {
        this.forceSwitch = forceSwitch;
    }

    public ArrayList<PokemonInfo> getPkmTeam() {
        return pkmTeam;
    }

    public void setPkmTeam(ArrayList<PokemonInfo> pkmTeam) {
        this.pkmTeam = pkmTeam;
    }

    public class ActiveMoveInfo implements Serializable {
        private ArrayList<MoveInfo> availableMoves = new ArrayList<>();

        public ActiveMoveInfo(JSONObject activeArrayElement) throws JSONException {
            JSONArray moves = activeArrayElement.getJSONArray("moves");
            for (int i = 0; i < moves.length(); i++) {
                MoveInfo moveInfo = new MoveInfo(moves.getJSONObject(i));
                getAvailableMoves().add(moveInfo);
            }
        }

        public ArrayList<MoveInfo> getAvailableMoves() {
            return availableMoves;
        }

        public void setAvailableMoves(ArrayList<MoveInfo> availableMoves) {
            this.availableMoves = availableMoves;
        }
    }

    public class MoveInfo implements Serializable {
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

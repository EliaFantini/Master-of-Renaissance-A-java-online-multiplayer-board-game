package it.polimi.ingsw.client;

import it.polimi.ingsw.enumerations.Resource;

import java.util.*;

public class LightClient {

    private int faithTrackPosition;
    private Map<Integer, Boolean> ownedLeaderCards;
    //Slot number is the key, the id is the value...if the slot number is not present, it is empty
    private int[] ownedDevelopmentCards;
    private int[] victoryPointsDevelopmentCardSlots;
    private String nickname;
    public static final int EMPTY_SLOT = -1;

    List<Resource>[] warehouse;
    int[] strongbox;
    Map<Integer, Integer> leaderDepots;

    public LightClient() {
        this.faithTrackPosition = 0;
        this.ownedLeaderCards = new HashMap<>();
        this.warehouse = new ArrayList[3];
        for (int i = 0; i < 3; i++) {
            warehouse[i] = new ArrayList<>();
        }
        this.strongbox = new int[4];
        this.ownedDevelopmentCards = new int[]{EMPTY_SLOT, EMPTY_SLOT, EMPTY_SLOT};
        this.victoryPointsDevelopmentCardSlots = new int[3];
    }

    public void addLeaderCard(Integer ID, boolean active) {
        ownedLeaderCards.put(ID, active);
    }

    //TODO: check that uses ID as Integer and not as int
    public void removeLeaderCard(Integer ID) {
        ownedLeaderCards.remove(ID);
    }

    public void faithTrackAdvancement(int steps) {
        faithTrackPosition += steps;
    }

    public int getFaithTrackPosition() {
        return faithTrackPosition;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void addDevelopmentCard(int id, int slot, int victoryPoints){
        ownedDevelopmentCards[slot] = id;
        victoryPointsDevelopmentCardSlots[slot] += victoryPoints;
    }

    public void updateDepotStatus(List<Resource>[] warehouseDepots, int[] strongboxDepots, Map<Integer, Integer> leaderDepots) {
        this.warehouse = warehouseDepots;
        this.strongbox = strongboxDepots;
        this.leaderDepots = leaderDepots;
    }

    public void updateMarkerPosition(int position){
        faithTrackPosition = position;
    }

    public void activateLeader(int id){
        if (ownedLeaderCards.containsKey(id))
            ownedLeaderCards.replace(id, true);
        else
            ownedLeaderCards.put(id, true);
    }

    public void updateOwnedDevelopmentCards(Map<Integer, Integer> ids, Map<Integer, Integer> victoryPoints){

    }
}

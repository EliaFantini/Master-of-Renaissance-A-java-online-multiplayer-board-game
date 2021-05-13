package it.polimi.ingsw.controller.actions;

import it.polimi.ingsw.server.ClientHandler;
import it.polimi.ingsw.server.Server;
import it.polimi.ingsw.controller.TurnController;
import it.polimi.ingsw.enumerations.EffectType;
import it.polimi.ingsw.enumerations.Resource;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.messages.toClient.ChooseProductionPowersRequest;
import it.polimi.ingsw.messages.toServer.ChooseProductionPowersResponse;
import it.polimi.ingsw.messages.toServer.MessageToServer;
import it.polimi.ingsw.messages.toServer.SelectStorageResponse;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.player.PersonalBoard;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.utility.RemoveResources;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ActivateProductionAction implements Action{

    private Player player;
    private PersonalBoard personalBoard;
    private Map<Resource, Integer> availableResources;
    private List<LeaderCard> availableProductionLeaderCards;
    private List<DevelopmentCard> availableDevelopmentCards;
    private ClientHandler clientHandler;
    private final int BASIC_PRODUCTION_POWER = 0;
    private TurnController turnController;

    public ActivateProductionAction(TurnController turnController){
        this.turnController=turnController;
        this.player = turnController.getCurrentPlayer();
        this.clientHandler = turnController.getController().getConnectionByNickname(player.getNickname());
        this.personalBoard = this.player.getPersonalBoard();
        //TODO: manage this exceptions
        this.availableResources = this.personalBoard.countResources();
        this.availableProductionLeaderCards = this.personalBoard.availableLeaderCards();
        this.availableDevelopmentCards = this.personalBoard.availableDevelopmentCards();
        this.availableProductionLeaderCards = this.availableProductionLeaderCards.stream().filter(
                lc -> lc.getEffect().getEffectType() == EffectType.PRODUCTION).collect(Collectors.toList());
    }
    @Override
    public void reset(Player currentPlayer) {
        this.player = currentPlayer;
        this.clientHandler = turnController.getController().getConnectionByNickname(currentPlayer.getNickname());
        this.personalBoard = this.player.getPersonalBoard();
        //TODO: manage this exceptions
        this.availableResources = this.personalBoard.countResources();
        this.availableProductionLeaderCards = this.personalBoard.availableLeaderCards();
        this.availableDevelopmentCards = this.personalBoard.availableDevelopmentCards();
        this.availableProductionLeaderCards = this.availableProductionLeaderCards.stream().filter(
                lc -> lc.getEffect().getEffectType() == EffectType.PRODUCTION).collect(Collectors.toList());
    }
    @Override
    public boolean isExecutable() {

        //first check on the basic production power
        if(availableResources.values().stream().mapToInt(Integer::intValue).sum() >= 2){
            return true;
        }

        Map<Resource, Integer> activationCost = null;
        DevelopmentCard developmentCard = null;
        Iterator<DevelopmentCard> developmentCardIterator = availableDevelopmentCards.iterator();

        while (developmentCardIterator.hasNext()){
            try {
                developmentCard = developmentCardIterator.next();
                activationCost = developmentCard.getProduction().getProductionPower().get(0).getResourceValue();
            } catch (ValueNotPresentException e) {
                e.printStackTrace();
                //TODO: manage better
                Server.SERVER_LOGGER.log(Level.WARNING, "The Development card " + developmentCard.toString() + "" +
                        "has no Production Effect. Removing from the list of cards present in the action");
               developmentCardIterator.remove();
            }

            if (hasResourcesForThisProduction(activationCost)){
                return true;
            }
        }

        Iterator<LeaderCard> leaderCardIterator = availableProductionLeaderCards.iterator();
        LeaderCard leaderCard = null;
        while(leaderCardIterator.hasNext()){
            try {
                leaderCard = leaderCardIterator.next();
                activationCost = leaderCard.getEffect().getProductionEffect().getProductionPower().get(0).getResourceValue();
            } catch (ValueNotPresentException | DifferentEffectTypeException e) {
                e.printStackTrace();
                //TODO: manage better
                Server.SERVER_LOGGER.log(Level.WARNING, "The Leader card " + leaderCard.toString() + "" +
                        "has no Production Effect. Removing from the list of cards present in the action");
                leaderCardIterator.remove();
            }
            if (hasResourcesForThisProduction(activationCost)){
                return true;
            }
        }
        return false;
    }

    /**
     * Method to check, given the activation Cost of a production power of a {@link Card}, if the player has the minimum
     * amount of resources to activate this production;
     * @param activationCost the {@link Map} with resources and quantity of the activation cost
     * @return true if the player has the minimum amount of resources for this production
     */
    private boolean hasResourcesForThisProduction(Map<Resource, Integer> activationCost){
        boolean hasResource = true;
        for (Map.Entry<Resource, Integer> entry : activationCost.entrySet()){
            try {
                hasResource = hasResource && entry.getValue() <= availableResources.get(entry.getKey());
            }catch(Exception e){
                //do nothing, it's only to easily skip a missing Resource in availableResources
            }
        }
        return hasResource;
    }

    @Override
    public void execute() {
        clientHandler.setCurrentAction(this);

        Iterator<DevelopmentCard> developmentCardIterator = availableDevelopmentCards.iterator();

        Map<Integer, List<Value>> availableProductionPowers = new HashMap<>();
        DevelopmentCard developmentCard;

        //add to availableProductionPowers all the Production powers from Development Cards that can be activated
        while(developmentCardIterator.hasNext()){
            developmentCard = developmentCardIterator.next();
            try {
                if(hasResourcesForThisProduction(developmentCard.getProduction().getProductionPower().get(0).getResourceValue()))
                    availableProductionPowers.put(developmentCard.getID(), developmentCard.getProduction().getProductionPower());
            } catch (ValueNotPresentException e) {
                e.printStackTrace();
                Server.SERVER_LOGGER.log(Level.WARNING, "The Development card " + developmentCard.toString() + "" +
                        "has no Production Effect. Removing from the list of cards present in the action");
                developmentCardIterator.remove();
            }
        }

        Iterator<LeaderCard> leaderCardIterator = availableProductionLeaderCards.iterator();
        LeaderCard leaderCard;

        //add to availableProductionPowers all the Production powers from Leader Cards that can be activated
        while(leaderCardIterator.hasNext()){
            leaderCard = leaderCardIterator.next();
            try {
                if(hasResourcesForThisProduction(leaderCard.getEffect().getProductionEffect().getProductionPower().get(0).getResourceValue()))
                    availableProductionPowers.put(leaderCard.getID(), leaderCard.getEffect().getProductionEffect().getProductionPower());
            } catch (ValueNotPresentException | DifferentEffectTypeException e) {
                e.printStackTrace();
                Server.SERVER_LOGGER.log(Level.WARNING, "The Leader card " + leaderCard.toString() + "" +
                        "has no Production Effect. Removing from the list of cards present in the action");
                leaderCardIterator.remove();
            }
        }

        //Building basic production power
        Production basic_production = buildBasicProductionPower();

        availableProductionPowers.put(BASIC_PRODUCTION_POWER, basic_production.getProductionPower());
        clientHandler.sendMessageToClient(new ChooseProductionPowersRequest(availableProductionPowers, availableResources));

    }

    private Production buildBasicProductionPower() {
        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.ANY, 2);
        Map<Resource, Integer> output = new HashMap<>();
        output.put(Resource.ANY, 1);
        Production basic_production = null;
        try {
            basic_production = new Production(new Value(null, cost, 0), new Value(null, output, 0));
        } catch (InvalidArgumentException e) {
            //e.printStackTrace();
            System.out.println("Exception should not be raised here. Correct the code");
        }
        return basic_production;
    }

    @Override
    public void handleMessage(MessageToServer message) {
        if(message instanceof ChooseProductionPowersResponse){
            List<Integer> productionPowerSelected = ((ChooseProductionPowersResponse) message).getProductionPowersSelected();
            List<Value> basicProductionPower;


            Map<Resource, Integer> resourceToRemove = new HashMap<>();
            Map<Resource, Integer> resourceToAdd = new HashMap<>();
            int faithPoints = 0;

            initializeResourceMaps(resourceToAdd, resourceToRemove);

            List<Value> productionPower;

            if(productionPowerSelected.size() < 1)
                return;

            if(productionPowerSelected.contains(BASIC_PRODUCTION_POWER)){
                basicProductionPower = ((ChooseProductionPowersResponse) message).getBasicProductionPower();
                manageCost(basicProductionPower.get(0), resourceToRemove);
                manageCost(basicProductionPower.get(0), resourceToAdd);
                productionPowerSelected.remove(Integer.valueOf(BASIC_PRODUCTION_POWER));
            }

            for (Integer id : productionPowerSelected){
                productionPower = getProductionByID(id);
                manageCost(productionPower.get(0), resourceToRemove);
                faithPoints += manageCost(productionPower.get(1), resourceToAdd);
            }

            RemoveResources.removeResources(resourceToRemove, clientHandler, player);

            try {
                personalBoard.addResourcesToStrongbox(resourceToAdd);
            } catch (InvalidDepotException e) {
                e.printStackTrace();
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }

            if(faithPoints > 0){
                try {
                    personalBoard.moveMarker(faithPoints);
                    turnController.checkFaithTrack();
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }

            turnController.setStandardActionDoneToTrue();
            turnController.setNextAction();
        }

        if(message instanceof SelectStorageResponse){
            player.getPersonalBoard().isResourceAvailableAndRemove( ((SelectStorageResponse) message).getResourceStorageType(),((SelectStorageResponse) message).getResource(),1,true);
        }

    }

    private void initializeResourceMaps(Map<Resource, Integer> resourceToAdd, Map<Resource, Integer> resourceToRemove) {
        List<Resource> realValues = Resource.realValues();
        for(Resource r : realValues){
            resourceToAdd.put(r, 0);
            resourceToRemove.put(r, 0);
        }
    }

    private int manageCost(Value value, Map<Resource, Integer> resourceToManage) {

        try {
            for(Map.Entry<Resource, Integer> entry : value.getResourceValue().entrySet()){
                resourceToManage.put(entry.getKey(), resourceToManage.get(entry.getKey()) + entry.getValue());
            }
        } catch (ValueNotPresentException e) {
            e.printStackTrace();
        }

        //if the Value has faith points they are returned, else 0 is returned
        try {
            return value.getFaithValue();
        } catch (ValueNotPresentException e) {
            return 0;
        }
    }

    private List<Value> getProductionByID(int id) {
        for (LeaderCard lc : availableProductionLeaderCards){
            if(lc.getID() == id) {
                try {
                    return lc.getEffect().getProductionEffect().getProductionPower();
                } catch (DifferentEffectTypeException e) {
                    e.printStackTrace();
                }
            }
        }
        for (DevelopmentCard dc : availableDevelopmentCards){
            if(dc.getID() == id){
                return dc.getProduction().getProductionPower();
            }
        }
        if(id == 0){
            return buildBasicProductionPower().getProductionPower();
        }
        return null;
    }
}

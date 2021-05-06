package it.polimi.ingsw.controller.actions;

import it.polimi.ingsw.Server.ClientHandler;
import it.polimi.ingsw.Server.Server;
import it.polimi.ingsw.controller.TurnController;
import it.polimi.ingsw.enumerations.EffectType;
import it.polimi.ingsw.enumerations.Resource;
import it.polimi.ingsw.exceptions.DifferentEffectTypeException;
import it.polimi.ingsw.exceptions.InactiveCardException;
import it.polimi.ingsw.exceptions.InvalidArgumentException;
import it.polimi.ingsw.exceptions.ValueNotPresentException;
import it.polimi.ingsw.messages.toClient.ChooseProductionPowersRequest;
import it.polimi.ingsw.messages.toServer.ChooseProductionPowersResponse;
import it.polimi.ingsw.messages.toServer.MessageToServer;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.player.PersonalBoard;
import it.polimi.ingsw.model.player.Player;

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

    public ActivateProductionAction(Player player, ClientHandler clientHandler){
        this.clientHandler = clientHandler;
        this.player = player;
        this.personalBoard = this.player.getPersonalBoard();
        //TODO: manage this exceptions
        try {
            this.availableResources = this.personalBoard.countResources();
        } catch (InactiveCardException | InvalidArgumentException | DifferentEffectTypeException e) {
            e.printStackTrace();
        }
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
        boolean executable = true;
        for (Map.Entry<Resource, Integer> entry : activationCost.entrySet()){
            try {
                executable = executable && entry.getValue() <= availableResources.get(entry.getKey());
            }catch(Exception e){
                //do nothing, it's only to easily skip a missing Resource in availableResources
            }
        }
        return executable;
    }

    @Override
    public void execute(TurnController turnController) {
        clientHandler.setCurrentAction(this);
        //List<Integer> availableProductionPowers = new ArrayList<>();
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
        availableProductionPowers.put(BASIC_PRODUCTION_POWER, basic_production.getProductionPower());
        clientHandler.sendMessageToClient(new ChooseProductionPowersRequest(availableProductionPowers, availableResources));

    }

    @Override
    public void handleMessage(MessageToServer message) {
        List<Integer> productionPowerSelected = ((ChooseProductionPowersResponse) message).getProductionPowersSelected();

        //check if productionPowerSelected.size() > 0. If so increment actionDone
        //else return

        //ask the client where he wants to remove the resources
        //add all resources to strongbox
        //if faithpoints were produced -> move marker, check faith track
    }
}

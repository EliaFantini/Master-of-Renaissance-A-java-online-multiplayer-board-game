package it.polimi.ingsw.controller.game_phases;


import it.polimi.ingsw.common.LightLeaderCard;
import it.polimi.ingsw.controller.Controller;
import it.polimi.ingsw.enumerations.*;
import it.polimi.ingsw.messages.toClient.game.ChooseLeaderCardsRequest;
import it.polimi.ingsw.messages.toClient.game.ChooseResourceTypeRequest;
import it.polimi.ingsw.messages.toClient.game.ChooseStorageTypeRequest;
import it.polimi.ingsw.messages.toClient.matchData.*;
import it.polimi.ingsw.jsonParsers.LightCardsParser;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.persistency.GameHistory;
import it.polimi.ingsw.model.persistency.PersistentControllerSetUpPhase;
import it.polimi.ingsw.model.persistency.PersistentGame;
import it.polimi.ingsw.server.ClientHandler;
import it.polimi.ingsw.common.LightDevelopmentCard;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.messages.toClient.*;
import it.polimi.ingsw.messages.toServer.game.ChooseLeaderCardsResponse;
import it.polimi.ingsw.messages.toServer.game.ChooseResourceTypeResponse;
import it.polimi.ingsw.messages.toServer.game.ChooseStorageTypeResponse;
import it.polimi.ingsw.messages.toServer.MessageToServer;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.jsonParsers.DevelopmentCardParser;
import it.polimi.ingsw.jsonParsers.LeaderCardParser;

import java.util.*;
import java.util.stream.Collectors;

public class SetUpPhase implements GamePhase {
    Controller controller;
    Map<String, Integer> initialResourceByNickname;
    Map<String, List<Resource>> resourcesToStoreByNickname;

    @Override
    public void executePhase(Controller controller) {
        this.controller = controller;
        resourcesToStoreByNickname = new HashMap<>();
        sendLightCards();
        setUpLeaderCards();
        controller.sendMessageToAll(new LoadDevelopmentCardGrid(controller.getGame().getDevelopmentCardGrid().getAvailableCards().stream().map(Card::getID).collect(Collectors.toList())));
    }

    public void handleMessage(MessageToServer message, ClientHandler clientHandler) {
        if (message instanceof ChooseLeaderCardsResponse && clientHandler.getClientHandlerPhase() == ClientHandlerPhase.WAITING_DISCARDED_LEADER_CARDS) {
            removeLeaderCards(((ChooseLeaderCardsResponse) message).getDiscardedLeaderCards(), clientHandler);
        }

        if (message instanceof ChooseResourceTypeResponse && clientHandler.getClientHandlerPhase() == ClientHandlerPhase.WAITING_CHOOSE_RESOURCE_TYPE)
            setInitialResourcesByNickname(((ChooseResourceTypeResponse) message).getResources(), clientHandler);

        if (message instanceof ChooseStorageTypeResponse && clientHandler.getClientHandlerPhase() == ClientHandlerPhase.WAITING_CHOOSE_STORAGE_TYPE)
            storeResource(((ChooseStorageTypeResponse) message).getResource(), ((ChooseStorageTypeResponse) message).getStorageType(), clientHandler);
    }

    /**
     * Method to assign 4 leader cards to each players and to randomly set the order in which the player will play
     */
    private void setUpLeaderCards() {
        List<LeaderCard> leaderCards = LeaderCardParser.parseCards();
        Collections.shuffle(leaderCards);

        //Shuffle the order of the nicknames
        List<String> nicknames = controller.getNicknames();
        Collections.shuffle(nicknames);

        //I set the number of resources for each player
        initialResourceByNickname = new HashMap<>();

        for (int i = 0; i < nicknames.size(); i++) {

            // 1. I assign leader cards and add the player to the game
            List<LeaderCard> leaderCardsAssigned = assignLeaderCards(leaderCards, 4 * i, 4 * i + 4);
            addPlayerToTheGame(nicknames.get(i), leaderCardsAssigned, i);

            // 2. I set the number of resources for each player
            initialResourceByNickname.put(nicknames.get(i), getNumberOfInitialResourcesByIndex(i));
        }

        //I send to everyone the view with the leader Cards to choose
        GameHistory.saveSetupPhase(new PersistentControllerSetUpPhase(new PersistentGame(controller.getGame()), controller.getControllerID(), resourcesToStoreByNickname));
        controller.sendMatchData(controller.getGame(), false);

        for (int i = 0; i < nicknames.size(); i++) {
            // 3. I send to the client the leader cards assigned
            ClientHandler connection = controller.getConnectionByNickname(nicknames.get(i));
            connection.setClientHandlerPhase(ClientHandlerPhase.WAITING_DISCARDED_LEADER_CARDS);
            connection.sendMessageToClient(new ChooseLeaderCardsRequest(controller.getPlayerByNickname(nicknames.get(i)).getPersonalBoard().getLeaderCards().stream().map(Card::getID).collect(Collectors.toList())));
        }

    }

    /**
     * Method to remove the two discarded {@link LeaderCard} from the {@link it.polimi.ingsw.model.player.PersonalBoard} of the {@link Player}
     * @param discardedCards the two cards to be removed
     * @param clientHandler the connection of the player who provided the cards
     */
    private void removeLeaderCards(List<Integer> discardedCards, ClientHandler clientHandler) {
        String nickname = clientHandler.getNickname();
        Player player = controller.getPlayerByNickname(nickname);
        for (Integer id : discardedCards)
            player.getPersonalBoard().removeLeaderCard(id);
        GameHistory.saveSetupPhase(new PersistentControllerSetUpPhase(new PersistentGame(controller.getGame()), controller.getControllerID(), resourcesToStoreByNickname));
        clientHandler.sendMessageToClient(new ReloadLeaderCardsOwned(nickname, player.getPersonalBoard().getLeaderCardsMap()));
        if (getNumberOfInitialResourcesByNickname(nickname) == 0) {
            controller.sendMessageToAll(new UpdateDepotsStatus(player.getNickname(), player.getPersonalBoard().getWarehouse().getWarehouseDepotsStatus(), player.getPersonalBoard().getStrongboxStatus(), player.getPersonalBoard().getLeaderStatus()));
            sendSetUpFinishedMessage(clientHandler);
        } else {
            assignResources(clientHandler);
        }
    }

    /**
     * Method to assign the right number of resources to a specific player, depending on his position in the turn round
     * @param clientHandler the player I want to assign the resources to
     */
    public void assignResources(ClientHandler clientHandler) {
        List<Resource> resourceTypes = Resource.realValues();
        clientHandler.setClientHandlerPhase(ClientHandlerPhase.WAITING_CHOOSE_RESOURCE_TYPE);
        clientHandler.sendMessageToClient(new ChooseResourceTypeRequest(resourceTypes, getNumberOfInitialResourcesByNickname(clientHandler.getNickname())));
    }

    /**
     * Method to ask where a specific player wants to store the resources he has taken
     * @param resources resources to store
     * @param clientHandler the connection of the player
     */
    private void setInitialResourcesByNickname(List<Resource> resources, ClientHandler clientHandler){
        resourcesToStoreByNickname.put(clientHandler.getNickname(), resources);
        GameHistory.saveSetupPhase(new PersistentControllerSetUpPhase(new PersistentGame(controller.getGame()), controller.getControllerID(), resourcesToStoreByNickname));
        List<String> availableDepots = ResourceStorageType.getWarehouseDepots();
        if (resources.size() == 2 && resources.get(0) == resources.get(1))
            availableDepots.remove(ResourceStorageType.WAREHOUSE_FIRST_DEPOT.name());
        clientHandler.setClientHandlerPhase(ClientHandlerPhase.WAITING_CHOOSE_STORAGE_TYPE);
        clientHandler.sendMessageToClient(new ChooseStorageTypeRequest(resources.get(0), availableDepots, false, false));
    }

    /**
     * Method to handle the response of the client, regarding the placement of his resources
     * @param resource the {@link Resource} to be stored
     * @param storageType where the {@link Resource} should be stored
     * @param clientHandler the connection of the player who has asked to store his {@link Resource}
     */
    private void storeResource(Resource resource, String storageType, ClientHandler clientHandler){
        Player player = controller.getPlayerByNickname(clientHandler.getNickname());
        resourcesToStoreByNickname.get(player.getNickname()).remove(resource);
        try {
            player.getPersonalBoard().addResources(ResourceStorageType.valueOf(storageType), resource, 1);
        } catch (InvalidDepotException | InvalidArgumentException | InvalidResourceTypeException | InsufficientSpaceException e) {
            e.printStackTrace();
        }
        GameHistory.saveSetupPhase(new PersistentControllerSetUpPhase(new PersistentGame(controller.getGame()), controller.getControllerID(), resourcesToStoreByNickname));
        if (resourcesToStoreByNickname.get(player.getNickname()).isEmpty()) {
            controller.sendMessageToAll(new UpdateDepotsStatus(player.getNickname(), player.getPersonalBoard().getWarehouse().getWarehouseDepotsStatus(), player.getPersonalBoard().getStrongboxStatus(), player.getPersonalBoard().getLeaderStatus()));
            sendSetUpFinishedMessage(clientHandler);
        } else {
            Resource resourceType = resourcesToStoreByNickname.get(player.getNickname()).get(0);
            List<String> availableStorage = player.getPersonalBoard().getWarehouse().getAvailableWarehouseDepotsForResourceType(resourceType).stream().map(x -> x.name()).collect(Collectors.toList());
            clientHandler.setClientHandlerPhase(ClientHandlerPhase.WAITING_CHOOSE_STORAGE_TYPE);
            clientHandler.sendMessageToClient(new ChooseStorageTypeRequest(resourceType, availableStorage, false, false));
        }

    }

    /**
     * Method to inform the client that he has finished the setUp phase and that the game will start when all the other players will be ready
     * @param clientHandler
     */
    private void sendSetUpFinishedMessage(ClientHandler clientHandler) {
        clientHandler.setClientHandlerPhase(ClientHandlerPhase.SET_UP_FINISHED);
        endPhaseManager(clientHandler);
    }

    public void endPhaseManager(ClientHandler clientHandler) {
        if (!(controller.getGamePhase() instanceof SetUpPhase))
            return;
        List<String> nicknames = controller.getClientHandlers().stream().map(ClientHandler::getNickname).collect(Collectors.toList());
        for (String nickname : nicknames) {
            if (controller.getConnectionByNickname(nickname).getClientHandlerPhase() != ClientHandlerPhase.SET_UP_FINISHED) {
                clientHandler.sendMessageToClient(new TextMessage("Waiting the other players, the game will start as soon as they all be ready..."));
                return;
            }
        }
        controller.setGamePhase(controller.getGame().getGameMode() == GameMode.MULTI_PLAYER ? new MultiplayerPlayPhase(controller) : new SinglePlayerPlayPhase(controller));
    }


    private void sendLightCards() {
        List<LightLeaderCard> leaderCards = LightCardsParser.getLightLeaderCards(LeaderCardParser.parseCards());
        List<LightDevelopmentCard> developmentCards = LightCardsParser.getLightDevelopmentCards(DevelopmentCardParser.parseCards());
        for (String nickname : controller.getNicknames()) {
            ClientHandler connection = controller.getConnectionByNickname(nickname);
            connection.sendMessageToClient(new LoadDevelopmentCardsMessage(developmentCards));
            connection.sendMessageToClient(new LoadLeaderCardsMessage(leaderCards));
        }
    }


    private List<LeaderCard> assignLeaderCards(List<LeaderCard> cards, int start, int end) {
        List<LeaderCard> IDs = new LinkedList<>();
        for (int i = start; i < end; i++)
            IDs.add(cards.get(i));
        return IDs;
    }

    private void addPlayerToTheGame(String nickname, List<LeaderCard> leaderCardsAssigned, int index) {
        try {
            controller.getGame().addPlayer(nickname, leaderCardsAssigned, getInitialFaithPoints(index), hasInkwell(index));
        } catch (InvalidArgumentException | InvalidPlayerAddException e) {
            e.printStackTrace();
        }
    }

    private boolean hasInkwell(int index) {
        return index == 0;
    }

    private int getInitialFaithPoints(int index) {
        return index > 1 ? 1 : 0;
    }

    private int getNumberOfInitialResourcesByIndex(int index) {
        if (index == 0)
            return 0;
        if (index < 3)
            return 1;
        return 2;
    }

    public int getNumberOfInitialResourcesByNickname(String nickname) {
        return initialResourceByNickname.get(nickname);
    }

    public String toString() {
        return "Set Up Phase";
    }
}

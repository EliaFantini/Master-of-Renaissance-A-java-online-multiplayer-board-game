package it.polimi.ingsw.messages.toServer;

import it.polimi.ingsw.common.ClientHandlerInterface;
import it.polimi.ingsw.common.ServerInterface;
import it.polimi.ingsw.controller.actions.Action;
import it.polimi.ingsw.controller.actions.TakeResourcesFromMarketAction;
import it.polimi.ingsw.messages.toClient.MarbleInsertionPositionRequest;

public class MarbleInsertionPositionResponse implements MessageToServer{
    Action action;
    private int insertionPosition;

    public MarbleInsertionPositionResponse(Action action, int insertionPosition){
        this.action = action;
        this.insertionPosition = insertionPosition;
    }

    private boolean isLegal(){
        return insertionPosition > 0 && insertionPosition < 8;
    }

    public int getInsertionPosition(){
        return this.insertionPosition;
    }

    @Override
    public void handleMessage(ServerInterface server, ClientHandlerInterface clientHandler) {
        if (!isLegal())
            clientHandler.sendMessageToClient(new MarbleInsertionPositionRequest(action, true));
        else
            clientHandler.getCurrentAction().handleMessage(this);
    }
}
package it.polimi.ingsw.messages.toClient;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.client.View;

import java.io.Serializable;

public class WaitingInTheLobbyMessage implements MessageToClient, Serializable {

    @Override
    public void handleMessage(View view, Client client) {
        view.displayWaitingInTheLobbyMessage();
    }
}
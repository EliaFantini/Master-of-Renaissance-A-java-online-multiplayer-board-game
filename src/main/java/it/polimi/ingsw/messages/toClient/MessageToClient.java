package it.polimi.ingsw.messages.toClient;

import it.polimi.ingsw.common.VirtualView;

import java.io.Serializable;

public interface MessageToClient extends Serializable {
    void handleMessage(VirtualView view);
}

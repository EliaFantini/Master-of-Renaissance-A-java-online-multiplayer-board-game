package it.polimi.ingsw.messages.toServer.game;

import it.polimi.ingsw.messages.toServer.MessageToServer;
import it.polimi.ingsw.server.Server;
import it.polimi.ingsw.common.ClientHandlerInterface;
import it.polimi.ingsw.common.ServerInterface;
import it.polimi.ingsw.enumerations.Resource;

import java.util.List;
import java.util.logging.Level;

public class ChooseResourceTypeResponse implements MessageToServer {

    private List<Resource> resources;

    public ChooseResourceTypeResponse(List<Resource> resources){
        this.resources = resources;
    }

    public List<Resource> getResources() {
        return resources;
    }
    @Override
    public void handleMessage(ServerInterface server, ClientHandlerInterface clientHandler) {
        Server.SERVER_LOGGER.log(Level.INFO, "New message from " + clientHandler.getNickname() + " that has choosen his resource types");
        clientHandler.getController().handleMessage(this, clientHandler);
    }
}

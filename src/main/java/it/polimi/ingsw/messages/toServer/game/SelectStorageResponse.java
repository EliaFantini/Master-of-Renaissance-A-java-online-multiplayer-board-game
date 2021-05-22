package it.polimi.ingsw.messages.toServer.game;

import it.polimi.ingsw.common.ClientHandlerInterface;
import it.polimi.ingsw.common.ServerInterface;
import it.polimi.ingsw.enumerations.Resource;
import it.polimi.ingsw.enumerations.ResourceStorageType;
import it.polimi.ingsw.messages.toServer.MessageToServer;
import it.polimi.ingsw.server.Server;

import java.util.logging.Level;

public class SelectStorageResponse implements MessageToServer {
    ResourceStorageType resourceStorageType;
    Resource resource;
    public SelectStorageResponse(Resource resource, ResourceStorageType resourceStorageType) {
        this.resourceStorageType=resourceStorageType;
        this.resource=resource;
    }

    public ResourceStorageType getResourceStorageType() {
        return resourceStorageType;
    }

    public Resource getResource() {
        return resource;
    }

    @Override
    public void handleMessage(ServerInterface server, ClientHandlerInterface clientHandler) {
        Server.SERVER_LOGGER.log(Level.INFO, "New message from " + clientHandler.getNickname() + " that has decided to store one" + resource + "in " + resourceStorageType);
        clientHandler.getCurrentAction().handleMessage(this);
    }
}

package it.polimi.ingsw.messages.toClient;

import it.polimi.ingsw.common.ClientInterface;
import it.polimi.ingsw.common.VirtualView;
import it.polimi.ingsw.enumerations.Marble;

import java.util.List;

public class ChooseWhiteMarbleConversionRequest implements MessageToClient{

    private List<Marble> conversions;
    private int numberOfWhiteMarbles;


    public ChooseWhiteMarbleConversionRequest(List<Marble> conversions, int numberOfWhiteMarbles){
        this.conversions = conversions;
        this.numberOfWhiteMarbles = numberOfWhiteMarbles;
    }

    @Override
    public void handleMessage(VirtualView view) {
        view.displayChooseWhiteMarbleConversionRequest(conversions, numberOfWhiteMarbles);

    }
}
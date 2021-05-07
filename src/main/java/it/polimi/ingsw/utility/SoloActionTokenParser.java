package it.polimi.ingsw.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import it.polimi.ingsw.controller.actions.BlackCrossMoveToken;
import it.polimi.ingsw.controller.actions.DiscardToken;
import it.polimi.ingsw.controller.actions.SoloActionToken;
import it.polimi.ingsw.enumerations.FlagColor;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class SoloActionTokenParser {

    private SoloActionTokenParser(){ throw new IllegalStateException("Utility class"); }

    public static Queue<SoloActionToken> parseTokens(){
        Queue<SoloActionToken> tokens = new LinkedList<>();
        String path = "json/SoloActionTokens.json";
        InputStream in = SoloActionTokenParser.class.getClassLoader().getResourceAsStream(path);
        JsonReader reader = null;
        try {
            reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        JsonArray jsonTokens = jsonObject.getAsJsonArray("soloActionTokens");

        for (JsonElement tokenElem : jsonTokens){
            JsonObject token = tokenElem.getAsJsonObject();

            String imageFront = token.get("imageFront").getAsString();
            String imageBack = token.get("imageBack").getAsString();
            String type = token.get("type").getAsString();
            if(type.equals("DISCARD")){
                String flagLevel = token.get("flagLevel").getAsString();
                String flagColor = token.get("flagColor").getAsString();
                int numberOfCards = token.get("numberOfCards").getAsInt();
                tokens.add(new DiscardToken(imageFront, imageBack, numberOfCards, FlagColor.valueOf(flagColor)));
            }else{
                boolean shuffle = token.get("shuffle").getAsBoolean();
                int numberOfMoves = token.get("numberOfMoves").getAsInt();
                tokens.add(new BlackCrossMoveToken(imageFront, imageBack, numberOfMoves, shuffle));
            }

        }
        return tokens;
    }


}

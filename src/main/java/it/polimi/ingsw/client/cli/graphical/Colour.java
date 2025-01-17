package it.polimi.ingsw.client.cli.graphical;

import it.polimi.ingsw.enumerations.Marble;
import it.polimi.ingsw.enumerations.Resource;

/**
 * Enumeration to assign a color to a symbol in the {@link it.polimi.ingsw.client.cli.CLI}
 */
public enum Colour {
    ANSI_BLACK("\u001B[30m"),
    ANSI_RED  ("\u001B[31m"),
    ANSI_GREEN("\u001B[32m"),
    ANSI_YELLOW("\u001B[33m"),
    ANSI_BLUE ("\u001B[34m"),
    ANSI_PURPLE("\u001B[35m"),
    ANSI_CYAN ("\u001B[36m"),
    ANSI_WHITE("\u001B[37m"),
    ANSI_BRIGHT_BLACK("\u001B[90m"),
    ANSI_BRIGHT_RED  ("\u001B[91m"),
    ANSI_BRIGHT_GREEN("\u001B[92m"),
    ANSI_BRIGHT_YELLOW("\u001B[93m"),
    ANSI_BRIGHT_BLUE ("\u001B[94m"),
    ANSI_BRIGHT_PURPLE("\u001B[95m"),
    ANSI_BRIGHT_CYAN ("\u001B[96m"),
    ANSI_BRIGHT_WHITE("\u001B[97m"),
    ANSI_DEFAULT("\u001B[0m");

    public static final String ANSI_RESET = "\u001B[0m";


    private final String code;

    Colour(String code){
        this.code = code;
    }

    public String getCode(){
        return code;
    }

    /**
     * Given a {@link Marble} return the corresponding color code
     * @param marble the {@link Marble} to convert to a color
     * @return a String representing the color code
     */
    public static String getMarbleColour(Marble marble){
        if (marble == Marble.YELLOW)
            return ANSI_BRIGHT_YELLOW.code;
        if (marble == Marble.GREY)
            return ANSI_WHITE.code;
        if (marble == Marble.PURPLE)
            return ANSI_BRIGHT_PURPLE.code;
        if (marble == Marble.BLUE)
            return ANSI_BRIGHT_BLUE.code;
        if (marble == Marble.RED)
            return ANSI_BRIGHT_RED.code;
        else
            return ANSI_BRIGHT_WHITE.code;
    }

    /**
     * Given a {@link Marble} return the corresponding color
     * @param marble the {@link Marble} to convert to a color
     * @return a {@link Colour}
     */
    public static Colour getColourByMarble(Marble marble){
        if (marble == Marble.YELLOW)
            return ANSI_BRIGHT_YELLOW;
        if (marble == Marble.GREY)
            return ANSI_BRIGHT_BLACK;
        if (marble == Marble.PURPLE)
            return ANSI_BRIGHT_PURPLE;
        if (marble == Marble.BLUE)
            return ANSI_BRIGHT_BLUE;
        if (marble == Marble.RED)
            return ANSI_BRIGHT_RED;
        else
            return ANSI_BRIGHT_WHITE;
    }

    /**
     * Given a {@link Resource} return the corresponding color
     * @param resource the {@link Resource} to convert to a color
     * @return a {@link Colour}
     */
    public static Colour getColourByResource(Resource resource){
        if (resource == Resource.COIN)
            return ANSI_BRIGHT_YELLOW;
        if (resource == Resource.STONE)
            return ANSI_BRIGHT_BLACK;
        if (resource == Resource.SHIELD)
            return ANSI_BRIGHT_BLUE;
        else
            return ANSI_BRIGHT_PURPLE;
    }

    /**
     * Given a {@link Resource} return the corresponding color code
     * @param resource the {@link Resource} to convert to a color
     * @return a String representing the color code
     */
    public static String getResourceColour(Resource resource){
        if (resource == Resource.COIN)
            return ANSI_BRIGHT_YELLOW.code;
        if (resource == Resource.STONE)
            return ANSI_WHITE.code;
        if (resource == Resource.SHIELD)
            return ANSI_BRIGHT_BLUE.code;
        else
            return ANSI_BRIGHT_PURPLE.code;
    }
}

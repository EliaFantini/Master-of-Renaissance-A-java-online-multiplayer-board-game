package it.polimi.ingsw.model.game;

import it.polimi.ingsw.enumerations.GameType;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.cards.LeaderCard;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.utility.LeaderCardParser;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class GameTest {

    List<LeaderCard> leaderCards;
    List<String> nicknames;
    Game multiGame;
    Game singleGame;

    @Before
    public void setUp() throws JsonFileNotFoundException, InvalidArgumentException, FileNotFoundException, UnsupportedEncodingException {
       leaderCards = LeaderCardParser.parseCards();
       nicknames = new ArrayList<String>();
       nicknames.add("Betti");
       nicknames.add("Elia");
       nicknames.add("Raffa");
    }

    @Test
    public void testGameType() throws InvalidArgumentException, UnsupportedEncodingException {
        multiGame = new Game(GameType.MULTI_PLAYER);
        assertEquals(multiGame.getGameType(), GameType.MULTI_PLAYER);
    }

    @Test
    public void testAddGetPlayers() throws InvalidArgumentException, InvalidMethodException, UnsupportedEncodingException, ZeroPlayerException, InvalidPlayerAddException {
        multiGame = new Game(GameType.MULTI_PLAYER);
        multiGame.addPlayer(nicknames.get(0), leaderCards.subList(0, 4));
        multiGame.addPlayer(nicknames.get(1), leaderCards.subList(4, 8));
        multiGame.addPlayer(nicknames.get(2), leaderCards.subList(8, 12));
        List<Player> players = multiGame.getPlayers();
        assertEquals(players.get(0).getNickname(), "Betti");
        assertEquals(players.get(0).getPersonalBoard().getLeaderCards().get(0).getVictoryPoints(), 2);
    }

    @Test
    public void testAddGetPlayer() throws InvalidArgumentException, UnsupportedEncodingException, InvalidMethodException, ZeroPlayerException, InvalidPlayerAddException {
        singleGame = new Game(GameType.SINGLE_PLAYER);
        singleGame.addPlayer(nicknames.get(0), leaderCards.subList(0, 4));
        assertEquals(singleGame.getSinglePlayer().getNickname(), "Betti");
    }

    @Test (expected = ZeroPlayerException.class)
    public void testEmptyRoomMultiplayer() throws InvalidArgumentException, UnsupportedEncodingException, InvalidMethodException, ZeroPlayerException {
        multiGame = new Game(GameType.MULTI_PLAYER);
        List<Player> players = multiGame.getPlayers();
    }

    @Test (expected = ZeroPlayerException.class)
    public void testEmptyRoomSingleplayer() throws InvalidMethodException, ZeroPlayerException, InvalidArgumentException, UnsupportedEncodingException {
        singleGame = new Game(GameType.SINGLE_PLAYER);
        Player player = singleGame.getSinglePlayer();
    }

    @Test ( expected = InvalidArgumentException.class)
    public void testInvalidAddSameNickname() throws InvalidArgumentException, InvalidMethodException, UnsupportedEncodingException, InvalidPlayerAddException {
        multiGame = new Game(GameType.MULTI_PLAYER);
        multiGame.addPlayer(nicknames.get(0), leaderCards.subList(0, 4));
        multiGame.addPlayer(nicknames.get(1), leaderCards.subList(4, 8));
        multiGame.addPlayer(nicknames.get(2), leaderCards.subList(8, 12));
        multiGame.addPlayer("Betti", leaderCards.subList(4, 8));
    }

    @Test (expected = InvalidPlayerAddException.class)
    public void testInvalidAddSinglePlayer() throws InvalidArgumentException, UnsupportedEncodingException, InvalidMethodException, InvalidPlayerAddException {
        singleGame = new Game(GameType.SINGLE_PLAYER);
        singleGame.addPlayer(nicknames.get(0), leaderCards.subList(0, 4));
        singleGame.addPlayer(nicknames.get(1), leaderCards.subList(4, 8));
    }

    @Test (expected = InvalidMethodException.class)
    public void testInvalidGetSinglePlayer() throws InvalidArgumentException, UnsupportedEncodingException, InvalidMethodException, ZeroPlayerException, InvalidPlayerAddException {
        singleGame = new Game(GameType.SINGLE_PLAYER);
        singleGame.addPlayer(nicknames.get(0), leaderCards.subList(0, 4));
        List<Player> players = singleGame.getPlayers();
    }

    @Test (expected = InvalidMethodException.class)
    public void testInvalidGetMultiPlayer() throws InvalidArgumentException, UnsupportedEncodingException, InvalidMethodException, ZeroPlayerException, InvalidPlayerAddException {
        multiGame = new Game(GameType.MULTI_PLAYER);
        multiGame.addPlayer(nicknames.get(0), leaderCards.subList(0, 4));
        Player players = multiGame.getSinglePlayer();
    }

    @Test (expected = NullPointerException.class)
    public void testInvalidLeaderCards() throws InvalidArgumentException, InvalidMethodException, UnsupportedEncodingException, InvalidPlayerAddException {
        multiGame = new Game(GameType.MULTI_PLAYER);
        multiGame.addPlayer(nicknames.get(0), null);
    }

}
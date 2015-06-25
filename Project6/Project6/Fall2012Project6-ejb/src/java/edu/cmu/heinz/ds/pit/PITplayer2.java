package edu.cmu.heinz.ds.pit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.*;
import javax.naming.*;


/*
 * PITplayer 0, 1, and 2 share identical code, except for three places:
 *  - the Queue that is being listened to (e.g. jms/PITplayer0)
 *  - the name of the class (e.g. PITplayer0)
 *  - the value of myPlayerNumber
 * 
 * Therefore as you change the code in one, make sure to reflect those changes
 * in all 3.  In your final submission, all 3 PITplayers should have identical
 * code except for in these three places.
 */
@MessageDriven(mappedName = "jms/PITplayer2", activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class PITplayer2 implements MessageListener {

    // Each PITplayer has a unique myPlayerNumber.  It should be the same as the Queue listened to.
    private final int myPlayerNumber = 2;
    // The following are all static for they are shared between multiple instances of this class
    // (of which there could be multiple).
    // Cards is this player's set of cards.  
    private final static ArrayList cards = new ArrayList();
    // tradeCount counts trades
    private static int tradeCount = 0;
    // maxTrades is the maximum number of trades, after which trading is stopped.
    private final static int maxTrades = 20000;
    // numPlayers are the number of Players trading.  This comes with a NewHand from the PITinit servlet
    private static int numPlayers = 0;
    //array to keep track state on each channel
    private ArrayList<ArrayList> playerChannels = new ArrayList<ArrayList>();
    //state whether the channels should be recorded
    private boolean[] isRecording;
    // The snapshot servlet (PITsnapshot) is expecting to be passed an ObjectMessage
    // where the object is a HashMap. Therefore this definition of that HashMap is 
    // provided although it is not currently used (it is for you to use).
    // Also included below is a utility method that will turn a HashMap into a string
    // which is useful for printing diagnostic messages to the console.
    private static HashMap<String, Integer> state;

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof ObjectMessage) {
                Object o = ((ObjectMessage) message).getObject();

                // There are 4 types of messages:  Reset, NewHand, Trade, and Marker

                // Reset the Player.  The Reset message is generated by the PITinit servlet
                if (o instanceof Reset) {
                    System.out.println("PITplayer" + myPlayerNumber + " received reset");
                    // Drop all cards in hand
                    cards.clear();
                    tradeCount = 0;
                    numPlayers = 0;
                    // Reply to the PITinit servlet acknowledging the Reset
                    sendToQueue("jms/PITmonitor", (Reset) o);
                    return;

                    // NewHand received from PITinit
                } else if (o instanceof NewHand) {
                    // Add new hand cards.  
                    // It is actually possible that a trade had added a card already.
                    cards.addAll(((NewHand) o).handCard);
                    numPlayers = ((NewHand) o).numPlayers;
                    System.out.println("PITplayer" + myPlayerNumber + " new hand: " + toString(cards));

                    // Receive a Trade from another Player
                } else if (o instanceof Trade) {
                    // Receiving a Trade card from another Player, add it to my hand of cards
                    Trade trade = (Trade) o;
                    cards.add(trade.tradeCard);

                    System.out.println("PITplayer" + myPlayerNumber + " received: " + trade.tradeCard + " from player: " + trade.sourcePlayer);
                    System.out.println("PITplayer" + myPlayerNumber + " hand: " + toString(cards));

                    //record all incoming card after snapshot has been taken
                    if (isRecording != null && isRecording[trade.sourcePlayer]) {
                        ((ArrayList) playerChannels.get(trade.sourcePlayer)).add(trade.tradeCard);
                    }

                    //Receive a Maker from another Player
                } else if (o instanceof Marker) {

                    System.out.println("PITplayer" + myPlayerNumber + " marker source = " + ((Marker) o).source);
                    //If the player is not yet taking part in this snapshot
                    //Begin keeping track of state all other incoming channels (except c): 
                    //(I.e. count commodities arriving via each channel.) 
                    if (state == null) {
                        isRecording = new boolean[numPlayers];
                        playerChannels.clear();
                        for (int i = 0; i < numPlayers; i++) {
                            playerChannels.add(new ArrayList<String>());
                            isRecording[i] = true;
                        }
                        isRecording[myPlayerNumber] = false;
                        if (((Marker) o).source >= 0) {
                            isRecording[((Marker) o).source] = false;
                        }
                        state = new HashMap<String, Integer>();

                        //Record my state now (count # of each commodity in hand only) 
                        for (Object cardName : cards) {
                            String cardNameStr = cardName.toString();
                            if (!state.containsKey(cardNameStr)) {
                                state.put(cardNameStr, 0);
                            }
                            state.put(cardNameStr, (state.get(cardNameStr) + 1));
                        }

                        //Do Marker sending rule 
                        sendMarkers();
                       
                        //if this marker is coming in from a new channel c { 
                    } else {
                        //   Record that the state of channel c is all messages it has received since it began 
                        //    keeping track. (No more coming.) 
                        //    Stop recording any further messages on channel c (they are now post-shapshot) 
                        if (((Marker) o).source < 0) {
                            return;
                        }
                        isRecording[((Marker) o).source] = false;
                        
                        //If this is last channel to receive marker on, send records to Monitor process 
                        boolean isActive = false;
                        for (int i = 0; i < numPlayers; i++) {
                            isActive = isActive || isRecording[i];
                        }
                        if (!isActive) {
                            //add commodities from each incoming channel into hash map
                            for (ArrayList<String> channel : playerChannels) {
                                for (String item : channel) {
                                    System.out.println("Player " + myPlayerNumber + " item : " + item);
                                    if (!state.containsKey(item)) {
                                        state.put(item, 0);
                                    }
                                    state.put(item, (state.get(item) + 1));
                                }
                            }
                            System.out.println("PITplayer" + myPlayerNumber + " sent snapshot to servlet: " + state);
                            sendToQueue("jms/PITsnapshot", state);

                            //Reset state to null to be able to take snapshot again
                            state = null;
                        }

                    }
                } else {
                    System.out.println("PITplayer" + myPlayerNumber + " received unknown Message type");
                    // just ignore it
                    return;
                }
            }

            // if hit maxTrades limit, then stop sending trades
            if (maxTrades(maxTrades)) {
                return;
            }

            /*
             * If numPlayers == 0, while we have received a Trade, we have not 
             * received our NewHand yet, so we don't know how many players there 
             * are.  Therefore, don't send out a Trade at this time.
             * 
             */
            if (numPlayers == 0) {
                return;
            }

            // Create a new Trade from my set of cards, and send to another player
            if (!cards.isEmpty()) {
                Trade newTrade = new Trade();
                newTrade.sourcePlayer = myPlayerNumber;
                newTrade.tradeCard = (String) cards.remove(0);
                // Find a random player to trade to (not including myself)
                int sendTo = Math.round((float) Math.random() * (numPlayers - 1));
                while (sendTo == myPlayerNumber) {
                    sendTo = Math.round((float) Math.random() * (numPlayers - 1));
                }
                //Send the card to the other player
                System.out.println("PITplayer" + myPlayerNumber + " sending: " + newTrade.tradeCard + " to player: " + sendTo);
                String sendToJNDI = "jms/PITplayer" + sendTo;
                sendToQueue(sendToJNDI, newTrade);
            }
        } catch (JMSException e) {
            System.out.println("JMS Exception thrown" + e);
        } catch (Throwable e) {
            System.out.println("Throwable thrown in PITplayer" + myPlayerNumber + ": " + e);
        }
    }

    // send markers to all channels
    private void sendMarkers() {
        try {
            for (int i = 0; i < this.numPlayers; i++) {
                if (i != this.myPlayerNumber) {
                    sendToQueue("jms/PITplayer" + i, new Marker(this.myPlayerNumber));
                }
            }
        } catch (JMSException e) {
            System.out.println("JMS Exception thrown" + e);
        } catch (Throwable e) {
            System.out.println("Throwable thrown in PITplayer - sendMakers" + myPlayerNumber + ": " + e);
        }
    }

    // Create a string of hand size and all cards for use in printing
    private String toString(ArrayList hand) {

        String cardsString = "size: " + hand.size() + " ";
        for (int i = 0; i < hand.size(); i++) {
            cardsString += hand.get(i) + " ";
        }
        return cardsString;
    }

    // Create a printable version of the "state".
    private String toString(HashMap<String, Integer> state) {
        String stateString = "";
        for (Iterator it = state.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String commodity = (String) entry.getKey();
            int number = ((Integer) entry.getValue()).intValue();
            stateString += "{" + commodity + ":" + number + "} ";
        }
        return stateString;
    }

    // Send an object to a Queue, given its JNDI name
    private void sendToQueue(String queueJNDI, Serializable message) throws Exception {
        // Gather necessary JMS resources
        Context ctx = new InitialContext();
        Connection con = ((ConnectionFactory) ctx.lookup("jms/myConnectionFactory")).createConnection();
        Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue q = (Queue) ctx.lookup(queueJNDI);
        MessageProducer writer = session.createProducer(q);
        ObjectMessage msg = session.createObjectMessage(message);
        // Send the object to the Queue
        writer.send(msg);
        session.close();
        con.close();
        ctx.close();
    }

    // stop trading when hit maxTrades
    private boolean maxTrades(int max) {
        // stop trading after some number of trades
        if ((tradeCount % 100) == 0) {
            System.out.println("PITplayer" + myPlayerNumber + " tradeCount: " + tradeCount);
        }
        return (tradeCount++ < max) ? false : true;
    }
}
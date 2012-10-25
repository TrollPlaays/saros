package de.fu_berlin.inf.dpp.communication.chat;

import org.jivesoftware.smackx.ChatState;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * A listener for {@link IChat} chat-related events.
 */
public interface IChatListener {

    /**
     * Gets called whenever a message has been received
     * 
     * @param sender
     *            {@link JID} who has sent the message
     * @param message
     *            the received message
     */
    public void messageReceived(JID sender, String message);

    /**
     * Gets called whenever a {@link JID}'s {@link ChatState} has been changed
     * 
     * @param jid
     *            who's {@link ChatState} has been changed
     * @param state
     *            new {@link ChatState}
     */
    public void stateChanged(JID jid, ChatState state);

    /**
     * Gets called whenever a participant connected from the chat.
     * 
     * @param jid
     *            the JID of the users that will receive the messages
     */
    public void connected(JID jid);

    /**
     * Gets called whenever a participant disconnected from the chat.
     * 
     * @param jid
     *            the JID of the user that will not receive any messages
     */
    public void disconnected(JID jid);

    /**
     * Gets called whenever a contact joined the chat. This will only happen
     * with multi user chats.
     * 
     * @param jid
     *            the JID of the user that joined the chat
     */
    public void joined(JID jid);

    /**
     * Gets called whenever a contact left the chat. This will probably only
     * happen with multi user chats.
     * 
     * @param jid
     *            the JID of the user that left the chat
     */
    public void left(JID jid);

}

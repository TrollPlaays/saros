package de.fu_berlin.inf.dpp.ui.widgets.session;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ChatState;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.communication.chat.IChat;
import de.fu_berlin.inf.dpp.communication.chat.single.ChatServiceImpl;
import de.fu_berlin.inf.dpp.communication.muc.events.IMUCManagerListener;
import de.fu_berlin.inf.dpp.communication.muc.events.MUCManagerAdapter;
import de.fu_berlin.inf.dpp.communication.muc.session.MUCSession;
import de.fu_berlin.inf.dpp.communication.muc.session.events.IMUCSessionListener;
import de.fu_berlin.inf.dpp.communication.muc.session.history.MUCSessionHistory;
import de.fu_berlin.inf.dpp.communication.muc.session.history.elements.MUCSessionHistoryElement;
import de.fu_berlin.inf.dpp.communication.muc.session.history.elements.MUCSessionHistoryJoinElement;
import de.fu_berlin.inf.dpp.communication.muc.session.history.elements.MUCSessionHistoryLeaveElement;
import de.fu_berlin.inf.dpp.communication.muc.session.history.elements.MUCSessionHistoryMessageReceptionElement;
import de.fu_berlin.inf.dpp.communication.muc.singleton.MUCManagerSingletonWrapperChatView;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.net.IRosterListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.sounds.SoundManager;
import de.fu_berlin.inf.dpp.ui.sounds.SoundPlayer;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.ChatControl;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.CharacterEnteredEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.ChatClearedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.IChatControlListener;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.MessageEnteredEvent;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.nebula.explanation.ListExplanationComposite.ListExplanation;
import de.fu_berlin.inf.nebula.explanation.explanatory.ListExplanatoryComposite;
import de.fu_berlin.inf.nebula.utils.ColorUtils;

/**
 * This component shows chat he right side of the {@link SarosView}
 * 
 * @author patbit
 */

public class ChatRoomsComposite extends ListExplanatoryComposite {

    private static final Logger log = Logger
        .getLogger(ChatRoomsComposite.class);

    /**
     * Default image for ChatView.
     */
    public static final Image chatViewImage = ImageManager
        .getImage("icons/view16/chat_misc.png");

    /**
     * Image while composing a message.
     */
    public static final Image composingImage = ImageManager
        .getImage("icons/view16/cmpsg_misc.png");

    protected ListExplanation howTo = new ListExplanation(SWT.ICON_INFORMATION,
        "To share projects you can either:", "Right-click on a project",
        "Right-click on a buddy", "Use the Saros menu in the Eclipse menu bar");

    protected ListExplanation chatError;

    protected boolean isSessionRunning;

    protected RosterTracker rosterTracker;

    /**
     * This RosterListener closure is added to the RosterTracker to get
     * notifications when the roster changes.
     */
    protected IRosterListener rosterListener = new IRosterListener() {

        /**
         * This method is mainly called, if the user name is changed, rebuild
         * Chat with uptodate nicknames from history
         */
        public void entriesUpdated(Collection<String> addresses) {

            Utils.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    if (!isDisposed()) {
                        log.info("roster entries changed, refreshing chat history");
                        refreshFromHistory();
                    }
                }
            });
        }

        public void entriesDeleted(Collection<String> addresses) {
            // do nothing
        }

        public void presenceChanged(Presence presence) {
            // do nothing
        }

        public void rosterChanged(Roster roster) {
            // do nothing
        }

        public void entriesAdded(Collection<String> addresses) {
            // do nothing
        }

    };

    @Inject
    /*
     * TODO: see
     * https://sourceforge.net/tracker/?func=detail&aid=3102858&group_id
     * =167540&atid=843362
     */
    protected EditorManager editorManager;
    protected AbstractSharedEditorListener sharedEditorListener = new AbstractSharedEditorListener() {
        @Override
        public void colorChanged() {
            if (chatControl != null && !chatControl.isDisposed()) {
                refreshFromHistory();
            }
        }
    };

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarting(ISarosSession session) {
            Utils.runSafeSWTAsync(log, new Runnable() {
                @Override
                public void run() {
                    isSessionRunning = true;
                }
            });
        }

        @Override
        public void sessionEnded(ISarosSession session) {
            Utils.runSafeSWTAsync(log, new Runnable() {
                @Override
                public void run() {
                    isSessionRunning = false;

                    if (ChatRoomsComposite.this.isDisposed())
                        return;

                    if (chatError == null)
                        return;

                    showErrorMessage(null);
                }
            });
        }
    };

    /**
     * Adds/removes an {@link IMUCSessionListener} to/from the
     * {@link MUCManagerSingletonWrapperChatView} depending on its availability.
     */
    protected IMUCManagerListener mucManagerListener = new MUCManagerAdapter() {
        @Override
        public void mucSessionJoined(MUCSession mucSession) {
            attachToMUCSession(mucSession);
        }

        @Override
        public void mucSessionLeft(MUCSession mucSession) {
            detachFromMUCSession(mucSession);
        }

        @Override
        public void mucSessionConnectionError(MUCSession mucSession,
            XMPPException exception) {

            final String errorMessage = "Could not connect to "
                + mucSession.getPreferences().getService()
                + "\nNo chat will be available for your current session";

            Utils.runSafeSWTAsync(log, new Runnable() {

                @Override
                public void run() {
                    if (ChatRoomsComposite.this.isDisposed())
                        return;

                    showErrorMessage(errorMessage);
                }

            });
        }
    };

    /**
     * Handles events on the {@link MUCManagerSingletonWrapperChatView}
     */
    protected IMUCSessionListener mucSessionListener = new IMUCSessionListener() {
        public void joined(final JID jid) {
            addChatLine(new MUCSessionHistoryJoinElement(jid, new Date()));

            if (isOwnJID(jid)) {
                Utils.runSafeSWTAsync(log, new Runnable() {
                    public void run() {

                        hideExplanation();
                        chatRoom1 = new CTabItem(chatRooms, SWT.NONE);
                        chatRoom1
                            .setText(Messages.ChatRoomsComposite_roundtable);
                        chatRoom1.setImage(chatViewImage);
                        chatRoom1.setControl(chatControl);
                        chatRooms.setSelection(0);
                    }
                });
            }
        }

        public void left(final JID jid) {
            addChatLine(new MUCSessionHistoryLeaveElement(jid, new Date()));

            if (isOwnJID(jid)) {
                Utils.runSafeSWTAsync(log, new Runnable() {
                    public void run() {
                        showExplanation(howTo);
                        if (chatRoom1 != null && !chatRoom1.isDisposed()) {
                            chatRoom1.dispose();
                        }

                        if (chatRooms != null && !chatRooms.isDisposed()) {
                            chatRooms.setSelection(0);
                        }
                    }
                });
            }
        }

        public void messageReceived(final JID jid, final String message) {
            addChatLine(new MUCSessionHistoryMessageReceptionElement(jid,
                new Date(), message));

            if (!isOwnJID(jid)) {
                Utils.runSafeSWTAsync(log, new Runnable() {
                    public void run() {
                        SoundPlayer.playSound(SoundManager.MESSAGE_RECEIVED);
                    }
                });
            } else {
                Utils.runSafeSWTAsync(log, new Runnable() {
                    public void run() {
                        SoundPlayer.playSound(SoundManager.MESSAGE_SENT);
                    }
                });
            }
        }

        public void stateChanged(final JID sender, final ChatState state) {
            log.debug("Received ChatState from " + sender + ": " //$NON-NLS-1$ //$NON-NLS-2$
                + state.toString());

            Utils.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    if (mucManager.getMUCSession() != null) {
                        if (mucManager.getMUCSession().getForeignStatesCount(
                            ChatState.composing) > 0) {
                            chatRooms.getSelection().setImage(composingImage);
                        } else {
                            chatRooms.getSelection().setImage(chatViewImage);
                        }
                    }
                }
            });
        }
    };

    /**
     * Handles events that occur in the {@link ChatControl}
     */
    protected IChatControlListener chatControlListener = new IChatControlListener() {
        /**
         * Update one's own {@link ChatState}
         */
        public void characterEntered(CharacterEnteredEvent event) {

            if (chatControl.getInputText().length() == 0) {
                mucManager.getMUCSession().setState(ChatState.inactive);
            } else {
                mucManager.getMUCSession().setState(ChatState.composing);
            }
        }

        /**
         * Sends the entered message
         */
        public void messageEntered(MessageEnteredEvent event) {
            String enteredMessage = event.getEnteredMessage();

            if (mucManager.getMUCSession() != null) {
                mucManager.getMUCSession().sendMessage(enteredMessage);
            }
        }

        public void chatCleared(ChatClearedEvent event) {
            /*
             * If the users chooses to clear the chat we do not want keep the
             * information in the chat history
             */
            if (mucManager.getMUCSession() != null) {
                mucManager.getMUCSession().clearHistory();
            }

        }
    };

    @Inject
    protected ISarosSessionManager sessionManager;
    @Inject
    protected MUCManagerSingletonWrapperChatView mucManager;

    Color white = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);

    CTabFolder chatRooms;
    ChatControl chatControl;

    protected CTabItem chatRoom1;

    @Inject
    protected ChatServiceImpl chatService;

    /**
     * Used to cache the sender names belonging to a {@link JID} determined
     * {@link User}
     */
    protected Map<JID, String> senderCache = new HashMap<JID, String>();

    /**
     * Used to cache the {@link Color}s belonging to a {@link JID} determined
     * {@link User}
     */
    protected Map<JID, Color> colorCache = new HashMap<JID, Color>();

    public ChatRoomsComposite(Composite parent, int style,
        final RosterTracker rosterTracker) {
        super(parent, style);

        this.rosterTracker = rosterTracker;
        rosterTracker.addRosterListener(rosterListener);

        SarosPluginContext.initComponent(this);

        this.sessionManager.addSarosSessionListener(sessionListener);
        this.editorManager.addSharedEditorListener(sharedEditorListener);
        this.mucManager.addMUCManagerListener(mucManagerListener);

        this.setLayout(new FillLayout());

        this.chatRooms = new CTabFolder(this, SWT.BOTTOM);
        this.setContentControl(this.chatRooms);

        this.chatRooms.setSimple(true);
        this.chatRooms.setBorderVisible(true);

        this.chatControl = new ChatControl(this.chatRooms, SWT.BORDER, white,
            white, 2);
        this.chatControl.addChatControlListener(chatControlListener);

        /*
         * IMPORTANT: The user can open and close Views as he wishes. This means
         * that the live cycle of this ChatView is completely independent of the
         * global MUCSession. Therefore we need to correctly validate the
         * MUCSession's state when this ChatView is reopened.
         */

        isSessionRunning = sessionManager.getSarosSession() != null;

        if (this.joinedSession()) {
            this.attachToMUCSession(mucManager.getMUCSession());
            chatRoom1 = new CTabItem(chatRooms, SWT.NONE);
            chatRoom1.setText(Messages.ChatRoomsComposite_roundtable);
            chatRoom1.setImage(chatViewImage);
            chatRoom1.setControl(chatControl);
            chatRooms.setSelection(0);
            hideExplanation();
        } else {
            showExplanation(howTo);
        }

        // Show already received messages
        this.refreshFromHistory();
        chatRooms.setSelection(0);

        this.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {

                sessionManager.removeSarosSessionListener(sessionListener);

                chatControl.removeChatControlListener(chatControlListener);
                if (mucManager.getMUCSession() != null) {
                    detachFromMUCSession(mucManager.getMUCSession());
                }
                mucManager.removeMUCManagerListener(mucManagerListener);
                editorManager.removeSharedEditorListener(sharedEditorListener);

                /**
                 * This must be called before finalization otherwise you will
                 * get NPE on RosterTracker.
                 */
                rosterTracker.removeRosterListener(rosterListener);
            }
        });

    }

    /**
     * Create a new single user chat with the given JID and open it.
     * 
     * @param jid
     * @param activateAfterCreation
     *            see {@link ChatRoomsComposite#openChat(IChat, boolean)} *
     */
    public void openChat(JID jid, boolean activateAfterCreation) {
        openChat(chatService.createChat(jid), activateAfterCreation);
    }

    /**
     * Open the tab for a given chat.
     * 
     * If the the corresponding tab already exists, it will be activated,
     * otherwise a new tab will be created.
     * 
     * @param chat
     *            The chat that should be displayed. If no corresponding chat
     *            tab exists, a new one will be created.
     * @param activateAfterCreation
     *            If a new tab is created, setting this parameter
     *            <code>false</code> will open the tab in background,
     *            <code>true</code> will activate it. If the newly created chat
     *            tab is the only one, it will of course be active anyway. If
     *            the chat tab already exists, this parameter has no effect: the
     *            tab will be activated anyway.
     */
    public void openChat(IChat chat, boolean activateAfterCreation) {
        if (selectExistentTab(chat)) {
            return;
        }

        hideExplanation();

        CTabItem chatTab = createChatTab(chat);
        if (activateAfterCreation || chatRooms.getItemCount() == 1) {
            chatRooms.setSelection(chatTab);
        }
    }

    private CTabItem createChatTab(IChat chat) {
        ChatControl control = new ChatControl(this, SWT.BORDER, white, white, 2);

        CTabItem chatTab = new CTabItem(chatRooms, SWT.CLOSE);
        chatTab.setText(chat.getTitle());
        /* Messages.ChatRoomsComposite_roundtable); */
        chatTab.setImage(chatViewImage);
        chatTab.setData(chat);
        chatTab.setControl(control);

        return chatTab;
    }

    private boolean selectExistentTab(IChat chat) {
        for (CTabItem item : chatRooms.getItems()) {
            if (item.getData().equals(chat)) {
                chatRooms.setSelection(item);
                return true;
            }
        }

        return false;
    }

    public void attachToMUCSession(MUCSession mucSession) {
        mucSession.addMUCSessionListener(mucSessionListener);
    }

    public void detachFromMUCSession(MUCSession mucSession) {
        mucSession.removeMUCSessionListener(mucSessionListener);
    }

    /**
     * Adds a new line to the chat control
     * 
     * @param jid
     * @param message
     * @param receivedOn
     */
    protected void addChatLine(final JID jid, final String message,
        final Date receivedOn) {

        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return;

        User user = sarosSession.getUser(jid);
        /*
         * For the case a left user notification came after the moment the user
         * left the session we need to cache his metrics.
         */
        if (user != null) {
            senderCache.put(jid, user.getHumanReadableName());
            colorCache.put(jid, SarosAnnotation.getUserColor(user));
        }
        // add default lightness to cached color
        final String sender = senderCache.get(jid);
        final Color color = ColorUtils.addLightness(colorCache.get(jid),
            SarosAnnotation.getLightnessScale());

        Utils.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                if (chatControl == null || chatControl.isDisposed()
                    || sender == null || color == null)
                    return;
                log.debug("Sender: " + sender); //$NON-NLS-1$
                log.debug("Color: " + color); //$NON-NLS-1$

                chatControl.addChatLine(sender, color, message, receivedOn);
            }
        });
    }

    protected void addChatLine(MUCSessionHistoryJoinElement join) {
        addChatLine(join.getSender(),
            Messages.ChatRoomsComposite_joined_the_chat, join.getDate());
    }

    protected void addChatLine(MUCSessionHistoryLeaveElement leave) {
        addChatLine(leave.getSender(),
            Messages.ChatRoomsComposite_left_the_chat, leave.getDate());
    }

    protected void addChatLine(
        MUCSessionHistoryMessageReceptionElement messageReception) {
        addChatLine(messageReception.getSender(),
            messageReception.getMessage(), messageReception.getDate());
    }

    /**
     * Recreates the {@link ChatControl}s contents on the base of the
     * {@link MUCSessionHistory}
     */
    public void refreshFromHistory() {
        if (this.chatControl == null || this.chatControl.isDisposed()
            || mucManager.getMUCSession() == null)
            return;

        MUCSessionHistoryElement[] entries = mucManager.getMUCSession()
            .getHistory();
        chatControl.silentClear();
        for (MUCSessionHistoryElement element : entries) {
            if (element instanceof MUCSessionHistoryJoinElement)
                addChatLine((MUCSessionHistoryJoinElement) element);
            if (element instanceof MUCSessionHistoryLeaveElement)
                addChatLine((MUCSessionHistoryLeaveElement) element);
            if (element instanceof MUCSessionHistoryMessageReceptionElement)
                addChatLine((MUCSessionHistoryMessageReceptionElement) element);
        }
    }

    /**
     * Returns true if the provided JID equals the one used for connection to
     * {@link MUCManagerSingletonWrapperChatView}.
     * 
     * @param jid
     * @return
     */
    public boolean isOwnJID(JID jid) {
        if (mucManager.getMUCSession() != null) {
            JID localJID = mucManager.getMUCSession().getJID();
            boolean isOwnJID = localJID.equals(jid);
            return isOwnJID;
        }
        return false;
    }

    /**
     * Returns true if the {@link MUCManagerSingletonWrapperChatView} has been
     * joined.
     * 
     * @return
     */
    public boolean joinedSession() {
        return (mucManager.getMUCSession() != null && mucManager
            .getMUCSession().isJoined());
    }

    /**
     * Returns the current chat control for the active chat room
     * 
     * @return the current chat control for the active chat room or
     *         <code>null</code> if there is no active chat room
     */
    public ChatControl getActiveChatControl() {
        return chatControl;
    }

    /**
     * Hides the explanation window and shows a error message instead. Calling
     * this method with a <code>null</code> argument or while no session is
     * running will display the explanation window instead.
     * 
     * @param message
     *            the message to show
     * 
     * @Note must be called within the SWT thread.
     */
    protected void showErrorMessage(String message) {
        assert Utils.isSWT();

        // FIXME there is no dispose method
        // if (chatError != null)
        // chatError.dispose();

        if (!isSessionRunning || message == null) {
            chatError = null;
            showExplanation(howTo);
            return;
        }

        chatError = new ListExplanation(SWT.ICON_ERROR, message);
        showExplanation(chatError);
    }

}

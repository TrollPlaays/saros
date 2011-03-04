package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.feedback.Messages;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;
import de.fu_berlin.inf.dpp.ui.preferencePages.GeneralPreferencePage;

public class SarosPreferencesImp extends Component implements SarosPreferences {

    private static transient SarosPreferencesImp self;

    /**
     * {@link SarosPreferencesImp} is a singleton, but inheritance is possible.
     */
    public static SarosPreferencesImp getInstance() {
        if (self != null)
            return self;
        self = new SarosPreferencesImp();

        return self;
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    public void createAccount(JID jid, String password) throws RemoteException {
        STFBotShell shell_preferences = preCondition();
        shell_preferences
            .bot()
            .buttonInGroup(BUTTON_ADD_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .click();
        bot().waitUntilShellIsOpen(SHELL_SAROS_CONFIGURATION);
        bot().shell(SHELL_SAROS_CONFIGURATION).activate();
        shell_preferences.bot()
            .buttonInGroup(GROUP_TITLE_CREATE_NEW_XMPP_JABBER_ACCOUNT).click();

        bot().waitUntilShellIsOpen(SHELL_CREATE_XMPP_JABBER_ACCOUNT);
        STFBotShell shell = bot().shell(SHELL_CREATE_XMPP_JABBER_ACCOUNT);
        shell.activate();
        sarosBot().confirmShellCreateNewXMPPAccount(jid, password);
        shell.bot().button(NEXT).click();
        shell.bot().button(FINISH).click();
        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        bot().waitUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void addAccount(JID jid, String password) throws RemoteException {
        STFBotShell shell_pref = preCondition();
        shell_pref
            .bot()
            .buttonInGroup(GeneralPreferencePage.ADD_BTN_TEXT,
                GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        sarosBot().confirmWizardAddXMPPJabberAccount(jid, password);
        bot().shell(SHELL_PREFERNCES).bot().button(APPLY).click();
        bot().shell(SHELL_PREFERNCES).bot().button(OK).click();
        bot().waitUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void activateAccount(JID jid) throws RemoteException {
        assert existsAccount(jid) : "the account (" + jid.getBase()
            + ") doesn't exist yet!";
        if (sarosBot().state().isAccountActiveNoGUI(jid))
            return;
        STFBotShell shell = preCondition();
        shell.bot().listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE)
            .select(jid.getBase());

        shell
            .bot()
            .buttonInGroup(GeneralPreferencePage.ACTIVATE_BTN_TEXT,
                GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        assert shell.bot().existsLabel("Active: " + jid.getBase());
        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        bot().waitUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void changeAccount(JID jid, String newXmppJabberID,
        String newPassword) throws RemoteException {
        STFBotShell shell = preCondition();
        shell.bot().listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE)
            .select(jid.getBase());

        shell
            .bot()
            .buttonInGroup(GeneralPreferencePage.CHANGE_BTN_TEXT,
                GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        sarosBot().confirmShellChangeXMPPAccount(newXmppJabberID, newPassword);
        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        bot().waitUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void deleteAccount(JID jid, String password) throws RemoteException {
        if (!sarosBot().state().isAccountExistNoGUI(jid, password))
            return;
        STFBotShell shell = preCondition();
        shell.bot().listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE)
            .select(jid.getBase());

        shell
            .bot()
            .buttonInGroup(GeneralPreferencePage.DELETE_BTN_TEXT,
                GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        if (sarosBot().state().isAccountActiveNoGUI(jid)) {
            bot().waitUntilShellIsOpen(SHELL_DELETING_ACTIVE_ACCOUNT);
            bot().shell(SHELL_DELETING_ACTIVE_ACCOUNT).activate();
            assert bot().shell(SHELL_DELETING_ACTIVE_ACCOUNT).isActive();
            throw new RuntimeException(
                "It's not allowd to delete a active account");
        }
        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        bot().waitUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void deleteAllNoActiveAccounts() throws RemoteException {
        STFBotShell shell = preCondition();
        String[] items = shell.bot()
            .listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getItems();
        for (String item : items) {
            shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
                .select(item);
            shell
                .bot()
                .buttonInGroup(GeneralPreferencePage.DELETE_BTN_TEXT,
                    GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
            if (bot().isShellOpen(SHELL_DELETING_ACTIVE_ACCOUNT)
                && bot().shell(SHELL_DELETING_ACTIVE_ACCOUNT).isActive()) {
                bot().shell(SHELL_DELETING_ACTIVE_ACCOUNT).bot().button(OK)
                    .click();
                continue;
            }
        }
        assert shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .itemCount() == 1;
        bot().shell(SHELL_PREFERNCES).bot().button(APPLY).click();
        bot().shell(SHELL_PREFERNCES).bot().button(OK).click();
        bot().waitUntilShellIsClosed(SHELL_PREFERNCES);
        bot().sleep(300);
    }

    public void setupSettingForScreensharing(int encoder, int videoResolution,
        int bandWidth, int capturedArea) throws RemoteException {
        clickMenuSarosPreferences();
        bot().waitUntilShellIsOpen(SHELL_PREFERNCES);
        STFBotShell shell = bot().shell(SHELL_PREFERNCES);
        shell.activate();

        shell.bot().tree().selectTreeItem(NODE_SAROS, NODE_SAROS_SCREENSHARING);
        shell.bot().ccomboBox(0).setSelection(encoder);
        shell.bot().ccomboBox(1).setSelection(videoResolution);

        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        bot().waitUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void disableAutomaticReminder() throws RemoteException {
        if (feedbackManager.isFeedbackDisabled()) {
            clickMenuSarosPreferences();
            bot().waitUntilShellIsOpen(SHELL_PREFERNCES);
            STFBotShell shell = bot().shell(SHELL_PREFERNCES);
            shell.activate();
            shell.bot().tree().selectTreeItem(NODE_SAROS, NODE_SAROS_FEEDBACK);
            shell
                .bot()
                .radioInGroup(
                    Messages.getString("feedback.page.radio.disable"),
                    Messages.getString("feedback.page.group.interval")).click();
            shell.bot().button(APPLY).click();
            shell.bot().button(OK).click();
            bot().waitUntilShellIsClosed(SHELL_PREFERNCES);
        }
    }

    public boolean existsAccount() throws RemoteException {
        STFBotShell shell = preCondition();
        String[] items = shell.bot()
            .listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getItems();
        if (items == null || items.length == 0)
            return false;
        else
            return true;
    }

    public boolean existsAccount(JID jid) throws RemoteException {
        STFBotShell shell = preCondition();

        String[] items = shell.bot()
            .listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getItems();
        for (String item : items) {
            if ((jid.getBase()).equals(item)) {
                shell.bot().button(CANCEL).click();
                return true;
            }
        }
        shell.bot().button(CANCEL).click();
        bot().waitUntilShellIsClosed(SHELL_PREFERNCES);
        return false;
    }

    public boolean isAccountActive(JID jid) throws RemoteException {

        STFBotShell shell = preCondition();
        String activeAccount = shell.bot()
            .labelInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getText();
        boolean isActive = false;
        if (activeAccount.equals("Active: " + jid.getBase()))
            isActive = true;
        shell.bot().button(CANCEL).click();
        bot().waitUntilShellIsClosed(SHELL_PREFERNCES);
        return isActive;
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     *************************************************************/
    /**
     * This is a convenient function to show the right setting-page of saros
     * item in the preferences dialog.
     */
    private STFBotShell preCondition() throws RemoteException {
        clickMenuSarosPreferences();
        bot().waitUntilShellIsOpen(SHELL_PREFERNCES);
        STFBotShell shell = bot().shell(SHELL_PREFERNCES);
        shell.activate();
        shell.bot().tree().selectTreeItem(NODE_SAROS);
        return shell;
    }

    /**
     * click the main menu Saros-> Preferences
     * 
     * @throws RemoteException
     */
    private void clickMenuSarosPreferences() throws RemoteException {
        bot().activateWorkbench();
        bot().menu(MENU_SAROS).menu(MENU_PREFERENCES).click();
    }

}
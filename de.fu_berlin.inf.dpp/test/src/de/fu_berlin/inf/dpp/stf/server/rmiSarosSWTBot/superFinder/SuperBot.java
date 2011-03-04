package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.AbstractTester;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.ShareWithC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.MenuBar;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.Views;

public interface SuperBot extends Remote {

    /**********************************************
     * 
     * finders
     * 
     **********************************************/
    public State state() throws RemoteException;

    public Wait condition() throws RemoteException;

    public Views views() throws RemoteException;

    public MenuBar menuBar() throws RemoteException;

    public void setJID(JID jid) throws RemoteException;

    /**
     * After the {@link ShareWithC#confirmShellAddBuddyToSession(String...)} the
     * popup wizard with the title "Session Invitation" should be appeared by
     * the invitees' side. This method confirm the wizard "Session Invitation"
     * using a new project.
     * 
     * @throws RemoteException
     */
    public void confirmShellAddProjectWithNewProject(String projectname)
        throws RemoteException;

    /**
     * After the {@link ShareWithC#confirmShellAddBuddyToSession(String...)} the
     * popup wizard with the title "Session Invitation" should be appeared by
     * the invitees' side. This method confirm the wizard "Session Invitation"
     * using a exist project.
     * 
     * @throws RemoteException
     */
    public void confirmShellAddProjectUsingExistProject(String projectName)
        throws RemoteException;

    /**
     * After the {@link ShareWithC#confirmShellAddBuddyToSession(String...)} the
     * popup wizard with the title "Session Invitation" should be appeared by
     * the invitees' side. This method confirm the wizard "Session Invitation"
     * using a exist project with clicking the button browser->confirming popup
     * window -> clicking the button "finish" -> conforming the local change
     * 
     * @throws RemoteException
     */
    public void confirmShellAddProjectUsingExistProjectWithCopyAfterCancelLocalChange(
        String projectName) throws RemoteException;

    /**
     * After the {@link ShareWithC#confirmShellAddBuddyToSession(String...)} the
     * popup wizard with the title "Session Invitation" should be appeared by
     * the invitees' side. This method confirm the wizard "Session Invitation"
     * using a exist project with copy
     * 
     * @throws RemoteException
     */
    public void confirmShellAddProjectUsingExistProjectWithCopy(
        String projectName) throws RemoteException;

    /**
     * After the {@link ShareWithC#confirmShellAddBuddyToSession(String...)} the
     * popup wizard with the title "Session Invitation" should be appeared by
     * the invitees' side. This method confirm the wizard "Session Invitation"
     * using a new project or a existed project according the passed parameter
     * "usingWhichProject".
     * 
     * @throws RemoteException
     */
    public void confirmShellAddProjectUsingWhichProject(String projectName,
        TypeOfCreateProject usingWhichProject) throws RemoteException;

    public void confirmShellChangeXMPPAccount(String xmppJabberID,
        String newPassword) throws RemoteException;

    /**
     * Confirm the popUp window "create new XMPP account".
     * 
     * @param jid
     *            jid of the new XMPP account
     * @param password
     *            password of the new XMPP account
     * @throws RemoteException
     */
    public void confirmShellCreateNewXMPPAccount(JID jid, String password)
        throws RemoteException;

    /**
     * confirm the wizard "Saros configuration".
     * 
     * @param jid
     *            jid of the new XMPP account
     * @param password
     *            password of the new XMPP account
     * @throws RemoteException
     */
    public void confirmWizardAddXMPPJabberAccount(JID jid, String password)
        throws RemoteException;

    /**
     * After clicking one of the sub menu of the context menu "Saros" in the
     * package explorer view host will get the popup window with the title
     * "Invitation". This method confirm the popup window.
     * 
     * @param inviteesBaseJIDs
     *            the base JID of the users with whom you want to share your
     *            project.
     * @throws RemoteException
     */
    public void confirmShellAddBuddyToSession(String... inviteesBaseJIDs)
        throws RemoteException;

    /**
     * Confirm the popUp window "Closing the session", which would be triggered,
     * when host try to leave a session.
     * 
     * @throws RemoteException
     */
    public void confirmShellClosingTheSession() throws RemoteException;

    /**
     * This popup window should be appeared by you, after someone else deleted
     * your contact from his buddies.
     * 
     * This method should be called by
     * {@link AbstractTester#deleteBuddyGUIDone(AbstractTester)}
     * 
     * @throws RemoteException
     */
    public void confirmShellRemovelOfSubscription() throws RemoteException;

    public void confirmShellAddBuddy(JID jid) throws RemoteException;

    public void confirmWizardShareProject(String projectName, JID... jids)
        throws RemoteException;

    public void confirmShellSessionInvitationAndAddProject(String projectName,
        TypeOfCreateProject usingWhichProject) throws RemoteException;

}
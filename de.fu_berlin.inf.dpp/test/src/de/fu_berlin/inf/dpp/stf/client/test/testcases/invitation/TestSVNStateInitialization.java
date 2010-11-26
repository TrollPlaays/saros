package de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.MusicianConfigurationInfos;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

/**
 * Tests for the initial synchronization of the SVN state during the invitation.
 */
public class TestSVNStateInitialization extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Alice has the project {@link STFTest#SVN_PROJECT_COPY}, which is
     * checked out from SVN</li>
     * <li>repository: {@link STFTest#SVN_REPOSITORY_URL}</li>
     * <li>path: {@link STFTest#SVN_PROJECT_PATH}</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void initMusicians() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        if (!alice.pEV.isProjectExist(SVN_PROJECT_COPY)) {
            alice.pEV.newJavaProject(SVN_PROJECT_COPY);
            alice.pEV.shareProjectWithSVNUsingSpecifiedFolderName(
                SVN_PROJECT_COPY, SVN_REPOSITORY_URL, SVN_PROJECT_PATH);
        }
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        if (MusicianConfigurationInfos.DEVELOPMODE) {
            alice.rosterV.disconnect();
            // don't delete SVN_PROJECT_COPY
        } else {
            alice.workbench.resetSaros();
        }
    }

    /**
     * Preconditions:
     * <ol>
     * <li>Alice copied {@link STFTest#SVN_PROJECT_COPY} to
     * {@link STFTest#SVN_PROJECT}.</li>
     * </ol>
     * Only SVN_PROJECT is used in the tests. Copying from SVN_PROJECT_COPY is
     * faster than checking out the project for every test.
     * 
     * @throws RemoteException
     */
    @Before
    public void setUp() throws RemoteException {
        alice.pEV.copyProject(SVN_PROJECT, SVN_PROJECT_COPY);
        assertTrue(alice.pEV.isProjectExist(SVN_PROJECT));
        assertTrue(alice.pEV.isProjectManagedBySVN(SVN_PROJECT));
        assertTrue(alice.pEV.isFileExist(SVN_CLS1_FULL_PATH));
    }

    @After
    public void tearDown() throws RemoteException {
        bob.sessionV.leaveTheSessionByPeer();
        bob.workbench.resetWorkbench();
        if (alice.pEV.isProjectExist(SVN_PROJECT))
            alice.pEV.deleteProject(SVN_PROJECT);
        bob.state.deleteAllProjects();

        alice.sessionV.leaveTheSessionByHost();
        alice.workbench.resetWorkbench();
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice shares project SVN_PROJECT with Bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of SVN_PROJECT is managed with SVN.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testSimpleCheckout() throws RemoteException {
        alice.shareProjectWithDone(SVN_PROJECT,
            CONTEXT_MENU_SHARE_PROJECT_WITH_VCS, bob);
        alice.sessionV.waitUntilSessionOpenBy(bob.state);
        assertTrue(bob.pEV.isProjectManagedBySVN(SVN_PROJECT));

        assertTrue(alice.state.isDriver(alice.jid));
        assertTrue(alice.state.isParticipant(bob.jid));
        assertTrue(bob.state.isObserver(bob.jid));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice updates {@link STFTest#SVN_CLS1} to revision
     * {@link STFTest#SVN_CLS1_REV2}.</li>
     * <li>Alice shares project {@link STFTest#SVN_PROJECT} with Bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of {@link STFTest#SVN_PROJECT} is managed with SVN.</li>
     * <li>Bob's copy of {@link STFTest#SVN_CLS1} has revision
     * {@link STFTest#SVN_CLS1_REV2}.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testCheckoutWithUpdate() throws RemoteException {
        alice.pEV.updateClass(SVN_PROJECT, SVN_PKG, SVN_CLS1, SVN_CLS1_REV2);
        alice.shareProjectWithDone(SVN_PROJECT,
            CONTEXT_MENU_SHARE_PROJECT_WITH_VCS, bob);
        alice.sessionV.waitUntilSessionOpenBy(bob.state);

        assertTrue(bob.pEV.isProjectManagedBySVN(SVN_PROJECT));
        assertEquals(bob.pEV.getRevision(SVN_CLS1_FULL_PATH), SVN_CLS1_REV2);
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice switches {@link STFTest#SVN_CLS1} to
     * {@link STFTest#SVN_CLS1_SWITCHED_URL}.</li>
     * <li>Alice shares project {@link STFTest#SVN_PROJECT} with Bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of {@link STFTest#SVN_PROJECT} is managed with SVN.</li>
     * <li>Bob's copy of {@link STFTest#SVN_CLS1} is switched to
     * {@link STFTest#SVN_CLS1_SWITCHED_URL}.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testCheckoutWithSwitch() throws RemoteException {
        alice.pEV.switchResource(SVN_CLS1_FULL_PATH, SVN_CLS1_SWITCHED_URL);
        alice.shareProjectWithDone(SVN_PROJECT,
            CONTEXT_MENU_SHARE_PROJECT_WITH_VCS, bob);
        alice.sessionV.waitUntilSessionOpenBy(bob.state);
        bob.sessionV.waitUntilSessionOpen();

        assertTrue(bob.pEV.isProjectManagedBySVN(SVN_PROJECT));
        assertEquals(SVN_CLS1_SWITCHED_URL,
            bob.pEV.getURLOfRemoteResource(SVN_CLS1_FULL_PATH));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice modifies her working copy by changing the content of the file
     * {@link STFTest#SVN_CLS1} to {@link STFTest#CP1}.</li>
     * <li>Alice shares project {@link STFTest#SVN_PROJECT} with Bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of {@link STFTest#SVN_PROJECT} is managed with SVN.</li>
     * <li>Bob's copy of {@link STFTest#SVN_CLS1} has the same content as
     * Alice's copy.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testCheckoutWithModification() throws RemoteException {
        String cls1_content_before = alice.editor.getTextOfJavaEditor(
            SVN_PROJECT, SVN_PKG, SVN_CLS1);
        alice.editor.setTextInJavaEditorWithSave(CP1, SVN_PROJECT, SVN_PKG,
            SVN_CLS1);
        String cls1_content_after = alice.editor.getTextOfJavaEditor(
            SVN_PROJECT, SVN_PKG, SVN_CLS1);
        assertFalse(cls1_content_after.equals(cls1_content_before));

        alice.shareProjectWithDone(SVN_PROJECT,
            CONTEXT_MENU_SHARE_PROJECT_WITH_VCS, bob);
        alice.sessionV.waitUntilSessionOpenBy(bob.state);
        bob.sessionV.waitUntilSessionOpen();

        assertTrue(bob.pEV.isProjectManagedBySVN(SVN_PROJECT));
        assertEquals(cls1_content_after,
            bob.editor.getTextOfJavaEditor(SVN_PROJECT, SVN_PKG, SVN_CLS1));
    }

}
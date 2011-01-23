package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.basicElements;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;
import de.fu_berlin.inf.dpp.ui.GeneralPreferencePage;

public class TestBasicComponentByAlice extends STFTest {
    private static final Logger log = Logger
        .getLogger(TestBasicComponentByAlice.class);

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbenchs();
    }

    @AfterClass
    public static void runAfterClass() {
        //
    }

    @Before
    public void runBeforeEveryTest() {
        //
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        deleteProjectsByActiveTesters();
    }

    @Test
    public void existsTreeItemInShell() throws RemoteException {
        alice.menu.clickMenuWithTexts("Window", "Show View", "Other...");
        alice.shell.activateShellWithWaitingOpen("Show View");
        assertTrue(alice.tree.existsSubItemInTreeItem("Console", "General"));
    }

    @Test
    public void existsTreeItemInShell2() throws RemoteException {
        alice.menu.clickMenuWithTexts("Saros", "Preferences");
        alice.shell.activateShellWithWaitingOpen("Preferences");
        assertTrue(alice.tree.existsSubItemInTreeItem("Annotations", "General",
            "Editors", "Text Editors"));
    }

    @Test
    public void existsTreeItemWithRegexsInShell() throws RemoteException {
        alice.menu.clickMenuWithTexts("Saros", "Preferences");
        alice.shell.activateShellWithWaitingOpen("Preferences");
        assertTrue(alice.tree.existsTreeItemWithRegexs("General", "Editors",
            "Text Editors", "Annotations"));
    }

    @Test
    public void existsTreeItemInView() throws RemoteException {
        alice.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.pEV.setFocusOnPEView();
        assertTrue(alice.tree.existsTreeItemInTreeInView("Package Explorer",
            PROJECT1));
        assertTrue(alice.fileM.existsProject(PROJECT1));
        assertTrue(alice.tree.existsSubItemInTreeItemInView("Package Explorer",
            CLS1 + ".java", PROJECT1, SRC, PKG1));
        alice.fileM.existsClass(PROJECT1, PKG1, CLS1);
    }

    @Test
    public void existsTreeItemWithRegexsInView() throws RemoteException {
        alice.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.pEV.setFocusOnPEView();
        assertTrue(alice.tree.existsTreeItemWithRegexsInView(
            "Package Explorer", PROJECT1));
        assertTrue(alice.fileM.existsProject(PROJECT1));
        assertTrue(alice.tree.existsTreeItemWithRegexsInView(
            "Package Explorer", PROJECT1, SRC, PKG1));
        assertTrue(alice.fileM.existsPkg(PROJECT1, PKG1));
        assertTrue(alice.tree.existsTreeItemWithRegexsInView(
            "Package Explorer", PROJECT1, SRC, PKG1, CLS1 + ".*"));
        assertTrue(alice.fileM.existsClass(PROJECT1, PKG1, CLS1));
        alice.fileM.newClass(PROJECT1, PKG1, CLS2);
        assertTrue(alice.tree.existsTreeItemWithRegexsInView(
            "Package Explorer", PROJECT1, SRC, PKG1, CLS2 + ".*"));
        assertTrue(alice.fileM.existsClass(PROJECT1, PKG1, CLS2));
    }

    @Test
    public void existsTreeItemWithRegexsInView2() throws RemoteException {
        alice.rosterV.connect(alice.jid, alice.password);
        assertTrue(alice.tree.existsTreeItemWithRegexsInView("Roster",
            "Buddies"));
        assertTrue(alice.tree.existsTreeItemWithRegexsInView("Roster",
            "Buddies", "bob_stf.*"));
    }

    @Test
    public void selectTreeItemInView() throws RemoteException {
        alice.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.pEV.setFocusOnPEView();
        assertTrue(alice.editor.isJavaEditorOpen(CLS1));
        alice.toolbarButton.clickToolbarButtonWithRegexTooltipInView(
            "Package Explorer", "Collapse All");
        alice.tree.selectTreeItemInView("Package Explorer", PROJECT1, SRC,
            PKG1, CLS1 + ".java");
        alice.menu.clickMenuWithTexts("File", "Close");
        assertFalse(alice.editor.isJavaEditorOpen(CLS1));
    }

    @Test
    public void selectTreeItemInShell() throws RemoteException {
        alice.menu.clickMenuWithTexts("Saros", "Preferences");
        alice.shell.activateShellWithWaitingOpen("Preferences");
        alice.tree.selectTreeItem("Saros");
        assertTrue(alice.button.existsButtonInGroup(
            GeneralPreferencePage.CHANGE_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE));
    }

    /**
     * To select a node with given regexs in a view, you should need to use the
     * method selectTreeItemWithRegexsInView, which use
     * bot.getViewByTitle("view title").bot().tree() instead of using
     * bot.tree().
     * 
     * @throws RemoteException
     */
    @Test(expected = WidgetNotFoundException.class)
    public void selectTreeItemWithRegexs() throws RemoteException {
        alice.fileM.newJavaProject(SVN_PROJECT_COPY);
        alice.team.shareProjectWithSVNUsingSpecifiedFolderName(
            SVN_PROJECT_COPY, SVN_REPOSITORY_URL, SVN_PROJECT_PATH);
        alice.pEV.setFocusOnPEView();
        alice.tree.selectTreeItemWithRegexs(changeToRegex(getClassNodes(
            SVN_PROJECT_COPY, "pkg", "Test")));
    }

    @Test
    public void selectTreeItemWithRegexsInView() throws RemoteException {
        alice.fileM.newJavaProject(SVN_PROJECT_COPY);
        alice.team.shareProjectWithSVNUsingSpecifiedFolderName(
            SVN_PROJECT_COPY, SVN_REPOSITORY_URL, SVN_PROJECT_PATH);
        alice.pEV.setFocusOnPEView();
        alice.pEV.openClass(SVN_PROJECT_COPY, "pkg", "Test");
        alice.editor.setTextInJavaEditorWithoutSave(CP1, SVN_PROJECT_COPY,
            "pkg", "Test");
        assertTrue(alice.editor.isClassDirty(SVN_PROJECT_COPY, "pkg", "Test",
            ID_JAVA_EDITOR));
        alice.toolbarButton.clickToolbarButtonWithRegexTooltipInView(
            "Package Explorer", "Collapse All");
        alice.tree.selectTreeItemWithRegexsInView("Package Explorer",
            changeToRegex(getClassNodes(SVN_PROJECT_COPY, "pkg", "Test")));
        alice.menu.clickMenuWithTexts("File", "Save");
        assertFalse(alice.editor.isClassDirty(SVN_PROJECT_COPY, "pkg", "Test",
            ID_JAVA_EDITOR));
    }

    @Test
    public void existsContextOfTreeItemInView() throws RemoteException {
        alice.rosterV.connect(alice.jid, alice.password);
        assertTrue(alice.tree.existsContextMenuOfTreeItemInView("Roster",
            "Rename...", "Buddies", "bob_stf.*"));
    }

    @Test
    public void existsSubmenuOfContextOfTreeItemInView() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        String[] contextNames = { "Saros", "Share project..." };
        assertTrue(alice.tree.existsContextMenusOfTreeItemInView(
            "Package Explorer", contextNames, PROJECT1));
    }

    @Test
    public void isContextOfTreeItemInViewEnabled() throws RemoteException {
        alice.rosterV.connect(alice.jid, alice.password);
        assertTrue(alice.tree.isContextMenuOfTreeItemInViewEnabled("Roster",
            "Rename...", "Buddies", "bob_stf.*"));
        assertFalse(alice.tree.isContextMenuOfTreeItemInViewEnabled("Roster",
            "Invite user...", "Buddies", "bob_stf.*"));
    }

    @Test
    public void isSubmenuOfContextOfTreeItemInViewEnabled()
        throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        String[] contextNames1 = { "Saros", "Share project..." };
        String[] contextNames2 = { "Saros", "Add to session (experimental)..." };
        assertTrue(alice.tree.isContextMenusOfTreeItemInViewEnabled(
            "Package Explorer", contextNames1, PROJECT1));
        assertFalse(alice.tree.isContextMenusOfTreeItemInViewEnabled(
            "Package Explorer", contextNames2, PROJECT1));
    }

    @Test
    public void clickContextsOfTreeItemInView() throws RemoteException {
        alice.rosterV.connect(alice.jid, alice.password);
        alice.tree.clickContextMenuOfTreeItemInView("Roster", "Rename...",
            "Buddies", "bob_stf.*");
        assertTrue(alice.shell.isShellOpen("Set new nickname"));
    }

    @Test
    public void clickSubMenuOfContextsOfTreeItemInView() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        String[] contextNames1 = { "Saros", "Share project..." };

        alice.tree.clickContextMenusOfTreeItemInView("Package Explorer",
            contextNames1, PROJECT1);
        assertTrue(alice.shell.activateShellWithWaitingOpen("Invitation"));
    }

}
package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.basicWidgets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;
import de.fu_berlin.inf.dpp.ui.GeneralPreferencePage;

public class TestBasicWidgetTree extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbench();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        deleteAllProjectsByActiveTesters();
    }

    @Test
    public void existsTreeItemInShell() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_WINDOW, MENU_SHOW_VIEW, MENU_OTHER);
        alice.shell.activateShellWithWaitingOpen(SHELL_SHOW_VIEW);
        assertTrue(alice.commonWidgets().shell(SHELL_SHOW_VIEW).bot().tree()
            .selectTreeItem(NODE_GENERAL).existsSubItem(NODE_CONSOLE));

    }

    @Test
    public void existsTreeItemInShell2() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_SAROS, MENU_PREFERENCES);
        alice.shell.activateShellWithWaitingOpen(SHELL_PREFERNCES);
        assertTrue(alice.commonWidgets().shell(SHELL_PREFERNCES).bot().tree()
            .selectTreeItem(NODE_GENERAL, NODE_EDITORS, NODE_TEXT_EDITORS)
            .existsSubItem(NODE_ANNOTATIONS));
    }

    @Test
    public void existsTreeItemWithRegexsInShell() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_SAROS, MENU_PREFERENCES);
        alice.shell.activateShellWithWaitingOpen(SHELL_PREFERNCES);
        assertTrue(alice.commonWidgets().shell(SHELL_PREFERNCES).bot().tree()
            .selectTreeItem(NODE_GENERAL, NODE_EDITORS, NODE_TEXT_EDITORS)
            .existsSubItemWithRegex(NODE_ANNOTATIONS));
    }

    @Test
    public void existsTreeItemInView() throws RemoteException {
        alice.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        alice.commonWidgets().view(VIEW_PACKAGE_EXPLORER).setFocus();
        assertTrue(alice.commonWidgets().view(VIEW_PACKAGE_EXPLORER).bot()
            .tree().existsSubItem(PROJECT1));

        assertTrue(alice.fileM.existsProjectNoGUI(PROJECT1));
        assertTrue(alice.commonWidgets().view(VIEW_PACKAGE_EXPLORER).bot()
            .tree().selectTreeItem(PROJECT1, SRC, PKG1)
            .existsSubItem(CLS1 + SUFFIX_JAVA));
        alice.fileM.existsClassNoGUI(PROJECT1, PKG1, CLS1);
    }

    @Test
    public void existsTreeItemWithRegexsInView() throws RemoteException {
        alice.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        alice.commonWidgets().view(VIEW_PACKAGE_EXPLORER).setFocus();

        assertTrue(alice.commonWidgets().view(VIEW_PACKAGE_EXPLORER).bot()
            .tree().existsSubItemWithRegexs(changeToRegex(PROJECT1)));

        assertTrue(alice.fileM.existsProjectNoGUI(PROJECT1));

        assertTrue(alice.commonWidgets().view(VIEW_PACKAGE_EXPLORER).bot()
            .tree().selectTreeItem(PROJECT1, SRC)
            .existsSubItemWithRegex(changeToRegex(PKG1)));

        assertTrue(alice.fileM.existsPkgNoGUI(PROJECT1, PKG1));

        assertTrue(alice.commonWidgets().view(VIEW_PACKAGE_EXPLORER).bot()
            .tree().selectTreeItem(PROJECT1, SRC, PKG1)
            .existsSubItemWithRegex(changeToRegex(CLS1)));

        assertTrue(alice.fileM.existsClassNoGUI(PROJECT1, PKG1, CLS1));
        alice.fileM.newClass(PROJECT1, PKG1, CLS2);

        assertTrue(alice.commonWidgets().view(VIEW_PACKAGE_EXPLORER).bot()
            .tree().selectTreeItem(PROJECT1, SRC, PKG1)
            .existsSubItemWithRegex(changeToRegex(CLS2)));

        assertTrue(alice.fileM.existsClassNoGUI(PROJECT1, PKG1, CLS2));
    }

    @Test
    public void existsTreeItemWithRegexsInView2() throws RemoteException {
        alice.sarosBuddiesV.connectNoGUI(alice.jid, alice.password);
        assertTrue(alice.commonWidgets().view(VIEW_SAROS_BUDDIES).bot().tree()
            .existsSubItemWithRegexs(changeToRegex(NODE_BUDDIES)));
        assertTrue(alice.commonWidgets().view(VIEW_SAROS_BUDDIES).bot().tree()
            .selectTreeItem(NODE_BUDDIES)
            .existsSubItemWithRegex(changeToRegex("bob_stf")));
    }

    @Test
    public void selectTreeItemInView() throws RemoteException {
        alice.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        alice.commonWidgets().view(VIEW_PACKAGE_EXPLORER).setFocus();
        assertTrue(alice.editor.isJavaEditorOpen(CLS1));
        alice.toolbarButton.clickToolbarButtonWithRegexTooltipOnView(
            VIEW_PACKAGE_EXPLORER, TB_COLLAPSE_ALL);
        alice.commonWidgets().view(VIEW_PACKAGE_EXPLORER).bot().tree()
            .selectTreeItem(PROJECT1, SRC, PKG1, CLS1 + SUFFIX_JAVA);
        alice.menu.clickMenuWithTexts(MENU_FILE, MENU_CLOSE);
        assertFalse(alice.editor.isJavaEditorOpen(CLS1));
    }

    @Test
    public void selectTreeItemInShell() throws RemoteException {
        alice.menu.clickMenuWithTexts(MENU_SAROS, MENU_PREFERENCES);
        alice.shell.activateShellWithWaitingOpen(SHELL_PREFERNCES);
        alice.commonWidgets().shell(SHELL_PREFERNCES).bot().tree()
            .selectTreeItem(NODE_SAROS);
        assertTrue(alice.button.existsButtonInGroup(
            GeneralPreferencePage.CHANGE_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE));
    }

    @Test
    public void selectTreeItemWithRegexs() throws RemoteException {
        alice.fileM.newJavaProject(SVN_PROJECT_COPY);
        alice.team.shareProjectWithSVNUsingSpecifiedFolderName(
            VIEW_PACKAGE_EXPLORER, SVN_PROJECT_COPY, SVN_REPOSITORY_URL,
            SVN_PROJECT_PATH);
        alice.commonWidgets().view(VIEW_PACKAGE_EXPLORER).setFocus();
        alice
            .commonWidgets()
            .view(VIEW_PACKAGE_EXPLORER)
            .bot()
            .tree()
            .selectTreeItemWithRegex(
                changeToRegex(getClassNodes(SVN_PROJECT_COPY, SVN_PKG, SVN_CLS1)));
    }

    @Test
    public void selectTreeItemWithRegexsInView() throws RemoteException {
        alice.fileM.newJavaProject(SVN_PROJECT_COPY);
        alice.team.shareProjectWithSVNUsingSpecifiedFolderName(
            VIEW_PACKAGE_EXPLORER, SVN_PROJECT_COPY, SVN_REPOSITORY_URL,
            SVN_PROJECT_PATH);
        alice.commonWidgets().view(VIEW_PACKAGE_EXPLORER).setFocus();
        alice.openC.openClass(VIEW_PACKAGE_EXPLORER, SVN_PROJECT_COPY, SVN_PKG,
            SVN_CLS1);
        alice.editor.setTextInEditorWithoutSave(CP1, SVN_CLS1_SUFFIX);
        assertTrue(alice.editor.isClassDirty(SVN_PROJECT_COPY, SVN_PKG,
            SVN_CLS1, ID_JAVA_EDITOR));
        alice.toolbarButton.clickToolbarButtonWithRegexTooltipOnView(
            VIEW_PACKAGE_EXPLORER, TB_COLLAPSE_ALL);

        alice
            .commonWidgets()
            .view(VIEW_PACKAGE_EXPLORER)
            .bot()
            .tree()
            .selectTreeItemWithRegex(
                changeToRegex(getClassNodes(SVN_PROJECT_COPY, SVN_PKG, SVN_CLS1)));
        alice.menu.clickMenuWithTexts(MENU_FILE, MENU_SAVE);
        assertFalse(alice.editor.isClassDirty(SVN_PROJECT_COPY, SVN_PKG,
            SVN_CLS1, ID_JAVA_EDITOR));
    }

    @Test
    public void existsContextOfTreeItemInView() throws RemoteException {
        alice.sarosBuddiesV.connectNoGUI(alice.jid, alice.password);
        assertTrue(alice.commonWidgets().view(VIEW_SAROS_BUDDIES).bot().tree()
            .selectTreeItemWithRegex(NODE_BUDDIES, "bob_stf.*")
            .existsContextMenu(CM_RENAME));
    }

    @Test
    public void existsSubmenuOfContextOfTreeItemInView() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        String[] contextNames = { CM_SAROS, CM_SHARE_PROJECT };
        assertTrue(alice.commonWidgets().view(VIEW_PACKAGE_EXPLORER).bot()
            .tree().selectTreeItem(PROJECT1).existsContextMenu(contextNames));

    }

    @Test
    public void isContextOfTreeItemInViewEnabled() throws RemoteException {
        alice.sarosBuddiesV.connectNoGUI(alice.jid, alice.password);
        assertTrue(alice.commonWidgets().view(VIEW_SAROS_BUDDIES).bot().tree()
            .selectTreeItemWithRegex(NODE_BUDDIES, "bob_stf.*")
            .isContextMenuEnabled(CM_RENAME));

        assertFalse(alice.commonWidgets().view(VIEW_SAROS_BUDDIES).bot().tree()
            .selectTreeItemWithRegex(NODE_BUDDIES, "bob_stf.*")
            .isContextMenuEnabled(CM_INVITE_BUDDY));
    }

    @Test
    public void isSubmenuOfContextOfTreeItemInViewEnabled()
        throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        String[] contextNames1 = { CM_SAROS, CM_SHARE_PROJECT };
        String[] contextNames2 = { CM_SAROS, CM_ADD_TO_SESSION };
        assertTrue(alice.commonWidgets().view(VIEW_PACKAGE_EXPLORER).bot()
            .tree().selectTreeItem(PROJECT1)
            .isContextMenuEnabled(contextNames1));

        assertFalse(alice.commonWidgets().view(VIEW_PACKAGE_EXPLORER).bot()
            .tree().selectTreeItem(PROJECT1)
            .isContextMenuEnabled(contextNames2));
    }

    @Test
    public void clickContextsOfTreeItemInView() throws RemoteException {
        alice.sarosBuddiesV.connectNoGUI(alice.jid, alice.password);
        alice.commonWidgets().view(VIEW_SAROS_BUDDIES).bot().tree()
            .selectTreeItemWithRegex(NODE_BUDDIES, "bob_stf.*")
            .contextMenu(CM_RENAME).click();
        assertTrue(alice.shell.isShellOpen(SHELL_SET_NEW_NICKNAME));
    }

    @Test
    public void clickSubMenuOfContextsOfTreeItemInView() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);

        alice.commonWidgets().view(VIEW_PACKAGE_EXPLORER).bot().tree()
            .selectTreeItem(PROJECT1).contextMenu(CM_SAROS, CM_SHARE_PROJECT)
            .click();

        assertTrue(alice.shell.activateShellWithWaitingOpen(SHELL_INVITATION));
    }
}

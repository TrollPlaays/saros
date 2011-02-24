package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface STFBotTable extends Remote {
    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean existsTableItem(String itemText) throws RemoteException;

    public List<String> getTableColumns() throws RemoteException;

    public boolean containsItem(String item) throws RemoteException;

    /**********************************************
     * 
     * finder
     * 
     **********************************************/

    public STFBotMenu contextMenu(String text) throws RemoteException;

    public STFBotTableItem getTableItem(String itemText) throws RemoteException;

    public STFBotTableItem getTableItem(int row) throws RemoteException;

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void select(String... items) throws RemoteException;

    public void click(int row, int column) throws RemoteException;

    public void unselect() throws RemoteException;

    public void selectionCount() throws RemoteException;

    public void check(int row, int column) throws RemoteException;

    public void check(int row, String columnName) throws RemoteException;

    public void setFocus() throws RemoteException;

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public int indexOfColumn(String column) throws RemoteException;

    public int indexOf(String item) throws RemoteException;

    public int indexOf(String item, int column) throws RemoteException;

    public int indexOf(String item, String column) throws RemoteException;

    public int rowCount() throws RemoteException;

    public int columnCount() throws RemoteException;

    public List<String> columns() throws RemoteException;

    public boolean existsContextMenu(String contextName) throws RemoteException;

    public boolean isEnabled() throws RemoteException;

    public boolean isVisible() throws RemoteException;

    public boolean isActive() throws RemoteException;

    public String getText() throws RemoteException;

    public String getToolTipText() throws RemoteException;

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilIsEnabled() throws RemoteException;

    public void waitUntilTableHasRows(int row) throws RemoteException;

    public void waitUntilTableItemExists(String itemText)
        throws RemoteException;

}
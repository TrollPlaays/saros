package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IEclipseBasicObject extends Remote {

    public void sleep(long millis) throws RemoteException;

    public void captureScreenshot(String filename) throws RemoteException;

    public boolean isTextWithLabelEqualWithText(String label, String text)
        throws RemoteException;

    public void clickButton(String mnemonicText) throws RemoteException;
}
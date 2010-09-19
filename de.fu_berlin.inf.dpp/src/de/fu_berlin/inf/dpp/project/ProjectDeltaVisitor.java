package de.fu_berlin.inf.dpp.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity;
import de.fu_berlin.inf.dpp.activities.business.IResourceActivity;

/**
 * Visits the resource changes in a shared project.
 */
public class ProjectDeltaVisitor implements IResourceDeltaVisitor {
    protected SharedResourcesManager sharedResourcesManager;
    protected ISarosSession sarosSession;

    /** The project visited. */
    protected SharedProject sharedProject;

    public ProjectDeltaVisitor(SharedResourcesManager sharedResourcesManager,
        ISarosSession sarosSession, SharedProject sharedProject) {
        this.sharedResourcesManager = sharedResourcesManager;
        this.sharedProject = sharedProject;
        this.sarosSession = sarosSession;
    }

    /** Stores activities to be sent due to one change event. */
    protected List<IResourceActivity> pendingActivities = new ArrayList<IResourceActivity>();

    public boolean visit(IResourceDelta delta) {
        IResource resource = delta.getResource();
        if (resource.isDerived()) {
            return false;
        }

        if (resource instanceof IFile) {
            handleFileDelta(delta);
            return true;
        } else if (resource instanceof IFolder) {
            // Note: IProject is not visited.
            return handleFolderDelta(delta);
        }

        return true;
    }

    protected boolean handleFolderDelta(IResourceDelta delta) {
        IResource resource = delta.getResource();

        int kind = delta.getKind();
        switch (kind) {
        case IResourceDelta.ADDED:
            addActivity(new FolderActivity(getUser(),
                FolderActivity.Type.Created, new SPath(resource)));
            return true;
        case IResourceDelta.REMOVED:
            addActivity(new FolderActivity(getUser(),
                FolderActivity.Type.Removed, new SPath(resource)));
            // We don't want to visit the children if this folder was removed.
            // The only interesting case is that a file was moved out of this or
            // a child folder, but we're still going to visit the move target.
            return false;
        default:
            return kind != IResourceDelta.NO_CHANGE;
        }
    }

    protected void handleFileDelta(IResourceDelta delta) {
        IResource resource = delta.getResource();
        int kind = delta.getKind();

        switch (kind) {
        case IResourceDelta.CHANGED:
            if (isContentChange(delta))
                addCreatedUnlessOpen(resource);
            return;

        case IResourceDelta.ADDED:

            // Was this file moved or renamed?
            if (isMovedFrom(delta)) {

                // Adds have getMovedFrom set:
                IPath oldPath = delta.getMovedFromPath();
                IProject oldProject = ProjectDeltaVisitor.getProject(oldPath);

                if (sarosSession.isShared(oldProject)) {
                    // Moving inside the shared project
                    try {
                        addActivity(FileActivity.moved(
                            getUser(),
                            new SPath(resource),
                            new SPath(oldProject, oldPath
                                .removeFirstSegments(1)),
                            isContentChange(delta)));
                        return;
                    } catch (IOException e) {
                        SharedResourcesManager.log
                            .warn("Resource could not be read for"
                                + " sending to peers:" + resource.getLocation());
                    }
                } else {
                    // Moving a file into the shared project
                    // -> Treat like an add!

                    // Fall-through
                }
            }

            // usual files adding procedure

            addCreatedUnlessOpen(resource);
            return;

        case IResourceDelta.REMOVED:
            if (isMoved(delta)) {

                // REMOVED deltas have MovedTo set
                IPath newPath = delta.getMovedToPath();
                IProject newProject = ProjectDeltaVisitor.getProject(newPath);

                if (sarosSession.isShared(newProject)) {
                    // Ignore "REMOVED" while moving into shared project
                    return;
                }
                // else moving file away from shared project, need to tell
                // others to delete! Fall-through...
            }

            addActivity(FileActivity.removed(getUser(), new SPath(resource),
                Purpose.ACTIVITY));
            return;

        default:
            return;
        }
    }

    protected User getUser() {
        return sarosSession.getLocalUser();
    }

    protected static IProject getProject(IPath newPath) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject newProject = root.getProject(newPath.segment(0));
        return newProject;
    }

    protected void addActivity(IResourceActivity activity) {
        pendingActivities.add(activity);
    }

    /**
     * Adds a FileActivity.created if the file is not currently in any open
     * editor. We ignore opened files because otherwise we might send CHANGED
     * events for files that are also handled by the editor manager.
     * 
     * @param resource
     */
    private void addCreatedUnlessOpen(IResource resource) {
        SPath spath = new SPath(resource);
        if (sharedResourcesManager.editorManager.isOpened(spath)) {
            return;
        }

        SharedResourcesManager.log.debug("Resource " + resource.getName()
            + " changed");
        User user = getUser();
        try {
            addActivity(FileActivity.created(user, spath, Purpose.ACTIVITY));
        } catch (IOException e) {
            SharedResourcesManager.log.warn(
                "Resource could not be read for sending to peers:"
                    + resource.getLocation(), e);
            return;
        }
    }

    /**
     * Returns true if the "Moved"-flags are set.
     */
    protected boolean isMoved(IResourceDelta delta) {
        return (isMovedFrom(delta) || isMovedTo(delta));
    }

    protected boolean isMovedFrom(IResourceDelta delta) {
        return ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0);
    }

    protected boolean isMovedTo(IResourceDelta delta) {
        return ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0);
    }

    protected boolean isSync(IResourceDelta delta) {
        return ((delta.getFlags() & IResourceDelta.SYNC) != 0);
    }

    /**
     * Returns true if the CONTENT flag is set, which means that the file's
     * timestamp changed.
     */
    protected boolean isContentChange(IResourceDelta delta) {
        return ((delta.getFlags() & IResourceDelta.CONTENT) != 0);

    }

}
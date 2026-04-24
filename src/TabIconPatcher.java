import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.IconPathPatcher;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

public class TabIconPatcher implements StartupActivity.DumbAware {
    private static final Logger LOG = Logger.getInstance(TabIconPatcher.class);
    private static volatile boolean installed = false;
    private static final Set<String> seenPaths = Collections.synchronizedSet(new HashSet<>());

    private static final Map<String, String> PATH_PATCHES = new HashMap<>();

    static {
        PATH_PATCHES.put("/toolwindows/toolWindowBuildProblems.svg", "/icons/error.png");
        PATH_PATCHES.put("/toolwindows/toolWindowProblems.svg", "/icons/error.png");
        PATH_PATCHES.put("/toolwindows/toolWindowMessages.svg", "/icons/error.png");
        PATH_PATCHES.put("/toolWindows/toolWindowProblems.svg", "/icons/error.png");
        PATH_PATCHES.put("/toolWindows/toolWindowMessages.svg", "/icons/error.png");
        PATH_PATCHES.put("/compiler/error.svg", "/icons/error.png");
        PATH_PATCHES.put("/general/error.svg", "/icons/error.png");
        PATH_PATCHES.put("/ide/error.svg", "/icons/error.png");

        PATH_PATCHES.put("/toolwindows/toolWindowRun.svg", "/icons/play.png");
        PATH_PATCHES.put("/toolWindows/toolWindowRun.svg", "/icons/play.png");
        PATH_PATCHES.put("/toolwindows/toolWindowDebug.svg", "/icons/play.png");
        PATH_PATCHES.put("/toolWindows/toolWindowDebug.svg", "/icons/play.png");
        PATH_PATCHES.put("/toolwindows/toolWindowServices.svg", "/icons/play.png");
        PATH_PATCHES.put("/toolWindows/toolWindowServices.svg", "/icons/play.png");

        PATH_PATCHES.put("/toolwindows/toolWindowBuild.svg", "/icons/build.png");
        PATH_PATCHES.put("/toolWindows/toolWindowBuild.svg", "/icons/build.png");

        PATH_PATCHES.put("/toolwindows/toolWindowProject.svg", "/icons/project.png");
        PATH_PATCHES.put("/toolWindows/toolWindowProject.svg", "/icons/project.png");

        PATH_PATCHES.put("/toolwindows/toolWindowGit.svg", "/icons/git.png");
        PATH_PATCHES.put("/toolWindows/toolWindowGit.svg", "/icons/git.png");
        PATH_PATCHES.put("/toolwindows/toolWindowVcs.svg", "/icons/git.png");
        PATH_PATCHES.put("/vcs/git.svg", "/icons/git.png");

        PATH_PATCHES.put("/toolwindows/toolWindowTerminal.svg", "/icons/terminal.png");
        PATH_PATCHES.put("/toolWindows/toolWindowTerminal.svg", "/icons/terminal.png");

        PATH_PATCHES.put("/toolwindows/toolWindowCommit.svg", "/icons/commit.png");
        PATH_PATCHES.put("/toolWindows/toolWindowCommit.svg", "/icons/commit.png");

        PATH_PATCHES.put("/toolwindows/toolWindowNotifications.svg", "/icons/notifications.png");
        PATH_PATCHES.put("/toolWindows/toolWindowNotifications.svg", "/icons/notifications.png");
        PATH_PATCHES.put("/ide/notifications.svg", "/icons/notifications.png");
    }

    @Override
    public void runActivity(@NotNull Project project) {
        if (!installed) {
            IconLoader.installPathPatcher(new DebugPathPatcher());
            installed = true;
            LOG.info("TabIconPatcher installed");
        }

        // Schedule multiple updates
        for (int delay : new int[]{100, 500, 1000, 3000, 5000, 10000}) {
            scheduleUpdate(project, delay);
        }
    }

    private void scheduleUpdate(Project project, int delay) {
        javax.swing.Timer timer = new javax.swing.Timer(delay, e -> {
            if (!project.isDisposed()) {
                updateAllIcons(project);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void updateAllIcons(Project project) {
        ToolWindowManager manager = ToolWindowManager.getInstance(project);
        if (manager == null) return;

        for (String id : manager.getToolWindowIds()) {
            ToolWindow tw = manager.getToolWindow(id);
            if (tw == null) continue;

            Icon icon = getZeldaIcon(tw.getId());
            if (icon != null) {
                try {
                    tw.setIcon(icon);
                } catch (Exception e) {
                    LOG.debug("Failed to set icon: " + id);
                }
            }
        }
    }

    private Icon getZeldaIcon(String id) {
        String lower = id.toLowerCase();
        String path = null;

        if (lower.equals("problems") || lower.equals("messages") || lower.contains("problem view")) {
            path = "/icons/error.png";
        } else if (lower.equals("run") || lower.equals("debug")) {
            path = "/icons/play.png";
        } else if (lower.equals("build") || lower.equals("build output")) {
            path = "/icons/build.png";
        } else if (lower.equals("project") || lower.equals("project view")) {
            path = "/icons/project.png";
        } else if (lower.equals("git") || lower.equals("vcs") || lower.equals("version control") || lower.equals("git branch")) {
            path = "/icons/git.png";
        } else if (lower.equals("terminal")) {
            path = "/icons/terminal.png";
        } else if (lower.equals("commit")) {
            path = "/icons/commit.png";
        } else if (lower.equals("notifications") || lower.equals("event log")) {
            path = "/icons/notifications.png";
        } else if (lower.equals("coverage")) {
            path = "/icons/coverage.png";
        } else if (lower.equals("services")) {
            path = "/icons/services.png";
        } else if (lower.equals("todo")) {
            path = "/icons/todo.png";
        }else if (lower.equals("structure")) {
            path = "/icons/structures.png";
        }

        if (path == null) return null;

        try {
            return IconLoader.getIcon(path, TabIconPatcher.class);
        } catch (Exception e) {
            return null;
        }
    }

    private static class DebugPathPatcher extends IconPathPatcher {
        @Override
        public @Nullable String patchPath(@NotNull String path, @Nullable ClassLoader cl) {
            boolean isNew = seenPaths.add(path);
            if (isNew && (path.contains("toolWindow") || path.contains("ToolWindow"))) {
                LOG.info("Icon path: " + path);
            }
            return PATH_PATCHES.get(path);
        }
    }
}

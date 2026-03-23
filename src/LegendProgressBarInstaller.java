import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.wm.IdeFrame;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public final class LegendProgressBarInstaller implements LafManagerListener, ApplicationActivationListener {

    public LegendProgressBarInstaller() {
        updateProgressBarUi();
    }

    @Override
    public void lookAndFeelChanged(@NotNull LafManager lafManager) {
        updateProgressBarUi();
    }

    @Override
    public void applicationActivated(@NotNull IdeFrame ideFrame) {
        updateProgressBarUi();
    }

    public static void updateProgressBarUi() {
        UIManager.put("ProgressBarUI", LegendOfZeldaProgressBarUi.class.getName());
        UIManager.getDefaults().put(LegendOfZeldaProgressBarUi.class.getName(), LegendOfZeldaProgressBarUi.class);
        SwingUtilities.invokeLater(() -> {
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
        });
    }
}

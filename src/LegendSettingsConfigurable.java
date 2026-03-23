import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;

import javax.swing.*;
import java.awt.*;

public final class LegendSettingsConfigurable implements Configurable {
    private static final Logger LOG = Logger.getInstance(LegendSettingsConfigurable.class);
    private LegendSettingsComponent settingsComponent;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Legend Progress Bar";
    }

    @Override
    public @Nullable JComponent createComponent() {
        try {
            if (settingsComponent == null) {
                settingsComponent = new LegendSettingsComponent();
            }
            return settingsComponent.getPanel();
        } catch (Throwable t) {
            LOG.error("Failed to create LegendSettingsComponent", t);
            JPanel error = new JPanel(new BorderLayout());
            JTextArea area = new JTextArea(
                    "Error creating Legend Progress Bar settings:\n" + t + "\nSee idea.log for details.");
            area.setEditable(false);
            error.add(new JScrollPane(area), BorderLayout.CENTER);
            return error;
        }
    }

    @Override
    public boolean isModified() {
        if (settingsComponent == null) return false;
        LegendSettingsState state = LegendSettingsState.getInstance();

        boolean charChanged = settingsComponent.getSelectedCharacter() != state.getSelectedCharacter();

        String uiItem   = settingsComponent.getSelectedItemPath();
        String savedItem = state.getSelectedItemPath();
        boolean itemChanged = uiItem == null ? savedItem != null : !uiItem.equals(savedItem);

        return charChanged || itemChanged;
    }

    @Override
    public void apply() {
        if (settingsComponent == null) return;
        LegendSettingsState state = LegendSettingsState.getInstance();
        state.setSelectedCharacter(settingsComponent.getSelectedCharacter());
        state.setSelectedItemPath(settingsComponent.getSelectedItemPath());
        LegendProgressBarInstaller.updateProgressBarUi();
    }

    @Override
    public void reset() {
        if (settingsComponent == null) return;
        LegendSettingsState state = LegendSettingsState.getInstance();
        settingsComponent.setSelectedCharacter(state.getSelectedCharacter());
        settingsComponent.setSelectedItemPath(state.getSelectedItemPath());
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service(Service.Level.APP)
@State(name = "LegendSettingsState", storages = @Storage("LegendSettingsState.xml"))
public final class LegendSettingsState implements PersistentStateComponent<LegendSettingsState> {

    // Persisted fields — public so XmlSerializer can read/write them
    public String characterName = LegendCharacter.LINK.name();
    public String selectedItemPath = null;

    public static LegendSettingsState getInstance() {
        Application application = ApplicationManager.getApplication();
        return application == null ? SingletonHolder.DEFAULT : application.getService(LegendSettingsState.class);
    }

    private static final class SingletonHolder {
        private static final LegendSettingsState DEFAULT = new LegendSettingsState();
    }

    // ── Character ─────────────────────────────────────────────────────────

    public LegendCharacter getSelectedCharacter() {
        try {
            return LegendCharacter.valueOf(characterName);
        } catch (IllegalArgumentException ex) {
            characterName = LegendCharacter.LINK.name();
            return LegendCharacter.LINK;
        }
    }

    public void setSelectedCharacter(@NotNull LegendCharacter character) {
        this.characterName = character.name();
    }

    // ── Item ──────────────────────────────────────────────────────────────

    public String getSelectedItemPath() {
        return selectedItemPath;
    }

    public void setSelectedItemPath(String path) {
        this.selectedItemPath = path;
    }

    // ── PersistentStateComponent ──────────────────────────────────────────

    @Override
    public @Nullable LegendSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull LegendSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}

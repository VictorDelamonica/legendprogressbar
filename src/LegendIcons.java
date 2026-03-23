import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public final class LegendIcons {
    private LegendIcons() {
    }

    public static Icon getIcon(LegendCharacter character, boolean mirrored) {
        if (character == null || !character.hasIcon()) {
            return null;
        }
        String path = mirrored && character.getMirroredIconPath() != null
                ? character.getMirroredIconPath()
                : character.getIconPath();
        if (path == null) {
            return null;
        }
        return IconLoader.getIcon(path, LegendIcons.class);
    }

    public static Icon getIconByPath(String path) {
        if (path == null) return null;
        try {
            return IconLoader.getIcon(path, LegendIcons.class);
        } catch (Throwable t) {
            return null;
        }
    }
}

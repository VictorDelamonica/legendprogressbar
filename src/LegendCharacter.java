import java.awt.*;

public enum LegendCharacter {
    LINK(
            "Link",
            new Color(12, 48, 23),
            "/link.png",
            "/rlink.png"
    ),
    ZELDA(
            "Zelda",
            new Color(39, 23, 85),
            "/zelda.png",
            "/rzelda.png"
    ),
    GANON(
            "Ganon",
            new Color(2, 17, 46),
            "/ganon.png",
            "/rganon.png"
    ),
    GANONDORF(
            "Ganondorf",
            new Color(47, 2, 2),
            "/ganondorf.png",
            "/rganondorf.png"
    );

    private final String displayName;
    private final Color backgroundStart;
    private final String iconPath;
    private final String mirroredIconPath;

    LegendCharacter(String displayName,
                    Color backgroundStart,
                    String iconPath,
                    String mirroredIconPath) {
        this.displayName = displayName;
        this.backgroundStart = backgroundStart;
        this.iconPath = iconPath;
        this.mirroredIconPath = mirroredIconPath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getBackgroundStart() {
        return backgroundStart;
    }

    public String getIconPath() {
        return iconPath;
    }

    public String getMirroredIconPath() {
        return mirroredIconPath;
    }

    public boolean hasIcon() {
        return iconPath != null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

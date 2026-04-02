import com.intellij.ui.JBColor;

import java.awt.*;

public enum LegendCharacter {
    LINK(
            "Link",
            new JBColor(new Color(12, 48, 23), new Color(12, 48, 23)),
            "/link.png",
            "/rlink.png"
    ),
    ZELDA(
            "Zelda",
            new JBColor(new Color(39, 23, 85), new Color(39, 23, 85)),
            "/zelda.png",
            "/rzelda.png"
    ),
    GANON(
            "Ganon",
            new JBColor(new Color(2, 17, 46), new Color(2, 17, 46)),
            "/ganon.png",
            "/rganon.png"
    ),
    GANONDORF(
            "Ganondorf",
            new JBColor(new Color(47, 2, 2), new Color(47, 2, 2)),
            "/ganondorf.png",
            "/rganondorf.png"
    ),
    SKULL_KID(
            "Skull Kid",
            new JBColor(new Color(150, 53, 32), new Color(150, 53, 32)), // Orange-brun, inspiré du masque
            "/Skull Kid.png",
            "/rskull_kid.png"
    ),
    //#rgb(148, 40, 42)
    MASK_VENDOR(
            "Mask Vendor",
            new JBColor(new Color(99, 24, 44), new Color(99, 24, 44)),
            "/Mask Vendor.png",
            "/rmask_vendor.png"
    ),
    SHEIK(
            "Sheik",
            new JBColor(new Color(106, 119, 144), new Color(106, 119, 144)), // Bleu/gris, inspiré de la tenue
            "/Sheik.png",
            "/rsheik.png"
    )
    ;

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

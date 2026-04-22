import javax.swing.Icon;
import java.util.Objects;

/**
 * Animation displayed when a build is cancelled.
 *
 * Shows the selected character in a neutral/grayed pose to indicate the build was cancelled.
 * If grayed pose frame is not available, falls back to the character's normal frame.
 */
public class CancelledAnimation implements Animation {

    private static final int ANIMATION_DURATION_MS = 1500; // Build feedback animations play for 1.5 seconds
    private static final String AUDIO_RESOURCE = null; // Cancelled builds are silent

    private final Icon[] frames;

    /**
     * Create a cancelled animation for the given character.
     *
     * @param character the character to animate (must not be null)
     * @throws IllegalStateException if no frames are available for the character
     */
    public CancelledAnimation(LegendCharacter character) {
        this.frames = loadFrames(character);
    }

    /**
     * Loads neutral/grayed pose frame for the character.
     *
     * Attempts to load a single frame from /infinit/{characterName}_gray.png.
     * If grayed pose is not available, falls back to the character's normal icon.
     * Throws IllegalStateException if no frames can be loaded at all.
     *
     * @param character The character whose cancelled animation to load
     * @return Array of Icon frames (single frame)
     * @throws IllegalStateException if the character has no icon available
     */
    private Icon[] loadFrames(LegendCharacter character) {
        Objects.requireNonNull(character, "character must not be null");
        // Character enum name must match resource filename convention (lowercase)
        // e.g., LINK enum → "link_gray.png"
        String characterName = character.name().toLowerCase();

        // Try to load grayed pose frame (_gray)
        String grayFramePath = "/infinit/" + characterName + "_gray.png";
        Icon grayFrame = LegendIcons.getIconByPath(grayFramePath);
        if (grayFrame != null) {
            return new Icon[]{grayFrame};
        }

        // Fallback: use the character's normal icon
        Icon normalFrame = LegendIcons.getIcon(character, false);
        if (normalFrame != null) {
            return new Icon[]{normalFrame};
        }

        // Contract violation: getFrames() must never return empty array
        throw new IllegalStateException("No frames available for character: " + character);
    }

    @Override
    public Icon[] getFrames() {
        return frames;
    }

    @Override
    public int getDurationMs() {
        return ANIMATION_DURATION_MS;
    }

    @Override
    public String getAudioResourcePath() {
        return AUDIO_RESOURCE;
    }
}

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Animation displayed when a build fails.
 *
 * Shows the selected character in a knockdown pose to provide failure feedback.
 * If knockdown pose frames are not available, falls back to the character's normal frame.
 */
public class FailureAnimation implements Animation {

    private static final int ANIMATION_DURATION_MS = 1500; // Build feedback animations play for 1.5 seconds
    private static final String AUDIO_RESOURCE = "/audio/failure.wav";

    private final Icon[] frames;

    /**
     * Create a failure animation for the given character.
     *
     * @param character the character to animate (must not be null)
     * @throws IllegalStateException if no frames are available for the character
     */
    public FailureAnimation(LegendCharacter character) {
        this.frames = loadFrames(character);
    }

    /**
     * Loads knockdown pose frames for the character.
     *
     * Attempts to load frames from /infinit/{characterName}_fail_0.png, _fail_1.png, etc.
     * If knockdown poses are not available, falls back to the character's normal icon.
     * Throws IllegalStateException if no frames can be loaded at all.
     *
     * @param character The character whose failure animation to load
     * @return Array of Icon frames in order
     * @throws IllegalStateException if the character has no icon available
     */
    private Icon[] loadFrames(LegendCharacter character) {
        Objects.requireNonNull(character, "character must not be null");
        // Character enum name must match resource filename convention (lowercase)
        // e.g., LINK enum → "link_fail_0.png", etc.
        String characterName = character.name().toLowerCase();
        List<Icon> loadedFrames = new ArrayList<>();

        // Try to load knockdown pose frames (_fail_0, _fail_1, etc.)
        int frameIndex = 0;
        while (true) {
            String framePath = "/infinit/" + characterName + "_fail_" + frameIndex + ".png";
            Icon frame = LegendIcons.getIconByPath(framePath);
            if (frame == null) {
                break; // No more frames found
            }
            loadedFrames.add(frame);
            frameIndex++;
        }

        // If we found knockdown pose frames, return them
        if (!loadedFrames.isEmpty()) {
            return loadedFrames.toArray(new Icon[0]);
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

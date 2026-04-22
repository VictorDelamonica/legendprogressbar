import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;

/**
 * Animation displayed when a build succeeds.
 *
 * Shows the selected character in a winning pose to provide celebratory feedback.
 * If winning pose frames are not available, falls back to the character's normal frame.
 */
public class SuccessAnimation implements Animation {

    private final Icon[] frames;

    public SuccessAnimation(LegendCharacter character) {
        this.frames = loadFrames(character);
    }

    /**
     * Loads winning pose frames for the character.
     *
     * Attempts to load frames from /infinit/{characterName}_win_0.png, _win_1.png, etc.
     * If winning poses are not available, falls back to the character's normal icon.
     * If no frames can be loaded at all, returns a single-frame array with null
     * (the animation system should handle null gracefully).
     *
     * @param character The character whose winning animation to load
     * @return Array of Icon frames in order
     */
    private Icon[] loadFrames(LegendCharacter character) {
        String characterName = character.name().toLowerCase();
        List<Icon> loadedFrames = new ArrayList<>();

        // Try to load winning pose frames (_win_0, _win_1, etc.)
        int frameIndex = 0;
        while (true) {
            String framePath = "/infinit/" + characterName + "_win_" + frameIndex + ".png";
            Icon frame = LegendIcons.getIconByPath(framePath);
            if (frame == null) {
                break; // No more frames found
            }
            loadedFrames.add(frame);
            frameIndex++;
        }

        // If we found winning pose frames, return them
        if (!loadedFrames.isEmpty()) {
            return loadedFrames.toArray(new Icon[0]);
        }

        // Fallback: use the character's normal icon
        Icon normalFrame = LegendIcons.getIcon(character, false);
        if (normalFrame != null) {
            return new Icon[]{normalFrame};
        }

        // Last resort: return empty array (should not happen in normal operation)
        return new Icon[]{};
    }

    @Override
    public Icon[] getFrames() {
        return frames;
    }

    @Override
    public int getDurationMs() {
        return 1500;
    }

    @Override
    public String getAudioResourcePath() {
        return "/audio/success.wav";
    }
}

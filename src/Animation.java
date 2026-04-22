import javax.swing.Icon;

/**
 * Interface for build feedback animations.
 *
 * All animation implementations (Success, Failure, Cancelled) should implement
 * this interface to provide frame sequences, duration, and optional audio feedback.
 */
public interface Animation {

    /**
     * Returns the sequence of frames for this animation.
     *
     * Each frame is an Icon that will be displayed in sequence to create
     * the animation effect.
     *
     * @return Array of Icon frames in display order. Must not be null or empty.
     */
    Icon[] getFrames();

    /**
     * Returns the total duration of this animation in milliseconds.
     *
     * For brief feedback animations (success, failure, cancelled builds),
     * typical values are 1500-2000ms.
     *
     * @return Duration in milliseconds. Must be positive.
     */
    int getDurationMs();

    /**
     * Returns the resource path to an audio clip for this animation.
     *
     * The path should reference an audio file in the resources directory
     * (e.g., "/audio/success.wav"). Return null for silent animations.
     *
     * @return Resource path to audio file (e.g., "/audio/success.wav"), or null for no audio
     */
    String getAudioResourcePath();
}

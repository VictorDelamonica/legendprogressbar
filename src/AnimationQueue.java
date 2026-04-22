import javax.swing.Icon;

/**
 * Manages animation timing and frame sequencing.
 *
 * This class tracks which animation is active, advances frames based on elapsed time,
 * and provides the current frame to display. All public methods are synchronized to
 * ensure thread-safe access from multiple threads (paint thread and build listener thread).
 */
public class AnimationQueue {
  private Animation currentAnimation;
  private long elapsedTimeMs;

  /**
   * Enqueue a new animation (replaces current if any).
   *
   * @param animation The animation to enqueue. Sets elapsed time to 0.
   */
  public synchronized void enqueue(Animation animation) {
    this.currentAnimation = animation;
    this.elapsedTimeMs = 0;
  }

  /**
   * Returns true if animation is currently playing (elapsed < duration).
   *
   * @return true if animation is active, false otherwise
   */
  public synchronized boolean isAnimationActive() {
    return currentAnimation != null && elapsedTimeMs < currentAnimation.getDurationMs();
  }

  /**
   * Returns the current frame Icon, or null if no animation active.
   *
   * Frame index is calculated based on elapsed time percentage.
   *
   * @return The current frame Icon, or null if no animation active or frames empty
   */
  public synchronized Icon getCurrentFrame() {
    if (currentAnimation == null) {
      return null;
    }

    Icon[] frames = currentAnimation.getFrames();
    if (frames == null || frames.length == 0) {
      return null;
    }

    int duration = currentAnimation.getDurationMs();
    if (duration <= 0) {
      return frames[0];
    }

    int frameIndex = (int) ((double) elapsedTimeMs / duration * frames.length);
    frameIndex = Math.min(frameIndex, frames.length - 1);
    return frames[frameIndex];
  }

  /**
   * Advance animation by deltaMs milliseconds.
   *
   * If elapsed time reaches or exceeds duration, clears the animation and resets elapsed time.
   *
   * @param deltaMs Milliseconds to advance. Must be >= 0. Clamped to prevent overflow.
   */
  public synchronized void updateFrame(long deltaMs) {
    if (currentAnimation == null) {
      return;
    }

    // Clamp deltaMs to prevent overflow
    elapsedTimeMs = elapsedTimeMs + Math.min(deltaMs, Long.MAX_VALUE - elapsedTimeMs);

    if (elapsedTimeMs >= currentAnimation.getDurationMs()) {
      currentAnimation = null;
      elapsedTimeMs = 0;
    }
  }

  /**
   * Get audio path for current animation (if active), or null.
   *
   * @return Resource path to audio file (e.g., "/audio/success.wav"), or null if inactive
   */
  public synchronized String getAudioResourcePath() {
    if (currentAnimation == null) {
      return null;
    }
    return currentAnimation.getAudioResourcePath();
  }
}

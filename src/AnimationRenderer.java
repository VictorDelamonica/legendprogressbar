import javax.swing.Icon;
import java.awt.Graphics2D;

/**
 * Renders animation frames from the animation queue onto a progress bar.
 *
 * This renderer manages the animation update cycle and paints the current frame
 * centered on the progress bar. It acts as an adapter between the AnimationQueue
 * and the UI painting system.
 */
public class AnimationRenderer {
  private final AnimationQueue animationQueue;

  /**
   * Creates a new AnimationRenderer with the given animation queue.
   *
   * @param animationQueue the queue to draw animation frames from
   */
  public AnimationRenderer(AnimationQueue animationQueue) {
    this.animationQueue = animationQueue;
  }

  /**
   * Advances the animation by the given number of milliseconds.
   *
   * This should be called once per paint cycle to keep animations advancing.
   *
   * @param deltaMs milliseconds to advance the animation
   */
  public void updateFrame(long deltaMs) {
    animationQueue.updateFrame(deltaMs);
  }

  /**
   * Paints the current animation frame centered on the given bounds.
   *
   * If no animation is active or no frame is available, this method does nothing.
   * The frame is centered both horizontally and vertically within the provided bounds.
   *
   * @param g the graphics context to paint to
   * @param x the left edge of the bounds
   * @param y the top edge of the bounds
   * @param width the width of the bounds
   * @param height the height of the bounds
   */
  public void paint(Graphics2D g, int x, int y, int width, int height) {
    if (!animationQueue.isAnimationActive()) {
      return;
    }

    Icon frame = animationQueue.getCurrentFrame();
    if (frame == null) {
      return;
    }

    // Center the animation frame within the progress bar bounds
    int frameWidth = frame.getIconWidth();
    int frameHeight = frame.getIconHeight();
    int frameX = x + (width - frameWidth) / 2;
    int frameY = y + (height - frameHeight) / 2;

    frame.paintIcon(null, g, frameX, frameY);
  }

  /**
   * Returns whether an animation is currently active.
   *
   * @return true if an animation is currently playing, false otherwise
   */
  public boolean isAnimationActive() {
    return animationQueue.isAnimationActive();
  }
}

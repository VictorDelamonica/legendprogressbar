/**
 * Singleton holder providing global access to the animation queue.
 *
 * This class ensures there is a single, globally-accessible AnimationQueue instance
 * that can be obtained from anywhere in the application (e.g., UI rendering and build listeners).
 *
 * Usage:
 *   AnimationQueue queue = AnimationQueueHolder.getInstance().getQueue();
 */
public class AnimationQueueHolder {
  private static final AnimationQueueHolder INSTANCE = new AnimationQueueHolder();
  private final AnimationQueue animationQueue = new AnimationQueue();

  private AnimationQueueHolder() {
  }

  /**
   * Returns the singleton instance of AnimationQueueHolder.
   *
   * @return The singleton AnimationQueueHolder instance
   */
  public static AnimationQueueHolder getInstance() {
    return INSTANCE;
  }

  /**
   * Returns the global AnimationQueue instance.
   *
   * @return The shared AnimationQueue
   */
  public AnimationQueue getQueue() {
    return animationQueue;
  }
}

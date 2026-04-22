import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listens for build completion events and queues appropriate animations.
 *
 * When the IDE finishes a build, determines the outcome (success, failure, or cancellation)
 * and queues the corresponding animation for the selected character.
 *
 * This listener is registered via plugin.xml as a CompileStatusNotification.
 * It runs on the build complete event, which fires after compilation finishes.
 */
public class BuildStateListener implements CompileStatusNotification {

    private final AnimationQueue animationQueue;

    /**
     * Create a BuildStateListener with the default AnimationQueue.
     * Used for production. The queue is obtained on-demand from AnimationQueueHolder (Task 8).
     */
    public BuildStateListener() {
        // TODO: Task 8 will replace this with AnimationQueueHolder.getInstance()
        // For now, create a temporary queue that won't be observed by the UI
        this(new AnimationQueue());
    }

    /**
     * Create a BuildStateListener with an injected AnimationQueue.
     * Used for testing to allow mock injection.
     *
     * @param animationQueue the AnimationQueue to use for enqueueing animations
     */
    public BuildStateListener(AnimationQueue animationQueue) {
        this.animationQueue = animationQueue;
    }

    /**
     * Called when a compilation completes.
     *
     * Determines the build outcome and queues the appropriate animation:
     * - Success (no errors) → SuccessAnimation
     * - Failure (errors present) → FailureAnimation
     * - Cancellation (compilation aborted) → CancelledAnimation
     *
     * If project is null or no character is selected, does nothing.
     *
     * @param aborted true if the compilation was aborted (Ctrl+C or user stop)
     * @param errors number of compilation errors
     * @param warnings number of compilation warnings (ignored for outcome determination)
     * @param compileContext the compile context providing access to the project
     */
    @Override
    public void finished(boolean aborted, int errors, int warnings, @NotNull CompileContext compileContext) {
        // Get the project from compile context
        Project project = compileContext.getProject();

        // Null-check project (should not happen, but defensive)
        if (project == null) {
            return;
        }

        // Get the selected character from settings
        LegendCharacter character = LegendSettingsState.getInstance().getSelectedCharacter();

        // Null-check character (should not happen due to fallback in LegendSettingsState, but defensive)
        if (character == null) {
            return;
        }

        // Determine outcome and queue appropriate animation
        if (aborted) {
            animationQueue.enqueue(new CancelledAnimation(character));
        } else if (errors > 0) {
            animationQueue.enqueue(new FailureAnimation(character));
        } else {
            animationQueue.enqueue(new SuccessAnimation(character));
        }
    }
}

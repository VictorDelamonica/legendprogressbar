import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import javax.swing.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnimationQueueTest {
    private AnimationQueue queue;
    private Animation mockAnimation;

    @BeforeEach
    void setUp() {
        queue = new AnimationQueue();
        mockAnimation = mock(Animation.class);
    }

    @Test
    void enqueue_makesAnimationActive() {
        when(mockAnimation.getDurationMs()).thenReturn(1500);
        when(mockAnimation.getFrames()).thenReturn(new Icon[]{mock(Icon.class)});

        queue.enqueue(mockAnimation);
        assertThat(queue.isAnimationActive()).isTrue();
    }

    @Test
    void isAnimationActive_returnsFalseInitially() {
        assertThat(queue.isAnimationActive()).isFalse();
    }

    @Test
    void getCurrentFrame_returnsFirstFrameWhenActive() {
        Icon frame1 = mock(Icon.class);
        Icon frame2 = mock(Icon.class);
        when(mockAnimation.getFrames()).thenReturn(new Icon[]{frame1, frame2});
        when(mockAnimation.getDurationMs()).thenReturn(1500);

        queue.enqueue(mockAnimation);
        assertThat(queue.getCurrentFrame()).isEqualTo(frame1);
    }

    @Test
    void updateFrame_advancesFrameBasedOnElapsedTime() {
        Icon frame1 = mock(Icon.class);
        Icon frame2 = mock(Icon.class);
        when(mockAnimation.getFrames()).thenReturn(new Icon[]{frame1, frame2});
        when(mockAnimation.getDurationMs()).thenReturn(1000);

        queue.enqueue(mockAnimation);
        queue.updateFrame(600); // 60% elapsed

        assertThat(queue.getCurrentFrame()).isEqualTo(frame2);
    }

    @Test
    void isAnimationActive_returnsFalseAfterDuration() {
        when(mockAnimation.getDurationMs()).thenReturn(1500);
        when(mockAnimation.getFrames()).thenReturn(new Icon[]{mock(Icon.class)});

        queue.enqueue(mockAnimation);
        queue.updateFrame(1500); // Advance to/past duration

        assertThat(queue.isAnimationActive()).isFalse();
    }

    @Test
    void getAudioResourcePath_returnsPathWhenActive() {
        when(mockAnimation.getDurationMs()).thenReturn(1500);
        when(mockAnimation.getFrames()).thenReturn(new Icon[]{mock(Icon.class)});
        when(mockAnimation.getAudioResourcePath()).thenReturn("/audio/success.wav");

        queue.enqueue(mockAnimation);
        assertThat(queue.getAudioResourcePath()).isEqualTo("/audio/success.wav");
    }

    @Test
    void getAudioResourcePath_returnsNullWhenInactive() {
        assertThat(queue.getAudioResourcePath()).isNull();
    }

    @Test
    void updateFrame_clampsDeltaTime() {
        when(mockAnimation.getDurationMs()).thenReturn(1500);
        when(mockAnimation.getFrames()).thenReturn(new Icon[]{mock(Icon.class)});

        queue.enqueue(mockAnimation);
        queue.updateFrame(Long.MAX_VALUE);

        assertThat(queue.isAnimationActive()).isFalse(); // Should not crash, should complete
    }
}

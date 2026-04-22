import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.Icon;
import java.awt.Graphics2D;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("AnimationRenderer")
class AnimationRendererTest {
  private AnimationRenderer renderer;

  @Mock
  private AnimationQueue mockQueue;

  @Mock
  private Graphics2D mockGraphics;

  @Mock
  private Icon mockFrame;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    renderer = new AnimationRenderer(mockQueue);
  }

  @Test
  @DisplayName("paint does nothing when animation is inactive")
  void paint_doesNothingWhenAnimationInactive() {
    when(mockQueue.isAnimationActive()).thenReturn(false);

    renderer.paint(mockGraphics, 0, 0, 100, 50);

    verify(mockQueue, never()).getCurrentFrame();
  }

  @Test
  @DisplayName("paint renders frame centered on progress bar when animation active")
  void paint_paintsFrameWhenAnimationActive() {
    when(mockFrame.getIconWidth()).thenReturn(32);
    when(mockFrame.getIconHeight()).thenReturn(32);
    when(mockQueue.isAnimationActive()).thenReturn(true);
    when(mockQueue.getCurrentFrame()).thenReturn(mockFrame);

    renderer.paint(mockGraphics, 0, 0, 100, 50);

    // Frame should be centered: x = (100 - 32) / 2 = 34, y = (50 - 32) / 2 = 9
    verify(mockFrame).paintIcon(null, mockGraphics, 34, 9);
  }

  @Test
  @DisplayName("paint handles frame with different dimensions")
  void paint_paintsFrameWithDifferentDimensions() {
    when(mockFrame.getIconWidth()).thenReturn(24);
    when(mockFrame.getIconHeight()).thenReturn(20);
    when(mockQueue.isAnimationActive()).thenReturn(true);
    when(mockQueue.getCurrentFrame()).thenReturn(mockFrame);

    renderer.paint(mockGraphics, 10, 5, 80, 40);

    // Frame should be centered: x = 10 + (80 - 24) / 2 = 38, y = 5 + (40 - 20) / 2 = 15
    verify(mockFrame).paintIcon(null, mockGraphics, 38, 15);
  }

  @Test
  @DisplayName("updateFrame delegates to queue")
  void updateFrame_delegatesToQueue() {
    renderer.updateFrame(16);

    verify(mockQueue).updateFrame(16);
  }

  @Test
  @DisplayName("updateFrame handles large delta time")
  void updateFrame_handlesLargeDeltaTime() {
    renderer.updateFrame(1000);

    verify(mockQueue).updateFrame(1000);
  }

  @Test
  @DisplayName("isAnimationActive delegates to queue and returns true when active")
  void isAnimationActive_delegatesToQueueReturnsTrueWhenActive() {
    when(mockQueue.isAnimationActive()).thenReturn(true);

    assertThat(renderer.isAnimationActive()).isTrue();
    verify(mockQueue).isAnimationActive();
  }

  @Test
  @DisplayName("isAnimationActive delegates to queue and returns false when inactive")
  void isAnimationActive_delegatesToQueueReturnsFalseWhenInactive() {
    when(mockQueue.isAnimationActive()).thenReturn(false);

    assertThat(renderer.isAnimationActive()).isFalse();
    verify(mockQueue).isAnimationActive();
  }

  @Test
  @DisplayName("paint skips rendering when getCurrentFrame returns null")
  void paint_skipsRenderingWhenFrameIsNull() {
    when(mockQueue.isAnimationActive()).thenReturn(true);
    when(mockQueue.getCurrentFrame()).thenReturn(null);

    renderer.paint(mockGraphics, 0, 0, 100, 50);

    // No frame should be painted
    verify(mockGraphics, never()).drawImage(any(), anyInt(), anyInt(), anyInt(), anyInt(), any());
  }
}

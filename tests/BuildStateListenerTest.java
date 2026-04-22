import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.project.Project;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("BuildStateListener")
class BuildStateListenerTest {

    private BuildStateListener listener;

    @Mock
    private AnimationQueue mockAnimationQueue;

    @Mock
    private CompileContext mockCompileContext;

    @Mock
    private Project mockProject;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listener = new BuildStateListener(mockAnimationQueue);
        when(mockCompileContext.getProject()).thenReturn(mockProject);
    }

    @Test
    @DisplayName("is a CompileStatusNotification")
    void implementsCompileStatusNotification() {
        assertThat(listener).isInstanceOf(CompileStatusNotification.class);
    }

    @Test
    @DisplayName("can be constructed with default AnimationQueue")
    void defaultConstructor() {
        BuildStateListener defaultListener = new BuildStateListener();
        assertThat(defaultListener).isNotNull();
    }

    @Test
    @DisplayName("can be constructed with injected AnimationQueue")
    void constructorWithInjectedQueue() {
        AnimationQueue customQueue = new AnimationQueue();
        BuildStateListener customListener = new BuildStateListener(customQueue);
        assertThat(customListener).isNotNull();
    }

    @Test
    @DisplayName("early exits when project is null")
    void finishedExitsEarlyWhenProjectNull() {
        when(mockCompileContext.getProject()).thenReturn(null);

        listener.finished(false, 0, 0, mockCompileContext);

        verify(mockAnimationQueue, never()).enqueue(any());
    }

    @Test
    @DisplayName("accesses project from CompileContext")
    void finishedCallsGetProjectOnContext() {
        when(mockCompileContext.getProject()).thenReturn(null);

        listener.finished(false, 0, 0, mockCompileContext);

        verify(mockCompileContext).getProject();
    }
}

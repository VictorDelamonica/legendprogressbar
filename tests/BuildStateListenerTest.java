import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;

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

    @Test
    @DisplayName("queues CancelledAnimation when build is aborted")
    void finishedWithAbortedTrue_queuesCancelledAnimation() {
        try (MockedStatic<LegendSettingsState> mockedSettings = mockStatic(LegendSettingsState.class);
             MockedStatic<LegendIcons> mockedIcons = mockStatic(LegendIcons.class)) {
            // Setup
            LegendCharacter mockCharacter = mock(LegendCharacter.class);
            when(mockCharacter.name()).thenReturn("LINK");
            LegendSettingsState mockState = mock(LegendSettingsState.class);
            when(mockState.getSelectedCharacter()).thenReturn(mockCharacter);
            mockedSettings.when(LegendSettingsState::getInstance).thenReturn(mockState);

            // Mock icon loading to return a dummy icon
            javax.swing.Icon mockIcon = mock(javax.swing.Icon.class);
            mockedIcons.when(() -> LegendIcons.getIconByPath(anyString())).thenReturn(null);
            mockedIcons.when(() -> LegendIcons.getIcon(any(LegendCharacter.class), anyBoolean())).thenReturn(mockIcon);

            AnimationQueue mockQueue = mock(AnimationQueue.class);
            BuildStateListener testListener = new BuildStateListener(mockQueue);

            // Execute
            testListener.finished(true, 0, 0, mockCompileContext);

            // Verify
            ArgumentCaptor<Animation> captor = ArgumentCaptor.forClass(Animation.class);
            verify(mockQueue).enqueue(captor.capture());
            assertThat(captor.getValue()).isInstanceOf(CancelledAnimation.class);
        }
    }

    @Test
    @DisplayName("queues FailureAnimation when errors are present")
    void finishedWithErrorsGreaterThanZero_queuesFailureAnimation() {
        try (MockedStatic<LegendSettingsState> mockedSettings = mockStatic(LegendSettingsState.class);
             MockedStatic<LegendIcons> mockedIcons = mockStatic(LegendIcons.class)) {
            // Setup
            LegendCharacter mockCharacter = mock(LegendCharacter.class);
            when(mockCharacter.name()).thenReturn("ZELDA");
            LegendSettingsState mockState = mock(LegendSettingsState.class);
            when(mockState.getSelectedCharacter()).thenReturn(mockCharacter);
            mockedSettings.when(LegendSettingsState::getInstance).thenReturn(mockState);

            // Mock icon loading to return a dummy icon
            javax.swing.Icon mockIcon = mock(javax.swing.Icon.class);
            mockedIcons.when(() -> LegendIcons.getIconByPath(anyString())).thenReturn(null);
            mockedIcons.when(() -> LegendIcons.getIcon(any(LegendCharacter.class), anyBoolean())).thenReturn(mockIcon);

            AnimationQueue mockQueue = mock(AnimationQueue.class);
            BuildStateListener testListener = new BuildStateListener(mockQueue);

            // Execute
            testListener.finished(false, 3, 0, mockCompileContext);

            // Verify
            ArgumentCaptor<Animation> captor = ArgumentCaptor.forClass(Animation.class);
            verify(mockQueue).enqueue(captor.capture());
            assertThat(captor.getValue()).isInstanceOf(FailureAnimation.class);
        }
    }

    @Test
    @DisplayName("queues SuccessAnimation when build succeeds")
    void finishedWithSuccessOutcome_queuesSuccessAnimation() {
        try (MockedStatic<LegendSettingsState> mockedSettings = mockStatic(LegendSettingsState.class);
             MockedStatic<LegendIcons> mockedIcons = mockStatic(LegendIcons.class)) {
            // Setup
            LegendCharacter mockCharacter = mock(LegendCharacter.class);
            when(mockCharacter.name()).thenReturn("GANON");
            LegendSettingsState mockState = mock(LegendSettingsState.class);
            when(mockState.getSelectedCharacter()).thenReturn(mockCharacter);
            mockedSettings.when(LegendSettingsState::getInstance).thenReturn(mockState);

            // Mock icon loading to return a dummy icon
            javax.swing.Icon mockIcon = mock(javax.swing.Icon.class);
            mockedIcons.when(() -> LegendIcons.getIconByPath(anyString())).thenReturn(null);
            mockedIcons.when(() -> LegendIcons.getIcon(any(LegendCharacter.class), anyBoolean())).thenReturn(mockIcon);

            AnimationQueue mockQueue = mock(AnimationQueue.class);
            BuildStateListener testListener = new BuildStateListener(mockQueue);

            // Execute
            testListener.finished(false, 0, 0, mockCompileContext);

            // Verify
            ArgumentCaptor<Animation> captor = ArgumentCaptor.forClass(Animation.class);
            verify(mockQueue).enqueue(captor.capture());
            assertThat(captor.getValue()).isInstanceOf(SuccessAnimation.class);
        }
    }

    @Test
    @DisplayName("skips queuing when character is null")
    void finishedWithNullCharacter_skipsQueuing() {
        try (MockedStatic<LegendSettingsState> mockedSettings = mockStatic(LegendSettingsState.class)) {
            // Setup
            LegendSettingsState mockState = mock(LegendSettingsState.class);
            when(mockState.getSelectedCharacter()).thenReturn(null);
            mockedSettings.when(LegendSettingsState::getInstance).thenReturn(mockState);

            AnimationQueue mockQueue = mock(AnimationQueue.class);
            BuildStateListener testListener = new BuildStateListener(mockQueue);

            // Execute
            testListener.finished(false, 0, 0, mockCompileContext);

            // Verify - nothing should be queued
            verify(mockQueue, never()).enqueue(any());
        }
    }
}

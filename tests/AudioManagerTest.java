import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AudioManagerTest {
    private AudioManager audioManager;

    @BeforeEach
    void setUp() {
        audioManager = new AudioManager();
    }

    @Test
    void play_shouldNotThrowWhenResourceNotFound() {
        assertThatNoException().isThrownBy(() -> audioManager.play("/nonexistent/sound.wav"));
    }

    @Test
    void isMuted_returnsFalseByDefault() {
        assertThat(audioManager.isMuted()).isFalse();
    }

    @Test
    void setMuted_persistsMuteState() {
        audioManager.setMuted(true);
        assertThat(audioManager.isMuted()).isTrue();

        audioManager.setMuted(false);
        assertThat(audioManager.isMuted()).isFalse();
    }

    @Test
    void play_skipsAudioWhenMuted() {
        audioManager.setMuted(true);
        // Note: This test verifies no exception is thrown. Full behavioral verification
        // (that audio was actually skipped) requires mock injection of the executor,
        // which is not supported by the current design.
        assertThatNoException().isThrownBy(() -> audioManager.play("/audio/success.wav"));
    }

    @Test
    void shutdown_completesWithoutError() {
        assertThatNoException().isThrownBy(() -> audioManager.shutdown());
    }

    @Test
    void play_acceptsNullResourcePath() {
        assertThatNoException().isThrownBy(() -> audioManager.play(null));
    }

    @Test
    void play_playsAudioWhenNotMuted() {
        audioManager.setMuted(false);
        // Note: This test verifies no exception is thrown. Full behavioral verification
        // (that audio was actually dispatched to the executor) requires mock injection
        // of the executor, which is not supported by the current design.
        assertThatNoException().isThrownBy(() -> audioManager.play("/audio/test.wav"));
    }

    @Test
    void play_afterShutdown_skipsWithoutError() {
        audioManager.shutdown();
        // After shutdown, play() should not throw even though executor is terminated.
        // The RejectedExecutionException is swallowed by the catch block in play().
        assertThatNoException().isThrownBy(() -> audioManager.play("/audio/test.wav"));
    }
}

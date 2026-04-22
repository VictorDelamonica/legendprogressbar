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
        assertThatNoException().isThrownBy(() -> audioManager.play("/audio/test.wav"));
    }
}

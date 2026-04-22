import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioManager {
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    private boolean muted = false;

    public synchronized void play(String resourcePath) {
        if (muted || resourcePath == null) {
            return;
        }

        try {
            EXECUTOR.submit(() -> playAudio(resourcePath));
        } catch (Exception e) {
            // If executor is terminated or rejected, silently skip
        }
    }

    private void playAudio(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                return;
            }

            try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(is))) {
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();

                // Block until the clip finishes playing
                while (clip.isRunning()) {
                    Thread.sleep(10);
                }
                clip.close();
            }
        } catch (Exception e) {
            // Silently skip on any error (missing resource, unsupported format, etc.)
        }
    }

    public synchronized boolean isMuted() {
        return muted;
    }

    public synchronized void setMuted(boolean muted) {
        this.muted = muted;
    }

    public void shutdown() {
        EXECUTOR.shutdown();
    }
}

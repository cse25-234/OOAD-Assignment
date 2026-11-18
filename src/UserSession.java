import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.input.InputEvent;
import javafx.util.Duration;

public final class UserSession {
    private static final long TIMEOUT_MILLIS = 10 * 60 * 1000L;

    private static String customerId;
    private static long lastActivityMillis;
    private static Timeline timeoutWatcher;

    private UserSession() {
    }

    public static synchronized void start(String id) {
        end();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required.");
        }
        customerId = id;
        lastActivityMillis = System.currentTimeMillis();
    }

    public static synchronized String getCustomerId() {
        return customerId;
    }

    public static void watch(Scene scene, Runnable onTimeout) {
        if (scene == null || onTimeout == null) {
            throw new IllegalArgumentException("Scene and timeout action are required.");
        }

        scene.addEventFilter(InputEvent.ANY, event -> recordActivity());

        synchronized (UserSession.class) {
            if (timeoutWatcher != null) {
                timeoutWatcher.stop();
            }

            timeoutWatcher = new Timeline(new KeyFrame(Duration.seconds(15),
                    (ActionEvent event) -> {
                        if (isExpired()) {
                            end();
                            onTimeout.run();
                        }
                    }));
            timeoutWatcher.setCycleCount(Timeline.INDEFINITE);
            timeoutWatcher.play();
        }
    }

    public static synchronized void end() {
        customerId = null;
        lastActivityMillis = 0L;
        if (timeoutWatcher != null) {
            timeoutWatcher.stop();
            timeoutWatcher = null;
        }
    }

    private static synchronized void recordActivity() {
        if (customerId != null) {
            lastActivityMillis = System.currentTimeMillis();
        }
    }

    private static synchronized boolean isExpired() {
        return customerId != null
                && System.currentTimeMillis() - lastActivityMillis >= TIMEOUT_MILLIS;
    }
}

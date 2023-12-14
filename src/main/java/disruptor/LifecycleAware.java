package disruptor;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.EventHandler;

/**
 * Implement this interface in your {@link EventHandler} to be notified when a thread for the {@link BatchEventProcessor} starts and shuts down.
 */
public interface LifecycleAware {

  /**
   * Called once on thread start before first event is available.
   */
  void onStart();

  /**
   * <p>Called once just before the thread is shutdown.</p>
   * <p>
   * Sequence event processing will already have stopped before this method is called. No events will be processed after this message.
   */
  void onShutdown();
}

package disruptor;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Test {

  public static void main(String[] args) {
    ExecutorService executorService = Executors.newCachedThreadPool();
    // 对象工厂。
    final EventFactory<User> eventFactory = new EventFactory<User>() {
      @Override
      public User newInstance() {
        return new User();
      }
    };
    final ProducerType producerType = ProducerType.MULTI;
    // 等待策略。
    final WaitStrategy waitStrategy = new BusySpinWaitStrategy();
    // 创建disruptor。
    Disruptor2<User> disruptor = new Disruptor2<>(eventFactory, 1024, new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        return new Thread(r, "test");
      }
    }, producerType, waitStrategy, executorService);
    // 添加异常处理。
    final ExceptionHandler<User> errorHandler = new ExceptionHandler<>() {
      @Override
      public void handleEventException(Throwable ignore, long sequence, User event) {
        //
      }

      @Override
      public void handleOnStartException(Throwable ignore) {
        //
      }

      @Override
      public void handleOnShutdownException(Throwable ignore) {
        //
      }
    };
    disruptor.setDefaultExceptionHandler(errorHandler);
    // 使用多消费者。
    RingBufferLogWorkHandler[] workHandlers = new RingBufferLogWorkHandler[4];
    for (int i = 0; i < workHandlers.length; i++) {
      workHandlers[i] = new RingBufferLogWorkHandler();
    }
    // 使用多消费者。
    disruptor.handleEventsWithWorkerPool(workHandlers);
    // 启动disruptor。
    disruptor.start();
    RingBufferLogEventTranslator eventTranslator = new RingBufferLogEventTranslator();

    for (int i = 0; i < 100; i++) {
      eventTranslator.setName("" + i);
      disruptor.publishEvent(eventTranslator);
    }
    disruptor.shutdown();

    executorService.shutdownNow();

    System.out.println("xxxxxxxx");
  }
}

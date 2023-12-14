package disruptor;

public interface EventReleaseAware {
  void setEventReleaser(EventReleaser eventReleaser);
}


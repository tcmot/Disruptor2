package disruptor;


public class RingBufferLogWorkHandler implements WorkHandler<User> {

  @Override
  public void onEvent(User event) throws Exception {
    System.out.println(event.getUsername());
  }
}

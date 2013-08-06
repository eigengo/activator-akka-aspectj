package monitor;

/**
 * @author janmachacek
 */
public class ActorSystemPerformanceMXBeanImpl implements ActorSystemPerformanceMXBean{
    private ActorSystemMessages messages;

    ActorSystemPerformanceMXBeanImpl(ActorSystemMessages messages) {
        this.messages = messages;
    }

    @Override
    public float getMessagesPerSecond() {

        return this.messages.average();
    }

}

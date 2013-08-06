package monitor;

import javax.management.*;
import java.lang.management.ManagementFactory;

/**
 * @author janmachacek
 */
public class JMXEndpoint {
    public static void start(ActorSystemMessages messages) throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("monitor:type=Performance");
        ActorSystemPerformanceMXBeanImpl mbean = new ActorSystemPerformanceMXBeanImpl(messages);
        mbs.registerMBean(mbean, name);
    }
}

#Monitoring Akka

#Monitoring Akka
Imagine that you need to find out how your actors are performing, but without using the [Typesafe Console](http://typesafe.com/platform/runtime/console). (You don't actually want to use this code in production, but it is an interesting project to learn.) What we are interested is intercepting the ``receiveMessage`` call of the ``ActorCell`` class.

#A short introduction to AOP
We could roll our own build of Akka, but that is _not a good idea_. Instead, we would like to somehow modify the bytecode of the existing Akka JARs to include our instrumentation code. It turns out that we can use _aspect oriented programming_ to implement cross-cutting concerns in our code. 

Cross-cutting concerns are pieces of code that cut accross the object hierarchy; in other words, we inject the same functionality to different levels of our class structure. In OOP, the only way to share functionality is by inheritance. I will explain the simplest case here, but the same rules apply to mixin inheritance. If I have

```scala
class A {
  def foo: Int = 42
}
class B {
  def bar: Int = 42
}
class AA extends A
class BB extends B
```

Then the only way to share the implementation of ``foo`` is to extend ``A``; the only way to share the implementation of ``bar`` is to extend ``B``. Suppose we now wanted to measure how many times we call ``foo`` on all subtypes of ``A`` and ``bar``, but only in subtypes of ``BB``. This now turns out to be rather clumsy. Even if all we do is ``println("Executing foo")`` or ``println("Executing bar")``, we have a lot of duplication on our hands.

Instead, we would like to write something like this:

```scala
before executing A.foo {
  println("Executing foo")
}

before executing BB.bar {
  println("Executing bar")	
}
```

And have some machinery apply this to the classes that make up our system. Importantly, we would like this logic to be applied to every subtype of ``A`` and every subtype of ``BB``, even if we only have their compiled forms in a JAR.

#Enter AspectJ
And this is exactly what AspectJ does. It allows us to define these cross-cutting concerns and to _weave_ them into our classes. The weaving can be done at compile time, or even at class load time. In other words, the AspectJ weaver can modify the classes with our cross-cutting concerns as the JVM loads them. 

The technical details now involve translating our ``before executing`` pseudo-syntax into the syntax that AspectJ can use. We are going to be particularly lazy and use AspectJ's Java syntax.

```java
package monitor;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

@Aspect
public class MonitorAspect {

  @Pointcut(
    value = "execution (* akka.actor.ActorCell.receiveMessage(..)) && args(msg)", 
    argNames = "msg")
  public void receiveMessagePointcut(Object msg) {}

  @Before(value = "receiveMessagePointcut(msg)", argNames = "jp,msg")
  public void message(JoinPoint jp, Object msg) {
    // log the message
    System.out.println("Message " + msg);
  }
}
```

The annotations come from the AspectJ dependencies, which, in SBT syntax are just:

```scala
"org.aspectj" % "aspectjweaver" % "1.7.2",
"org.aspectj" % "aspectjrt"     % "1.7.2"
```

Excellent. We have defined a _pointcut_, which you can imagine as a target in the structure of our system. In this particular case, the pointcut says on execution of the ``receiveMessage`` method in ``akka.actor...ActorCell`` class, with any parameters of any type ``(..)``, returning any type ``*``; as long as the argument is called ``msg`` and inferred to be of type ``Object``. Without an _advice_ though, a pointcut would be useless--just like having a method that is never called. An advice applies our logic at the point identified by the pointcut. Here, we have the ``message`` advice, which runs on execution of the methods matched by the ``receiveMssagePointcut``. At the moment, it does nothing of importance.

To see this in action, we need to tell the JVM to use the AspectJ weaver. We do so by specifying the Java agent, which registers a Java ``Instrumentation`` implementation, which performs the class file transformation. However, this transformation is costly. Imagine if we had to re-compile every class as it is loaded. To restrict the scope of the transformations, the AspectJ load-time weaver loads an XML file (boo, hiss!; I know) from ``META-INF/aop.xml``, which includes its settings.

```xml
<aspectj>

  <aspects>
    <aspect name="monitor.MonitorAspect"/>
  </aspects>

  <weaver options="-XnoInline">
    <include within="monitor.*"/>
    <include within="akka.actor.*"/>
  </weaver>

</aspectj>
```

Now, to get it all running, all you need to do is to include the ``javaagent:~/path-to/aspectjweaver.jar`` when we start the JVM.

#Measuring
Now that we have our ``monitor.MonitoringAspect`` and ``META-INF/aop.xml`` ready, all we need to do is to implement the logic that records the messages and prints out their per-second averages. I will leave it to the curious reader to come up with _much better_ approach, but here is one that works; all be it in a very naive way:

```java
package monitor;

import java.util.HashMap;
import java.util.Map;

class ActorSystemMessages {
    private final Map<Long, Integer> messages = new HashMap<>();

    void recordMessage() {
        long second = System.currentTimeMillis() / 1000;
        int count = 0;
        if (messages.containsKey(second)) {
            count = messages.get(second);
        }
        messages.put(second, ++count);
    }

    float average() {
        if (messages.isEmpty()) return 0;
        int total = 0;
        for (Integer i : messages.values()) {
            total += i;
        }

        return total / messages.size();
    }
}
```

We can now use it in our ``MonitorAspect``:

```java
@Aspect
public class MonitorAspect {
    private ActorSystemMessages asm = new ActorSystemMessages();

    @Pointcut(
        value = "execution (* akka.actor.ActorCell.receiveMessage(..)) && args(msg)", 
        argNames = "msg")
    public void receiveMessagePointcut(Object msg) {}

    @Before(value = "receiveMessagePointcut(msg)", argNames = "jp,msg")
    public void message(JoinPoint jp, Object msg) {
        asm.recordMessage();
        System.out.println("Average throughput " + asm.average());
    }
}
```

#Summary
This article is a simple exploration of AOP (as implemented in AspectJ) and its use in Scala and Akka. The implementation is very simplistic; if you use it in production, I shall endorse you for _Enterprise PHP_ on LinkedIn. However, it is an interesting exercise and really shows how Scala fits well into even the sligtly more esoteric Java libraries. The source code for your compiling pleasure is at [https://github.com/eigengo/activator-akka-aspectj](https://github.com/eigengo/activator-akka-aspectj).
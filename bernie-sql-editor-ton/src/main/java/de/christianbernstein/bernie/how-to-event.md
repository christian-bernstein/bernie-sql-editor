# How-to - *Events*
___

Create an event handler class using `static` context

```java
import de.christianbernstein.bernie.ses.annotations.RegisterEventClass;
import de.christianbernstein.bernie.sdk.union.Event;
import de.christianbernstein.bernie.sdk.union.EventListener;

@RegisterEventClass
class EventListenerClass {

    @EventListener
    private static onEvent(Event event) {
        // [...]
    }
}
```

Create an event handler class using `dynamic` context.</br>
**Important:** Creating event handlers in the dynamic context isn't tested yet, 
it might not work properly of have unwanted side effects! 
Working with locally stored variables can create problems, if the construct-phase is called twice or 
the class is instantiated more than once.

```java
import de.christianbernstein.bernie.ses.annotations.Construct;
import de.christianbernstein.bernie.ses.annotations.RegisterEventClass;
import de.christianbernstein.bernie.sdk.misc.Instance;
import de.christianbernstein.bernie.sdk.union.Event;
import de.christianbernstein.bernie.sdk.union.EventListener;

@Construct
@RegisterEventClass
class EventListenerClass {

    @Instance
    private EventListenerClass instance;

    @EventListener
    private static onEvent(Event event) {
        // [...]
    }
}
```

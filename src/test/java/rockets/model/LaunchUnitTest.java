package rockets.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

//test1 test1111111 wwww

class LaunchUnitTest {
    private Launch target;

    @BeforeEach
    public void setUp() {
        target = new Launch();
    }

    @DisplayName("should return toString")
    @Test
    public void shouldReturnNormalToString() {
        Set<Payloads> payloads = new HashSet<>();
        LaunchServiceProvider launchServiceProvider = new LaunchServiceProvider("rtest", 1, "rtest");
        payloads.add(new Payloads("test", "test", "test"));
        payloads.add(new Payloads("test1", "test1", "test1"));
        target = new Launch(launchServiceProvider, payloads, "test", "test", "test");
        assertEquals("Rocket{launchSite='test', orbit='test', function='test', launchOutcome='null'}", target.toString());
    }

    @DisplayName("should return Launch EventI nfo")
    @Test
    public void shouldReturnLaunchEventInfo() {
        Set<Payloads> payloads = new HashSet<>();
        LaunchServiceProvider launchServiceProvider = new LaunchServiceProvider("rtest", 1, "rtest");
        payloads.add(new Payloads("test", "test", "test"));
        payloads.add(new Payloads("test1", "test1", "test1"));
        target = new Launch(launchServiceProvider, payloads, "test", "test", "test");
        assertEquals("This launch is provided by rtest and its payloads have :[Payloads{name='test', country='test', manufacturer='test}, Payloads{name='test1', country='test1', manufacturer='test1}] and its function is :test and its launch site is in test and its orbit is in test", target.getLaunchEventInfo());
    }

    @DisplayName("should return Payload Info")
    @Test
    public void shouldRPayLoadsInfo() {
        Set<Payloads> payloads = new HashSet<>();
        LaunchServiceProvider launchServiceProvider = new LaunchServiceProvider("rtest", 1, "rtest");
        payloads.add(new Payloads("test", "test", "test"));
        payloads.add(new Payloads("test1", "test1", "test1"));
        target = new Launch(launchServiceProvider, payloads, "test", "test", "test");
        List<String> list = new ArrayList<>();
        for (Payloads payload : payloads) {
            list.add(payload.getName());
        }
        assertEquals("[test, test1]", Arrays.toString(list.toArray()));
    }

    @DisplayName("should return exception if the rockets mass less the payloads mass")
    @Test
    public void shouldReturnExceptionWhenLessGTO() {
        Set<Payloads> payloads = new HashSet<>();
        LaunchServiceProvider launchServiceProvider = new LaunchServiceProvider("rtest", 1, "rtest");
        Set<Rocket> rockets = new HashSet<>();
        Rocket rocket=new Rocket("test","test","test");
        rocket.setMassToGTO(1);
        Payloads payloads1= new Payloads("test", "test", "test");
        payloads1.setMassToGTO(1);
        payloads.add(payloads1);
        Payloads payloads2= new Payloads("test", "test", "test");
        payloads2.setMassToGTO(1);
        payloads.add(payloads2);
//        target = new Launch(launchServiceProvider, payloads, "test", "gto", "test");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> target = new Launch(launchServiceProvider, payloads, "test", "gto", "test"));
        assertEquals("too much loads, system not allow", exception.getMessage());

    }

}
package rockets.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RocketUnitTest {

    private Rocket target;

    @Test
    public void shouldThrowExceptionWhenConstructWithNameNull(){
        LaunchServiceProvider launchServiceProvider = new LaunchServiceProvider("rtest", 1, "rtest");
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> target=new Rocket(null,"test",launchServiceProvider));
        assertEquals("The validated object is null", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenConstructWithCountryNull(){
        LaunchServiceProvider launchServiceProvider = new LaunchServiceProvider("rtest", 1, "rtest");
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> target=new Rocket("test",null,launchServiceProvider));
        assertEquals("The validated object is null", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenConstructWithManuNull(){
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> target=new Rocket("test", "test",null));
        assertEquals("The validated object is null", exception.getMessage());
    }


    @Test
    public void shouldThrowExceptionWhenConstructWithManuRocketNull(){
        LaunchServiceProvider launchServiceProvider = new LaunchServiceProvider("rtest", 1, "rtest");
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> target=new Rocket("test", "test",launchServiceProvider,null));
        assertEquals("The validated object is null", exception.getMessage());
    }

    @DisplayName("should return true when objects equals")
    @Test
    public void shouldReturnTrueWhenObjectEquals() throws Exception {
        LaunchServiceProvider launchServiceProvider = new LaunchServiceProvider("rtest", 1, "rtest");
        target = new Rocket("test", "test", launchServiceProvider);
        Rocket other = new Rocket("test", "test", launchServiceProvider);
        boolean isMatch = target.equals(other);
        assertTrue(isMatch);
    }

    @DisplayName("should return false when objects equals")
    @Test
    public void shouldReturnFalseWhenObjectEqualsNameNot() throws Exception {
        LaunchServiceProvider launchServiceProvider = new LaunchServiceProvider("rtest", 1, "rtest");
        target = new Rocket("test", "test", launchServiceProvider);
        Rocket other = new Rocket("test1", "test", launchServiceProvider);
        boolean isMatch = target.equals(other);
        assertFalse(isMatch);
    }

    @DisplayName("should return false when objects equals")
    @Test
    public void shouldReturnFalseWhenObjectEqualsCountyNot() throws Exception {
        LaunchServiceProvider launchServiceProvider = new LaunchServiceProvider("rtest", 1, "rtest");
        target = new Rocket("test", "test", launchServiceProvider);
        Rocket other = new Rocket("test", "test1", launchServiceProvider);
        boolean isMatch = target.equals(other);
        assertFalse(isMatch);
    }
    @DisplayName("should return false when objects equals")
    @Test
    public void shouldReturnFalseWhenObjectEqualsManuNot() throws Exception {
        LaunchServiceProvider launchServiceProvider1 = new LaunchServiceProvider("rtest", 1, "rtest");
        LaunchServiceProvider launchServiceProvider2 = new LaunchServiceProvider("rtest", 2, "rtest");
        target = new Rocket("test", "test", launchServiceProvider1);
        Rocket other = new Rocket("test", "test", launchServiceProvider2);
        boolean isMatch = target.equals(other);
        assertFalse(isMatch);
    }

    @DisplayName("should return false when objects equals")
    @Test
    public void shouldReturnNormalString() throws Exception {
        LaunchServiceProvider launchServiceProvider = new LaunchServiceProvider("rtest", 1, "rtest");
        target = new Rocket("test", "test", launchServiceProvider);
        assertEquals("Rocket{name='test', country='test'," +
                " manufacturer='LaunchServiceProvider{name='rtest', yearFounded='1', country='rtest'}', massToLEO='0', massToGTO='0', massToOther='0'}",target.toString());
    }
    @DisplayName("should return false when objects equals")
    @Test
    public void shouldReturnNormalHashCode() throws Exception {
        LaunchServiceProvider launchServiceProvider = new LaunchServiceProvider("rtest", 1, "rtest");
        target = new Rocket("test", "test", launchServiceProvider);
        assertEquals(855975333,target.hashCode());
    }







}
package rockets.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class LaunchServiceProviderUnitTest {
    private LaunchServiceProvider target;

    @BeforeEach
    public void setUp() {
        target=new LaunchServiceProvider("test",2,"test");
    }


    @DisplayName("should return to string hash code Info")
    @Test
    public void shouldReturnHashCode() {

        assertEquals(-873586367,target.hashCode());
    }
    @DisplayName("should return true ")
    @Test
    public void shouldReturnFalseWhenEqual() {
        LaunchServiceProvider other=new LaunchServiceProvider("test", 2, "test");

        assertTrue(other.equals(target));
    }
    @DisplayName("should return false ")
    @Test
    public void shouldReturnFalseWhenNotEqual() {
        LaunchServiceProvider other=new LaunchServiceProvider("test1", 2, "test");

        assertFalse(other.equals(target));
    }

}
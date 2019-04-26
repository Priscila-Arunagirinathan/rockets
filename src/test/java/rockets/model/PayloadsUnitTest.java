package rockets.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PayloadsUnitTest {
    private Payloads target;

    @BeforeEach
    public void setUp() {
        target = new Payloads("test", "test", "test");

    }


    @DisplayName("should return to string hash code Info")
    @Test
    public void shouldReturnHashCode() {

        assertEquals(-763334991, target.hashCode());
    }

    @DisplayName("should return true ")
    @Test
    public void shouldReturnFalseWhenEqual() {
        Payloads other= new Payloads("satellites", "union", "test");
        assertNotEquals(other, target);
    }

    @DisplayName("should return false ")
    @Test
    public void shouldReturnFalseWhenNotEqual() {
        Payloads other = new Payloads("test1", "test", "test");

        assertNotEquals(other, target);
    }


}
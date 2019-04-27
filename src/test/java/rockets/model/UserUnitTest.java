package rockets.model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class UserUnitTest {
    private User target;

    @BeforeEach
    public void setUp() {
        target = new User();
    }


    @DisplayName("should throw exception when pass a empty email address to setEmail function")
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    public void shouldThrowExceptionWhenSetEmailToEmpty(String email) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> target.setEmail(email));
        assertEquals("email cannot be null or empty", exception.getMessage());
    }

    @DisplayName("should throw exception when pass a not valid email address")
    @ParameterizedTest
    @ValueSource(strings = {"test", "test.test", "test.@", "test.@xx", "tse@xx."})
    public void shouldReturnErrorWhenEmailNotValid(String email) throws Exception {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> target.setEmail(email));
        assertEquals("not valid email format", exception.getMessage());

    }


    @DisplayName("should throw exception when pass null to setEmail function")
    @Test
    public void shouldThrowExceptionWhenSetEmailToNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> target.setEmail(null));
        assertEquals("email cannot be null or empty", exception.getMessage());
    }

    @DisplayName("should throw exceptions when pass a null password to setPassword function")
    @Test
    public void shouldThrowExceptionWhenSetPasswordToNull() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> target.setPassword(null));
        assertEquals("password cannot be null or empty", exception.getMessage());
    }

    @DisplayName("should return true when two users have the same email")
    @Test
    public void shouldReturnTrueWhenUsersHaveSameEmail() {
        String email = "abc@example.com";
        target.setEmail(email);
        User anotherUser = new User();
        anotherUser.setEmail(email);
        assertTrue(target.equals(anotherUser));
    }


    @DisplayName("should return false when two users have different emails")
    @Test
    public void shouldReturnFalseWhenUsersHaveDifferentEmails() {
        target.setEmail("abc@example.com");
        User anotherUser = new User();
        anotherUser.setEmail("def@example.com");
        assertFalse(target.equals(anotherUser));
    }

    @DisplayName("should return easy when the password just has int or string")
    @Test
    public void shouldReturnEasyWhenThePasswordJustHasInt() {
        target.setPassword("111");
        assertEquals("easy",target.getPasswordDifficulty());

    }
    @DisplayName("should return easy when the password just has int or string")
    @Test
    public void shouldReturnEasyWhenThePasswordJustHasString() {
        target.setPassword("test");
        assertEquals("easy",target.getPasswordDifficulty());

    }

    @DisplayName("should throw exceptions when pass null to setLastName function")
    @Test
    public void shouldThrowExceptionWhenSetLastNameToNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> target.setLastName(null));
        assertEquals("last name cannot be null or empty", exception.getMessage());
    }

    @DisplayName("should throw exception when pass a empty last name to setLastName function")
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    public void shouldThrowExceptionWhenSetLastNameToEmpty(String lastName) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> target.setLastName(lastName));
        assertEquals("last name cannot be null or empty", exception.getMessage());
    }

    @DisplayName("should return 0 when the new user date today")
    @Test
    public void shouldReturnZeroWhenTheNewUserDateToday() throws Exception{
        assertEquals(0,target.getPassDistance());
    }

    @DisplayName("should return false when the password not match")
    @Test
    public void shouldReturnFalseWhenThePasswordNotMath() throws Exception{
        target.setPassword("test");
        boolean isMatch=target.isPasswordMatch("tests");
        assertFalse(isMatch);
    }

    @DisplayName("should return true when the password not match")
    @Test
    public void shouldReturnTrueWhenThePasswordNotMath() throws Exception{
        target.setPassword("test");
        boolean isMatch=target.isPasswordMatch("test");
        assertTrue(isMatch);
    }

    @DisplayName("should return true when objects equals")
    @Test
    public void shouldReturnTrueWhenObjectEquals() throws Exception{
        target.setEmail("email1@u.com");
        User other=new User();
        other.setEmail("email1@u.com");
        boolean isMatch=target.equals(other);
        assertTrue(isMatch);
    }

    @DisplayName("should return false when objects equals")
    @Test
    public void shouldReturnFalseWhenObjectEquals() throws Exception{
        target.setEmail("email1@u.com");
        User other=new User();
        other.setEmail("email2@u.com");
        boolean isMatch=target.equals(other);
        assertFalse(isMatch);
    }

    @DisplayName("should return true when hashcode")
    @Test
    public void shouldReturnTrueWhenHashCode() throws Exception {
        target.setEmail("email1@u.com");
        assertEquals("email1@u.com",target.getEmail());
    }

    @DisplayName("should return true when toString")
    @Test
    public void shouldReturnTrueWhenToString() throws Exception {
        target.setFirstName("first");
        target.setLastName("second");
        target.setEmail("email1@u.com");
        assertEquals("User{firstName='first', lastName='second', email='email1@u.com'}",target.toString());
    }

}
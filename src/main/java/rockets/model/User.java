package rockets.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;


import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

public class User extends Entity {
    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private String passwordSetTime;

    private String passwordDifficulty;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        notBlank(lastName, "last name cannot be null or empty");
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        notBlank(email, "email cannot be null or empty");
        if (!checkIsEmailValid(email)) {
            throw new IllegalArgumentException("not valid email format");
        }
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        notNull(password, "password cannot be null or empty");
        notBlank(password, "password cannot be null or empty");
        this.password = password;
        this.passwordSetTime = df.format(new Date());
    }

    // match the given password against user's password and return the result
    public boolean isPasswordMatch(String password) {
        return this.password.equals(password.trim());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    private boolean checkIsEmailValid(String email) {
        boolean isValid = false;
        String validEmailRegex = "^([a-z0-9A-Z]+[-|.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(validEmailRegex);
        Matcher matcher = pattern.matcher(email);
        isValid = matcher.matches();
        return isValid;
    }

    public int getPassDistance() throws ParseException {
        if (this.passwordSetTime==null){
            return 0;
        };
        Date nowTime=new Date();
        Date begintime=df.parse(this.passwordSetTime);
        return (int) ((nowTime.getTime() - begintime.getTime()) / (24 * 60 * 60 * 1000));
    }
}
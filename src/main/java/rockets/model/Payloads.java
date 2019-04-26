package rockets.model;

import java.util.Objects;
import static org.apache.commons.lang3.Validate.notNull;

public class Payloads {
    private String name;

    private String country;

    private String manufacturer;

    private int massToLEO;

    private int massToGTO;

    private int massToOther;

    public Payloads(String name, String country, String manufacturer) {
        notNull(name);
        notNull(country);
        notNull(manufacturer);
        this.name = name;
        this.country = country;
        this.manufacturer = manufacturer;
    }

    public int getMassToLEO() {
        return massToLEO;
    }

    public void setMassToLEO(int massToLEO) {
        this.massToLEO = massToLEO;
    }

    public int getMassToGTO() {
        return massToGTO;
    }

    public void setMassToGTO(int massToGTO) {
        this.massToGTO = massToGTO;
    }

    public int getMassToOther() {
        return massToOther;
    }

    public void setMassToOther(int massToOther) {
        this.massToOther = massToOther;
    }

    public String getName() {
        return name;
    }


    public String getCountry() {
        return country;
    }


    public String getManufacturer() {
        return manufacturer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payloads payloads = (Payloads) o;
        return Objects.equals(name, payloads.name) &&
                Objects.equals(country, payloads.country) &&
                Objects.equals(manufacturer, payloads.manufacturer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, country, manufacturer);
    }

    @Override
    public String toString() {
        return "Payloads{" +
                "name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", manufacturer='" + manufacturer +
                '}';
    }
}

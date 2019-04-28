package rockets.model;

import java.util.Objects;

import static org.apache.commons.lang3.Validate.notNull;

public class Rocket extends Entity {
    private String name;

    private String country;

    private LaunchServiceProvider manufacturer;

    private int massToLEO;

    private int massToGTO;

    private int massToOther;

    private Rocket rocketKind;

    private String characteristics;

    /**
     * All parameters shouldn't be null.
     *
     * @param name
     * @param country
     * @param manufacturer
     */
    public Rocket(String name, String country, LaunchServiceProvider manufacturer) {
        notNull(name);
        notNull(country);
        notNull(manufacturer);

        this.name = name;
        this.country = country;
        this.manufacturer = manufacturer;
    }


    public Rocket(String name, String country, LaunchServiceProvider manufacturer, Rocket rocketKind) {
        notNull(name);
        notNull(country);
        notNull(manufacturer);
        notNull(rocketKind);

        this.name = name;
        this.country = country;
        this.manufacturer = manufacturer;
        if (this.getId()!=null){
            if (!rocketKind.getId().equals(this.getId())){
                this.rocketKind = rocketKind;
            }
        }

    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public LaunchServiceProvider getManufacturer() {
        return manufacturer;
    }

    public int getMassToLEO() {
        return massToLEO;
    }

    public int getMassToGTO() {
        return massToGTO;
    }

    public int getMassToOther() {
        return massToOther;
    }

    public void setMassToLEO(int massToLEO) {
        this.massToLEO = massToLEO;
    }

    public void setMassToGTO(int massToGTO) {
        this.massToGTO = massToGTO;
    }

    public void setMassToOther(int massToOther) {
        this.massToOther = massToOther;
    }

    public Rocket getRocketKind() {
        return rocketKind;
    }

    public String getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(String characteristics) {
        this.characteristics = characteristics;
    }

    public void setRocketKind(Rocket rocketKind) {
        this.rocketKind = rocketKind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rocket rocket = (Rocket) o;
        return Objects.equals(name, rocket.name) &&
                Objects.equals(country, rocket.country) &&
                Objects.equals(manufacturer, rocket.manufacturer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, country, manufacturer);
    }

    @Override
    public String toString() {
        return "Rocket{" +
                "name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", manufacturer='" + manufacturer.toString() + '\'' +
                ", massToLEO='" + massToLEO + '\'' +
                ", massToGTO='" + massToGTO + '\'' +
                ", massToOther='" + massToOther + '\'' +
                '}';
    }
}

package rockets.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.apache.commons.lang3.Validate.notNull;

public class Launch extends Entity {
    public enum LaunchOutcome {
        FAILED, SUCCESSFUL
    }

    private LocalDate launchDate;

    private Rocket launchVehicle;

    private LaunchServiceProvider launchServiceProvider;

    private Set<Payloads> payloads;

    private String launchSite;

    private String orbit;

    private String function;

    private BigDecimal price;

    private LaunchOutcome launchOutcome;

    public LocalDate getLaunchDate() {
        return launchDate;
    }

    public void setLaunchDate(LocalDate launchDate) {
        this.launchDate = launchDate;
    }

    public Rocket getLaunchVehicle() {
        return launchVehicle;
    }

    public void setLaunchVehicle(Rocket launchVehicle) {
        this.launchVehicle = launchVehicle;
    }

    public LaunchServiceProvider getLaunchServiceProvider() {
        return launchServiceProvider;
    }

    public void setLaunchServiceProvider(LaunchServiceProvider launchServiceProvider) {
        this.launchServiceProvider = launchServiceProvider;
    }

    public Set<Payloads> getPayload() {
        return payloads;
    }

    public void setPayload(Set<Payloads> payload) {

        if (orbit.equalsIgnoreCase("gto")){
            int payloadTotalMass = 0;
            for (Payloads payloads1 : payload) {
                payloadTotalMass += payloads1.getMassToGTO();
            }
            int rocketTotalMass=0;
            for (Rocket rocket : this.getLaunchServiceProvider().getRockets()) {
                rocketTotalMass+=rocket.getMassToGTO();
            }

            if (payloadTotalMass<=rocketTotalMass){
                this.payloads=payload;
            }else{
                throw new IllegalArgumentException("too much loads, system not allow");
            }
        }else if (orbit.equalsIgnoreCase("leo")) {
            int payloadTotalMass = 0;
            for (Payloads payloads1 : payload) {
                payloadTotalMass += payloads1.getMassToLEO();
            }
            int rocketTotalMass = 0;
            for (Rocket rocket : this.getLaunchServiceProvider().getRockets()) {
                rocketTotalMass += rocket.getMassToLEO();
            }

            if (payloadTotalMass <= rocketTotalMass) {
                this.payloads = payload;
            } else {
                throw new IllegalArgumentException("too much loads, system not allow");
            }
        }else {
            int payloadTotalMass = 0;
            for (Payloads payloads1 : payload) {
                payloadTotalMass += payloads1.getMassToOther();
            }
            int rocketTotalMass = 0;
            for (Rocket rocket : this.getLaunchServiceProvider().getRockets()) {
                rocketTotalMass += rocket.getMassToOther();
            }

            if (payloadTotalMass <= rocketTotalMass) {
                this.payloads = payload;
            } else {
                throw new IllegalArgumentException("too much loads, system not allow");
            }
        }



        this.payloads = payload;
    }

    public String getLaunchSite() {
        return launchSite;
    }

    public void setLaunchSite(String launchSite) {
        this.launchSite = launchSite;
    }

    public String getOrbit() {
        return orbit;
    }

    public void setOrbit(String orbit) {
        this.orbit = orbit;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LaunchOutcome getLaunchOutcome() {
        return launchOutcome;
    }

    public Launch(LaunchServiceProvider launchServiceProvider, String launchSite, String orbit, String function) {
        notNull(launchServiceProvider);
        notNull(launchSite);
        notNull(orbit);
        notNull(function);
        this.launchServiceProvider = launchServiceProvider;
        this.launchSite = launchSite;
        this.orbit = orbit;
        this.function = function;
    }

    public Launch(LaunchServiceProvider launchServiceProvider, Set<Payloads> payloads, String launchSite, String orbit, String function) {
        notNull(launchServiceProvider);
        notNull(payloads);
        notNull(launchSite);
        notNull(orbit);
        notNull(function);
        this.orbit = orbit;
        this.launchServiceProvider = launchServiceProvider;
        this.setPayload(payloads);
        this.launchSite = launchSite;

        this.function = function;
    }

    public Launch() {
    }

    public void setLaunchOutcome(LaunchOutcome launchOutcome) {

        this.launchOutcome = launchOutcome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Launch launch = (Launch) o;
        return Objects.equals(launchDate, launch.launchDate) &&
                Objects.equals(launchVehicle, launch.launchVehicle) &&
                Objects.equals(launchServiceProvider, launch.launchServiceProvider) &&
                Objects.equals(orbit, launch.orbit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(launchDate, launchVehicle, launchServiceProvider, orbit);
    }

    @Override
    public String toString(){
        return "Rocket{" +
                "launchSite='" + launchSite + '\'' +
                ", orbit='" + orbit + '\'' +
                ", function='" + function + '\'' +
                ", launchOutcome='" + launchOutcome + '\'' +
                '}';
    }

    public String getLaunchEventInfo(){
        return "This launch is provided by "+launchServiceProvider.getName()+" " +
                "and its payloads have :"+ Arrays.toString(new ArrayList<>(payloads).toArray())+" " +
                "and its function is :"+function+
                " and its launch site is in "+launchSite+
                " and its orbit is in "+ orbit;
    }

}

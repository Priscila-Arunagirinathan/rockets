package rockets.mining;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rockets.dataaccess.DAO;
import rockets.model.Launch;
import rockets.model.LaunchServiceProvider;
import rockets.model.Payloads;
import rockets.model.Rocket;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.math.BigDecimal.ROUND_HALF_DOWN;
import static rockets.model.Launch.LaunchOutcome.FAILED;

public class RocketMiner {
    private static Logger logger = LoggerFactory.getLogger(RocketMiner.class);

    private DAO dao;

    public RocketMiner(DAO dao) {
        this.dao = dao;
    }

    static class ReliableServer{
        // Record the number of successes
        public BigDecimal success;
        // Record the number of failures
        public BigDecimal failed;
        // Recording success rate
        public BigDecimal percentage;
    }

    /**
     * TODO: to be implemented & tested!
     * Returns the top-k most active rockets, as measured by number of completed launches.
     *
     * @param k the number of rockets to be returned.
     * @return the list of k most active rockets.
     */
    public List<Rocket> mostLaunchedRockets(int k) {
        logger.info("Returns the top "+k+" most active rockets, as measured by number of completed launches.");
        // get the lauch list from the database
        Collection<Launch> launches = dao.loadAll(Launch.class);
        // get the rocket list from the database
        Collection<Rocket> rockets = dao.loadAll(Rocket.class);
        //Calculate the number of launches per rocket from the launch list and rank them in reverse order
        List<Rocket> result = launches.stream().map(s->s.getLaunchVehicle())
                .collect(Collectors.groupingBy(s->s,Collectors.counting()))
                .entrySet().stream().sorted(new Comparator<Map.Entry<Rocket, Long>>() {
                    @Override
                    public int compare(Map.Entry<Rocket, Long> o1, Map.Entry<Rocket, Long> o2) {
                        return (int)(o2.getValue()-o1.getValue());
                    }
                }).map(s->s.getKey()).collect(Collectors.toList());
        //Insert rockets that are not in the answer list at the end of the list.
        for(Rocket r:rockets){
            if(!result.contains(r)){
                result.add(r);
            }
        }
        return result.stream().limit(k).collect(Collectors.toList());
    }

    /**
     * TODO: to be implemented & tested!
     * <p>
     * Returns the top-k most reliable launch service providers as measured
     * by percentage of successful launches.
     *
     * @param k the number of launch service providers to be returned.
     * @return the list of k most reliable ones.
     */
    public List<LaunchServiceProvider> mostReliableLaunchServiceProviders(int k) {
        logger.info("Returns the top "+k+" most reliable launch service providers as measured by percentage of successful launches.");
        //get the lauch list from the database
        Collection<Launch> launches = dao.loadAll(Launch.class);
        // get the launch service provider list from the database
        Collection<LaunchServiceProvider> lsps = dao.loadAll(LaunchServiceProvider.class);
        //Define a Hashmap and use the helper class ReliableServer to record the number of successful and failed launches
        // and the success rate of each service provider
        HashMap<LaunchServiceProvider,ReliableServer> reliableServers = new HashMap<LaunchServiceProvider,ReliableServer>();
        for(Launch l:launches){
            // This provider has already appeared in previous records
            if(reliableServers.containsKey(l.getLaunchServiceProvider())) {
                //ReliableServer Get ReliableServer from the provider
                ReliableServer oldValue = reliableServers.get(l.getLaunchServiceProvider());
                //Update the provider's ReliableServer based on whether the current traversal launch failed or succeeded
                if(l.getLaunchOutcome()==FAILED){
                    oldValue.failed = oldValue.failed.add(new BigDecimal(1));
                }else{
                    oldValue.success = oldValue.success.add(new BigDecimal(1));
                }
                //Computing Service Provider's Current Launch Success Rate
                oldValue.percentage = oldValue.success.divide(oldValue.failed.add(oldValue.success),10,ROUND_HALF_DOWN);
                reliableServers.replace(l.getLaunchServiceProvider(),oldValue);
            }else{
                //Initialize ReliableServer of the current service provider based on the success or failure of this launch
                ReliableServer server = new ReliableServer();
                if(l.getLaunchOutcome()==FAILED){
                    server.failed = new BigDecimal(1);
                    server.success = new BigDecimal(0);
                    server.percentage = new BigDecimal(1);
                }else{
                    server.failed = new BigDecimal(0);
                    server.success = new BigDecimal(1);
                    server.percentage = new BigDecimal(0);
                }
                reliableServers.put(l.getLaunchServiceProvider(),server);
            }
        }
        //Ranking of launching service providers according to the success rate of launching
        List<Map.Entry<LaunchServiceProvider,ReliableServer>> list = new ArrayList<Map.Entry<LaunchServiceProvider,ReliableServer>>(reliableServers.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<LaunchServiceProvider, ReliableServer>>() {
            @Override
            public int compare(Map.Entry<LaunchServiceProvider, ReliableServer> o1, Map.Entry<LaunchServiceProvider, ReliableServer> o2) {
                return o2.getValue().percentage.subtract(o1.getValue().percentage).compareTo(BigDecimal.ZERO);
            }
        });
        List<LaunchServiceProvider> result = list.stream().map(a->a.getKey()).collect(Collectors.toList());
        //Insert a service provider that is not in the result list at the end of the queue
        for(LaunchServiceProvider p:lsps){
            if(!result.contains(p)) {
                result.add(p);
            }
        }
        return result.stream().limit(k).collect(Collectors.toList());
    }

    /**
     * <p>
     * Returns the top-k most recent launches.
     *
     * @param k the number of launches to be returned.
     * @return the list of k most recent launches.
     */
    public List<Launch> mostRecentLaunches(int k) {
        logger.info("find most recent " + k + " launches");
        Collection<Launch> launches = dao.loadAll(Launch.class);
        Comparator<Launch> launchDateComparator = (a, b) -> -a.getLaunchDate().compareTo(b.getLaunchDate());
        return launches.stream().sorted(launchDateComparator).limit(k).collect(Collectors.toList());
    }

    /**
     * TODO: to be implemented & tested!
     * <p>
     * Returns the dominant country who has the most launched rockets in an orbit.
     *
     * @param orbit the orbit
     * @return the country who sends the most launched rockets in an orbit.
     */
    public String dominantCountry(String orbit) {
        logger.info("Returns the dominant country who has the most launched rockets in "+orbit);
        Collection<Launch> launches =  dao.loadAll(Launch.class);
        return launches.stream().filter(a->a.getOrbit().equals(orbit)).map(s->s.getLaunchServiceProvider().getCountry())
                .collect(Collectors.groupingBy(s->s,Collectors.counting()))
                .entrySet().stream().sorted(new Comparator<Map.Entry<String, Long>>() {
                    @Override
                    public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                        return (int)(o2.getValue()-o1.getValue());
                    }
                }).collect(Collectors.toList()).get(0).getKey();

    }

    /**
     * TODO: to be implemented & tested!
     * <p>
     * Returns the top-k most expensive launches.
     *
     * @param k the number of launches to be returned.
     * @return the list of k most expensive launches.
     */
    public List<Launch> mostExpensiveLaunches(int k) {
        logger.info("Returns the top "+k+" most expensive launches.");
        Collection<Launch> launches =  dao.loadAll(Launch.class);
        return launches.stream().sorted(new Comparator<Launch>() {
            @Override
            public int compare(Launch o1, Launch o2) {
                return o2.getPrice().subtract(o1.getPrice()).compareTo(BigDecimal.ZERO);
            }
        }).limit(k).collect(Collectors.toList());
    }

    /**
     * TODO: to be implemented & tested!
     * <p>
     * Returns a list of launch service provider that has the top-k highest
     * sales revenue in a year.
     *
     * @param k the number of launch service provider.
     * @param year the year in request
     * @return the list of k launch service providers who has the highest sales revenue.
     */
    public List<LaunchServiceProvider> highestRevenueLaunchServiceProviders(int k, int year) {
        logger.info("Returns a list of launch service provider that has the top "+k+" highest sales revenue.");
        Collection<Launch> launches = dao.loadAll(Launch.class);
        Collection<LaunchServiceProvider> lsps = dao.loadAll(LaunchServiceProvider.class);
        HashMap<LaunchServiceProvider,BigDecimal> result = new HashMap<LaunchServiceProvider,BigDecimal>();
        //go through launch record which launch date equals year
        for(Launch l:launches.stream().filter(s->s.getLaunchDate().getYear()==year).collect(Collectors.toList())){
            //Calculate the sales of each service provider
            if(result.containsKey(l.getLaunchServiceProvider())){
                BigDecimal oldValue = result.get(l.getLaunchServiceProvider());
                result.replace(l.getLaunchServiceProvider(),oldValue.add(l.getPrice()));
            }else{
                result.put(l.getLaunchServiceProvider(),l.getPrice());
            }
        }
        //Sort service providers according to sales
        List<LaunchServiceProvider> list =  result.entrySet().stream().sorted(new Comparator<Map.Entry<LaunchServiceProvider, BigDecimal>>() {
            @Override
            public int compare(Map.Entry<LaunchServiceProvider, BigDecimal> o1, Map.Entry<LaunchServiceProvider, BigDecimal> o2) {
                return o2.getValue().subtract(o1.getValue()).compareTo(BigDecimal.ZERO);
            }
        }).map(s->s.getKey()).collect(Collectors.toList());
        //Insert the non-launched launch service provider at the end of the result list
        for(LaunchServiceProvider p:lsps){
            if(!list.contains(p)) {
                list.add(p);
            }
        }
        return list.stream().limit(k).collect(Collectors.toList());
    }




    //Extra part
    /**
     *
     * Returns the dominant launch server provider who has the most launched rockets in an orbit.
     *
     * @param orbit the orbit
     * @return the dominant launch server provider who has the most launched rockets in an orbit.
     */
    public LaunchServiceProvider dominantLaunchServiceProvider(String orbit){
        logger.info("Returns the launch server provider who has the most launched rockets in "+orbit);
        Collection<Launch> launches =  dao.loadAll(Launch.class);
        return launches.stream().filter(a->a.getOrbit().equals(orbit)).map(s->s.getLaunchServiceProvider())
                .collect(Collectors.groupingBy(s->s,Collectors.counting()))
                .entrySet().stream().sorted(new Comparator<Map.Entry<LaunchServiceProvider, Long>>() {
                    @Override
                    public int compare(Map.Entry<LaunchServiceProvider, Long> o1, Map.Entry<LaunchServiceProvider, Long> o2) {
                        return (int)(o2.getValue()-o1.getValue());
                    }
                }).collect(Collectors.toList()).get(0).getKey();
    }

    /**
     *  return the lauch which payloads is the top-k lightest
     * @param k the number of launch.
     * @return the list of k lauch which payloads is lightest
     */
    public List<Launch> lightestPayloadsLauchInLEO(int k){
        logger.info("Returns the lauch which payloads is the top "+k+" lightest");
        Collection<Launch> launches =  dao.loadAll(Launch.class);
        HashMap<Launch,Integer> launchPayloadsInLEO = new HashMap<Launch,Integer>();
        for(Launch l:launches){
            if(launchPayloadsInLEO.containsKey(l)){
                int oldValue = launchPayloadsInLEO.get(l);
                for(Payloads p:l.getPayload()){
                    oldValue+=p.getMassToLEO();
                }
                launchPayloadsInLEO.replace(l,oldValue);
            }else{
                int newValue = 0;
                for(Payloads p:l.getPayload()){
                    newValue+=p.getMassToLEO();
                }
                launchPayloadsInLEO.put(l,newValue);
            }
        }
        List<Map.Entry<Launch,Integer>> list = new ArrayList<Map.Entry<Launch,Integer>>(launchPayloadsInLEO.entrySet());
        return list.stream().sorted(new Comparator<Map.Entry<Launch, Integer>>() {
            @Override
            public int compare(Map.Entry<Launch, Integer> o1, Map.Entry<Launch, Integer> o2) {
                return o1.getValue()-o2.getValue();
            }
        }).map(s->s.getKey()).limit(k).collect(Collectors.toList());
    }

    /**
     * return the country who sends the most payloads in an orbit.
     * @param orbit the orbit
     * @return the country who sends the most payloads in an orbit.
     */
    public String dominantCountryInPayLoads(String orbit){
        logger.info("Returns the dominant country who has the most payloads in "+orbit);
        Collection<Launch> launches =  dao.loadAll(Launch.class);
        //Define a HashMap for storing country names and load
        HashMap<String,Integer> payloadInCountry = new HashMap<String,Integer>();
        for(Launch l:launches) {
            if(payloadInCountry.containsKey(l.getLaunchServiceProvider().getCountry())){
                int oldValue = payloadInCountry.get(l.getLaunchServiceProvider().getCountry());
                for(Payloads pay : l.getPayload()){
                    if("leo".equals(orbit.toLowerCase())) {
                        oldValue += pay.getMassToLEO();
                    }else if("gto".equals(orbit.toLowerCase())){
                        oldValue += pay.getMassToGTO();
                    }else if("other".equals(orbit.toLowerCase())) {
                        oldValue += pay.getMassToOther();
                    }
                }
                payloadInCountry.replace(l.getLaunchServiceProvider().getCountry(),oldValue);
            }else{
                int newValue = 0;
                for(Payloads pay : l.getPayload()){
                    if("leo".equals(orbit.toLowerCase())) {
                        newValue += pay.getMassToLEO();
                    }else if("gto".equals(orbit.toLowerCase())){
                        newValue += pay.getMassToGTO();
                    }else if("other".equals(orbit.toLowerCase())) {
                        newValue += pay.getMassToOther();
                    }
                }
                payloadInCountry.put(l.getLaunchServiceProvider().getCountry(),newValue);
            }
        }
        return payloadInCountry.entrySet().stream().sorted(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue()-o1.getValue();
            }
        }).map(s->s.getKey()).collect(Collectors.toList()).get(0);
    }
}

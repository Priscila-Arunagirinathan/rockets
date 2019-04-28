package rockets.mining;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rockets.dataaccess.DAO;
import rockets.dataaccess.neo4j.Neo4jDAO;
import rockets.model.Launch;
import rockets.model.LaunchServiceProvider;
import rockets.model.Payloads;
import rockets.model.Rocket;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.math.BigDecimal.ROUND_HALF_DOWN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static rockets.model.Launch.LaunchOutcome.FAILED;
import static rockets.model.Launch.LaunchOutcome.SUCCESSFUL;

public class RocketMinerUnitTest {
    Logger logger = LoggerFactory.getLogger(RocketMinerUnitTest.class);

    private DAO dao;
    private RocketMiner miner;
    private List<Rocket> rockets;
    private List<LaunchServiceProvider> lsps;
    private List<Launch> launches;

    @BeforeEach
    public void setUp() {
        dao = mock(Neo4jDAO.class);
        miner = new RocketMiner(dao);
        rockets = Lists.newArrayList();

        lsps = Arrays.asList(
                new LaunchServiceProvider("ULA", 1990, "USA"),
                new LaunchServiceProvider("SpaceX", 2002, "USA"),
                new LaunchServiceProvider("ESA", 1975, "Europe ")
        );

        // index of lsp of each rocket
        int[] lspIndex = new int[]{0, 0, 0, 1, 1};
        // 5 rockets
        for (int i = 0; i < 5; i++) {
            rockets.add(new Rocket("rocket_" + i, "USA", lsps.get(lspIndex[i])));
        }
        // month of each launch
        int[] months = new int[]{1, 6, 4, 3, 4, 11, 6, 5, 12, 5};

        // index of rocket of each launch
        int[] rocketIndex = new int[]{0, 0, 0, 0, 1, 1, 1, 2, 2, 3};

        // index of each launchServiceProvider
        int[] serviceProviderIndex = new int[]{0, 0, 0, 0, 0, 1, 1, 1, 2, 2};

        // OutCome of each launch
        Launch.LaunchOutcome[] outcome = new Launch.LaunchOutcome[]{FAILED,SUCCESSFUL,FAILED,SUCCESSFUL,FAILED,
                SUCCESSFUL,FAILED,SUCCESSFUL,FAILED,SUCCESSFUL};

        //price of each launch
        int[] prices = new int[]{189,249,235,467,456,123,334,556,889,122};

        // 10 launches
        launches = IntStream.range(0, 10).mapToObj(i -> {
            logger.info("create " + i + " launch in month: " + months[i]);
            Set<Payloads> payloads = new HashSet<Payloads>();
            HashMap<String,Integer> mass = initPayLoads(payloads);
            Launch l = new Launch();
            l.setLaunchDate(LocalDate.of(2017, months[i], 1));
            Rocket rocket = rockets.get(rocketIndex[i]);
            rocket.setMassToGTO(mass.get("GTO")+100);
            rocket.setMassToLEO(mass.get("LEO")+100);
            rocket.setMassToOther(mass.get("Other")+100);
            l.setLaunchVehicle(rocket);
            l.setLaunchOutcome(outcome[i]);
            l.setLaunchServiceProvider(lsps.get(lspIndex[Integer.parseInt(rockets.get(rocketIndex[i]).getName().split("_")[1])]));
            l.setPrice(new BigDecimal(prices[i]));
            l.setLaunchSite("VAFB");
            l.setOrbit("LEO");
            l.setPayload(payloads);
            spy(l);
            return l;
        }).collect(Collectors.toList());
    }

    /**
     * return the map of total mass and int the payloads
     * @return the map of total mass
     */
    public HashMap<String,Integer> initPayLoads(Set<Payloads> payLoads){
        int leo = 0;
        int gto = 0;
        int other = 0;
        for(int i=0;i<(int)(Math.random()*10)+10;i++){
            Payloads pay = new Payloads("Pay_"+i,"USA","SPACEX");
            pay.setMassToLEO((int)(Math.random()*300)+100);
            leo += pay.getMassToLEO();
            pay.setMassToGTO((int)(Math.random()*100)+50);
            gto += pay.getMassToGTO();
            pay.setMassToOther((int)(Math.random()*10)+10);
            other += pay.getMassToOther();
            payLoads.add(pay);
        }
        HashMap<String,Integer> result = new HashMap<String,Integer>();
        result.put("LEO",leo);
        result.put("GTO",gto);
        result.put("Other",other);
        return result;
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    public void shouldReturnTopMostRecentLaunches(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<Launch> sortedLaunches = new ArrayList<>(launches);
        sortedLaunches.sort((a, b) -> -a.getLaunchDate().compareTo(b.getLaunchDate()));
        List<Launch> loadedLaunches = miner.mostRecentLaunches(k);
        assertEquals(k, loadedLaunches.size());
        assertEquals(sortedLaunches.subList(0, k), loadedLaunches);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    public void shouldReturnTopMostLaunchedRockets(int k){
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        when(dao.loadAll(Rocket.class)).thenReturn(rockets);
        List<Rocket> result = launches.stream().map(s->s.getLaunchVehicle())
                .map(array-> Stream.of(array)).flatMap(stream->stream).collect(Collectors.groupingBy(s->s,Collectors.counting()))
                .entrySet().stream().sorted(new Comparator<Map.Entry<Rocket, Long>>() {
                    @Override
                    public int compare(Map.Entry<Rocket, Long> o1, Map.Entry<Rocket, Long> o2) {
                        return (int)(o2.getValue()-o1.getValue());
                    }
                }).map(s->s.getKey()).collect(Collectors.toList());
        for(Rocket r:rockets){
            if(!result.contains(r)){
                result.add(r);
            }
        }
        List<Rocket> loadedRockets = miner.mostLaunchedRockets(k);
        assertEquals(k,loadedRockets.size());
        assertEquals(result.subList(0, k), loadedRockets);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    public void shouldReturnMostReliableLaunchServiceProviders(int k){
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        when(dao.loadAll(LaunchServiceProvider.class)).thenReturn(lsps);
        HashMap<LaunchServiceProvider, RocketMiner.ReliableServer> reliableServers = new HashMap<LaunchServiceProvider, RocketMiner.ReliableServer>();
        for(Launch l:launches){
            if(reliableServers.containsKey(l.getLaunchServiceProvider())) {
                RocketMiner.ReliableServer oldValue = reliableServers.get(l.getLaunchServiceProvider());
                if(l.getLaunchOutcome()==FAILED){
                    oldValue.failed = oldValue.failed.add(new BigDecimal(1));
                }else{
                    oldValue.success = oldValue.success.add(new BigDecimal(1));
                }
                oldValue.percentage = oldValue.success.divide(oldValue.failed.add(oldValue.success),10,ROUND_HALF_DOWN);
                reliableServers.replace(l.getLaunchServiceProvider(),oldValue);
            }else{
                RocketMiner.ReliableServer server = new RocketMiner.ReliableServer();
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
        List<Map.Entry<LaunchServiceProvider, RocketMiner.ReliableServer>> list = new ArrayList<Map.Entry<LaunchServiceProvider, RocketMiner.ReliableServer>>(reliableServers.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<LaunchServiceProvider, RocketMiner.ReliableServer>>() {
            @Override
            public int compare(Map.Entry<LaunchServiceProvider, RocketMiner.ReliableServer> o1, Map.Entry<LaunchServiceProvider, RocketMiner.ReliableServer> o2) {
                return o2.getValue().percentage.subtract(o1.getValue().percentage).compareTo(BigDecimal.ZERO);
            }
        });
        List<LaunchServiceProvider> result = list.stream().map(a->a.getKey()).filter(a->a!=null).collect(Collectors.toList());
        for(LaunchServiceProvider p:lsps){
            if(!result.contains(p)) {
                result.add(p);
            }
        }
        result = result.stream().limit(k).collect(Collectors.toList());
        List<LaunchServiceProvider> loadedProviders = miner.mostReliableLaunchServiceProviders(k);
        assertEquals(k,loadedProviders.size());
        assertEquals(result.subList(0,k),loadedProviders);
    }

    @ParameterizedTest
    @ValueSource(strings = {"LEO"})
    public void shouldReturnDominantCountry(String orbit){
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        String country = launches.stream().filter(a->a.getOrbit().equals(orbit)).map(s->s.getLaunchServiceProvider().getCountry())
                .map(array-> Stream.of(array)).flatMap(stream->stream).collect(Collectors.groupingBy(s->s,Collectors.counting()))
                .entrySet().stream().sorted(new Comparator<Map.Entry<String, Long>>() {
                    @Override
                    public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                        return (int)(o2.getValue()-o1.getValue());
                    }
                }).collect(Collectors.toList()).get(0).getKey();
        assertEquals(country, miner.dominantCountry(orbit));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    public void shouldMostExpensiveLaunches(int k){
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<Launch> sortedLaunches = launches.stream().sorted(new Comparator<Launch>() {
            @Override
            public int compare(Launch o1, Launch o2) {
                return o2.getPrice().subtract(o1.getPrice()).compareTo(BigDecimal.ZERO);
            }
        }).collect(Collectors.toList());
        List<Launch> loadedLaunches =  miner.mostExpensiveLaunches(k);
        assertEquals(k,loadedLaunches.size());
        assertEquals(sortedLaunches.subList(0,k),loadedLaunches);
    }


    @ParameterizedTest
    @MethodSource("highestRevenueLaunchServiceParamProvider")
    public void shouldHighestRevenueLaunchServiceProviders(int k, int year){
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        when(dao.loadAll(LaunchServiceProvider.class)).thenReturn(lsps);
        HashMap<LaunchServiceProvider,BigDecimal> result = new HashMap<LaunchServiceProvider,BigDecimal>();
        for(Launch l:launches.stream().filter(s->s.getLaunchDate().getYear()==year).collect(Collectors.toList())){
            if(result.containsKey(l.getLaunchServiceProvider())){
                BigDecimal oldValue = result.get(l.getLaunchServiceProvider());
                result.replace(l.getLaunchServiceProvider(),oldValue.add(l.getPrice()));
            }else{
                result.put(l.getLaunchServiceProvider(),l.getPrice());
            }
        }
        List<LaunchServiceProvider> sortedProviders =  result.entrySet().stream().sorted(new Comparator<Map.Entry<LaunchServiceProvider, BigDecimal>>() {
            @Override
            public int compare(Map.Entry<LaunchServiceProvider, BigDecimal> o1, Map.Entry<LaunchServiceProvider, BigDecimal> o2) {
                return o2.getValue().subtract(o1.getValue()).compareTo(BigDecimal.ZERO);
            }
        }).map(s->s.getKey()).collect(Collectors.toList());
        for(LaunchServiceProvider p:lsps){
            if(!sortedProviders.contains(p)) {
                sortedProviders.add(p);
            }
        }
        List<LaunchServiceProvider> loadedProviders = miner.highestRevenueLaunchServiceProviders(k,year);
        assertEquals(k,loadedProviders.size());
        assertEquals(sortedProviders.subList(0,k),loadedProviders);
    }

    static Stream<Arguments> highestRevenueLaunchServiceParamProvider(){
        return Stream.of(
                Arguments.of(1,2017),
                Arguments.of(2,2017),
                Arguments.of(3,2017)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"LEO"})
    public void shouldReturnDominantLaunchServiceProvider(String orbit){
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        LaunchServiceProvider provider =  launches.stream().filter(a->a.getOrbit().equals(orbit)).map(s->s.getLaunchServiceProvider())
                .collect(Collectors.groupingBy(s->s,Collectors.counting()))
                .entrySet().stream().sorted(new Comparator<Map.Entry<LaunchServiceProvider, Long>>() {
                    @Override
                    public int compare(Map.Entry<LaunchServiceProvider, Long> o1, Map.Entry<LaunchServiceProvider, Long> o2) {
                        return (int)(o2.getValue()-o1.getValue());
                    }
                }).collect(Collectors.toList()).get(0).getKey();
        assertEquals(provider, miner.dominantLaunchServiceProvider(orbit));
    }

    @ParameterizedTest
    @ValueSource(ints = {1,2,3})
    public void shouldReturnLightestPayloadsLauchInLEO(int k){
        when(dao.loadAll(Launch.class)).thenReturn(launches);
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
        List<Launch> sortedLaunch = list.stream().sorted(new Comparator<Map.Entry<Launch, Integer>>() {
            @Override
            public int compare(Map.Entry<Launch, Integer> o1, Map.Entry<Launch, Integer> o2) {
                return o1.getValue()-o2.getValue();
            }
        }).map(s->s.getKey()).collect(Collectors.toList());
        List<Launch> loadedLaunch = miner.lightestPayloadsLauchInLEO(k);
        assertEquals(k,loadedLaunch.size());
        assertEquals(sortedLaunch.subList(0,k),loadedLaunch);
    }

    @ParameterizedTest
    @ValueSource(strings = {"LEO"})
    public void dominantCountryInPayLoads(String orbit){
        when(dao.loadAll(Launch.class)).thenReturn(launches);
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
        String expectCountry = payloadInCountry.entrySet().stream().sorted(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue()-o1.getValue();
            }
        }).map(s->s.getKey()).collect(Collectors.toList()).get(0);
        String loadedCountry = miner.dominantCountryInPayLoads(orbit);
        assertEquals(expectCountry, loadedCountry);
    }
}
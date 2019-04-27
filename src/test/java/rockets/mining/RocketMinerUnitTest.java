package rockets.mining;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rockets.dataaccess.DAO;
import rockets.dataaccess.neo4j.Neo4jDAO;
import rockets.model.Launch;
import rockets.model.LaunchServiceProvider;
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

        // OutCome of each launch
        Launch.LaunchOutcome[] outcome = new Launch.LaunchOutcome[]{FAILED,SUCCESSFUL,FAILED,SUCCESSFUL,FAILED,
                SUCCESSFUL,FAILED,SUCCESSFUL,FAILED,SUCCESSFUL};

        // 10 launches
        launches = IntStream.range(0, 10).mapToObj(i -> {
            logger.info("create " + i + " launch in month: " + months[i]);
            Launch l = new Launch();
            l.setLaunchDate(LocalDate.of(2017, months[i], 1));
            l.setLaunchVehicle(rockets.get(rocketIndex[i]));
            l.setLaunchSite("VAFB");
            l.setOrbit("LEO");
            spy(l);
            return l;
        }).collect(Collectors.toList());
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
}
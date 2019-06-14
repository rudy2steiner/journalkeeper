package com.jd.journalkeeper.journalstore;

import com.jd.journalkeeper.core.api.RaftEntry;
import com.jd.journalkeeper.core.api.ResponseConfig;
import com.jd.journalkeeper.core.server.Server;
import com.jd.journalkeeper.utils.net.NetworkingUtils;
import com.jd.journalkeeper.utils.test.TestPathUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author liyue25
 * Date: 2019-04-25
 */
public class JournalStoreTest {
    private static final Logger logger = LoggerFactory.getLogger(JournalStoreTest.class);
    private Path base = null;
//    Server<List<byte[]>,JournalStoreQuery, List<byte[]>>


    @Before
    public void before() throws IOException {
        base = TestPathUtils.prepareBaseDir();
    }
    @After
    public void after() {
        TestPathUtils.destroyBaseDir(base.toFile());
    }


    @Test
    public void writeReadOneNode() throws Exception{
        writeReadTest(1, new int [] {2, 3, 4, 5, 6}, 1024, 1024, 1024);
    }

    @Test
    public void writeReadTripleNodes() throws Exception{
        writeReadTest(3, new int [] {2, 3, 4, 5, 6}, 1024, 1024, 1024);
    }

    /**
     * 读写测试
     * @param nodes 节点数量
     * @param partitions 分区数量
     * @param entrySize 数据大小
     * @param batchSize 每批数据条数
     * @param batchCount 批数
     */
    private void writeReadTest(int nodes, int [] partitions, int entrySize, int batchSize, int batchCount) throws Exception {
        List<JournalStoreServer> servers = createServers(nodes, base);
        try {
            JournalStoreClient client = servers.get(0).createClient();
            client.waitForLeader(10000);

            client.scalePartitions(partitions).get();

            // Wait for all node to finish scale partitions.
            Thread.sleep(1000L);


            byte[] rawEntries = new byte[entrySize];
            for (int i = 0; i < rawEntries.length; i++) {
                rawEntries[i] = (byte) (i % Byte.MAX_VALUE);
            }

            CountDownLatch latch = new CountDownLatch(partitions.length * batchCount);
            // write
            for (int partition : partitions) {
                for (int i = 0; i < batchCount; i++) {
                    client.append(partition, batchSize, rawEntries)
                            .whenComplete((v, e) -> {
                                latch.countDown();
                                if (e != null) {
                                    logger.warn("Exception:", e);
                                }
                            });
                }
            }
            logger.info("Write finished, waiting for responses...");

            while (!latch.await(1, TimeUnit.SECONDS)) {
                Thread.yield();
            }

            logger.info("Replication finished.");

            // read

            for (int partition : partitions) {
                for (int i = 0; i < batchCount; i++) {
                    List<RaftEntry> raftEntries = client.get(partition, i * batchSize, batchSize).get();
                    Assert.assertEquals(raftEntries.size(), 1);
                    RaftEntry entry = raftEntries.get(0);
                    Assert.assertEquals(partition, entry.getHeader().getPartition());
                    Assert.assertEquals(batchSize, entry.getHeader().getBatchSize());
                    Assert.assertEquals(0, entry.getHeader().getOffset());
                    Assert.assertArrayEquals(rawEntries, entry.getEntry());
                }
            }
        } finally {
            stopServers(servers);

        }

    }

    private void stopServers(List<JournalStoreServer> servers) {
        for (JournalStoreServer server : servers) {
            try {
                server.stop();
            } catch (Throwable t) {
                logger.warn("Stop server {} exception: ", server.serverUri(), t);
            }
        }
    }

    private List<JournalStoreServer> createServers(int nodes, Path path) throws IOException {
        logger.info("Create {} nodes servers", nodes);
        List<URI> serverURIs = new ArrayList<>(nodes);
        List<Properties> propertiesList = new ArrayList<>(nodes);
        for (int i = 0; i < nodes; i++) {
            URI uri = URI.create("jk://localhost:" + NetworkingUtils.findRandomOpenPortOnAllLocalInterfaces());
            serverURIs.add(uri);
            Path workingDir = path.resolve("server" + i);
            Properties properties = new Properties();
            properties.setProperty("working_dir", workingDir.toString());
            properties.setProperty("snapshot_step", "0");
            properties.setProperty("persistence.journal.file_data_size", String.valueOf(128 * 1024));
            properties.setProperty("persistence.index.file_data_size", String.valueOf(16 * 1024));
            propertiesList.add(properties);
        }
        return createServers(serverURIs, propertiesList);
    }

    private List<JournalStoreServer> createServers(List<URI> serverURIs, List<Properties> propertiesList) throws IOException {
        List<JournalStoreServer> journalStoreServers = new ArrayList<>(serverURIs.size());
        for (int i = 0; i < serverURIs.size(); i++) {
            JournalStoreServer journalStoreServer  = new JournalStoreServer(propertiesList.get(i));
            journalStoreServers.add(journalStoreServer);
            journalStoreServer.init(serverURIs.get(i), serverURIs);
            journalStoreServer.recover();
            journalStoreServer.start();
        }
        return journalStoreServers;
    }
}
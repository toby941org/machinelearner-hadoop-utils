package com.thoughtworks.hadoop.utils;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.thoughtworks.hadoop.utils.commands.HCommandArgument;
import com.thoughtworks.hadoop.utils.commands.HCommandOutput;
import com.thoughtworks.hadoop.utils.commands.HMount;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class HMountTest {
    private ClusterClient mockClusterClient;
    private HMount hMount;
    private HCommandArgument argument;

    @Before
    public void setUp() throws Exception {
        mockClusterClient = mock(ClusterClient.class);
        hMount = new HMount(mockClusterClient);
        argument = new HCommandArgument();
        argument.put("f", "./hadoop");
    }

    @After
    public void tearDown() throws Exception {
        String dir_name = argument.get("f");
        File hadoopDir = new File(dir_name);
        FileUtils.deleteDirectory(hadoopDir);

    }

    @Test
    public void shouldGetListOfTaskTrackers() throws IOException {
        List<String> actualTaskTrackerNames = Arrays.asList("tracker_hadoop01.com:bla.bla", "tracker_hadoop02.com:localhost:localhost");
        List<String> expectedTaskTrackerNames = Arrays.asList("hadoop01.com", "hadoop02.com");

        when(mockClusterClient.getTaskTrackerNames()).thenReturn(actualTaskTrackerNames);
        Collection<String> taskTrackerNames = hMount.getTaskTrackerNames();
        verify(mockClusterClient).getTaskTrackerNames();
        Assert.assertFalse("Should not be empty", taskTrackerNames.isEmpty());
        Assert.assertEquals(expectedTaskTrackerNames, taskTrackerNames);
    }

    @Test
    public void shouldGetListOfDataNodes() throws IOException {
        List<String> actualDataNodes = Arrays.asList("hadoop01.com", "hadoop02.com");

        when(mockClusterClient.getDataNodes()).thenReturn(actualDataNodes);
        Collection<String> dataNodes = hMount.getDataNodes();
        verify(mockClusterClient, times(1)).getDataNodes();
        Assert.assertFalse("Should not be empty", dataNodes.isEmpty());
        Assert.assertEquals(actualDataNodes, dataNodes);
    }

    @Test
    public void shouldExecuteCommandSuccessfully() {
        HCommandOutput output = hMount.execute(argument);
        assertEquals(HCommandOutput.Result.SUCCESS, output.getResult());
    }

    @Test
    public void shouldExecuteCommandAndGetAllClusterDetails() {
        List<String> expectedTaskTrackerNames = Arrays.asList("hadoop01.com", "hadoop02.com");
        List<String> actualTaskTrackerNames = Arrays.asList("tracker_hadoop01.com:bla.bla", "tracker_hadoop02.com:localhost:localhost");
        List<String> expectedDataNodes = Arrays.asList("hadoop01.com", "hadoop02.com");
        String jobTrackerName = "jobtracker.hadoop.com";
        String nameNodeName = "nameNode.hadoop.com";
        int jtPortNumber = 12345;
        int nnPortNumber = 23456;
        Gson gson = new Gson();
        JsonObject expectedJSON = new JsonObject();
        String taskTrackers = gson.toJson(expectedTaskTrackerNames);
        String dataNodes = gson.toJson(expectedDataNodes);
        expectedJSON.addProperty("taskTrackers", taskTrackers);
        expectedJSON.addProperty("jobTracker", jobTrackerName);
        expectedJSON.addProperty("jobTrackerPort", jtPortNumber);
        expectedJSON.addProperty("dataNodes", dataNodes);
        expectedJSON.addProperty("nameNodePort", nnPortNumber);
        expectedJSON.addProperty("nameNode", nameNodeName);
        String expected = expectedJSON.toString();

        when(mockClusterClient.getTaskTrackerNames()).thenReturn(actualTaskTrackerNames);
        when(mockClusterClient.getJobTrackerName()).thenReturn(jobTrackerName);
        when(mockClusterClient.getNameNodeName()).thenReturn(nameNodeName);
        when(mockClusterClient.getDataNodes()).thenReturn(expectedDataNodes);
        when(mockClusterClient.getJtPortNumber()).thenReturn(jtPortNumber);
        when(mockClusterClient.getNnPortNumber()).thenReturn(nnPortNumber);

        HCommandOutput output = hMount.execute(argument);

        assertEquals(HCommandOutput.Result.SUCCESS, output.getResult());
        assertEquals(expectedJSON, (new Gson()).fromJson(output.getOutput(), JsonObject.class));

    }

}

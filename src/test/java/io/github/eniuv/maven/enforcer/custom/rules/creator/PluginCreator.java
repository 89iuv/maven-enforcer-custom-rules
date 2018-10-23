package io.github.eniuv.maven.enforcer.custom.rules.creator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;

public class PluginCreator {

    public static Plugin create(String groupId, String artifactId, String version) {
        final Plugin plugin = new Plugin();
        plugin.setGroupId(groupId);
        plugin.setArtifactId(artifactId);
        plugin.setVersion(version);

        return plugin;
    }

    public static Plugin create(String groupId, String artifactId, String version, List<String> goals) {
        final Plugin plugin = new Plugin();
        plugin.setGroupId(groupId);
        plugin.setArtifactId(artifactId);
        plugin.setVersion(version);
        plugin.setExecutions(new ArrayList<>(0));

        for (String goal: goals) {
            final PluginExecution pluginExecution = new PluginExecution();
            pluginExecution.setGoals(Collections.singletonList(goal));

            plugin.getExecutions().add(pluginExecution);
        }

        return plugin;
    }
}

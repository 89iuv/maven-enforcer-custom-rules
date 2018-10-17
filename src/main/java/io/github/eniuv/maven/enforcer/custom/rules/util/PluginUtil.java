package io.github.eniuv.maven.enforcer.custom.rules.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

public class PluginUtil {

    public static List<String> getNonThreadSafeGoals(Plugin plugin, List<String> goals, BuildPluginManager pluginBuildManager,
                                               List<RemoteRepository> repositories, RepositorySystemSession repositorySession)
            throws InvalidPluginDescriptorException, MojoNotFoundException, PluginResolutionException, PluginDescriptorParsingException, PluginNotFoundException {
        List<String> nonThreadSafeGoals = new ArrayList<>(0);

        for (String goal : goals) {
            final MojoDescriptor mojoDescriptor = pluginBuildManager.getMojoDescriptor(plugin, goal, repositories, repositorySession);
            if (!mojoDescriptor.isThreadSafe()) {
                nonThreadSafeGoals.add(goal);
            }
        }

        return nonThreadSafeGoals;
    }

    public static boolean doesPluginMatchInList(Plugin plugin, List<Plugin> pluginList) {
        final Optional<Plugin> first = pluginList.stream()
                .filter(currentPlugin -> isPluginMatchFor(plugin, currentPlugin))
                .findFirst();

        return first.isPresent();
    }

    public static List<String> getPluginGoals(Plugin plugin) {
        List<String> pluginGoals = new ArrayList<>(0);

        final List<PluginExecution> executions = plugin.getExecutions();
        for (PluginExecution pluginExecution : executions) {
            pluginGoals.addAll(pluginExecution.getGoals());

        }

        return pluginGoals;
    }

    public static boolean isPluginMatchFor(Plugin firstPlugin, Plugin secondPlugin) {
        if (firstPlugin.getGroupId() != null
                && secondPlugin.getGroupId() != null
                && !firstPlugin.getGroupId().equals(secondPlugin.getGroupId())) {
            return false;
        }

        if (firstPlugin.getArtifactId() != null
                && secondPlugin.getArtifactId() != null
                && !firstPlugin.getArtifactId().equals(secondPlugin.getArtifactId())) {
            return false;
        }

        if (firstPlugin.getVersion() != null
                && secondPlugin.getVersion() != null
                && !firstPlugin.getVersion().equals(secondPlugin.getVersion())) {
            return false;
        }

        return true;
    }

}

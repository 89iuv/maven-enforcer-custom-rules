package io.github.eniuv.maven.enforcer.custom.rules.plugin;

import java.util.ArrayList;
import java.util.List;

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

public class PluginService {
    private BuildPluginManager pluginBuildManager;
    private List<RemoteRepository> repositories;
    private RepositorySystemSession repositorySession;

    private PluginService() {
    }

    public PluginService(BuildPluginManager pluginBuildManager, List<RemoteRepository> repositories, RepositorySystemSession repositorySession) {
        this.pluginBuildManager = pluginBuildManager;
        this.repositories = repositories;
        this.repositorySession = repositorySession;
    }

    public List<String> getNonThreadSafeGoals(Plugin plugin)
            throws InvalidPluginDescriptorException, MojoNotFoundException, PluginResolutionException, PluginDescriptorParsingException, PluginNotFoundException {

        List<String> nonThreadSafeGoals = new ArrayList<>(0);

        for (String goal : getGoals(plugin)) {
            final MojoDescriptor mojoDescriptor = pluginBuildManager.getMojoDescriptor(plugin, goal, repositories, repositorySession);
            if (!mojoDescriptor.isThreadSafe()) {
                nonThreadSafeGoals.add(goal);
            }
        }

        return nonThreadSafeGoals;
    }

    private List<String> getGoals(Plugin plugin) {
        List<String> pluginGoals = new ArrayList<>(0);

        final List<PluginExecution> executions = plugin.getExecutions();
        for (PluginExecution pluginExecution : executions) {
            pluginGoals.addAll(pluginExecution.getGoals());

        }

        return pluginGoals;
    }
}

package com.lazydash.maven.enforcer.custom.rules;

import java.util.List;

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

public class NonThreadSafePluginRule implements EnforcerRule {

    /**
     * Simple param. This rule will fail if the value is true.
     */
    private boolean failOnError = false;

    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        Log log = helper.getLog();

        try {
            MavenProject project = helper.getComponent(MavenProject.class);
            MavenSession session = helper.getComponent(MavenSession.class);
            BuildPluginManager pluginManager = helper.getComponent(BuildPluginManager.class);

            final List<RemoteRepository> repositories = project.getRemotePluginRepositories();
            final RepositorySystemSession repositorySession = session.getRepositorySession();


            final List<Plugin> plugins = project.getBuild().getPlugins();
            for (Plugin plugin : plugins) {
                // skip maven plugins
                if ("org.apache.maven.plugins".equals(plugin.getGroupId())) {
                    continue;
                }

                final List<PluginExecution> executions = plugin.getExecutions();
                for (PluginExecution pluginExecution : executions) {
                    final List<String> goals = pluginExecution.getGoals();
                    for (String goal : goals) {
                        final MojoDescriptor mojoDescriptor = pluginManager.getMojoDescriptor(plugin, goal, repositories, repositorySession);
                        if (!mojoDescriptor.isThreadSafe()) {
                            log.error("The Goal: \"" + goal + "\" of Plugin:"
                                    + " \"" + plugin.getArtifactId()
                                    + ":" + plugin.getGroupId()
                                    + ":" + plugin.getVersion()
                                    + "\" " + "is not thread safe.");
                            if (this.failOnError) {
                                throw new EnforcerRuleException("Use of non thread safe plugins is not allowed.");
                            }
                        }
                    }
                }
            }

        } catch (ComponentLookupException
                | PluginNotFoundException
                | InvalidPluginDescriptorException
                | PluginDescriptorParsingException
                | PluginResolutionException
                | MojoNotFoundException e) {
            throw new EnforcerRuleException(e.getMessage());
        }
    }

    /**
     * If your rule is cacheable, you must return a unique id when parameters or conditions
     * change that would cause the result to be different. Multiple cached results are stored
     * based on their id.
     * <p>
     * The easiest way to do this is to return a hash computed from the values of your parameters.
     * <p>
     * If your rule is not cacheable, then the result here is not important, you may return anything.
     */
    public String getCacheId() {
        //no hash on boolean...only parameter so no hash is needed.
        return "" + this.failOnError;
    }

    /**
     * This tells the system if the results are cacheable at all. Keep in mind that during
     * forked builds and other things, a given rule may be executed more than once for the same
     * project. This means that even things that change from project to project may still
     * be cacheable in certain instances.
     */
    public boolean isCacheable() {
        return false;
    }

    /**
     * If the rule is cacheable and the same id is found in the cache, the stored results
     * are passed to this method to allow double checking of the results. Most of the time
     * this can be done by generating unique ids, but sometimes the results of objects returned
     * by the helper need to be queried. You may for example, store certain objects in your rule
     * and then query them later.
     */
    public boolean isResultValid(EnforcerRule arg0) {
        return false;
    }
}

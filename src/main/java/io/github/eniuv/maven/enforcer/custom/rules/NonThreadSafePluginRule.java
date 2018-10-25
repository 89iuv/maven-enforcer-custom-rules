package io.github.eniuv.maven.enforcer.custom.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import io.github.eniuv.maven.enforcer.custom.rules.constants.ThreadSafePlugins;
import io.github.eniuv.maven.enforcer.custom.rules.log.LogUtil;
import io.github.eniuv.maven.enforcer.custom.rules.plugin.PluginMatcher;
import io.github.eniuv.maven.enforcer.custom.rules.plugin.PluginService;

public class NonThreadSafePluginRule implements EnforcerRule {
    // all variables are accessed from enforcer plugin via reflection
    // there is no point in making them private
    boolean fail = true;
    boolean excludeMavenPlugins = true;
    List<Plugin> exclude = new ArrayList<>(0);

    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        try {
            final Log log = helper.getLog();
            final MavenProject project = (MavenProject) helper.evaluate("${project}");
            final MavenSession session = (MavenSession) helper.evaluate("${session}");
            final BuildPluginManager pluginManager = (BuildPluginManager) helper.getComponent(BuildPluginManager.class);
            final List<RemoteRepository> repositories = project.getRemotePluginRepositories();
            final RepositorySystemSession repositorySession = session.getRepositorySession();

            final PluginService pluginService = getPluginService(pluginManager, repositories, repositorySession);

            if (excludeMavenPlugins) {
                final Plugin mavenGroupPlugin = new Plugin();
                mavenGroupPlugin.setGroupId("org.apache.maven.plugins");
                exclude.add(mavenGroupPlugin);
            }

            exclude.forEach(plugin -> LogUtil.logInfoExcludePlugin(log, plugin));
            final ThreadSafePlugins arePluginsThreadSafe = doesProjectHaveNonThreadSafePlugin(log, project, pluginService);

            if (this.fail && arePluginsThreadSafe.equals(ThreadSafePlugins.NOT_ALL_PLUGINS_ARE_THREAD_SAFE)) {
                throw new EnforcerRuleException("Use of non thread safe plugins is not allowed.");
            }

        } catch (ExpressionEvaluationException
                | ComponentLookupException
                | PluginNotFoundException
                | InvalidPluginDescriptorException
                | PluginDescriptorParsingException
                | PluginResolutionException
                | MojoNotFoundException e) {
            throw new EnforcerRuleException(e.getMessage(), e);
        }
    }

    private ThreadSafePlugins doesProjectHaveNonThreadSafePlugin(Log log, MavenProject project, PluginService pluginService) throws InvalidPluginDescriptorException, MojoNotFoundException, PluginResolutionException, PluginDescriptorParsingException, PluginNotFoundException {
        ThreadSafePlugins arePluginsThreadSafe = ThreadSafePlugins.ALL_PLUGINS_ARE_THREAD_SAFE;
        for (Plugin plugin : project.getBuild().getPlugins()) {
            if (PluginMatcher.doesPluginMatchInList(plugin, exclude)) {
                continue;
            }

            final List<String> nonThreadSafeGoals = pluginService.getNonThreadSafeGoals(plugin);
            if (!nonThreadSafeGoals.isEmpty()) {
                LogUtil.logErrorGoalsOfPluginAreNotThreadSafe(log, plugin, nonThreadSafeGoals);
                arePluginsThreadSafe = ThreadSafePlugins.NOT_ALL_PLUGINS_ARE_THREAD_SAFE;
            }
        }

        return arePluginsThreadSafe;
    }

    public String getCacheId() {
        return null;
    }

    public boolean isCacheable() {
        return false;
    }

    public boolean isResultValid(EnforcerRule arg0) {
        return false;
    }

    PluginService getPluginService(BuildPluginManager pluginManager, List<RemoteRepository> repositories, RepositorySystemSession repositorySession) {
        return new PluginService(pluginManager, repositories, repositorySession);
    }
}

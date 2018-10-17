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

import io.github.eniuv.maven.enforcer.custom.rules.util.LogUtil;
import io.github.eniuv.maven.enforcer.custom.rules.util.PluginUtil;

public class NonThreadSafePluginRule implements EnforcerRule {
    private boolean fail = true;
    private boolean excludeMavenPlugins = true;
    private List<Plugin> exclude = new ArrayList<>(0);

    public List<Plugin> getExclude() {
        return exclude;
    }

    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        try {
            final Log log = helper.getLog();
            final MavenProject project = (MavenProject) helper.evaluate("${project}");
            final MavenSession session = (MavenSession) helper.evaluate("${session}");
            final BuildPluginManager pluginManager = (BuildPluginManager) helper.getComponent(BuildPluginManager.class);
            final List<RemoteRepository> repositories = project.getRemotePluginRepositories();
            final RepositorySystemSession repositorySession = session.getRepositorySession();

            if (excludeMavenPlugins) {
                final Plugin mavenGroupPlugin = new Plugin();
                mavenGroupPlugin.setGroupId("org.apache.maven.plugins");
                exclude.add(mavenGroupPlugin);
            }

            exclude.forEach(plugin -> LogUtil.logInfoExcludePlugin(log, plugin));

            boolean pass = true;
            for (Plugin plugin : project.getBuild().getPlugins()) {
                if (PluginUtil.doesPluginMatchInList(plugin, exclude)) {
                    continue;
                }

                final List<String> pluginGoals = PluginUtil.getPluginGoals(plugin);
                final List<String> nonThreadSafeGoals = PluginUtil.getNonThreadSafeGoals(plugin, pluginGoals, pluginManager, repositories, repositorySession);

                if (!nonThreadSafeGoals.isEmpty()) {
                    LogUtil.logErrorGoalsOfPluginAreNotThreadSafe(log, plugin, nonThreadSafeGoals);
                    pass = false;
                }
            }

            if (this.fail && !pass) {
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

    public String getCacheId() {
        return null;
    }

    public boolean isCacheable() {
        return false;
    }

    public boolean isResultValid(EnforcerRule arg0) {
        return false;
    }
}

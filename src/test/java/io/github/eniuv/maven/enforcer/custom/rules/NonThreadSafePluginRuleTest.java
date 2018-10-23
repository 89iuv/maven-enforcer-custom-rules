package io.github.eniuv.maven.enforcer.custom.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import io.github.eniuv.maven.enforcer.custom.rules.creator.PluginCreator;
import io.github.eniuv.maven.enforcer.custom.rules.plugin.PluginService;

@RunWith(MockitoJUnitRunner.class)
public class NonThreadSafePluginRuleTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MavenProject mavenProject;

    @Mock
    private MavenSession mavenSession;

    @Mock
    private BuildPluginManager buildPluginManager;

    @Mock
    private List<RemoteRepository> repositories;

    @Mock
    private RepositorySystemSession repositorySession;

    @Mock
    private EnforcerRuleHelper helper;

    @Mock
    private PluginService pluginService;

    @Spy
    private NonThreadSafePluginRule rule;

    @Before
    public void before() throws Exception{
        when(helper.getLog()).thenReturn(mock(Log.class));

        when(helper.evaluate("${project}")).thenReturn(mavenProject);
        when(helper.evaluate("${session}")).thenReturn(mavenSession);
        when(helper.getComponent(BuildPluginManager.class)).thenReturn(buildPluginManager);

        when(mavenProject.getRemotePluginRepositories()).thenReturn(repositories);
        when(mavenSession.getRepositorySession()).thenReturn(repositorySession);

        doReturn(pluginService).when(rule).getPluginService(buildPluginManager, repositories, repositorySession);
    }

    @Test(expected = EnforcerRuleException.class)
    public void executeDefault() throws Exception {
        // given
        final Plugin pluginOne = PluginCreator.create("com.example.group.one", "example-artifact-one", "0.0.1", Arrays.asList("one-goal-one", "one-goal-two", "one-goal-three"));
        when(pluginService.getNonThreadSafeGoals(pluginOne)).thenReturn(new ArrayList<>(0));

        final Plugin pluginTwo = PluginCreator.create("com.example.group.two", "example-artifact-two", "0.0.1", Arrays.asList("two-goal-one", "two-goal-two", "two-goal-three"));
        when(pluginService.getNonThreadSafeGoals(pluginTwo)).thenReturn(Arrays.asList("goal-one", "goal-two"));

        when(mavenProject.getBuild().getPlugins()).thenReturn(Arrays.asList(pluginOne, pluginTwo));

        // when
        rule.execute(helper);

        // then
        // exception EnforcerRuleException is thrown
    }

    @Test
    public void executeWithExclusion() throws Exception {
        // given
        final Plugin plugin = PluginCreator.create("com.example.group.one", "example-artifact-one", "0.0.1", Arrays.asList("goal-one", "goal-two", "goal-three"));
        when(mavenProject.getBuild().getPlugins()).thenReturn(Collections.singletonList(plugin));

        rule.exclude.add(
                PluginCreator.create("com.example.group.one", "example-artifact-one", "0.0.1")
        );

        // when
        rule.execute(helper);

        // then
        // exception is NOT thrown because the plugin is excluded
    }

    @Test
    public void executeWithFailFalse() throws Exception {
        // given
        final Plugin plugin = PluginCreator.create("com.example.group.one", "example-artifact-one", "0.0.1", Arrays.asList("goal-one", "goal-two", "goal-three"));
        when(pluginService.getNonThreadSafeGoals(plugin)).thenReturn(Arrays.asList("goal-one", "goal-two"));
        when(mavenProject.getBuild().getPlugins()).thenReturn(Collections.singletonList(plugin));

        rule.fail = false;

        // when
        rule.execute(helper);

        // then
        // exception EnforcerRuleException is NOT thrown because fail is set to false
    }

    @Test(expected = EnforcerRuleException.class)
    public void executeWithExcludeMavenFalse() throws Exception {
        // given
        final Plugin plugin = PluginCreator.create("org.apache.maven.plugins", "maven-site-plugin", "3.3.5", Arrays.asList("deploy", "site", "help"));
        when(pluginService.getNonThreadSafeGoals(plugin)).thenReturn(Arrays.asList("deploy", "site"));
        when(mavenProject.getBuild().getPlugins()).thenReturn(Collections.singletonList(plugin));

        rule.excludeMavenPlugins = false;

        // when
        rule.execute(helper);

        // then
        // exception EnforcerRuleException is thrown
    }

    @Test
    public void executeWithExcludeMavenTrue() throws Exception {
        // given
        final Plugin plugin = PluginCreator.create("org.apache.maven.plugins", "maven-site-plugin", "3.3.5", Arrays.asList("deploy", "site", "help"));
        when(mavenProject.getBuild().getPlugins()).thenReturn(Collections.singletonList(plugin));

        rule.excludeMavenPlugins = true;

        // when
        rule.execute(helper);

        // then
        // exception EnforcerRuleException is NOT thrown
    }

    @Test(expected = EnforcerRuleException.class)
    public void repackageExceptions() throws Exception {
        // given
        final Plugin plugin = PluginCreator.create("com.example.group.one", "example-artifact-one", "0.0.1", Arrays.asList("goal-one", "goal-two", "goal-three"));
        when(mavenProject.getBuild().getPlugins()).thenReturn(Collections.singletonList(plugin));

        when(pluginService.getNonThreadSafeGoals(any())).thenThrow(MojoNotFoundException.class);

        // when
        rule.execute(helper);

        // then
        // the exception MojoNotFoundException will be repackage into the exception EnforcerRuleException
    }

    @Test
    public void cacheNotEnabled() {
        assertThat(rule.getCacheId()).isNull();
        assertThat(rule.isCacheable()).isFalse();
        assertThat(rule.isResultValid(null)).isFalse();
    }
}
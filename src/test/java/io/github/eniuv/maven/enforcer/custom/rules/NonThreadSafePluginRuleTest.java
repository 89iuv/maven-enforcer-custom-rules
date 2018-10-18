package io.github.eniuv.maven.enforcer.custom.rules;

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
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.github.eniuv.maven.enforcer.custom.rules.util.PluginUtil;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PluginUtil.class)
public class NonThreadSafePluginRuleTest {
    private final ArrayList<RemoteRepository> remoteRepositories = new ArrayList<>(0);
    private final List<Plugin> mavenProjectPlugins = new ArrayList<>(0);

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MavenProject mavenProject;

    @Mock
    private Log log;

    @Mock
    private RepositorySystemSession repositorySystemSession;

    @Mock
    private MavenSession mavenSession;

    @Mock
    private BuildPluginManager buildPluginManager;

    @Mock
    private EnforcerRuleHelper helper;

    private NonThreadSafePluginRule rule = new NonThreadSafePluginRule();

    @Before
    public void before() throws Exception {
        Mockito.when(helper.getLog()).thenReturn(log);

        Mockito.when(helper.evaluate("${project}")).thenReturn(mavenProject);
        Mockito.when(helper.evaluate("${session}")).thenReturn(mavenSession);
        Mockito.when(helper.getComponent(BuildPluginManager.class)).thenReturn(buildPluginManager);

        Mockito.when(mavenProject.getRemotePluginRepositories()).thenReturn(remoteRepositories);
        Mockito.when(mavenProject.getBuild().getPlugins()).thenReturn(mavenProjectPlugins);
        Mockito.when(mavenSession.getRepositorySession()).thenReturn(repositorySystemSession);

        PowerMockito.mockStatic(PluginUtil.class);
    }

    @Test(expected = EnforcerRuleException.class)
    public void executeFail() throws Exception {
        // given
        final Plugin pluginOne = new Plugin();
        pluginOne.setGroupId("com.example.group");
        pluginOne.setArtifactId("example-artifact-one");
        pluginOne.setVersion("0.0.1");
        final List<String> pluginOneGoals = Arrays.asList("one", "two", "three", "four");
        final List<String> pluginOneNonThreadSafeGoals = Arrays.asList("three", "four");
        addPluginToMavenProjectMock(pluginOne, pluginOneGoals, pluginOneNonThreadSafeGoals);

        final Plugin pluginTwo = new Plugin();
        pluginTwo.setGroupId("com.example.group");
        pluginTwo.setArtifactId("example-artifact-two");
        pluginTwo.setVersion("0.0.1");
        final List<String> pluginTwoGoals = Arrays.asList("five", "six", "seven");
        final List<String> pluginTwoNonThreadSafeGoals = Collections.emptyList();
        addPluginToMavenProjectMock(pluginTwo, pluginTwoGoals, pluginTwoNonThreadSafeGoals);

        final Plugin pluginThree = new Plugin();
        pluginThree.setGroupId("com.example.group");
        pluginThree.setArtifactId("example-artifact-three");
        pluginThree.setVersion("0.0.1");
        addPluginToMavenProjectMock(pluginThree, Collections.emptyList(), Collections.emptyList());

        final Plugin excludedPlugin = new Plugin();
        excludedPlugin.setGroupId("com.example.group");
        excludedPlugin.setArtifactId("example-artifact-three");
        rule.getExclude().add(excludedPlugin);

        PowerMockito.when(PluginUtil.doesPluginMatchInList(excludedPlugin, rule.getExclude())).thenReturn(true);

        // when
        try {
            rule.execute(helper);

        } catch (EnforcerRuleException e) {
            // then
            Mockito.verify(log).info("NonThreadSafePluginRule: Exclude \"org.apache.maven.plugins\".");
            Mockito.verify(log).info("NonThreadSafePluginRule: Exclude \"com.example.group:example-artifact-three\".");

            Mockito.verify(log).error("The Goal: \"three\" of Plugin: \"com.example.group:example-artifact-one:0.0.1\" is not thread safe.");
            Mockito.verify(log).error("The Goal: \"four\" of Plugin: \"com.example.group:example-artifact-one:0.0.1\" is not thread safe.");

            throw e;
        }
    }

    @Test
    public void executePass() throws Exception {
        // given
        final Plugin pluginOne = new Plugin();
        pluginOne.setGroupId("com.example.group");
        pluginOne.setArtifactId("example-artifact-one");
        pluginOne.setVersion("0.0.1");
        final List<String> pluginOneGoals = Arrays.asList("one", "two", "three", "four");
        final List<String> pluginOneNonThreadSafeGoals = Collections.emptyList();
        addPluginToMavenProjectMock(pluginOne, pluginOneGoals, pluginOneNonThreadSafeGoals);

        // when
        rule.execute(helper);

        // then
        Mockito.verify(log).info("NonThreadSafePluginRule: Exclude \"org.apache.maven.plugins\".");

    }

    @Test(expected = EnforcerRuleException.class)
    public void exception() throws Exception {
        // give
        final Plugin pluginOne = new Plugin();
        pluginOne.setGroupId("com.example.group");
        pluginOne.setArtifactId("example-artifact-one");
        pluginOne.setVersion("0.0.1");
        final List<String> pluginOneGoals = Arrays.asList("one", "two", "three", "four");

        mavenProjectPlugins.add(pluginOne);
        PowerMockito.when(PluginUtil.getPluginGoals(pluginOne)).thenReturn(pluginOneGoals);
        PowerMockito.when(PluginUtil.getPluginNonThreadSafeGoals(pluginOne, pluginOneGoals, buildPluginManager, remoteRepositories, repositorySystemSession))
                .thenThrow(new MojoNotFoundException("one", new PluginDescriptor()));

        // when
        rule.execute(helper);

        // then
        // exception should be caught and repackage
    }

    @Test
    public void cacheNotEnabled() {
        Assert.assertNull(rule.getCacheId());
        Assert.assertFalse(rule.isCacheable());
        Assert.assertFalse(rule.isResultValid(null));
    }

    private void addPluginToMavenProjectMock(Plugin plugin, List<String> goals, List<String> nonThreadSafeGoals) throws Exception {
        mavenProjectPlugins.add(plugin);

        PowerMockito.when(PluginUtil.getPluginGoals(plugin)).thenReturn(goals);
        PowerMockito.when(PluginUtil.getPluginNonThreadSafeGoals(plugin, goals, buildPluginManager, remoteRepositories, repositorySystemSession)).thenReturn(nonThreadSafeGoals);
    }

}
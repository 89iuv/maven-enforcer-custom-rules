package io.github.eniuv.maven.enforcer.custom.rules.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.junit.Test;

import io.github.eniuv.maven.enforcer.custom.rules.creator.PluginCreator;

public class PluginMatcherTest {

    @Test
    public void doesPluginMatchInListExpectTrue() {
        // given
        final Plugin plugin = PluginCreator.create("com.example.group.one", "example-artifact-one", "0.0.1");

        final List<Plugin> pluginList = Arrays.asList(
                PluginCreator.create("com.example.group.two", "example-artifact-two", "0.0.2"),
                PluginCreator.create("com.example.group.one", "example-artifact-one", "0.0.1")
        );

        // when
        final boolean matchInList = PluginMatcher.doesPluginMatchInList(plugin, pluginList);

        // then
        assertThat(matchInList).isTrue();
    }

    @Test
    public void doesPluginMatchInListExpectFalse() {
        // given
        final Plugin plugin = new Plugin();
        plugin.setGroupId("com.example.group.one");
        plugin.setArtifactId("example-artifact-one");
        plugin.setVersion("0.0.1");

        final List<Plugin> pluginList = Arrays.asList(
                PluginCreator.create("com.example.group.two", "example-artifact-one", "0.0.1"),
                PluginCreator.create("com.example.group.one", "example-artifact-two", "0.0.1"),
                PluginCreator.create("com.example.group.one", "example-artifact-one", "0.0.2")
        );

        // when
        final boolean matchInList = PluginMatcher.doesPluginMatchInList(plugin, pluginList);

        // then
        assertThat(matchInList).isFalse();
    }

    @Test
    public void isPluginMatchFor() {
        assertThat(PluginMatcher.isPluginMatchFor(
                PluginCreator.create("com.example.group.one", "example-artifact-one", "0.0.1"),
                PluginCreator.create("com.example.group.one", "example-artifact-one", "0.0.1"))
        ).isTrue();

        assertThat(PluginMatcher.isPluginMatchFor(
                PluginCreator.create("com.example.group.one", "example-artifact-one", null),
                PluginCreator.create("com.example.group.one", "example-artifact-one", "0.0.1"))
        ).isTrue();

        assertThat(PluginMatcher.isPluginMatchFor(
                PluginCreator.create("com.example.group.one", null, "0.0.1"),
                PluginCreator.create("com.example.group.one", "example-artifact-one", "0.0.1"))
        ).isTrue();

        assertThat(PluginMatcher.isPluginMatchFor(
                PluginCreator.create("com.example.group.one", null, null),
                PluginCreator.create("com.example.group.one", "example-artifact-one", "0.0.1"))
        ).isTrue();

        assertThat(PluginMatcher.isPluginMatchFor(
                PluginCreator.create("com.example.group.one", "example-artifact-one", "0.0.2"),
                PluginCreator.create("com.example.group.one", "example-artifact-one", "0.0.1"))
        ).isFalse();

        assertThat(PluginMatcher.isPluginMatchFor(
                PluginCreator.create("com.example.group.one", "example-artifact-two", "0.0.1"),
                PluginCreator.create("com.example.group.one", "example-artifact-one", "0.0.1"))
        ).isFalse();

        assertThat(PluginMatcher.isPluginMatchFor(
                PluginCreator.create("com.example.group.two", "example-artifact-one", "0.0.1"),
                PluginCreator.create("com.example.group.one", "example-artifact-one", "0.0.1"))
        ).isFalse();
    }

}
package io.github.eniuv.maven.enforcer.custom.rules.log;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LogUtilTest {

    @Test
    public void logErrorGoalsOfPluginAreNotThreadSafe() {
        // given
        final Log logMock = Mockito.mock(Log.class);

        final Plugin plugin = new Plugin();
        plugin.setGroupId("com.example.group");
        plugin.setArtifactId("example-artifact");
        plugin.setVersion("0.0.1");

        final List<String> goals = Arrays.asList("one", "two", "three");

        // when
        LogUtil.logErrorGoalsOfPluginAreNotThreadSafe(logMock, plugin, goals);

        //then
        Mockito.verify(logMock).error("The Goal: \"one\" of Plugin: \"com.example.group:example-artifact:0.0.1\" is not thread safe.");
        Mockito.verify(logMock).error("The Goal: \"two\" of Plugin: \"com.example.group:example-artifact:0.0.1\" is not thread safe.");
        Mockito.verify(logMock).error("The Goal: \"three\" of Plugin: \"com.example.group:example-artifact:0.0.1\" is not thread safe.");
        Mockito.verifyNoMoreInteractions(logMock);
    }

    @Test
    public void logInfoExcludePlugin() {
        // given
        final Log logMock = Mockito.mock(Log.class);

        final Plugin plugin = new Plugin();
        plugin.setGroupId("com.example.group");
        plugin.setArtifactId("example-artifact");
        plugin.setVersion("0.0.1");

        // when
        LogUtil.logInfoExcludePlugin(logMock, plugin);

        // then
        Mockito.verify(logMock).info("NonThreadSafePluginRule: Exclude \"com.example.group:example-artifact:0.0.1\".");
        Mockito.verifyNoMoreInteractions(logMock);
    }

    @Test
    public void logInfoExcludePluginNullArtifact() {
        // given
        final Log logMock = Mockito.mock(Log.class);

        final Plugin plugin = new Plugin();
        plugin.setGroupId("com.example.group");
        plugin.setArtifactId(null);
        plugin.setVersion("0.0.1");

        // when
        LogUtil.logInfoExcludePlugin(logMock, plugin);

        // then
        Mockito.verify(logMock).info("NonThreadSafePluginRule: Exclude \"com.example.group:0.0.1\".");
        Mockito.verifyNoMoreInteractions(logMock);
    }

    @Test
    public void logInfoExcludePluginNullVersion() {
        // given
        final Log logMock = Mockito.mock(Log.class);

        final Plugin plugin = new Plugin();
        plugin.setGroupId("com.example.group");
        plugin.setArtifactId("example-artifact");
        plugin.setVersion(null);

        // when
        LogUtil.logInfoExcludePlugin(logMock, plugin);

        // then
        Mockito.verify(logMock).info("NonThreadSafePluginRule: Exclude \"com.example.group:example-artifact\".");
        Mockito.verifyNoMoreInteractions(logMock);
    }

    @Test
    public void logInfoExcludePluginNullVersionNullArtifact() {
        // given
        final Log logMock = Mockito.mock(Log.class);

        final Plugin plugin = new Plugin();
        plugin.setGroupId("com.example.group");
        plugin.setArtifactId(null);
        plugin.setVersion(null);

        // when
        LogUtil.logInfoExcludePlugin(logMock, plugin);

        // then
        Mockito.verify(logMock).info("NonThreadSafePluginRule: Exclude \"com.example.group\".");
        Mockito.verifyNoMoreInteractions(logMock);
    }
}
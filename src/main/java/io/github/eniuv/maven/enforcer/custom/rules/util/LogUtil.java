package io.github.eniuv.maven.enforcer.custom.rules.util;

import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.logging.Log;

public class LogUtil {
    public static void logErrorGoalsOfPluginAreNotThreadSafe(Log log, Plugin plugin, List<String> goals) {
        goals.forEach(goal -> logErrorGoalOfPluginIsNotThreadSafe(log, plugin, goal));
    }

    public static void logInfoExcludePlugin(Log log, Plugin plugin) {
        String logSkipPluginMessage = "NonThreadSafePluginRule: Exclude "
                + "\"" + plugin.getGroupId()
                + (plugin.getArtifactId() != null ? ":" + plugin.getArtifactId() : "")
                + (plugin.getVersion() != null ? ":" + plugin.getVersion() : "")
                + "\".";

        log.info(logSkipPluginMessage);
    }

    private static void logErrorGoalOfPluginIsNotThreadSafe(Log log, Plugin plugin, String goal) {
        log.error("The Goal: \"" + goal + "\" of Plugin:"
                + " \"" + plugin.getGroupId()
                + ":" + plugin.getArtifactId()
                + ":" + plugin.getVersion()
                + "\" " + "is not thread safe.");
    }
}

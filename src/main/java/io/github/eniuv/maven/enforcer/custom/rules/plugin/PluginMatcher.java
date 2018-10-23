package io.github.eniuv.maven.enforcer.custom.rules.plugin;

import java.util.List;
import java.util.Optional;

import org.apache.maven.model.Plugin;

public class PluginMatcher {

    public static boolean doesPluginMatchInList(Plugin plugin, List<Plugin> pluginList) {
        final Optional<Plugin> first = pluginList.stream()
                .filter(currentPlugin -> isPluginMatchFor(plugin, currentPlugin))
                .findFirst();

        return first.isPresent();
    }

    /**
     * This method will test if a plugin is match of another plugin.
     * If the groupId or artifactId or version are present and equal then we have a match.
     * Match example:
     * - "com.groupId.one:artifactId-one:1.1.1" will match with "com.groupId.one:artifactId-one:null"
     * - "com.groupId.one:artifactId-one:1.1.1" will match with "com.groupId.one:null:1.1.1"
     * - "com.groupId.one:artifactId-one:1.1.1" will match with "com.groupId.one:null:null"
     * NOT match example:
     * - "com.groupId.one:artifactId-one:1.1.1" will NOT match with "com.groupId.one:artifactId-two:null"
     * - "com.groupId.one:artifactId-one:1.1.1" will NOT match with "com.groupId.one:null:1.1.2"
     * - "com.groupId.one:artifactId-one:1.1.1" will NOT match with "com.groupId.two:artifactId-one:1.1.1"
     *
     * @param firstPlugin  The first plugin.
     * @param secondPlugin The second plugin.
     * @return True if there is a match and false otherwise.
     */
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

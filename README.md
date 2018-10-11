# maven-enforcer-custom-rules
Custom Rules for Maven Enforcer Plugin

# usage example
```
 <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
    <version>1.4.1</version>
    <dependencies>
        <dependency>
            <groupId>io.github.89iuv</groupId>
            <artifactId>maven-enforcer-custom-rules</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
    <executions>
        <execution>
            <id>enforce-versions</id>
            <goals>
                <goal>enforce</goal>
            </goals>
            <configuration>
                <rules>
                    <myCustomRule implementation="io.github.eniuv.maven.enforcer.custom.rules.NonThreadSafePluginRule">
                        <!-- default: true - set this to "false" in order to include org.apache.maven.plugins plugins -->
                        <!-- the goals "site" and "deploy" from "maven-site-plugin:org.apache.maven.plugins" are reported as not thread safe and they need to be excluded -->
                        <excludeMavenPlugins>true</excludeMavenPlugins>
    
                        <!-- default: true - set this to "false" in order let the build pass with errors -->
                        <fail>true</fail>
    
                        <!--uncomment the following example section to add plugins to the exclusion list-->
                        <!-- <exclude>
                            <Plugin>
                                <groupId>org.codehaus.mojo</groupId>
                                <artifactId>javacc-maven-plugin</artifactId>
                                <version>2.6</version>
                            </Plugin>
                            <Plugin>
                                <groupId>nl.geodienstencentrum.maven</groupId>
                                <artifactId>sass-maven-plugin</artifactId>
                            </Plugin>
                        </exclude> -->
                    </myCustomRule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

# output sample
```$xslt
[INFO] NonThreadSafePluginRule: Exclude "org.apache.maven.plugins".
[ERROR] The Goal: "update-stylesheets" of Plugin: "sass-maven-plugin:nl.geodienstencentrum.maven:3.5.5" is not thread safe.
[ERROR] The Goal: "javacc" of Plugin: "javacc-maven-plugin:org.codehaus.mojo:2.6" is not thread safe.
[WARNING] Rule 1: io.github.eniuv.maven.enforcer.custom.rules.NonThreadSafePluginRule failed with message:
Use of non thread safe plugins is not allowed.
...
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-enforcer-plugin:1.4.1:enforce (enforce-versions) on project maven: Some Enforcer rules have failed. Look above for specific messages explaining why the rule failed. -> [Help 1]
```

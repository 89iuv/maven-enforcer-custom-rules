package io.github.eniuv.maven.enforcer.custom.rules.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PluginUtilTest {

    @Test
    public void getNonThreadSafeGoals() throws Exception {
        // given
        final Plugin plugin = new Plugin();
        plugin.setGroupId("com.example.group");
        plugin.setArtifactId("example-artifact");
        plugin.setVersion("0.0.1");
        final List<String> goals = Arrays.asList("one", "two", "three");

        final List<RemoteRepository> remoteRepositoryList = new ArrayList<>(0);
        final RepositorySystemSession repositorySystemSessionMock = Mockito.mock(RepositorySystemSession.class);

        final MojoDescriptor goalOneMojoMock = new MojoDescriptorMock();
        goalOneMojoMock.setThreadSafe(false);

        final MojoDescriptor twoOneMojoMock = new MojoDescriptorMock();
        twoOneMojoMock.setThreadSafe(true);

        final MojoDescriptor threeOneMojoMock = new MojoDescriptorMock();
        threeOneMojoMock.setThreadSafe(false);

        final BuildPluginManager buildPluginManagerMock = Mockito.mock(BuildPluginManager.class);
        Mockito.when(buildPluginManagerMock.getMojoDescriptor(plugin, "one", remoteRepositoryList, repositorySystemSessionMock)).thenReturn(goalOneMojoMock);
        Mockito.when(buildPluginManagerMock.getMojoDescriptor(plugin, "two", remoteRepositoryList, repositorySystemSessionMock)).thenReturn(twoOneMojoMock);
        Mockito.when(buildPluginManagerMock.getMojoDescriptor(plugin, "three", remoteRepositoryList, repositorySystemSessionMock)).thenReturn(threeOneMojoMock);

        // when
        final List<String> nonThreadSafeGoals = PluginUtil.getPluginNonThreadSafeGoals(plugin, goals, buildPluginManagerMock, remoteRepositoryList, repositorySystemSessionMock);

        // then
        Assert.assertEquals(2, nonThreadSafeGoals.size());
        Assert.assertTrue(nonThreadSafeGoals.contains("one"));
        Assert.assertTrue(nonThreadSafeGoals.contains("three"));
    }

    @Test
    public void doesPluginMatchInListExpectTrue() {
        // given
        final Plugin pluginOne = new Plugin();
        pluginOne.setGroupId("com.example.group");
        pluginOne.setArtifactId("example-artifact-one");
        pluginOne.setVersion("0.0.1");

        final Plugin pluginTwo = new Plugin();
        pluginTwo.setGroupId("com.example.group");
        pluginTwo.setArtifactId("example-artifact-two");
        pluginTwo.setVersion("0.0.1");

        final List<Plugin> pluginList = Arrays.asList(pluginOne, pluginTwo);

        // when
        final boolean matchInList = PluginUtil.doesPluginMatchInList(pluginOne, pluginList);

        // then
        Assert.assertTrue(matchInList);
    }

    @Test
    public void doesPluginMatchInListExpectFalse() {
        // given
        final Plugin pluginOne = new Plugin();
        pluginOne.setGroupId("com.example.group");
        pluginOne.setArtifactId("example-artifact-one");
        pluginOne.setVersion("0.0.1");

        final Plugin pluginTwo = new Plugin();
        pluginTwo.setGroupId("com.example.group");
        pluginTwo.setArtifactId("example-artifact-two");
        pluginTwo.setVersion("0.0.1");

        final List<Plugin> pluginList = Collections.singletonList(pluginTwo);

        // when
        final boolean matchInList = PluginUtil.doesPluginMatchInList(pluginOne, pluginList);

        // then
        Assert.assertFalse(matchInList);
    }


    @Test
    public void isPluginMatchWithAllFieldsExpectTrue(){
        // given
        final Plugin pluginOne = new Plugin();
        pluginOne.setGroupId("com.example.group");
        pluginOne.setArtifactId("example-artifact-one");
        pluginOne.setVersion("0.0.1");

        final Plugin pluginTwo = new Plugin();
        pluginTwo.setGroupId("com.example.group");
        pluginTwo.setArtifactId("example-artifact-one");
        pluginTwo.setVersion("0.0.1");

        // when
        final boolean match = PluginUtil.isPluginMatchFor(pluginOne, pluginTwo);

        // then
        Assert.assertTrue(match);
    }

    @Test
    public void isPluginMatchWithMissingVersionExpectTrue(){
        // given
        final Plugin pluginOne = new Plugin();
        pluginOne.setGroupId("com.example.group");
        pluginOne.setArtifactId("example-artifact-one");
        pluginOne.setVersion(null);

        final Plugin pluginTwo = new Plugin();
        pluginTwo.setGroupId("com.example.group");
        pluginTwo.setArtifactId("example-artifact-one");
        pluginTwo.setVersion("0.0.1");

        // when
        final boolean match = PluginUtil.isPluginMatchFor(pluginOne, pluginTwo);

        // then
        Assert.assertTrue(match);
    }

    @Test
    public void isPluginMatchWithMissingArtifactIdExpectTrue(){
        // given
        final Plugin pluginOne = new Plugin();
        pluginOne.setGroupId("com.example.group");
        pluginOne.setArtifactId(null);
        pluginOne.setVersion("0.0.1");

        final Plugin pluginTwo = new Plugin();
        pluginTwo.setGroupId("com.example.group");
        pluginTwo.setArtifactId("example-artifact-one");
        pluginTwo.setVersion("0.0.1");

        // when
        final boolean match = PluginUtil.isPluginMatchFor(pluginOne, pluginTwo);

        // then
        Assert.assertTrue(match);
    }

    @Test
    public void isPluginMatchWithMissingVersionAndArtifactIdExpectTrue(){
        // given
        final Plugin pluginOne = new Plugin();
        pluginOne.setGroupId("com.example.group");
        pluginOne.setArtifactId(null);
        pluginOne.setVersion(null);

        final Plugin pluginTwo = new Plugin();
        pluginTwo.setGroupId("com.example.group");
        pluginTwo.setArtifactId("example-artifact-one");
        pluginTwo.setVersion("0.0.1");

        // when
        final boolean match = PluginUtil.isPluginMatchFor(pluginOne, pluginTwo);

        // then
        Assert.assertTrue(match);
    }

    @Test
    public void isPluginMatchOnVersionExpectFalse(){
        // given
        final Plugin pluginOne = new Plugin();
        pluginOne.setGroupId("com.example.group");
        pluginOne.setArtifactId("example-artifact-one");
        pluginOne.setVersion("0.0.1");

        final Plugin pluginTwo = new Plugin();
        pluginTwo.setGroupId("com.example.group");
        pluginTwo.setArtifactId("example-artifact-one");
        pluginTwo.setVersion("0.0.2");

        // when
        final boolean match = PluginUtil.isPluginMatchFor(pluginOne, pluginTwo);

        // then
        Assert.assertFalse(match);
    }

    @Test
    public void isPluginMatchOnArtifactIdExpectFalse(){
        // given
        final Plugin pluginOne = new Plugin();
        pluginOne.setGroupId("com.example.group");
        pluginOne.setArtifactId("example-artifact-one");
        pluginOne.setVersion("0.0.1");

        final Plugin pluginTwo = new Plugin();
        pluginTwo.setGroupId("com.example.group");
        pluginTwo.setArtifactId("example-artifact-two");
        pluginTwo.setVersion("0.0.1");

        // when
        final boolean match = PluginUtil.isPluginMatchFor(pluginOne, pluginTwo);

        // then
        Assert.assertFalse(match);
    }

    @Test
    public void isPluginMatchOnGroupIdExpectFalse(){
        // given
        final Plugin pluginOne = new Plugin();
        pluginOne.setGroupId("com.example.group.one");
        pluginOne.setArtifactId("example-artifact-one");
        pluginOne.setVersion("0.0.1");

        final Plugin pluginTwo = new Plugin();
        pluginTwo.setGroupId("com.example.group.two");
        pluginTwo.setArtifactId("example-artifact-one");
        pluginTwo.setVersion("0.0.1");

        // when
        final boolean match = PluginUtil.isPluginMatchFor(pluginOne, pluginTwo);

        // then
        Assert.assertFalse(match);
    }

    @Test
    public void getPluginGoals() {
        // given
        final List<String> expectedGoals = Arrays.asList("one", "two", "three");

        final PluginExecution pluginExecutionOne = new PluginExecution();
        pluginExecutionOne.setGoals(Arrays.asList(expectedGoals.get(0), expectedGoals.get(1)));

        final PluginExecution pluginExecutionTwo = new PluginExecution();
        pluginExecutionTwo.setGoals(Collections.singletonList(expectedGoals.get(2)));

        final ArrayList<PluginExecution> executions = new ArrayList<>(0);
        executions.add(pluginExecutionOne);
        executions.add(pluginExecutionTwo);

        final Plugin plugin = new Plugin();
        plugin.setGroupId("com.example.group");
        plugin.setArtifactId("example-artifact");
        plugin.setVersion("0.0.1");
        plugin.setExecutions(executions);

        // when
        final List<String> pluginGoals = PluginUtil.getPluginGoals(plugin);

        // then
        Assert.assertEquals(pluginGoals.size(), expectedGoals.size());
        pluginGoals.forEach(goal -> Assert.assertTrue(expectedGoals.contains(goal)));
    }

    /**
     * Helper class needed because Mokito can not mock MojoDescriptor.class of the following:
     * final MojoDescriptor mojoDescriptorMock = Mockito.mock(MojoDescriptor.class);
     * ->
     * org.mockito.exceptions.base.MockitoException:
     * Mockito cannot mock this class: class org.apache.maven.plugin.descriptor.MojoDescriptor.
     *
     * Mockito can only mock non-private & non-final classes.
     * If you're not sure why you're getting this error, please report to the mailing list.
     *
     *
     * Java               : 1.8
     * JVM vendor name    : Oracle Corporation
     * JVM vendor version : 25.151-b12
     * JVM name           : Java HotSpot(TM) 64-Bit Server VM
     * JVM version        : 1.8.0_151-b12
     * JVM info           : mixed mode
     * OS name            : Windows 7
     * OS version         : 6.1
     *
     *
     * Underlying exception : java.lang.reflect.MalformedParameterizedTypeException
     *
     * 	at io.github.eniuv.maven.enforcer.custom.rules.util.PluginUtilTest.getPluginNonThreadSafeGoals(PluginUtilTest.java:44)
     * 	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
     * 	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
     * 	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
     * 	at java.lang.reflect.Method.invoke(Method.java:498)
     * 	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
     * 	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
     * 	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
     * 	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
     * 	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)
     * 	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)
     * 	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)
     * 	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
     * 	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
     * 	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
     * 	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
     * 	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
     * 	at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
     * 	at org.mockito.internal.runners.DefaultInternalRunner$1.run(DefaultInternalRunner.java:79)
     * 	at org.mockito.internal.runners.DefaultInternalRunner.run(DefaultInternalRunner.java:85)
     * 	at org.mockito.internal.runners.StrictRunner.run(StrictRunner.java:39)
     * 	at org.mockito.junit.MockitoJUnitRunner.run(MockitoJUnitRunner.java:163)
     * 	at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
     * 	at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:68)
     * 	at com.intellij.rt.execution.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:47)
     * 	at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:242)
     * 	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:70)
     * Caused by: java.lang.reflect.MalformedParameterizedTypeException
     * 	at sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl.validateConstructorArguments(ParameterizedTypeImpl.java:58)
     * 	at sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl.<init>(ParameterizedTypeImpl.java:51)
     * 	at sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl.make(ParameterizedTypeImpl.java:92)
     * 	at sun.reflect.generics.factory.CoreReflectionFactory.makeParameterizedType(CoreReflectionFactory.java:105)
     * 	at sun.reflect.generics.visitor.Reifier.visitClassTypeSignature(Reifier.java:140)
     * 	at sun.reflect.generics.tree.ClassTypeSignature.accept(ClassTypeSignature.java:49)
     * 	at sun.reflect.generics.repository.ClassRepository.getSuperclass(ClassRepository.java:90)
     * 	at java.lang.Class.getGenericSuperclass(Class.java:777)
     * 	at net.bytebuddy.description.type.TypeDescription$Generic$LazyProjection$ForLoadedSuperClass.resolve(TypeDescription.java:6415)
     * 	at net.bytebuddy.description.type.TypeDescription$Generic$LazyProjection.accept(TypeDescription.java:6091)
     * 	at net.bytebuddy.description.type.TypeDescription$Generic$LazyProjection$WithResolvedErasure.resolve(TypeDescription.java:6685)
     * 	at net.bytebuddy.description.type.TypeDescription$Generic$LazyProjection.accept(TypeDescription.java:6091)
     * 	at net.bytebuddy.dynamic.scaffold.MethodGraph$Compiler$Default.analyzeNullable(MethodGraph.java:600)
     * 	at net.bytebuddy.dynamic.scaffold.MethodGraph$Compiler$Default.doAnalyze(MethodGraph.java:614)
     * 	at net.bytebuddy.dynamic.scaffold.MethodGraph$Compiler$Default.analyze(MethodGraph.java:581)
     * 	at net.bytebuddy.dynamic.scaffold.MethodGraph$Compiler$Default.analyzeNullable(MethodGraph.java:600)
     * 	at net.bytebuddy.dynamic.scaffold.MethodGraph$Compiler$Default.doAnalyze(MethodGraph.java:614)
     * 	at net.bytebuddy.dynamic.scaffold.MethodGraph$Compiler$Default.compile(MethodGraph.java:552)
     * 	at net.bytebuddy.dynamic.scaffold.MethodGraph$Compiler$AbstractBase.compile(MethodGraph.java:450)
     * 	at net.bytebuddy.dynamic.scaffold.MethodRegistry$Default.prepare(MethodRegistry.java:448)
     * 	at net.bytebuddy.dynamic.scaffold.subclass.SubclassDynamicTypeBuilder.make(SubclassDynamicTypeBuilder.java:183)
     * 	at net.bytebuddy.dynamic.scaffold.subclass.SubclassDynamicTypeBuilder.make(SubclassDynamicTypeBuilder.java:174)
     * 	at net.bytebuddy.dynamic.DynamicType$Builder$AbstractBase.make(DynamicType.java:3376)
     * 	at net.bytebuddy.dynamic.DynamicType$Builder$AbstractBase$Delegator.make(DynamicType.java:3565)
     * 	at org.mockito.internal.creation.bytebuddy.SubclassBytecodeGenerator.mockClass(SubclassBytecodeGenerator.java:128)
     * 	at org.mockito.internal.creation.bytebuddy.TypeCachingBytecodeGenerator$1.call(TypeCachingBytecodeGenerator.java:37)
     * 	at org.mockito.internal.creation.bytebuddy.TypeCachingBytecodeGenerator$1.call(TypeCachingBytecodeGenerator.java:34)
     * 	at net.bytebuddy.TypeCache.findOrInsert(TypeCache.java:137)
     * 	at net.bytebuddy.TypeCache$WithInlineExpunction.findOrInsert(TypeCache.java:350)
     * 	at net.bytebuddy.TypeCache.findOrInsert(TypeCache.java:159)
     * 	at net.bytebuddy.TypeCache$WithInlineExpunction.findOrInsert(TypeCache.java:361)
     * 	at org.mockito.internal.creation.bytebuddy.TypeCachingBytecodeGenerator.mockClass(TypeCachingBytecodeGenerator.java:32)
     * 	at org.mockito.internal.creation.bytebuddy.SubclassByteBuddyMockMaker.createMockType(SubclassByteBuddyMockMaker.java:71)
     * 	at org.mockito.internal.creation.bytebuddy.SubclassByteBuddyMockMaker.createMock(SubclassByteBuddyMockMaker.java:42)
     * 	at org.mockito.internal.creation.bytebuddy.ByteBuddyMockMaker.createMock(ByteBuddyMockMaker.java:25)
     * 	at org.mockito.internal.util.MockUtil.createMock(MockUtil.java:35)
     * 	at org.mockito.internal.MockitoCore.mock(MockitoCore.java:69)
     * 	at org.mockito.Mockito.mock(Mockito.java:1895)
     * 	at org.mockito.Mockito.mock(Mockito.java:1804)
     * 	... 27 more
     *
     */
    private class MojoDescriptorMock extends MojoDescriptor {}

}
package io.github.eniuv.maven.enforcer.custom.rules.plugin;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.assertj.core.api.Assertions;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.github.eniuv.maven.enforcer.custom.rules.creator.PluginCreator;

@RunWith(MockitoJUnitRunner.class)
public class PluginServiceTest {

    @Mock
    private BuildPluginManager pluginBuildManager;

    @Mock
    private List<RemoteRepository> repositories;

    @Mock
    private RepositorySystemSession repositorySession;

    @InjectMocks
    private PluginService pluginService;

    @Test
    public void getNonThreadSafeGoals() throws Exception {
        // given
        final Plugin plugin = PluginCreator.create(
                "com.example.group.one",
                "example-artifact-one",
                "0.0.1",
                Arrays.asList("goal-one", "goal-two", "goal-three"));

        final MojoDescriptor mojoThreadSafeTrue = new MojoDescriptorMock(true);
        final MojoDescriptor mojoThreadSafeFalse = new MojoDescriptorMock(false);

        when(pluginBuildManager.getMojoDescriptor(plugin, "goal-one", repositories, repositorySession)).thenReturn(mojoThreadSafeTrue);
        when(pluginBuildManager.getMojoDescriptor(plugin, "goal-two", repositories, repositorySession)).thenReturn(mojoThreadSafeFalse);
        when(pluginBuildManager.getMojoDescriptor(plugin, "goal-three", repositories, repositorySession)).thenReturn(mojoThreadSafeTrue);

        // when
        final List<String> nonThreadSafeGoals = pluginService.getNonThreadSafeGoals(plugin);

        // then
        Assertions.assertThat(nonThreadSafeGoals)
                .hasSize(1)
                .contains("goal-two");
    }

    /**
     * Workaround for mocking MojoDescriptor.class.
     *
     * When mocking MojoDescriptor.class using mockito the test will give the following error at runtime:
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
     * 	at io.github.eniuv.maven.enforcer.custom.rules.plugin.PluginServiceTest.getNonThreadSafeGoals(PluginServiceTest.java:49)
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
     * 	at org.junit.runners.Suite.runChild(Suite.java:128)
     * 	at org.junit.runners.Suite.runChild(Suite.java:27)
     * 	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
     * 	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
     * 	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
     * 	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
     * 	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
     * 	at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
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
     * 	... 35 more
     */
    private class MojoDescriptorMock extends MojoDescriptor {

        MojoDescriptorMock(boolean isThreadSafe) {
            this.setThreadSafe(isThreadSafe);
        }

    }
}
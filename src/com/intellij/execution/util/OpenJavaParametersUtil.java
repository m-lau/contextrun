package com.intellij.execution.util;

import com.intellij.execution.CantRunException;
import com.intellij.execution.CommonJavaRunConfigurationParameters;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.JavaExecutionUtil;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.configurations.RuntimeConfigurationWarning;
import com.intellij.execution.configurations.SimpleJavaParameters;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.ex.PathUtilEx;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.ExportableOrderEntry;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: mlauer
 * Date: 04/01/13
 * Time: 19:21
 * To change this template use File | Settings | File Templates.
 */
public class OpenJavaParametersUtil {
    private OpenJavaParametersUtil() { }

    public static void configureConfiguration(SimpleJavaParameters parameters, CommonJavaRunConfigurationParameters configuration) {
        OpenProgramParametersUtil.configureConfiguration(parameters, configuration);

        Project project = configuration.getProject();
        Module module = OpenProgramParametersUtil.getModule(configuration);

        String vmParameters = configuration.getVMParameters();
        if (vmParameters != null) {
            vmParameters = OpenProgramParametersUtil.expandPath(vmParameters, module, project);

            if (parameters.getEnv() != null) {
                for (Map.Entry<String, String> each : parameters.getEnv().entrySet()) {
                    vmParameters = StringUtil.replace(vmParameters, "$" + each.getKey() + "$", each.getValue(), false); //replace env usages
                }
            }
        }

        parameters.getVMParametersList().addParametersString(vmParameters);
    }

    public static int getClasspathType(final RunConfigurationModule configurationModule, final String mainClassName,
                                       final boolean classMustHaveSource) throws CantRunException {
        final Module module = configurationModule.getModule();
        if (module == null) throw CantRunException.noModuleConfigured(configurationModule.getModuleName());
        final PsiClass psiClass = JavaExecutionUtil.findMainClass(module, mainClassName);
        if (psiClass == null) {
            if (!classMustHaveSource) return JavaParameters.JDK_AND_CLASSES_AND_TESTS;
            throw CantRunException.classNotFound(mainClassName, module);
        }
        final PsiFile psiFile = psiClass.getContainingFile();
        if (psiFile == null) throw CantRunException.classNotFound(mainClassName, module);
        final VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) throw CantRunException.classNotFound(mainClassName, module);
        Module classModule = psiClass.isValid() ? ModuleUtilCore.findModuleForPsiElement(psiClass) : null;
        if (classModule == null) classModule = module;
        ModuleFileIndex fileIndex = ModuleRootManager.getInstance(classModule).getFileIndex();
        if (fileIndex.isInSourceContent(virtualFile)) {
            return fileIndex.
                    isInTestSourceContent(virtualFile) ? JavaParameters.JDK_AND_CLASSES_AND_TESTS : JavaParameters.JDK_AND_CLASSES;
        }
        final List<OrderEntry> entriesForFile = fileIndex.getOrderEntriesForFile(virtualFile);
        for (OrderEntry entry : entriesForFile) {
            if (entry instanceof ExportableOrderEntry && ((ExportableOrderEntry)entry).getScope() == DependencyScope.TEST) return JavaParameters.JDK_AND_CLASSES_AND_TESTS;
        }
        return JavaParameters.JDK_AND_CLASSES;
    }

    public static void configureModule(final RunConfigurationModule runConfigurationModule,
                                       final JavaParameters parameters,
                                       final int classPathType,
                                       final String jreHome) throws CantRunException {
        Module module = runConfigurationModule.getModule();
        if (module == null) {
            throw CantRunException.noModuleConfigured(runConfigurationModule.getModuleName());
        }
        configureModule(module, parameters, classPathType, jreHome);
    }

    public static void configureModule(Module module, JavaParameters parameters, int classPathType, String jreHome) throws CantRunException {
        parameters.configureByModule(module, classPathType, createModuleJdk(module, jreHome));
    }

    public static void configureProject(Project project, final JavaParameters parameters, final int classPathType, final String jreHome)
            throws CantRunException {
        parameters.configureByProject(project, classPathType, createProjectJdk(project, jreHome));
    }

    private static Sdk createModuleJdk(final Module module, final String jreHome) throws CantRunException {
        return jreHome == null ? JavaParameters.getModuleJdk(module) : createAlternativeJdk(jreHome);
    }

    private static Sdk createProjectJdk(final Project project, final String jreHome) throws CantRunException {
        return jreHome == null ? createProjectJdk(project) : createAlternativeJdk(jreHome);
    }

    private static Sdk createProjectJdk(final Project project) throws CantRunException {
        final Sdk jdk = PathUtilEx.getAnyJdk(project);
        if (jdk == null) {
            throw CantRunException.noJdkConfigured();
        }
        return jdk;
    }

    private static Sdk createAlternativeJdk(final String jreHome) throws CantRunException {
        final Sdk jdk = JavaSdk.getInstance().createJdk("", jreHome);
        if (jdk == null) throw CantRunException.noJdkConfigured();
        return jdk;
    }

    public static void checkAlternativeJRE(CommonJavaRunConfigurationParameters configuration) throws RuntimeConfigurationWarning {
        if (configuration.isAlternativeJrePathEnabled()) {
            if (configuration.getAlternativeJrePath() == null ||
                    configuration.getAlternativeJrePath().length() == 0 ||
                    !JavaSdk.checkForJre(configuration.getAlternativeJrePath())) {
                throw new RuntimeConfigurationWarning(
                        ExecutionBundle.message("jre.path.is.not.valid.jre.home.error.mesage", configuration.getAlternativeJrePath()));
            }
        }
    }
}
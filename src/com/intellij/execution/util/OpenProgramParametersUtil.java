package com.intellij.execution.util;

/**
 * Created with IntelliJ IDEA.
 * User: mlauer
 * Date: 04/01/13
 * Time: 19:30
 * To change this template use File | Settings | File Templates.
 */


import com.intellij.execution.CommonProgramRunConfigurationParameters;
import com.intellij.execution.configurations.RuntimeConfigurationWarning;
import com.intellij.execution.configurations.SimpleProgramParameters;
import com.intellij.ide.DataManager;
import com.intellij.ide.macro.Macro;
import com.intellij.ide.macro.OpenMacroManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class OpenProgramParametersUtil {
    public static void configureConfiguration(SimpleProgramParameters parameters, CommonProgramRunConfigurationParameters configuration) {
        new ProgramParametersConfigurator(){
            public void configureConfiguration(SimpleProgramParameters parameters, CommonProgramRunConfigurationParameters configuration) {
                Project project = configuration.getProject();
                Module module = getModule(configuration);


                String programParametersRaw = configuration.getProgramParameters();
                String programParameters = "";
                DataContext dataContext = DataManager.getInstance().getDataContext();

                try {
                    programParameters= OpenMacroManager.getInstance().expandMacrosInString(programParametersRaw, true, dataContext);
                } catch (Macro.ExecutionCancelledException e) {
                    e.printStackTrace();
                    programParameters="### "+e.toString();
                }

                parameters.getProgramParametersList().addParametersString(this.expandPath(programParameters, module, project));

                parameters.setWorkingDirectory(getWorkingDir(configuration, project, module));

                parameters.setupEnvs(configuration.getEnvs(), configuration.isPassParentEnvs());
                if (parameters.getEnv() != null) {
                    Map<String, String> expanded = new HashMap<String, String>();
                    for (Map.Entry<String, String> each : parameters.getEnv().entrySet()) {
                        expanded.put(each.getKey(), expandPath(each.getValue(), module, project));
                    }
                    parameters.setEnv(expanded);
                }
            }
        }.configureConfiguration(parameters, configuration);
    }

    public static String getWorkingDir(CommonProgramRunConfigurationParameters configuration, Project project, Module module) {
        return new ProgramParametersConfigurator().getWorkingDir(configuration, project, module);
    }

    public static void checkWorkingDirectoryExist(CommonProgramRunConfigurationParameters configuration, Project project, Module module)
            throws RuntimeConfigurationWarning {
        new ProgramParametersConfigurator().checkWorkingDirectoryExist(configuration, project, module);
    }

    protected static String expandPath(String path, Module module, Project project) {
        return new ProgramParametersConfigurator(){
            @Override
            public String expandPath(String s, Module module, Project project) {
                return super.expandPath(s, module, project);
            }
        }.expandPath(path, module, project);
    }

    @Nullable
    protected static Module getModule(CommonProgramRunConfigurationParameters configuration) {
        return new ProgramParametersConfigurator(){
            @Nullable
            @Override
            protected Module getModule(CommonProgramRunConfigurationParameters commonProgramRunConfigurationParameters) {
                return super.getModule(commonProgramRunConfigurationParameters);
            }
        }.getModule(configuration);
    }
}

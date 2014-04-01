package de.lauerit.contextrun.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.execution.util.OpenJavaParametersUtil;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA.
 * User: mlauer
 * Date: 04/01/13
 * Time: 15:08
 * To change this template use File | Settings | File Templates.
 */
public class ContextRunConfiguration extends ApplicationConfiguration {

    public ContextRunConfiguration(String s, Project project, ContextRunConfigurationType configurationType) {
        super(s, project, configurationType.getConfigurationFactories()[0]);
    }

    public ContextRunConfiguration(String s, Project project, ConfigurationFactory configurationFactory) {
        super(s, project, configurationFactory);
    }

    public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
        final JavaCommandLineState state = new ContextRunState(this, env);
        state.setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject()));
        return state;
    }

    protected ModuleBasedConfiguration createInstance() {
        return new ContextRunConfiguration(getName(), getProject(), ContextRunConfigurationType.INSTANCE);
    }

    public static class ContextRunState extends JavaApplicationCommandLineState {

        private final ContextRunConfiguration myConfiguration;

        public ContextRunState(@NotNull final ContextRunConfiguration configuration,
                                               final ExecutionEnvironment environment) {
            super(configuration, environment);
            myConfiguration = configuration;
        }

        protected JavaParameters createJavaParameters() throws ExecutionException {
            final JavaParameters params = new JavaParameters();
            final JavaRunConfigurationModule module = myConfiguration.getConfigurationModule();

            final int classPathType = JavaParametersUtil.getClasspathType(module,
                    myConfiguration.MAIN_CLASS_NAME,
                    false);
            final String jreHome = myConfiguration.ALTERNATIVE_JRE_PATH_ENABLED ? myConfiguration.ALTERNATIVE_JRE_PATH
                    : null;
            JavaParametersUtil.configureModule(module, params, classPathType, jreHome);
            OpenJavaParametersUtil.configureConfiguration(params, myConfiguration);

            params.setMainClass(myConfiguration.MAIN_CLASS_NAME);
            for(RunConfigurationExtension ext: Extensions.getExtensions(RunConfigurationExtension.EP_NAME)) {
                ext.updateJavaParameters(myConfiguration, params, getRunnerSettings());
            }

            return params;
        }


        @NotNull
        @Override
        protected OSProcessHandler startProcess() throws ExecutionException {
            final OSProcessHandler handler = super.startProcess();
            RunnerSettings runnerSettings = getRunnerSettings();
            JavaRunConfigurationExtensionManager.getInstance().attachExtensionsToProcess(myConfiguration, handler, runnerSettings);
            return handler;
        }

        protected ApplicationConfiguration getConfiguration() {
            return myConfiguration;
        }
    }


}



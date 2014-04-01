package de.lauerit.contextrun.actions;

import com.intellij.execution.*;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import de.lauerit.contextrun.run.ContextRunConfigurationType;

import java.util.List;

/**
 * Created by mlauer on 01/04/14.
 */
public class ContextRunAction extends AnAction {

    protected final RunnerAndConfigurationSettings configuration;

    public ContextRunAction(RunnerAndConfigurationSettings configuration) {
        super(configuration.getName(), null,  AllIcons.Nodes.RunnableMark);
        this.configuration = configuration;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        Executor runExecutor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID);


        ExecutionManager.getInstance(project).restartRunProfile(project,
                runExecutor,
                ExecutionTargetManager.getActiveTarget(project),
                configuration,
                (RunContentDescriptor) null);

    }
}

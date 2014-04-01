package de.lauerit.contextrun.actions;

import com.intellij.execution.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import de.lauerit.contextrun.run.ContextRunConfigurationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mlauer on 01/04/14.
 */
public class ContextRunActionGroup extends ActionGroup {

    protected int counter = 1;




    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
        if (anActionEvent != null) {
            Project project = anActionEvent.getProject();
            RunManager runManager = RunManager.getInstance(project);
            RunnerAndConfigurationSettings[] configs = runManager.getConfigurationSettings(ContextRunConfigurationType.INSTANCE);
            int n = configs.length;
            if(n > 0) {
                AnAction[] children = new AnAction[n];
                for (int i = 0; i < n; i++) {
                    children[i] = new ContextRunAction(configs[i]);
                }
                return children;
            };
        }
        return AnAction.EMPTY_ARRAY;
    }

}

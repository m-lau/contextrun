package de.lauerit.contextrun.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import de.lauerit.contextrun.ContextRunBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: mlauer
 * Date: 04/01/13
 * Time: 13:38
 * To change this template use File | Settings | File Templates.
 */
public class ContextRunConfigurationType implements ConfigurationType {
    private final ContextRunFactory myConfigurationFactory;

    public static final ContextRunConfigurationType INSTANCE = new ContextRunConfigurationType();

    public ContextRunConfigurationType() {
        myConfigurationFactory = new ContextRunFactory(this);
    }

    public String getDisplayName() {
        return "ContextRun";
    }

    public String getConfigurationTypeDescription() {
        return "ContextRun menu entry";
    }

    public Icon getIcon() {
        return AllIcons.Nodes.RunnableMark;
    }

    @NonNls
    @NotNull
    public String getId() {
        return "ContextRunConfiguration";
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{myConfigurationFactory};
    }

    public static ContextRunConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(ContextRunConfigurationType.class);
    }


    private static class ContextRunFactory extends ConfigurationFactory {

        public ContextRunFactory(ConfigurationType type) {
            super(type);
        }

        public RunConfiguration createTemplateConfiguration(Project project) {
            return new ContextRunConfiguration("ContextRun", project, this);
        }

    }

}

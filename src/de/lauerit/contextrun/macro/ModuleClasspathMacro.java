package de.lauerit.contextrun.macro;

import com.intellij.ide.macro.Macro;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;

/**
 * Created with IntelliJ IDEA.
 * User: mlauer
 * Date: 25/09/13
 * Time: 09:31
 * To change this template use File | Settings | File Templates.
 */
public class ModuleClasspathMacro extends Macro {
    public String getName() {
        return "ModuleClasspath";
    }

    public String getDescription() {
        return "Module's classpath";
    }

    public String expand(DataContext dataContext) {

        final Module module = LangDataKeys.MODULE.getData(dataContext);
        if (module == null) return null;
        return OrderEnumerator.orderEntries(module).getPathsList().getPathsString();
    }
}

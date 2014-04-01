package de.lauerit.contextrun.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created with IntelliJ IDEA.
 * User: mlauer
 * Date: 04/01/13
 * Time: 14:09
 * To change this template use File | Settings | File Templates.
 */
public class SampleAction extends AnAction {



    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        System.out.println("Hello World!");
    }

}



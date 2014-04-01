package com.intellij.ide.macro;

/**
 * Created with IntelliJ IDEA.
 * User: mlauer
 * Date: 25/09/13
 * Time: 15:08
 * To change this template use File | Settings | File Templates.
 */

import com.intellij.application.options.PathMacrosImpl;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ConvertingIterator;
import com.intellij.util.containers.Convertor;
import com.intellij.util.containers.HashMap;
import de.lauerit.contextrun.macro.ModuleClasspathMacro;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;


public final class OpenMacroManager {
    private final HashMap<String, Macro> myMacrosMap = new HashMap<String, Macro>();

    public static OpenMacroManager getInstance() {
        return ServiceManager.getService(OpenMacroManager.class);
    }

    private OpenMacroManager() {
        registerMacro(new SourcepathMacro());
        registerMacro(new FileDirMacro());
        registerMacro(new FileExtMacro());
        registerMacro(new FileNameMacro());
        registerMacro(new FileNameWithoutExtension());
        registerMacro(new FilePathMacro());
        registerMacro(new FileEncodingMacro());
        registerMacro(new FileDirRelativeToProjectRootMacro());
        registerMacro(new FilePathRelativeToProjectRootMacro());
        registerMacro(new FileDirRelativeToSourcepathMacro());
        registerMacro(new FilePathRelativeToSourcepathMacro());
        registerMacro(new JdkPathMacro());
        registerMacro(new PromptMacro());
        registerMacro(new FilePromptMacro());
        registerMacro(new SourcepathEntryMacro());
        registerMacro(new ProjectFileDirMacro());
        registerMacro(new ProjectNameMacro());
        registerMacro(new ProjectPathMacro());

        registerMacro(new ModuleFilePathMacro());
        registerMacro(new ModuleFileDirMacro());
        registerMacro(new ModuleNameMacro());
        registerMacro(new ModulePathMacro());
        registerMacro(new ModuleSdkPathMacro());

        registerMacro(new FileRelativePathMacro());
        registerMacro(new FileRelativeDirMacro());
        registerMacro(new LineNumberMacro());
        registerMacro(new ColumnNumberMacro());

        registerMacro(new SelectedTextMacro());
        registerMacro(new SelectionStartLineMacro());
        registerMacro(new SelectionStartColumnMacro());
        registerMacro(new SelectionEndLineMacro());
        registerMacro(new SelectionEndColumnMacro());

        registerMacro(new ModuleClasspathMacro());

        if (File.separatorChar != '/') {
            registerMacro(new FileDirRelativeToProjectRootMacro2());
            registerMacro(new FilePathRelativeToProjectRootMacro2());
            registerMacro(new FileDirRelativeToSourcepathMacro2());
            registerMacro(new FilePathRelativeToSourcepathMacro2());
            registerMacro(new FileRelativeDirMacro2());
            registerMacro(new FileRelativePathMacro2());
        }
        for (Macro macro : Extensions.getExtensions(Macro.EP_NAME)) {
            registerMacro(macro);
        }
    }

    private void registerMacro(Macro macro) {
        //assert PathMacrosImpl.getToolMacroNames().contains(macro.getName()) : "Macro '" + macro.getName() + "' should be registered in PathMacros!";

        myMacrosMap.put(macro.getName(), macro);
    }

    public Collection<Macro> getMacros() {
        return myMacrosMap.values();
    }

    public void cacheMacrosPreview(DataContext dataContext) {
        dataContext = getCorrectContext(dataContext);
        for (Macro macro : getMacros()) {
            macro.cachePreview(dataContext);
        }
    }

    private static DataContext getCorrectContext(DataContext dataContext) {
        if (PlatformDataKeys.FILE_EDITOR.getData(dataContext) != null) {
            return dataContext;
        }
        Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return dataContext;
        }
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        VirtualFile[] files = editorManager.getSelectedFiles();
        if (files.length == 0) {
            return dataContext;
        }
        FileEditor fileEditor = editorManager.getSelectedEditor(files[0]);
        return fileEditor == null ? dataContext : DataManager.getInstance().getDataContext(fileEditor.getComponent());
    }

    /**
     * Expands all macros that are found in the <code>str</code>.
     */
    @Nullable
    public String expandMacrosInString(String str, boolean firstQueueExpand, DataContext dataContext) throws Macro.ExecutionCancelledException {
        return expandMacroSet(str, firstQueueExpand, dataContext, getMacros().iterator());
    }

    @Nullable
    private String expandMacroSet(String str,
                                  boolean firstQueueExpand, DataContext dataContext, Iterator<Macro> macros
    ) throws Macro.ExecutionCancelledException {
        if (str == null) return null;
        while (macros.hasNext()) {
            Macro macro = macros.next();
            if (macro instanceof SecondQueueExpandMacro && firstQueueExpand) continue;
            String name = "$" + macro.getName() + "$";
            if (str.indexOf(name) >= 0) {
                String expanded = macro.expand(dataContext);
                //if (dataContext instanceof DataManagerImpl.MyDataContext) {
                //  // hack: macro.expand() can cause UI events such as showing dialogs ('Prompt' macro) which may 'invalidate' the datacontext
                //  // since we know exactly that context is valid, we need to update its event count
                //  ((DataManagerImpl.MyDataContext)dataContext).setEventCount(IdeEventQueue.getInstance().getEventCount());
                //}
                if (expanded == null) {
                    expanded = "";
                }
                str = StringUtil.replace(str, name, expanded);
            }
        }
        return str;
    }

    public String expandSilentMarcos(String str, boolean firstQueueExpand, DataContext dataContext) throws Macro.ExecutionCancelledException {
        final Convertor<Macro, Macro> convertor = new Convertor<Macro, Macro>() {
            public Macro convert(Macro macro) {
                if (macro instanceof PromptingMacro) {
                    return new Macro.Silent(macro, "");
                }
                return macro;
            }
        };
        return expandMacroSet(
                str, firstQueueExpand, dataContext, ConvertingIterator.create(getMacros().iterator(), convertor)
        );
    }

}

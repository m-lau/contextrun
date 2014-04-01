ContextRun
==========

ContextRun is a plugin for [IntelliJ Idea](http://www.jetbrains.com/idea/).

It supports a new RunConfigurationType "ContextRun", which works like a normal "Application" run configuration,
    but can passes information on the command line about the context (for example: current selecet file) to the
    application.

ContextRun configurations also show up in the ContextMenu of the Project view and in the editor view.

The macros work the same way than the macros for external tool configuration and the additional macro
$ModuleClasspath$.

Some important macros are:

<table>
<tr>
<th>macro</th><th>description</th></tr>
<tr><td>$ModuleClasspath$</td><td>configured classpath of the module</td></tr>
<tr><td>$FilePath$        </td><td>full path of the current file</td></tr>
<tr><td>$FileClass$       </td><td>name of the class in current file</td></tr>
<tr><td>$FileName$        </td><td>file name of the current file</td></tr>
<tr><td>$FileDir$         </td><td>directory of the current file</td></tr>
<tr><td>$FileExt$         </td><td>extension of the current file</td></tr>
<tr><td>$LineNumber$      </td><td>current line number</td></tr>
<tr><td>$ModuleName$      </td><td>name of the current module</td></tr>
<tr><td>$SelectedText$   </td><td>selected text in editor</td></tr>
</table>

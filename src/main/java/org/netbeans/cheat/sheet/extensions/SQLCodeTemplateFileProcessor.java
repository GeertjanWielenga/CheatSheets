package org.netbeans.cheat.sheet.extensions;
import org.netbeans.cheat.sheet.api.CodeTemplateFileProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;
@ServiceProvider(service = CodeTemplateFileProcessor.class)
public class SQLCodeTemplateFileProcessor implements CodeTemplateFileProcessor {
    @Override
    public FileObject getInternalFile() {
        return FileUtil.getConfigFile("Editors/text/x-sql/CodeTemplates/Defaults/org-netbeans-modules-sql-editor-codetemplates.xml");
    }
    @Override
    public FileObject getFolderContainingCustomizableFile() {
        return FileUtil.getConfigFile("Editors/text/x-sql/CodeTemplates");
    }
    @Override
    public String toString() {
        return "SQL";
    }
}

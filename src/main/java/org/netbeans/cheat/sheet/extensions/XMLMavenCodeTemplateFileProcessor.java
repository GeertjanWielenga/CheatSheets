package org.netbeans.cheat.sheet.extensions;
import org.netbeans.cheat.sheet.api.CodeTemplateFileProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;
@ServiceProvider(service = CodeTemplateFileProcessor.class)
public class XMLMavenCodeTemplateFileProcessor implements CodeTemplateFileProcessor {
    @Override
    public FileObject getInternalFile() {
        return FileUtil.getConfigFile("Editors/text/x-maven-pom+xml/CodeTemplates/Defaults/org-netbeans-modules-maven-grammar-CodeTemplates.xml");
    }
    @Override
    public FileObject getFolderContainingCustomizableFile() {
        return FileUtil.getConfigFile("Editors/text/x-maven-pom+xml/CodeTemplates");
    }
    @Override
    public String toString() {
        return "Maven POM";
    }
}

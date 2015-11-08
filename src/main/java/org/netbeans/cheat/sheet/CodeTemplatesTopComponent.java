package org.netbeans.cheat.sheet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.cheat.sheet.api.CodeTemplateFileProcessor;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.BeanNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@ConvertAsProperties(
        dtd = "-//org.netbeans.cheats//CodeTemplates//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "CodeTemplatesTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(position = 20, mode = "properties", openAtStartup = true)
@ActionID(category = "Window", id = "org.netbeans.cheats.CodeTemplatesTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_CodeTemplatesAction",
        preferredID = "CodeTemplatesTopComponent"
)
@Messages({
    "CTL_CodeTemplatesAction=Code Templates",
    "CTL_CodeTemplatesTopComponent=Code Templates",})
public final class CodeTemplatesTopComponent extends TopComponent implements ExplorerManager.Provider, LookupListener {

    final DefaultComboBoxModel dlm = new DefaultComboBoxModel();
    private final ExplorerManager em = new ExplorerManager();
    private FileObject internalFo = null;
    private boolean enabled = true;

    public CodeTemplatesTopComponent() {
        setName(Bundle.CTL_CodeTemplatesTopComponent());
        setLayout(new BorderLayout());
        FileUtil.getConfigFile("Editors/text").addRecursiveListener(new FileChangeAdapter() {
            @Override
            public void fileChanged(FileEvent fe) {
                StatusDisplayer.getDefault().setStatusText("Changed File: " + fe.getFile().getPath());
                CodeTemplateFileProcessor selectedItem = (CodeTemplateFileProcessor) dlm.getSelectedItem();
                em.setRootContext(new AbstractNode(Children.create(new CodeTemplatesChildFactory(selectedItem), true)));
            }
        });
        addComboBox();
        addOutlineView();
        add(addContextSensitiveCheckbox(), BorderLayout.SOUTH);
    }

    private void addOutlineView() {
        OutlineView ov = new OutlineView("Template");
        ov.setPropertyColumns("value", "Expands To...","description", "Description");
        ov.getOutline().setRootVisible(false);
        add(ov, BorderLayout.CENTER);
    }

    private void addComboBox() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        JComboBox box = new JComboBox();
        Collection<? extends CodeTemplateFileProcessor> ctfps = Lookup.getDefault().lookupAll(CodeTemplateFileProcessor.class);
        for (CodeTemplateFileProcessor ctfp : ctfps) {
                dlm.addElement(ctfp);
        }
        box.setModel(dlm);
        CodeTemplateFileProcessor selectedItem = (CodeTemplateFileProcessor) dlm.getSelectedItem();
        em.setRootContext(new AbstractNode(Children.create(new CodeTemplatesChildFactory(selectedItem), true)));
        box.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CodeTemplateFileProcessor selectedItem = (CodeTemplateFileProcessor) dlm.getSelectedItem();
                em.setRootContext(new AbstractNode(Children.create(new CodeTemplatesChildFactory(selectedItem), true)));
            }
        });
        panel.add(box);
        JButton optionsButton = new JButton(ImageUtilities.loadImageIcon("org/netbeans/cheats/options.png", false));
        optionsButton.setPreferredSize(new Dimension(16,16));
        optionsButton.setToolTipText("Click to Modify the Code Templates");
        optionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsDisplayer.getDefault().open(OptionsDisplayer.EDITOR+"/CodeTemplates");
            }
        });
        panel.add(optionsButton);
        add(panel, BorderLayout.NORTH);
    }

    private JCheckBox addContextSensitiveCheckbox() {
        final JCheckBox cb = new JCheckBox("Context Sensitive");
        cb.setSelected(true);
        cb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cb.isSelected()) {
                    enabled = true;
                    StatusDisplayer.getDefault().setStatusText("Context sensitivity enabled.");
                } else {
                    enabled = false;
                    StatusDisplayer.getDefault().setStatusText("Context sensitivity disabled.");
                }
            }
        });
        return cb;
    }
    
    private class CodeTemplatesChildFactory extends ChildFactory<DisplayObject> {
        private final CodeTemplateFileProcessor selectedItem;
        private CodeTemplatesChildFactory(CodeTemplateFileProcessor selectedItem) {
            this.selectedItem = selectedItem;
        }
        @Override
        protected boolean createKeys(final List<DisplayObject> list) {
            if (selectedItem.getFolderContainingCustomizableFile() != null) {
                if (selectedItem.getFolderContainingCustomizableFile().getChildren().length > 0) {
                    FileObject fo = selectedItem.getFolderContainingCustomizableFile().getChildren()[0];
                    if (fo != null && fo.isFolder() && selectedItem.getFolderContainingCustomizableFile().getChildren().length == 2) {
                        fo = selectedItem.getFolderContainingCustomizableFile().getChildren()[1];
                        parseFile(fo, list);
                    }
                    if (fo != null && fo.isData()) {
                        parseFile(fo, list);
                    }
                }
            }
            internalFo = selectedItem.getInternalFile();
            if (internalFo != null) {
                parseFile(internalFo, list);
            }
            return true;
        }

        private void parseFile(FileObject foToParse, List<DisplayObject> list) throws DOMException {
            try {
                InputStream is = foToParse.getInputStream();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new InputSource(is));
                NodeList nodeList = doc.getElementsByTagName("*");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    org.w3c.dom.Node mainNode = nodeList.item(i);
                    NamedNodeMap map = mainNode.getAttributes();
                    //Iterate through the map of attributes:
                    for (int j = 0; j < map.getLength(); j++) {
                        //Each iteration, create a new Node:
                        org.w3c.dom.Node attrNode = map.item(j);
                        //Get the name of the current Attribute:
                        String attrName = attrNode.getNodeName();
                        if (attrName.equals("abbreviation")) {
                            String abbreviation = attrNode.getNodeValue();
                            String expandsTo = null;
                            String description = "";
                            NodeList childNodes = mainNode.getChildNodes();
                            for (int k = 0; k < childNodes.getLength(); k++) {
                                org.w3c.dom.Node item = childNodes.item(k);
                                String nodeName = item.getNodeName();
                                if (nodeName.equals("code")) {
                                    expandsTo = item.getTextContent();
                                }
                                if (nodeName.equals("description")) {
                                    description = item.getTextContent();
                                }
                            }
                            DisplayObject newObject = new DisplayObject(abbreviation, expandsTo, description);
                            boolean unique = true;
                            for (DisplayObject anObjectInTheList : list) {
                                if (anObjectInTheList.getKey().equals(newObject.getKey())) {
                                    unique = false;
                                }
                            }
                            if (unique) {
                                list.add(newObject);
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SAXException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ParserConfigurationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        @Override
        protected Node createNodeForKey(final DisplayObject object) {
            BeanNode node = null;
            try {
                node = new BeanNode(object){
                    @Override
                    public Action[] getActions(boolean context) {
                        return new Action[]{};
                    }
                };
                node.setDisplayName(object.getKey());
                node.setShortDescription(object.getValue());
            } catch (IntrospectionException ex) {
                Exceptions.printStackTrace(ex);
            }
            return node;
        }
    }

    void writeProperties(java.util.Properties p) {
        p.setProperty("version", "1.0");
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    Lookup.Result<FileObject> fileObjectResult;

    @Override
    public void resultChanged(LookupEvent le) {
        if (enabled) {
            if (!fileObjectResult.allInstances().isEmpty()) {
                internalFo = fileObjectResult.allInstances().iterator().next();
                for (int i = 0; i < dlm.getSize(); i++) {
                    Object element = dlm.getElementAt(i);
                    StatusDisplayer.getDefault().setStatusText(internalFo.getMIMEType() + "/" + element.toString().toLowerCase() + "/" + internalFo.getPath());
                    if (internalFo.getMIMEType().contains(element.toString().toLowerCase())) {
                        dlm.setSelectedItem(element);
                    }
                }
            }
        }
    }

    @Override
    protected void componentOpened() {
        fileObjectResult = Utilities.actionsGlobalContext().lookupResult(FileObject.class);
        fileObjectResult.addLookupListener(this);
    }

    @Override
    protected void componentClosed() {
        fileObjectResult.removeLookupListener(this);
    }

}

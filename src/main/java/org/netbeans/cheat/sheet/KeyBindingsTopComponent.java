package org.netbeans.cheat.sheet;

import java.awt.BorderLayout;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.BeanNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@ConvertAsProperties(
        dtd = "-//org.netbeans.cheats//KeyBindings//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "KeyBindingsTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(position = 10, mode = "properties", openAtStartup = true)
@ActionID(category = "Window", id = "org.netbeans.cheats.KeyBindingsTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_KeyBindingsAction",
        preferredID = "KeyBindingsTopComponent"
)
@Messages({
    "CTL_KeyBindingsAction=Key Bindings",
    "CTL_KeyBindingsTopComponent=Key Bindings",})
public final class KeyBindingsTopComponent extends TopComponent implements ExplorerManager.Provider {

    private final ExplorerManager em = new ExplorerManager();

    public KeyBindingsTopComponent() {
        setName(Bundle.CTL_KeyBindingsTopComponent());
        setLayout(new BorderLayout());
        OutlineView ov = new OutlineView("Action");
        ov.setPropertyColumns("value", "Shortcut");
        ov.getOutline().setRootVisible(false);
        add(ov, BorderLayout.CENTER);
        em.setRootContext(new AbstractNode(Children.create(new KeyBindingsChildFactory(), true)));
    }

    private static class KeyBindingsChildFactory extends ChildFactory<DisplayObject> {

        @Override
        protected boolean createKeys(List<DisplayObject> list) {
            try {
                FileObject kb = FileUtil.getConfigFile("Editors/Keybindings/NetBeans/Defaults/org-netbeans-modules-editor-keybindings.xml");
//                InputStream is = kb.getInputStream();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                //Using factory get an instance of document builder
                DocumentBuilder db = dbf.newDocumentBuilder();
                //parse using builder to get DOM representation of the XML file
                Document doc = db.parse(new InputSource(kb.getInputStream()));
//                Document doc = XMLUtil.parse(new InputSource(is), false, false, null, null);
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
                        if (attrName.equals("actionName")) {
                            String key = attrNode.getNodeValue();
                            String value = map.item(j + 1).getNodeValue();
                            if (value.startsWith("DS-")) {
                                value = value.replace("DS-", "Ctrl-Shift-");
                            } else if (value.startsWith("S-")) {
                                value = value.replace("S-", "Shift-");
                            } else if (value.startsWith("D-")) {
                                value = value.replace("D-", "Ctrl-");
                            } else if (value.startsWith("KP_")) {
                                value = value.replace("KP_", "Keypad-");
                            } else if (value.startsWith("O-")) {
                                value = value.replace("O-", "Alt-");
                            } else if (value.startsWith("OS-")) {
                                value = value.replace("OS-", "Alt-Shift-");
                            }
                            list.add(new DisplayObject(key, value,""));
                        }
                    }
                }
//                DataObject.find(kb).getLookup().lookup(OpenCookie.class).open();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SAXException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ParserConfigurationException ex) {
                Exceptions.printStackTrace(ex);
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(DisplayObject object) {
            BeanNode node = null;
            try {
                node = new BeanNode(object);
                node.setDisplayName(object.getKey());
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
}

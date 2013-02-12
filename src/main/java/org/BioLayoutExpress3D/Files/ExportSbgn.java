package org.BioLayoutExpress3D.Files;

import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.HashMap;
import java.util.List;
import org.sbgn.*;
import org.sbgn.bindings.*;
import javax.xml.bind.JAXBException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.BioLayoutExpress3D.CoreUI.*;
import org.BioLayoutExpress3D.DataStructures.Tuple2;
import org.BioLayoutExpress3D.DataStructures.Tuple6;
import org.BioLayoutExpress3D.StaticLibraries.*;
import org.BioLayoutExpress3D.Graph.*;
import org.BioLayoutExpress3D.Graph.GraphElements.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;
import org.BioLayoutExpress3D.Network.GraphmlComponentContainer;
import org.BioLayoutExpress3D.Network.GraphmlNetworkContainer;
import org.BioLayoutExpress3D.Network.NetworkRootContainer;
import org.BioLayoutExpress3D.Utils.Point3D;

/**
 * The ExportSbgn class is used to export to SBGN-ML files
 *
 * @author Tim Angus <tim.angus@roslin.ed.ac.uk>
 *
*/
public final class ExportSbgn
{
    // Constant to adjust to an appropriate scale for SBGN files
    final static float SCALE = 40.0f;
    final static String PROCESS_EDGE_GLYPH_INDICATOR = "pegi";
    final static String ENERGY_TRANSFER_GLYPH_INDICATOR = "em/t";

    private LayoutFrame layoutFrame = null;
    private JFileChooser fileChooser = null;
    private AbstractAction exportSbgnAction = null;
    private FileNameExtensionFilter fileNameExtensionFilterSbgn = null;

    private NetworkRootContainer nc;
    private GraphmlNetworkContainer gnc;

    public ExportSbgn(LayoutFrame layoutFrame)
    {
        this.layoutFrame = layoutFrame;

        initComponents();
    }

    private void initComponents()
    {
        fileNameExtensionFilterSbgn = new FileNameExtensionFilter("Save as an SBGN File", "sbgn");

        String saveFilePath = FILE_CHOOSER_PATH.get().substring(
                0, FILE_CHOOSER_PATH.get().lastIndexOf(System.getProperty("file.separator")) + 1);
        fileChooser = new JFileChooser(saveFilePath);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(fileNameExtensionFilterSbgn);

        exportSbgnAction = new AbstractAction("SBGN File...")
        {
            @Override
            public void actionPerformed(ActionEvent action)
            {
                setFileChooser("Save SBGN File");
                save();
            }
        };
        exportSbgnAction.setEnabled(false);
    }

    private void setFileChooser(String fileChooserTitle)
    {
        fileChooser.setDialogTitle(fileChooserTitle);
        fileChooser.setSelectedFile(new File(IOUtils.getPrefix(layoutFrame.getFileNameLoaded())));
    }

    public AbstractAction getExportSbgnAction()
    {
        return exportSbgnAction;
    }

    private void save()
    {
        int dialogReturnValue;
        boolean doSaveFile = false;
        File saveFile = null;

        if (fileChooser.showSaveDialog(layoutFrame) == JFileChooser.APPROVE_OPTION)
        {
            String fileExtension = fileNameExtensionFilterSbgn.getExtensions()[0];

            String fileName = fileChooser.getSelectedFile().getAbsolutePath();
            fileName = IOUtils.checkFileNameExtension(fileName, fileExtension);
            saveFile = new File(fileName + "." + fileExtension);

            if (saveFile.exists())
            {
                // Do you want to overwrite
                dialogReturnValue = JOptionPane.showConfirmDialog(layoutFrame,
                        "This File Already Exists.\nDo you want to Overwrite it?",
                        "This File Already Exists. Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION);

                if (dialogReturnValue == JOptionPane.YES_OPTION)
                {
                    doSaveFile = true;
                }
            }
            else
            {
                doSaveFile = true;
            }
        }

        if (doSaveFile)
        {
            // saving process on its own thread, to effectively decouple it from the main GUI thread
            Thread runLightWeightThread = new Thread(new ExportSbgnProcess(saveFile));
            runLightWeightThread.setPriority(Thread.NORM_PRIORITY);
            runLightWeightThread.start();
        }
    }

    private static final java.util.Map<String, String> LABEL_TO_GLYPH_CLASS;
    static
    {
        java.util.Map<String, String> map = new HashMap<String, String>();
        map.put("D",  "dissociation");
        map.put("&",  "and");
        map.put("OR", "or");
        map.put("B",  "process");
        map.put("O",  "process");
        map.put("X",  "process");
        map.put("AX", "process");
        map.put("AC", "process");
        map.put("C",  "process");
        map.put("TL", "process");
        map.put("T",  "process");
        map.put("I",  "process");
        map.put("A",  "process");
        map.put("-P", "process");
        map.put("P",  "process");
        map.put("PT", "process");
        map.put("Su", "process");
        map.put("AP", "process");
        map.put("Gy", "process");
        map.put("Ub", "process");
        map.put("Me", "process");
        map.put("Se", "process");
        map.put("Pa", "process");
        map.put("Pr", "process");
        map.put("S",  "process");
        map.put("At", "process");
        map.put("My", "process");
        map.put("H+", "process");
        map.put("OH", "process");
        map.put("Pe", "process");
        map.put("Ox", "process");
        map.put("Sec","process");
        map.put("/",  "source and sink");
        LABEL_TO_GLYPH_CLASS = Collections.unmodifiableMap(map);
    }

    private class Component
    {
        private int n;
        private String name;
        private List<String> infoList;
        private List<String> modList;

        public Component(int n, String name, List<String> infoList, List<String> modList)
        {
            this.n = n;
            this.name = name;
            this.infoList = infoList;
            this.modList = modList;
        }

        public int getNumber() { return n; }
        public String getName() { return name; }
        public List<String> getInfoList() { return infoList; }
        public List<String> getModList() { return modList; }
    }

    private class ComponentList
    {
        private String alias;
        private List<Component> components;

        public ComponentList(String alias, List<Component> components)
        {
            this.alias = alias;
            this.components = components;
        }

        public String getAlias() { return alias; }
        public List<Component> getComponents() { return components; }
    }

    private static boolean stringIsNotWhitespace(String s)
    {
        return s.trim().length() > 0;
    }

    private ComponentList parseMepnLabel(String mepnLabel)
    {
        String alias = null;
        Pattern aliasRegex = Pattern.compile("[^\\n]+(\\n\\s*\\(([^\\)]*)\\))");
        Matcher aliasMatcher = aliasRegex.matcher(mepnLabel);
        String strippedLabel = mepnLabel;
        if (aliasMatcher.find())
        {
            alias = aliasMatcher.group(2);
            strippedLabel = mepnLabel.replace(aliasMatcher.group(1), "");
        }

        List<Component> list = new ArrayList<Component>();

        String[] components = strippedLabel.split("\\s*:\\s*");

        // Match this pattern: <n>PROT1[A]
        Pattern regex = Pattern.compile("(?:<([^>]+)>)?([^\\[]*)(?:\\[([^\\]]+)\\])?");
        for (String component : components)
        {
            Matcher m = regex.matcher(component);
            int n = 1; // Assume 1 until told otherwise
            String name = null;
            List<String> modList = new ArrayList<String>();

            String s;
            while (m.find())
            {
                s = m.group(1);
                if (s != null && stringIsNotWhitespace(s))
                {
                    try { n = Integer.parseInt(s); }
                    catch(NumberFormatException e)
                    {
                        n = -1;
                    }
                }

                s = m.group(2);
                if (s != null && stringIsNotWhitespace(s))
                {
                    name = s;
                }

                s = m.group(3);
                if (s != null && stringIsNotWhitespace(s))
                {
                    modList.add(s);
                }
            }

            Component c = new Component(n, name, new ArrayList<String>(), modList);
            list.add(c);
        }

        return new ComponentList(alias, list);
    }

    private void configureComponentGlyph(String type, Component c, Glyph glyph)
    {
        final float INFO_X = 0.3f * SCALE;
        Bbox glyphBbox = glyph.getBbox();
        int multimer = c.getNumber();

        if (multimer != 1)
        {
            c.getInfoList().add(type);

            if (multimer < 0)
            {
                c.getInfoList().add("N:?");
            }
            else
            {
                c.getInfoList().add("N:" + multimer);
            }

            glyph.setClazz(type + " multimer");
        }
        else
        {
            glyph.setClazz(type);
        }

        int infoIndex = 1;
        for (String info : c.getInfoList())
        {
            // Infos
            Glyph infoGlyph = new Glyph();
            infoGlyph.setId(glyph.getId() + ".info" + infoIndex);
            infoGlyph.setClazz("unit of information");

            Bbox bbox = new Bbox();
            bbox.setX(glyphBbox.getX() + (INFO_X * infoIndex));
            bbox.setY(glyphBbox.getY());
            infoGlyph.setBbox(bbox);

            Label label = new Label();
            label.setText(info);
            infoGlyph.setLabel(label);

            glyph.getGlyph().add(infoGlyph);

            infoIndex++;
        }

        int modIndex = 1;
        for (String mod : c.getModList())
        {
            // Mods
            Glyph multimerGlyph = new Glyph();
            multimerGlyph.setId(glyph.getId() + ".mod" + modIndex);
            multimerGlyph.setClazz("state variable");

            Bbox bbox = new Bbox();
            bbox.setX(glyphBbox.getX() + (INFO_X * modIndex));
            bbox.setY(glyphBbox.getY() + glyphBbox.getH());
            multimerGlyph.setBbox(bbox);

            Label label = new Label();
            label.setText(mod);
            multimerGlyph.setLabel(label);

            glyph.getGlyph().add(multimerGlyph);

            modIndex++;
        }

        String name = c.getName();
        if (name != null && name.length() > 0)
        {
            Label label = new Label();
            label.setText(name);
            glyph.setLabel(label);
        }
    }

    private List<Bbox> subDivideBbox(Bbox parent, int subdivisions, float targetAspect)
    {
        List<Bbox> list = new ArrayList<Bbox>();
        float parentAspect = parent.getW() / parent.getH();

        if (subdivisions > 1)
        {
            Bbox first = new Bbox();
            Bbox second = new Bbox();
            if (parentAspect < targetAspect)
            {
                // Top
                first.setW(parent.getW());
                first.setH(parent.getH() * 0.5f);
                first.setX(parent.getX());
                first.setY(parent.getY());

                // Bottom
                second.setW(parent.getW());
                second.setH(parent.getH() * 0.5f);
                second.setX(parent.getX());
                second.setY(parent.getY() + second.getH());
            }
            else
            {
                // Left
                first.setW(parent.getW() * 0.5f);
                first.setH(parent.getH());
                first.setX(parent.getX());
                first.setY(parent.getY());

                // Right
                second.setW(parent.getW() * 0.5f);
                second.setH(parent.getH());
                second.setX(parent.getX() + second.getW());
                second.setY(parent.getY());
            }

            list.addAll(subDivideBbox(first, subdivisions / 2, targetAspect));
            list.addAll(subDivideBbox(second, subdivisions / 2, targetAspect));
        }
        else
        {
            list.add(parent);
        }

        return list;
    }

    private List<Bbox> subDivideGlyph(Glyph glyph, ComponentList cl)
    {
        final float ALIAS_VERTICAL_SPACE = 1.0f * SCALE;
        final float TARGET_ASPECT = 2.0f;
        int subdivisions = cl.getComponents().size();

        int pow2 = 1;
        while (pow2 < subdivisions)
        {
            pow2 <<= 1;
        }

        Bbox parentBbox = glyph.getBbox();
        Bbox componentsBbox = new Bbox();
        String alias = cl.getAlias();
        if (alias != null)
        {
            componentsBbox.setX(parentBbox.getX());
            componentsBbox.setY(parentBbox.getY());
            componentsBbox.setW(parentBbox.getW());
            componentsBbox.setH(parentBbox.getH() - ALIAS_VERTICAL_SPACE);

            Bbox labelBbox = new Bbox();
            labelBbox.setX(componentsBbox.getX());
            labelBbox.setY(componentsBbox.getY() + componentsBbox.getH());
            labelBbox.setW(componentsBbox.getW());
            labelBbox.setH(ALIAS_VERTICAL_SPACE);

            Label label = new Label();
            label.setText(alias);
            label.setBbox(labelBbox);
            glyph.setLabel(label);
        }
        else
        {
            componentsBbox.setX(parentBbox.getX());
            componentsBbox.setY(parentBbox.getY());
            componentsBbox.setW(parentBbox.getW());
            componentsBbox.setH(parentBbox.getH());
        }

        List<Bbox> list = subDivideBbox(componentsBbox, pow2, TARGET_ASPECT);
        List<Bbox> scaledList = new ArrayList<Bbox>(list.size());

        for (Bbox bbox : list)
        {
            final float SUB_SCALE = 0.6f;
            float xOffset = 0.5f * (bbox.getW() - (bbox.getW() * SUB_SCALE));
            float yOffset = 0.5f * (bbox.getH() - (bbox.getH() * SUB_SCALE));

            Bbox scaledBbox = new Bbox();
            scaledBbox.setW(bbox.getW() * SUB_SCALE);
            scaledBbox.setH(bbox.getH() * SUB_SCALE);
            scaledBbox.setX(bbox.getX() + xOffset);
            scaledBbox.setY(bbox.getY() + yOffset);

            scaledList.add(scaledBbox);
        }

        Collections.sort(scaledList, new java.util.Comparator<Bbox>()
        {
            @Override
            public int compare(Bbox a, Bbox b)
            {
                if (a.getY() == b.getY())
                {
                    return (a.getX() < b.getX()) ? -1 : 1;
                }
                else
                {
                    return (a.getY() < b.getY()) ? -1 : 1;
                }
            }
        });

        return scaledList;
    }

    private void configureComponentGlyph(String type, ComponentList cl, Glyph glyph)
    {
        if (cl.getComponents().size() > 1)
        {
            glyph.setClazz("complex");
            List<Glyph> subGlyphs = glyph.getGlyph();
            List<Bbox> subBboxes = subDivideGlyph(glyph, cl);

            int index = 0;
            for (Component c : cl.getComponents())
            {
                Glyph subGlyph = new Glyph();
                subGlyph.setId(glyph.getId() + "." + (index + 1));
                subGlyph.setBbox(subBboxes.get(index));
                configureComponentGlyph(type, c, subGlyph);

                subGlyphs.add(subGlyph);

                index++;
            }
        }
        else
        {
            configureComponentGlyph(type, cl.getComponents().get(0), glyph);
        }
    }

    private boolean specialiseSpnDistSpacerOrOutput(String mepnShape, String mepnLabel, Color mepnBackColor, Glyph glyph)
    {
        if (mepnLabel.isEmpty())
        {
            if (mepnShape.equals("diamond") && mepnBackColor.equals(Color.BLACK))
            {
                // Distribution
                glyph.setClazz("process");
                return true;
            }
            else if (mepnShape.equals("ellipse") && mepnBackColor.equals(Color.WHITE))
            {
                // Spacer
                glyph.setClazz(PROCESS_EDGE_GLYPH_INDICATOR);
                return true;
            }
        }

        return false;
    }

    private boolean specialiseSpnTokenInput(String mepnShape, String mepnLabel, Color mepnBackColor, Glyph glyph)
    {
        if (mepnLabel.isEmpty() && mepnBackColor.equals(Color.BLACK) &&
            (mepnShape.equals("rectangle") || mepnShape.equals("roundedrectangle")))
        {
            glyph.setClazz("source and sink");
            makeSquarePreserveArea(glyph.getBbox());
            return true;
        }

        return false;
    }

    private void specialiseSimpleBiochemical(String mepnLabel, Glyph glyph)
    {
        makeSquarePreserveArea(glyph.getBbox());

        if (mepnLabel.length() > 0)
        {
            ComponentList cl = parseMepnLabel(mepnLabel);

            if (cl.getComponents().size() == 1)
            {
                configureComponentGlyph("simple chemical", cl.getComponents().get(0), glyph);
            }
            else
            {
                // Multiple components? Just use the label directly
                Label label = new Label();
                label.setText(mepnLabel);
                glyph.setLabel(label);

                glyph.setClazz("simple chemical");
            }
        }
    }

    private boolean specialiseSbgnGlyph(String mepnShape, String mepnLabel, Color mepnBackColor, Glyph glyph)
    {
        if (specialiseSpnTokenInput(mepnShape, mepnLabel, mepnBackColor, glyph) ||
                specialiseSpnDistSpacerOrOutput(mepnShape, mepnLabel, mepnBackColor, glyph))
        {
            return true;
        }
        else if (mepnShape.equals("ellipse"))
        {
            String glyphClass = LABEL_TO_GLYPH_CLASS.get(mepnLabel);
            if (glyphClass != null)
            {
                glyph.setClazz(glyphClass);
                return true;
            }

            // Assume every other ellipse is an...
            glyph.setClazz("unspecified entity");

            if (mepnLabel.length() > 0)
            {
                Label label = new Label();
                label.setText(mepnLabel);
                glyph.setLabel(label);
            }

            return true;
        }
        else if (mepnShape.equals("diamond"))
        {
            if (mepnLabel.equals("A") || mepnLabel.equals("C") || mepnLabel.equals("I"))
            {
                // In theory this only occurs in mEPN 2010 diagrams
                // This isn't a real class, but a marker for later when the edges are being processed
                glyph.setClazz(PROCESS_EDGE_GLYPH_INDICATOR + mepnLabel);

                return true;
            }
            else
            {
                // Ion/simple molecule
                specialiseSimpleBiochemical(mepnLabel, glyph);

                return true;
            }
        }
        else if (mepnShape.equals("hexagon"))
        {
            // Simple biochemical
            specialiseSimpleBiochemical(mepnLabel, glyph);

            return true;
        }
        else if (mepnShape.equals("octagon"))
        {
            // Pathway output/module
            glyph.setClazz("submap");

            if (mepnLabel.length() > 0)
            {
                Label label = new Label();
                label.setText(mepnLabel);
                glyph.setLabel(label);
            }

            //FIXME: add tags to incident edges

            return true;
        }
        else if (mepnShape.equals("parallelogram") || mepnShape.equals("rectangle"))
        {
            // Gene/DNA sequence
            ComponentList cl = parseMepnLabel(mepnLabel);

            if (cl.getComponents().size() > 0)
            {
                if (cl.getComponents().size() == 1)
                {
                    Component c = cl.getComponents().get(0);

                    if (mepnShape.equals("parallelogram"))
                    {
                        c.getInfoList().add("ct:grr");
                    }
                    else if (mepnShape.equals("rectangle"))
                    {
                        c.getInfoList().add("ct:gene");
                    }
                }

                configureComponentGlyph("nucleic acid feature", cl, glyph);
                return true;
            }
        }
        else if (mepnShape.equals("roundrectangle"))
        {
            // Peptide/protein/protein complex
            ComponentList cl = parseMepnLabel(mepnLabel);

            if (cl.getComponents().size() > 0)
            {
                configureComponentGlyph("macromolecule", cl, glyph);
                return true;
            }
        }
        else if (mepnShape.equals("trapezoid"))
        {
            // Drug
            glyph.setClazz("perturbing agent");

            if (mepnLabel.length() > 0)
            {
                Label label = new Label();
                label.setText(mepnLabel);
                glyph.setLabel(label);
            }

            return true;
        }
        else if (mepnShape.equals("trapezoid2"))
        {
            // Energy/molecular transfer
            // This isn't a real class, but a marker for later
            glyph.setClazz(ENERGY_TRANSFER_GLYPH_INDICATOR + mepnLabel);

            return true;
        }

        return false;
    }

    private Glyph translateNodeToSbgnGlyph(GraphNode graphNode, String id)
    {
        float x, y;

        if (nc.getIsGraphml() && YED_STYLE_RENDERING_FOR_GPAPHML_FILES.get())
        {
            float[] graphmlCoord = gnc.getAllGraphmlNodesMap().get(graphNode.getNodeName()).first;
            x = graphmlCoord[2] * SCALE;
            y = graphmlCoord[3] * SCALE;
        }
        else
        {
            x = graphNode.getX() * SCALE;
            y = graphNode.getY() * SCALE;
        }

        Glyph glyph = new Glyph();
        glyph.setId(id);

        Tuple6<float[], String[], String[], String[], String[], String> nodeData =
                gnc.getAllGraphmlNodesMap().get(graphNode.getNodeName());
        String mepnShape = nodeData.sixth;
        float mepnWidth = nodeData.first[1];
        float mepnHeight = nodeData.first[0];
        float mepnAspect = mepnWidth / mepnHeight;
        String mepnLabel = Graph.customizeNodeName(nc.getNodeName(graphNode.getNodeName()));
        Color mepnBackColor;
        try { mepnBackColor = Color.decode(nodeData.second[1]); }
        catch (Exception e)
        {
            mepnBackColor = Color.WHITE;
        }

        float width = mepnWidth * SCALE;
        float height = mepnHeight * SCALE;

        Bbox bbox = new Bbox();
        bbox.setX(x - (width * 0.5f));
        bbox.setW(width);
        bbox.setY(y - (height * 0.5f));
        bbox.setH(height);
        glyph.setBbox(bbox);

        if (!specialiseSbgnGlyph(mepnShape, mepnLabel, mepnBackColor, glyph))
        {
            // Fallback when we don't know what it is
            glyph.setClazz("unspecified entity");

            Label label = new Label();
            label.setText("Unknown entity: " + mepnLabel);
            glyph.setLabel(label);
        }

        return glyph;
    }

    private static Point2D.Float getCentreOf(Bbox bbox)
    {
        return new Point2D.Float(bbox.getX() + (bbox.getW() * 0.5f), bbox.getY() + (bbox.getH() * 0.5f));
    }

    private static Point2D.Float getCentreOf(Glyph glyph)
    {
        return getCentreOf(glyph.getBbox());
    }

    private static void makeSquarePreserveArea(Bbox bbox)
    {
        float diameter = (float) java.lang.Math.sqrt(bbox.getW() * bbox.getH());
        float halfDiameter = diameter * 0.5f;
        Point2D.Float centre = getCentreOf(bbox);

        bbox.setX(centre.x - halfDiameter);
        bbox.setY(centre.y - halfDiameter);
        bbox.setW(diameter);
        bbox.setH(diameter);
    }

    private static void setArcStartToGlyph(Arc arc, Glyph glyph)
    {
        arc.setSource(glyph);
        Point2D.Float point = getCentreOf(glyph);
        Arc.Start start = new Arc.Start();
        start.setX(point.x);
        start.setY(point.y);
        arc.setStart(start);
    }

    private static void setArcEndToGlyph(Arc arc, Glyph glyph)
    {
        arc.setTarget(glyph);
        Point2D.Float point = getCentreOf(glyph);
        Arc.End end = new Arc.End();
        end.setX(point.x);
        end.setY(point.y);
        arc.setEnd(end);
    }

    private static String processEdgeGlyphSpecifierToSbgnArcClass(char mepnSpecifier)
    {
        switch (mepnSpecifier)
        {
            default:
            case 'A': return "stimulation";
            case 'C': return "catalysis";
            case 'I': return "inhibition";
        }
    }

    private Arc mergeArcs(Arc source, Glyph intermediate, Arc target)
    {
        Arc newArc = new Arc();
        newArc.setId(source.getId() + "." + target.getId());
        newArc.setSource(source.getSource());
        newArc.setStart(source.getStart());
        newArc.setTarget(target.getTarget());
        newArc.setEnd(target.getEnd());

        List<Arc.Next> newArcNextList = newArc.getNext();
        newArcNextList.addAll(source.getNext());

        //FIXME: if this point is on the line made by adjacent points, don't need to add it
        Arc.Next intermediateNext = new Arc.Next();
        Point2D.Float point = getCentreOf(intermediate);
        intermediateNext.setX(point.x);
        intermediateNext.setY(point.y);
        newArcNextList.add(intermediateNext);

        newArcNextList.addAll(target.getNext());

        String intermediateClazz = intermediate.getClazz();
        String mepnSpecifier = intermediateClazz.replace(PROCESS_EDGE_GLYPH_INDICATOR, "");

        if (mepnSpecifier.length() == 1)
        {
            newArc.setClazz(mepnSpecifier);
        }
        else
        {
            newArc.setClazz(target.getClazz());
        }

        return newArc;
    }

    private static List<Arc> arcsGoingTo(Glyph glyph, List<Arc> arcList)
    {
        List<Arc> out = new ArrayList();

        for (Arc arc : arcList)
        {
            if (arc.getTarget() == glyph)
            {
                out.add(arc);
            }
        }

        return out;
    }

    private static List<Arc> arcsComingFrom(Glyph glyph, List<Arc> arcList)
    {
        List<Arc> out = new ArrayList();

        for (Arc arc : arcList)
        {
            if (arc.getSource() == glyph)
            {
                out.add(arc);
            }
        }

        return out;
    }

    private boolean pointsAreColinear(List<Point2D.Float> points, int first, int middle, int last)
    {
        final float EPSILON = 1.0f;

        Line2D.Float line = new Line2D.Float(points.get(first), points.get(last));
        return (float)line.ptLineDist(points.get(middle)) < EPSILON;
    }

    private List<Point2D.Float> simplifyArcs(List<Point2D.Float> points)
    {
        List<Point2D.Float> results = new ArrayList<Point2D.Float>();
        int first = 0;

        while (first < points.size())
        {
            int middle = first + 1;
            int last = first + 2;

            results.add(points.get(first));

            // While middle lies on line made by first to last
            while (last < points.size() && pointsAreColinear(points, first, middle, last))
            {
                middle++;
                last++;
            }

            first = middle;
        }

        return results;
    }

    private void simplifyArcs(Map map)
    {
        List<Arc> arcList = map.getArc();

        for (Arc arc : arcList)
        {
            if (arc.getNext().size() > 0)
            {
                List<Point2D.Float> points = new ArrayList<Point2D.Float>();
                points.add(new Point2D.Float(arc.getStart().getX(), arc.getStart().getY()));
                for (Arc.Next next : arc.getNext())
                {
                    points.add(new Point2D.Float(next.getX(), next.getY()));
                }
                points.add(new Point2D.Float(arc.getEnd().getX(), arc.getEnd().getY()));

                List<Point2D.Float> results = simplifyArcs(points);

                if (results.size() != points.size())
                {
                    Arc.Start start = new Arc.Start();
                    start.setX(results.get(0).x);
                    start.setY(results.get(0).y);
                    arc.setStart(start);

                    arc.getNext().clear();
                    for (int i = 1; i < results.size() - 1; i++)
                    {
                        Arc.Next next = new Arc.Next();
                        next.setX(results.get(i).x);
                        next.setY(results.get(i).y);
                        arc.getNext().add(next);
                    }


                    Arc.End end = new Arc.End();
                    end.setX(results.get(results.size() - 1).x);
                    end.setY(results.get(results.size() - 1).y);
                    arc.setEnd(end);
                }
            }
        }
    }

    private void transformProcessEdgeGlyphs(Map map)
    {
        List<Arc> arcList = map.getArc();

        for (Iterator<Glyph> glyphIt = map.getGlyph().iterator(); glyphIt.hasNext();)
        {
            Glyph glyph = glyphIt.next();

            if (glyph.getClazz().startsWith(PROCESS_EDGE_GLYPH_INDICATOR))
            {
                // Build lists of Arcs going to and coming from this glyph
                List<Arc> sourceArcList = arcsGoingTo(glyph, arcList);
                List<Arc> targetArcList = arcsComingFrom(glyph, arcList);

                // For each pair of Arcs that share the glyph
                for (Arc source : sourceArcList)
                {
                    for (Arc target : targetArcList)
                    {
                        arcList.add(mergeArcs(source, glyph, target));
                    }
                }

                // Remove original Arcs
                arcList.removeAll(sourceArcList);
                arcList.removeAll(targetArcList);

                // Remove original glyph
                glyphIt.remove();
            }
        }
    }

    private void transformEnergyTransferGlyphs(Map map)
    {
        List<Glyph> glyphList = map.getGlyph();
        List<Arc> arcList = map.getArc();
        List<Glyph> glyphsToAdd = new ArrayList<Glyph>();
        List<Arc> arcsToAdd = new ArrayList<Arc>();
        List<Glyph> glyphsToRemove = new ArrayList<Glyph>();
        List<Arc> arcsToRemove = new ArrayList<Arc>();

        for (Glyph glyph : glyphList)
        {
            if (glyph.getClazz().startsWith(ENERGY_TRANSFER_GLYPH_INDICATOR))
            {
                List<Arc> arcs = new ArrayList<Arc>();

                arcs.addAll(arcsComingFrom(glyph, arcList));
                arcs.addAll(arcsGoingTo(glyph, arcList));

                String originalLabel = glyph.getClazz().replace(ENERGY_TRANSFER_GLYPH_INDICATOR, "");
                Pattern r = Pattern.compile("^\\s*(.+?)\\s*->\\s*(.+?)\\s*$");
                Matcher m = r.matcher(originalLabel);

                if (arcs.size() != 1 || !m.matches())
                {
                    // This doesn't look like energy transfer
                    glyph.setClazz("unspecified entity");
                    Label label = new Label();
                    label.setText("Unknown energy/molecular transfer: " + originalLabel);
                    glyph.setLabel(label);
                    continue;
                }

                String leftText = m.group(1);
                String rightText = m.group(2);

                Arc arc = arcs.get(0);
                Glyph target = (Glyph)arc.getTarget();

                if (glyph == target)
                {
                    // Sometimes it points the other way
                    target = (Glyph)arc.getSource();
                }

                // Replace original glyph with two smaller ones
                Bbox originalBbox = glyph.getBbox();
                final float NEW_GLYPH_SCALE = 1.0f;
                float simpleChemicalWidth = originalBbox.getH() * NEW_GLYPH_SCALE;
                float horizontalShift = simpleChemicalWidth * 0.5f;

                Glyph leftGlyph = new Glyph();
                Arc leftArc = new Arc();
                {
                    Bbox leftBbox = new Bbox();
                    leftBbox.setW(simpleChemicalWidth);
                    leftBbox.setH(simpleChemicalWidth);
                    leftBbox.setX(originalBbox.getX() - horizontalShift);
                    leftBbox.setY(originalBbox.getY());

                    leftGlyph.setId(glyph.getId() + ".left");
                    leftGlyph.setBbox(leftBbox);
                    specialiseSimpleBiochemical(leftText, leftGlyph);

                    leftArc.setId(arc.getId() + ".left");
                    setArcStartToGlyph(leftArc, leftGlyph);
                    setArcEndToGlyph(leftArc, target);
                    leftArc.setClazz("production");
                }

                Glyph rightGlyph = new Glyph();
                Arc rightArc = new Arc();
                {
                    Bbox rightBbox = new Bbox();
                    rightBbox.setW(simpleChemicalWidth);
                    rightBbox.setH(simpleChemicalWidth);
                    rightBbox.setX(originalBbox.getX() + originalBbox.getW() -
                            simpleChemicalWidth + horizontalShift);
                    rightBbox.setY(originalBbox.getY());

                    rightGlyph.setId(glyph.getId() + ".right");
                    rightGlyph.setBbox(rightBbox);
                    specialiseSimpleBiochemical(rightText, rightGlyph);

                    rightArc.setId(arc.getId() + ".right");
                    setArcStartToGlyph(rightArc, target);
                    setArcEndToGlyph(rightArc, rightGlyph);
                    rightArc.setClazz("production");
                }

                glyphsToAdd.add(leftGlyph);
                arcsToAdd.add(leftArc);
                glyphsToAdd.add(rightGlyph);
                arcsToAdd.add(rightArc);

                glyphsToRemove.add(glyph);
                arcsToRemove.add(arc);
            }
        }

        glyphList.addAll(glyphsToAdd);
        glyphList.removeAll(glyphsToRemove);
        arcList.addAll(arcsToAdd);
        arcList.removeAll(arcsToRemove);
    }

    private void specialiseSbgnArc(List<String> arrowHeads, Glyph source, Glyph target, Arc arc)
    {
        if (arrowHeads.contains("transparent_circle"))
        {
            arc.setClazz("catalysis");
        }
        else if (arrowHeads.contains("t_shape") ||
                arrowHeads.contains("diamond") ||
                arrowHeads.contains("white_diamond"))
        {
            arc.setClazz("inhibition");
        }
        else if (LABEL_TO_GLYPH_CLASS.containsValue(source.getClazz()))
        {
            // Treat everything that's coming from a process as production
            arc.setClazz("production");
        }
        else
        {
            // Everything else
            arc.setClazz("consumption");
        }
    }

    private Arc translateEdgeToSbgnArc(GraphEdge graphEdge, String id, Glyph source, Glyph target)
    {
        GraphNode startNode = graphEdge.getNodeFirst();
        GraphNode endNode = graphEdge.getNodeSecond();
        float startX, startY;
        float endX, endY;

        if (nc.getIsGraphml() && YED_STYLE_RENDERING_FOR_GPAPHML_FILES.get())
        {
            float[] graphmlStartCoord = gnc.getAllGraphmlNodesMap().get(startNode.getNodeName()).first;
            float[] graphmlEndCoord = gnc.getAllGraphmlNodesMap().get(endNode.getNodeName()).first;
            startX = graphmlStartCoord[2] * SCALE;
            startY = graphmlStartCoord[3] * SCALE;
            endX = graphmlEndCoord[2] * SCALE;
            endY = graphmlEndCoord[3] * SCALE;
        }
        else
        {
            startX = startNode.getX() * SCALE;
            startY = startNode.getY() * SCALE;
            endX = endNode.getX() * SCALE;
            endY = endNode.getY() * SCALE;
        }

        Arc arc = new Arc();
        arc.setId(id);

        if (source != null)
        {
            arc.setSource(source);
        }

        if (target != null)
        {
            arc.setTarget(target);
        }

        Arc.Start start = new Arc.Start();
        start.setX(startX);
        start.setY(startY);
        arc.setStart(start);

        Tuple6<String, Tuple2<float[], ArrayList<Point2D.Float>>, String[], String[], String[], String[]>
                    edgeData = gnc.getAllGraphmlEdgesMap().get(startNode.getNodeName() + " " +
                    endNode.getNodeName());

        ArrayList<Point2D.Float> intermediatePoints = edgeData.second.second;
        String[] arrowHeads = edgeData.fourth;

        if (nc.getIsGraphml() && YED_STYLE_RENDERING_FOR_GPAPHML_FILES.get())
        {
            if (intermediatePoints != null)
            {
                List<Arc.Next> nextList = arc.getNext();
                for (Point2D.Float polylinePoint2D : intermediatePoints)
                {
                    Arc.Next next = new Arc.Next();
                    next.setX(polylinePoint2D.x * SCALE);
                    next.setY(polylinePoint2D.y * SCALE);
                    nextList.add(next);
                }
            }
        }

        Arc.End end = new Arc.End();
        end.setX(endX);
        end.setY(endY);
        arc.setEnd(end);

        specialiseSbgnArc(Arrays.asList(arrowHeads), source, target, arc);

        return arc;
    }

    private Glyph translateContainerToSbgnGlyph(GraphmlComponentContainer componentContainer, String id)
    {
        float x = componentContainer.rectangle2D.x * SCALE;
        float y = componentContainer.rectangle2D.y * SCALE;
        float width = componentContainer.rectangle2D.width * SCALE;
        float height = componentContainer.rectangle2D.height * SCALE;

        Glyph glyph = new Glyph();
        glyph.setId(id);

        String mepnLabel = componentContainer.name;
        Color mepnBackColor = componentContainer.color;

        Bbox bbox = new Bbox();
        bbox.setX(x - (width * 0.5f));
        bbox.setW(width);
        bbox.setY(y - (height * 0.5f));
        bbox.setH(height);
        glyph.setBbox(bbox);

        glyph.setClazz("compartment");

        Label label = new Label();

        // Label at the top centre of the compartment
        Bbox labelBbox = new Bbox();
        labelBbox.setX(x - (width * 0.5f));
        labelBbox.setW(width);
        labelBbox.setY(y - (height * 0.5f));
        labelBbox.setH(100.0f);
        label.setBbox(labelBbox);
        label.setText(mepnLabel);
        glyph.setLabel(label);

        return glyph;
    }

    private Sbgn translateMepnToSbgn(Graph in)
    {
        Sbgn sbgn = new Sbgn();
        Map map = new Map();
        sbgn.setMap(map);
        map.setLanguage("process description");
        java.util.Map<Integer,Glyph> sbgnGlyphs = new HashMap<Integer,Glyph>();

        if (nc.getIsGraphml() && YED_STYLE_RENDERING_FOR_GPAPHML_FILES.get())
        {
            int containerId = 1;
            for (GraphmlComponentContainer componentContainer : gnc.getAllPathwayComponentContainersFor2D())
            {
                String id = "container" + Integer.toString(containerId++);
                Glyph glyph = translateContainerToSbgnGlyph(componentContainer, id);

                map.getGlyph().add(glyph);
            }
        }

        for (GraphNode graphNode : in.getGraphNodes())
        {
            String id = "glyph" + Integer.toString(graphNode.getNodeID());
            Glyph glyph = translateNodeToSbgnGlyph(graphNode, id);

            map.getGlyph().add(glyph);
            sbgnGlyphs.put(graphNode.getNodeID(), glyph);
        }

        int edgeId = 1;
        List<GraphEdge> sortedGraphEdges = new ArrayList<GraphEdge>(in.getGraphEdges());
        Collections.sort(sortedGraphEdges, new java.util.Comparator<GraphEdge>()
        {
            @Override
            public int compare(GraphEdge a, GraphEdge b)
            {
                int aSourceNodeId = a.getNodeFirst().getNodeID();
                int aTargetNodeId = a.getNodeSecond().getNodeID();
                int bSourceNodeId = b.getNodeFirst().getNodeID();
                int bTargetNodeId = b.getNodeSecond().getNodeID();

                if (aSourceNodeId == bSourceNodeId)
                {
                    return aTargetNodeId - bTargetNodeId;
                }
                else
                {
                    return aSourceNodeId - bSourceNodeId;
                }
            }
        });

        for (GraphEdge graphEdge : sortedGraphEdges)
        {
            String id = "a" + Integer.toString(edgeId++);
            Glyph source = sbgnGlyphs.get(graphEdge.getNodeFirst().getNodeID());
            Glyph target = sbgnGlyphs.get(graphEdge.getNodeSecond().getNodeID());
            Arc arc = translateEdgeToSbgnArc(graphEdge, id, source, target);

            map.getArc().add(arc);
        }

        transformProcessEdgeGlyphs(map);
        transformEnergyTransferGlyphs(map);
        simplifyArcs(map);

        return sbgn;
    }

    private void saveSbgnFile(File saveFile)
    {
        try
        {
            nc = layoutFrame.getNetworkRootContainer();
            gnc = nc.getGraphmlNetworkContainer();

            Sbgn sbgn = translateMepnToSbgn(layoutFrame.getGraph());
            SbgnUtil.writeToFile(sbgn, saveFile);
        }
        catch (JAXBException e)
        {
            if (DEBUG_BUILD)
            {
                println("Failed to write SBGN file" + e.toString());
            }
        }
    }

    private class ExportSbgnProcess implements Runnable
    {
        private File saveFile = null;

        private ExportSbgnProcess(File saveFile)
        {
            this.saveFile = saveFile;
        }

        @Override
        public void run()
        {
            saveSbgnFile(saveFile);

            FILE_CHOOSER_PATH.set(saveFile.getAbsolutePath());
        }
    }
}
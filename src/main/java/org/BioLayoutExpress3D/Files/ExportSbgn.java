package org.BioLayoutExpress3D.Files;

import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.ArrayList;
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

    private class ProteinComponent
    {
        private int n;
        private String name;
        private List<String> modList;

        public ProteinComponent(int n, String name, List<String> modList)
        {
            this.n = n;
            this.name = name;
            this.modList = modList;
        }

        public int getNumber() { return n; }
        public String getName() { return name; }
        public List<String> getModList() { return modList; }
    }

    private class ProteinComponentList
    {
        private String alias;
        private List<ProteinComponent> components;

        public ProteinComponentList(String alias, List<ProteinComponent> components)
        {
            this.alias = alias;
            this.components = components;
        }

        public String getAlias() { return alias; }
        public List<ProteinComponent> getComponents() { return components; }
    }

    private static boolean stringIsNotWhitespace(String s)
    {
        return s.trim().length() > 0;
    }

    private ProteinComponentList parseMepnLabel(String mepnLabel)
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

        List<ProteinComponent> list = new ArrayList<ProteinComponent>();

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

            ProteinComponent pc = new ProteinComponent(n, name, modList);
            list.add(pc);
        }

        return new ProteinComponentList(alias, list);
    }

    private void configureComponentGlyph(String type, ProteinComponent pc, Glyph glyph)
    {
        final float INFO_X = 0.3f * SCALE;
        Bbox glyphBbox = glyph.getBbox();
        int multimer = pc.getNumber();

        if (multimer != 1)
        {
            Glyph multimerGlyph = new Glyph();
            multimerGlyph.setId(glyph.getId() + ".multimer");
            multimerGlyph.setClazz("unit of information");

            // N:x
            Bbox bbox = new Bbox();
            bbox.setX(glyphBbox.getX() + INFO_X);
            bbox.setY(glyphBbox.getY());
            multimerGlyph.setBbox(bbox);

            Label label = new Label();
            if (multimer < 0)
            {
                label.setText("N:?");
            }
            else
            {
                label.setText("N:" + multimer);
            }
            multimerGlyph.setLabel(label);

            glyph.getGlyph().add(multimerGlyph);

            glyph.setClazz(type + " multimer");
        }
        else
        {
            glyph.setClazz(type);
        }

        int modIndex = 1;
        List<String> modList = pc.getModList();
        for (String mod : modList)
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

        String name = pc.getName();
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

    private List<Bbox> subDivideGlyph(Glyph glyph, ProteinComponentList pcl)
    {
        final float ALIAS_VERTICAL_SPACE = 1.0f * SCALE;
        final float TARGET_ASPECT = 2.0f;
        int subdivisions = pcl.getComponents().size();

        int pow2 = 1;
        while (pow2 < subdivisions)
        {
            pow2 <<= 1;
        }

        Bbox parentBbox = glyph.getBbox();
        Bbox componentsBbox = new Bbox();
        String alias = pcl.getAlias();
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

    private void configureComponentGlyph(String type, ProteinComponentList pcl, Glyph glyph)
    {
        if (pcl.getComponents().size() > 1)
        {
            glyph.setClazz("complex");
            List<Glyph> subGlyphs = glyph.getGlyph();
            List<Bbox> subBboxes = subDivideGlyph(glyph, pcl);

            int index = 0;
            for (ProteinComponent pc : pcl.getComponents())
            {
                Glyph subGlyph = new Glyph();
                subGlyph.setId(glyph.getId() + "." + (index + 1));
                subGlyph.setBbox(subBboxes.get(index));
                configureComponentGlyph(type, pc, subGlyph);

                subGlyphs.add(subGlyph);

                index++;
            }
        }
        else
        {
            configureComponentGlyph(type, pcl.getComponents().get(0), glyph);
        }
    }

    private boolean specialiseSbgnGlyph(String mepnShape, String mepnLabel, Glyph glyph)
    {
        if (mepnShape.equals("ellipse"))
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
                glyph.setClazz("simple chemical");

                if (mepnLabel.length() > 0)
                {
                    Label label = new Label();
                    label.setText(mepnLabel);
                    glyph.setLabel(label);
                }

                return true;
            }
        }
        else if (mepnShape.equals("hexagon"))
        {
            // Simple biochemical
            glyph.setClazz("simple chemical");

            if (mepnLabel.length() > 0)
            {
                Label label = new Label();
                label.setText(mepnLabel);
                glyph.setLabel(label);
            }

            return true;
        }
        else if (mepnShape.equals("octagon"))
        {
            //FIXME: implement
            // Pathway output/module
        }
        else if (mepnShape.equals("parallelogram") || mepnShape.equals("rectangle"))
        {
            // Gene/DNA sequence
            ProteinComponentList pcl = parseMepnLabel(mepnLabel);

            if (pcl.getComponents().size() > 0)
            {
                configureComponentGlyph("nucleic acid feature", pcl, glyph);
                return true;
            }
        }
        else if (mepnShape.equals("roundrectangle"))
        {
            // Peptide/protein/protein complex
            ProteinComponentList pcl = parseMepnLabel(mepnLabel);

            if (pcl.getComponents().size() > 0)
            {
                configureComponentGlyph("macromolecule", pcl, glyph);
                return true;
            }
        }
        else if (mepnShape.equals("trapezoid"))
        {
            // Drug
            glyph.setClazz("unspecified entity");

            if (mepnLabel.length() > 0)
            {
                Label label = new Label();
                label.setText("Drug: " + mepnLabel);
                glyph.setLabel(label);
            }

            return true;
        }
        else if (mepnShape.equals("trapezoid2"))
        {
            //FIXME: implement
            // Energy/molecular transfer
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

        float width = mepnWidth * SCALE;
        float height = mepnHeight * SCALE;

        Bbox bbox = new Bbox();
        bbox.setX(x - (width * 0.5f));
        bbox.setW(width);
        bbox.setY(y - (height * 0.5f));
        bbox.setH(height);
        glyph.setBbox(bbox);

        if (!specialiseSbgnGlyph(mepnShape, mepnLabel, glyph))
        {
            // Fallback when we don't know what it is
            glyph.setClazz("unspecified entity");

            Label label = new Label();
            label.setText("***FIXME*** " + mepnLabel);
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

    private static String processEdgeGlyphToSbgnArcClass(Glyph glyph)
    {
        String clazz = glyph.getClazz();
        String mepnSpecifier = clazz.replace(PROCESS_EDGE_GLYPH_INDICATOR, "");

        switch (mepnSpecifier.charAt(0))
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

        newArc.setClazz(processEdgeGlyphToSbgnArcClass(intermediate));

        return newArc;
    }

    private void transformProcessEdgeGlyphs(Map map)
    {
        List<Arc> arcList = map.getArc();

        for (Iterator<Glyph> glyphIt = map.getGlyph().iterator(); glyphIt.hasNext();)
        {
            Glyph glyph = glyphIt.next();

            if (glyph.getClazz().startsWith(PROCESS_EDGE_GLYPH_INDICATOR))
            {
                List<Arc> sourceArcList = new ArrayList<Arc>();
                List<Arc> targetArcList = new ArrayList<Arc>();

                // Build lists of Arcs going to and coming from this glyph
                for (Arc arc : arcList)
                {
                    if (arc.getSource() == glyph)
                    {
                        targetArcList.add(arc);
                    }
                    else if (arc.getTarget() == glyph)
                    {
                        sourceArcList.add(arc);
                    }
                }

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

    private void specialiseSbgnArc(String[] arrowHeads, Glyph source, Glyph target, Arc arc)
    {
        if (!target.getClazz().equals("process"))
        {
            // Treat everything that isn't pointing to a process as production
            arc.setClazz("production");
        }
        else
        {
            // Fallback when we don't know what it is
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

        specialiseSbgnArc(arrowHeads, source, target, arc);

        return arc;
    }

    private Sbgn translateMepnToSbgn(Graph in)
    {
        Sbgn sbgn = new Sbgn();
        Map map = new Map();
        sbgn.setMap(map);
        map.setLanguage("process description");
        java.util.Map<Integer,Glyph> sbgnGlyphs = new HashMap<Integer,Glyph>();

        for (GraphNode graphNode : in.getGraphNodes())
        {
            String id = "glyph" + Integer.toString(graphNode.getNodeID());
            Glyph glyph = translateNodeToSbgnGlyph(graphNode, id);

            map.getGlyph().add(glyph);
            sbgnGlyphs.put(graphNode.getNodeID(), glyph);
        }

        int edgeId = 1;
        for (GraphEdge graphEdge : in.getGraphEdges())
        {
            String id = "a" + Integer.toString(edgeId++);
            Glyph source = sbgnGlyphs.get(graphEdge.getNodeFirst().getNodeID());
            Glyph target = sbgnGlyphs.get(graphEdge.getNodeSecond().getNodeID());
            Arc arc = translateEdgeToSbgnArc(graphEdge, id, source, target);

            map.getArc().add(arc);
        }

        transformProcessEdgeGlyphs(map);

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
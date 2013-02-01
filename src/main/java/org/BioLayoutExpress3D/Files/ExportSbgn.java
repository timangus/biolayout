package org.BioLayoutExpress3D.Files;

import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
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
    final float SCALE = 40.0f;

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

    private ProteinComponentList parseMepnLabel(String mepnLabel)
    {
        String alias = "";
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
                if (s != null && !s.isEmpty())
                {
                    try { n = Integer.parseInt(s); }
                    catch(NumberFormatException e) { n = -1; }
                }

                s = m.group(2);
                if (s != null && !s.isEmpty())
                {
                    name = s;
                }

                s = m.group(3);
                if (s != null && !s.isEmpty())
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
        if (pc.getNumber() > 1)
        {
            glyph.setClazz(type + " multimer");
        }
        else
        {
            glyph.setClazz(type);
        }

        Label label = new Label();
        label.setText(pc.getName());
        glyph.setLabel(label);
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
                // Top half
                first.setW(parent.getW());
                first.setH(parent.getH() * 0.5f);
                first.setX(parent.getX());
                first.setY(parent.getY());

                // Bottom half
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

    private List<Bbox> subDivideGlyph(Glyph glyph, int subdivisions)
    {
        final float TARGET_ASPECT = 4.0f;

        int pow2 = 1;
        while (pow2 < subdivisions)
        {
            pow2 <<= 1;
        }

        List<Bbox> list = subDivideBbox(glyph.getBbox(), pow2, TARGET_ASPECT);
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

        return scaledList;
    }

    private void configureComplexGlyph(String type, ProteinComponentList pcl, Glyph glyph)
    {
        if (pcl.getComponents().size() > 1)
        {
            glyph.setClazz("complex");
            List<Glyph> subGlyphs = glyph.getGlyph();
            List<Bbox> subBboxes = subDivideGlyph(glyph, pcl.getComponents().size());

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

            //FIXME: use pcl.getAlias();
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

            Label label = new Label();
            label.setText(mepnLabel);
            glyph.setLabel(label);
            return true;
        }
        else if (mepnShape.equals("diamond"))
        {
            //FIXME: implement
            // Ion/simple molecule
            // Also, some annotated edges e.g. Catalyses ---<>--->
        }
        else if (mepnShape.equals("hexagon"))
        {
            //FIXME: implement
            // Simple biochemical
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
                configureComplexGlyph("nucleic acid feature", pcl, glyph);
                return true;
            }
        }
        else if (mepnShape.equals("roundrectangle"))
        {
            // Peptide/protein/protein complex
            ProteinComponentList pcl = parseMepnLabel(mepnLabel);

            if (pcl.getComponents().size() > 0)
            {
                configureComplexGlyph("macromolecule", pcl, glyph);
                return true;
            }
        }
        else if (mepnShape.equals("trapezoid"))
        {
            //FIXME: implement
            // Drug
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
            glyph.setClazz("simple chemical");

            Label label = new Label();
            label.setText("***FIXME*** " + mepnLabel);
            glyph.setLabel(label);
        }

        return glyph;
    }

    private boolean specialiseSbgnArc(String[] arrowHeads, Glyph source, Glyph target, Arc arc)
    {
        if (!target.getClazz().equals("process")) //FIXME: probably flawed logic
        {
            arc.setClazz("production");
            return true;
        }

        return false;
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

        if (!specialiseSbgnArc(arrowHeads, source, target, arc))
        {
            // Fallback when we don't know what it is
            arc.setClazz("consumption");
        }

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
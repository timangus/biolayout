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

            //FIXME: probably need to account for "Generic entity" in here too
        }
        else if (mepnShape.equals("diamond"))
        {
            //FIXME: implement
        }
        else if (mepnShape.equals("hexagon"))
        {
            //FIXME: implement
        }
        else if (mepnShape.equals("octagon"))
        {
            //FIXME: implement
        }
        else if (mepnShape.equals("parallelogram"))
        {
            //FIXME: implement
        }
        else if (mepnShape.equals("rectangle"))
        {
            //FIXME: implement
        }
        else if (mepnShape.equals("roundrectangle"))
        {
            //FIXME: implement
        }
        else if (mepnShape.equals("trapezoid"))
        {
            //FIXME: implement
        }
        else if (mepnShape.equals("trapezoid2"))
        {
            //FIXME: implement
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
package org.BioLayoutExpress3D.Files;

import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import org.sbgn.*;
import org.sbgn.bindings.*;
import javax.xml.bind.JAXBException;
import org.BioLayoutExpress3D.CoreUI.*;
import org.BioLayoutExpress3D.StaticLibraries.*;
import org.BioLayoutExpress3D.Graph.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;

/**
 * The ExportSbgn class is used to export to SBGN-ML files
 *
 * @author Tim Angus <tim.angus@roslin.ed.ac.uk>
 *
*/
public final class ExportSbgn
{

    private LayoutFrame layoutFrame = null;
    private JFileChooser fileChooser = null;
    private AbstractAction exportSbgnAction = null;
    private FileNameExtensionFilter fileNameExtensionFilterSbgn = null;

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
            String fileExtension;

            if (fileChooser.getFileFilter().equals(fileNameExtensionFilterSbgn))
            {
                fileExtension = fileNameExtensionFilterSbgn.getExtensions()[0];
            }
            else // default file extension will be the SBGN file format
            {
                fileExtension = fileNameExtensionFilterSbgn.getExtensions()[0];
            }

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

    private void translateMepnToSbgn(Graph in, Sbgn out)
    {
        //TODO: implement this
    }

    private void saveSbgnFile(File saveFile)
    {
        try
        {
            Sbgn sbgn = new Sbgn();
            Map map = new Map();
            sbgn.setMap(map);

            translateMepnToSbgn(layoutFrame.getGraph(), sbgn);

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
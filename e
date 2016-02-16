[1mdiff --git a/src/main/java/org/Kajeka/ClassViewerUI/Tables/ClassViewerUpdateEnrichmentTable.java b/src/main/java/org/Kajeka/ClassViewerUI/Tables/ClassViewerUpdateEnrichmentTable.java[m
[1mindex d533b2b..190fa94 100644[m
[1m--- a/src/main/java/org/Kajeka/ClassViewerUI/Tables/ClassViewerUpdateEnrichmentTable.java[m
[1m+++ b/src/main/java/org/Kajeka/ClassViewerUI/Tables/ClassViewerUpdateEnrichmentTable.java[m
[36m@@ -403,7 +403,7 @@[m [mpublic final class ClassViewerUpdateEnrichmentTable implements Runnable {[m
                     });[m
                     LogAxis logaxis = new LogAxis("Adj. P-value (Smaller is more significant)");[m
                     logaxis.setBase(10);[m
[31m-                    LogFormat format = new LogFormat(10.0, "10", "E", true);[m
[32m+[m[32m                    LogFormat format = new LogFormat(10.0, "1", "E", true);[m[41m[m
                     logaxis.setNumberFormatOverride(format);[m
                     //logaxis.setLowerBound(Double.MIN_VALUE);[m
                     pValuechart.getCategoryPlot().setRangeAxis(logaxis);[m
[1mdiff --git a/src/main/java/org/Kajeka/CoreUI/LayoutFrame.java b/src/main/java/org/Kajeka/CoreUI/LayoutFrame.java[m
[1mindex 8917044..ec06129 100644[m
[1m--- a/src/main/java/org/Kajeka/CoreUI/LayoutFrame.java[m
[1m+++ b/src/main/java/org/Kajeka/CoreUI/LayoutFrame.java[m
[36m@@ -1107,7 +1107,6 @@[m [mpublic final class LayoutFrame extends JFrame implements GraphListener[m
         CorrelationLoader correlationLoader = null;[m
         String reasonForLoadFailure = null;[m
         double correlationCutOffValue = 0.0;[m
[31m-        boolean tabDelimited = fileExtension.equals(SupportedInputFileTypes.EXPRESSION.toString());[m
 [m
         // Blast data[m
         if ( fileExtension.equals( SupportedInputFileTypes.BLAST.toString() ) )[m
[36m@@ -1125,6 +1124,7 @@[m [mpublic final class LayoutFrame extends JFrame implements GraphListener[m
         else if ( fileExtension.equals( SupportedInputFileTypes.CSV.toString() ) ||[m
                 fileExtension.equals( SupportedInputFileTypes.EXPRESSION.toString() ))[m
         {[m
[32m+[m[32m            boolean tabDelimited = fileExtension.equals(SupportedInputFileTypes.EXPRESSION.toString());[m
             CorrelationLoaderDialog correlationLoaderDialog = new CorrelationLoaderDialog(this, file, tabDelimited);[m
 [m
             if (!correlationLoaderDialog.failed())[m
[36m@@ -1143,7 +1143,7 @@[m [mpublic final class LayoutFrame extends JFrame implements GraphListener[m
                         correlationLoaderDialog.getFirstDataColumn(),[m
                         correlationLoaderDialog.getFirstDataRow(),[m
                         correlationLoaderDialog.transpose() );[m
[31m-                isSuccessful = correlationLoader.parse(this, tabDelimited);[m
[32m+[m[32m                isSuccessful = correlationLoader.parse(this);[m
                 reasonForLoadFailure = correlationLoader.reasonForFailure; // "" if no failure[m
 [m
                 if (isSuccessful)[m
[36m@@ -1369,7 +1369,8 @@[m [mpublic final class LayoutFrame extends JFrame implements GraphListener[m
                 // loading annotations now[m
                 if ( DATA_TYPE.equals(DataTypes.CORRELATION) )[m
                 {[m
[31m-                    isSuccessful = isSuccessful && correlationLoader.parseAnnotations(this, nc, tabDelimited);[m
[32m+[m[32m                    boolean tabDelimited = fileExtension.equals(SupportedInputFileTypes.EXPRESSION.toString());[m
[32m+[m[32m                    isSuccessful = isSuccessful && correlationLoader.parseAnnotations(this, nc);[m
                 }[m
                 // else loading presaved data that points to data[m
                 // load data and annotations from original file[m
[36m@@ -1389,12 +1390,13 @@[m [mpublic final class LayoutFrame extends JFrame implements GraphListener[m
                                 CORRELATION_DATA_FIRST_COLUMN,[m
                                 CORRELATION_DATA_FIRST_ROW,[m
                                 CORRELATION_DATA_TRANSPOSE);[m
[31m-                        isSuccessful = correlationLoader.parse(this, tabDelimited);[m
[32m+[m[32m                        boolean tabDelimited = fileExtension.equals(SupportedInputFileTypes.EXPRESSION.toString()); //wrong[m
[32m+[m[32m                        isSuccessful = correlationLoader.parse(this);[m
                         reasonForLoadFailure = correlationLoader.reasonForFailure; // "" if no failure[m
 [m
                         correlationData.preprocess(layoutProgressBarDialog, CURRENT_SCALE_TRANSFORM);[m
 [m
[31m-                        isSuccessful = isSuccessful && correlationLoader.parseAnnotations(this, nc, tabDelimited);[m
[32m+[m[32m                        isSuccessful = isSuccessful && correlationLoader.parseAnnotations(this, nc);[m
                         DATA_TYPE = DataTypes.CORRELATION;[m
                         CORRELATION_FILE_PATH = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf( System.getProperty("file.separator") ) + 1);[m
                     }[m
[1mdiff --git a/src/main/java/org/Kajeka/Correlation/CorrelationLoader.java b/src/main/java/org/Kajeka/Correlation/CorrelationLoader.java[m
[1mindex 62e5b19..3f4fd61 100644[m
[1m--- a/src/main/java/org/Kajeka/Correlation/CorrelationLoader.java[m
[1m+++ b/src/main/java/org/Kajeka/Correlation/CorrelationLoader.java[m
[36m@@ -1,7 +1,6 @@[m
 package org.Kajeka.Correlation;[m
 [m
 import java.io.*;[m
[31m-import static java.lang.Math.*;[m
 import org.Kajeka.Analysis.*;[m
 import org.Kajeka.CoreUI.*;[m
 import org.Kajeka.CoreUI.Dialogs.*;[m
[36m@@ -11,14 +10,13 @@[m [mimport static org.Kajeka.Environment.GlobalEnvironment.*;[m
 import static org.Kajeka.DebugConsole.ConsoleOutput.*;[m
 [m
 /**[m
[31m-*[m
[31m-* @author Anton Enright, full refactoring by Thanos Theo, 2008-2009[m
[31m-* @version 3.0.0.0[m
[31m-*[m
[31m-*/[m
[31m-[m
[31m-public final class CorrelationLoader[m
[31m-{[m
[32m+[m[32m *[m
[32m+[m[32m * @author Anton Enright, full refactoring by Thanos Theo, 2008-2009[m
[32m+[m[32m * @version 3.0.0.0[m
[32m+[m[32m *[m
[32m+[m[32m */[m
[32m+[m[32mpublic final class CorrelationLoader {[m
[32m+[m
     private File file = null;[m
     private CorrelationData correlationData = null;[m
     private LayoutClassSetsManager layoutClassSetsManager = null;[m
[36m@@ -29,14 +27,12 @@[m [mpublic final class CorrelationLoader[m
     boolean isSuccessful = false;[m
     public String reasonForFailure = "";[m
 [m
[31m-    public CorrelationLoader(LayoutClassSetsManager layoutClassSetsManager)[m
[31m-    {[m
[32m+[m[32m    public CorrelationLoader(LayoutClassSetsManager layoutClassSetsManager) {[m
         this.layoutClassSetsManager = layoutClassSetsManager;[m
     }[m
 [m
     public boolean init(File file, CorrelationData correlationData,[m
[31m-            int firstDataColumn, int firstDataRow, boolean transpose)[m
[31m-    {[m
[32m+[m[32m            int firstDataColumn, int firstDataRow, boolean transpose) {[m
         this.firstDataColumn = firstDataColumn;[m
         this.firstDataRow = firstDataRow;[m
         this.transpose = transpose;[m
[36m@@ -46,34 +42,29 @@[m [mpublic final class CorrelationLoader[m
         return true;[m
     }[m
 [m
[31m-    class ParseProgressIndicator implements TextDelimitedMatrix.ProgressIndicator[m
[31m-    {[m
[32m+[m[32m    class ParseProgressIndicator implements TextDelimitedMatrix.ProgressIndicator {[m
[32m+[m
         private LayoutProgressBarDialog layoutProgressBarDialog;[m
 [m
[31m-        public ParseProgressIndicator(LayoutProgressBarDialog layoutProgressBarDialog)[m
[31m-        {[m
[32m+[m[32m        public ParseProgressIndicator(LayoutProgressBarDialog layoutProgressBarDialog) {[m
             this.layoutProgressBarDialog = layoutProgressBarDialog;[m
         }[m
 [m
         @Override[m
[31m-        public void notify(int percent)[m
[31m-        {[m
[32m+[m[32m        public void notify(int percent) {[m
             layoutProgressBarDialog.incrementProgress(percent);[m
         }[m
     }[m
 [m
[31m-    private void setReasonForFailure(int row, int column, String reason)[m
[31m-    {[m
[31m-        if (row >= 0 && column >= 0)[m
[31m-        {[m
[32m+[m[32m    private void setReasonForFailure(int row, int column, String reason) {[m
[32m+[m[32m        if (row >= 0 && column >= 0) {[m
             reasonForFailure += "At row " + (row + 1) + ", column " + (column + 1) + ":\n";[m
         }[m
 [m
         reasonForFailure += reason;[m
     }[m
 [m
[31m-    public boolean parse(LayoutFrame layoutFrame, boolean tabDelimited)[m
[31m-    {[m
[32m+[m[32m    public boolean parse(LayoutFrame layoutFrame) {[m
         LayoutProgressBarDialog layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();[m
 [m
         layoutProgressBarDialog.prepareProgressBar(100, "Reading Correlation Data: ");[m
[36m@@ -85,17 +76,18 @@[m [mpublic final class CorrelationLoader[m
         int row = -1;[m
         int column = -1;[m
 [m
[31m-        try[m
[31m-        {[m
[32m+[m[32m        try {[m
             reasonForFailure = "";[m
 [m
[32m+[m[32m            String absFileName = file.getAbsolutePath();[m
[32m+[m[32m            String ext = absFileName.substring(absFileName.lastIndexOf(".") + 1, absFileName.length()).toUpperCase();[m
[32m+[m[32m            boolean tabDelimited = ext.equals(SupportedInputFileTypes.EXPRESSION.toString());[m
             String delimiter = tabDelimited ? "\t" : "";[m
             tdm = new TextDelimitedMatrix(file, ppi, delimiter);[m
 [m
             layoutProgressBarDialog.setText("Parsing " + tdm.numLines() + " lines");[m
 [m
[31m-            if (!tdm.parse())[m
[31m-            {[m
[32m+[m[32m            if (!tdm.parse()) {[m
                 reasonForFailure = tdm.reasonForFailure;[m
                 return false;[m
             }[m
[36m@@ -114,59 +106,44 @@[m [mpublic final class CorrelationLoader[m
 [m
             layoutProgressBarDialog.setText("Loading data");[m
 [m
[31m-            for (row = 0; row < numRows; row++)[m
[31m-            {[m
[32m+[m[32m            for (row = 0; row < numRows; row++) {[m
                 int percent = (100 * row) / numRows;[m
                 layoutProgressBarDialog.incrementProgress(percent);[m
 [m
[31m-                for (column = 0; column < numColumns; column++)[m
[31m-                {[m
[32m+[m[32m                for (column = 0; column < numColumns; column++) {[m
                     String value = tdm.valueAt(column, row);[m
                     int dataColumn = column - firstDataColumn;[m
                     int dataRow = row - firstDataRow;[m
 [m
[31m-                    if (row == 0)[m
[31m-                    {[m
[31m-                        if (column >= firstDataColumn)[m
[31m-                        {[m
[32m+[m[32m                    if (row == 0) {[m
[32m+[m[32m                        if (column >= firstDataColumn) {[m
                             // Data column names[m
                             correlationData.setColumnName(dataColumn, value);[m
[31m-                        }[m
[31m-                        else if (column >= 1) // First column is always the row ID[m
[32m+[m[32m                        } else if (column >= 1) // First column is always the row ID[m
                         {[m
                             // Annotation classes[m
                             String annotation = cleanString(value);[m
                             layoutClassSetsManager.createNewClassSet(annotation);[m
                             rowAnnotationLabels[column - 1] = annotation;[m
                         }[m
[31m-                    }[m
[31m-                    else if (row < firstDataRow)[m
[31m-                    {[m
[31m-                        if (column == 0)[m
[31m-                        {[m
[32m+[m[32m                    } else if (row < firstDataRow) {[m
[32m+[m[32m                        if (column == 0) {[m
                             // Column annotation name[m
                             String annotation = cleanString(value);[m
                             correlationData.addColumnAnnotation(row - 1, annotation);[m
[31m-                        }[m
[31m-                        else if (column >= firstDataColumn)[m
[31m-                        {[m
[32m+[m[32m                        } else if (column >= firstDataColumn) {[m
                             // Column annotation[m
[31m-                            CorrelationData.ColumnAnnotation columnAnnotation =[m
[31m-                                    correlationData.getColumnAnnotationByIndex(row - 1);[m
[32m+[m[32m                            CorrelationData.ColumnAnnotation columnAnnotation[m
[32m+[m[32m                                    = correlationData.getColumnAnnotationByIndex(row - 1);[m
 [m
                             columnAnnotation.setValue(dataColumn, value);[m
 [m
                         }[m
[31m-                    }[m
[31m-                    else if (row >= firstDataRow)[m
[31m-                    {[m
[31m-                        if (column == 0)[m
[31m-                        {[m
[32m+[m[32m                    } else if (row >= firstDataRow) {[m
[32m+[m[32m                        if (column == 0) {[m
                             // Row names[m
                             correlationData.setRowID(dataRow, value);[m
[31m-                        }[m
[31m-                        else if (column >= firstDataColumn)[m
[31m-                        {[m
[32m+[m[32m                        } else if (column >= firstDataColumn) {[m
                             // The data itself[m
                             float currentValue = Float.parseFloat(value.replace(',', '.'));[m
                             correlationData.setDataValue(dataRow, dataColumn, currentValue);[m
[36m@@ -176,37 +153,28 @@[m [mpublic final class CorrelationLoader[m
             }[m
 [m
             correlationData.sumRows();[m
[31m-        }[m
[31m-        catch (NumberFormatException nfe)[m
[31m-        {[m
[31m-            if (DEBUG_BUILD)[m
[31m-            {[m
[32m+[m[32m        } catch (NumberFormatException nfe) {[m
[32m+[m[32m            if (DEBUG_BUILD) {[m
                 println("NumberFormatException in parse():\n" + nfe.getMessage());[m
             }[m
 [m
             setReasonForFailure(row, column, nfe.toString());[m
             return false;[m
[31m-        }[m
[31m-        catch (IOException ioe)[m
[31m-        {[m
[31m-            if (DEBUG_BUILD)[m
[31m-            {[m
[32m+[m[32m        } catch (IOException ioe) {[m
[32m+[m[32m            if (DEBUG_BUILD) {[m
                 println("IOException in parse():\n" + ioe.getMessage());[m
             }[m
 [m
             setReasonForFailure(row, column, ioe.toString());[m
             return false;[m
[31m-        }[m
[31m-        finally[m
[31m-        {[m
[32m+[m[32m        } finally {[m
             layoutProgressBarDialog.endProgressBar();[m
         }[m
 [m
         return true;[m
     }[m
 [m
[31m-    public boolean parseAnnotations(LayoutFrame layoutFrame, NetworkContainer nc, boolean tabDelimited)[m
[31m-    {[m
[32m+[m[32m    public boolean parseAnnotations(LayoutFrame layoutFrame, NetworkContainer nc) {[m
         LayoutProgressBarDialog layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();[m
 [m
         layoutProgressBarDialog.prepareProgressBar(100, "Reading Correlation Data: ");[m
[36m@@ -218,15 +186,16 @@[m [mpublic final class CorrelationLoader[m
 [m
         int chipGeneCount = 0;[m
 [m
[31m-        try[m
[31m-        {[m
[32m+[m[32m        try {[m
[32m+[m[32m            String absFileName = file.getAbsolutePath();[m
[32m+[m[32m            String ext = absFileName.substring(absFileName.lastIndexOf(".") + 1, absFileName.length()).toUpperCase();[m
[32m+[m[32m            boolean tabDelimited = ext.equals(SupportedInputFileTypes.EXPRESSION.toString());[m
             String delimiter = tabDelimited ? "\t" : "";[m
             tdm = new TextDelimitedMatrix(file, ppi, delimiter);[m
 [m
             layoutProgressBarDialog.setText("Parsing " + tdm.numLines() + " lines");[m
 [m
[31m-            if (!tdm.parse())[m
[31m-            {[m
[32m+[m[32m            if (!tdm.parse()) {[m
                 reasonForFailure = tdm.reasonForFailure;[m
                 return false;[m
             }[m
[36m@@ -237,36 +206,28 @@[m [mpublic final class CorrelationLoader[m
 [m
             layoutProgressBarDialog.setText("Loading annotations");[m
 [m
[31m-            for (int row = firstDataRow; row < numRows; row++)[m
[31m-            {[m
[32m+[m[32m            for (int row = firstDataRow; row < numRows; row++) {[m
                 int percent = (100 * row) / numRows;[m
                 layoutProgressBarDialog.incrementProgress(percent);[m
 [m
                 Vertex vertex = null;[m
                 chipGeneCount++;[m
[31m-                for (int column = 0; column < firstDataColumn; column++)[m
[31m-                {[m
[32m+[m[32m                for (int column = 0; column < firstDataColumn; column++) {[m
                     String value = tdm.valueAt(column, row);[m
                     int dataColumn = column - firstDataColumn;[m
                     int dataRow = row - firstDataRow;[m
 [m
[31m-                    if (column == 0)[m
[31m-                    {[m
[32m+[m[32m                    if (column == 0) {[m
                         vertex = nc.getVerticesMap().get(correlationData.getRowID(dataRow));[m
[31m-                    }[m
[31m-                    else if (vertex != null)[m
[31m-                    {[m
[32m+[m[32m                    } else if (vertex != null) {[m
                         String annotation = cleanString(value);[m
[31m-                        LayoutClasses layoutClasses =[m
[31m-                                layoutClassSetsManager.getClassSetByName(rowAnnotationLabels[column - 1]);[m
[32m+[m[32m                        LayoutClasses layoutClasses[m
[32m+[m[32m                                = layoutClassSetsManager.getClassSetByName(rowAnnotationLabels[column - 1]);[m
 [m
[31m-                        if (annotation.isEmpty())[m
[31m-                        {[m
[32m+[m[32m                        if (annotation.isEmpty()) {[m
                             // if the annotation is empty set it to the noclass class[m
                             layoutClasses.setClass(vertex, 0);[m
[31m-                        }[m
[31m-                        else[m
[31m-                        {[m
[32m+[m[32m                        } else {[m
                             VertexClass vc = layoutClasses.createClass(annotation);[m
                             layoutClasses.setClass(vertex, vc);[m
                             AnnotationTypeManagerBG.getInstanceSingleton().add(vertex.getVertexName(),[m
[36m@@ -275,18 +236,13 @@[m [mpublic final class CorrelationLoader[m
                     }[m
                 }[m
             }[m
[31m-        }[m
[31m-        catch (IOException ioe)[m
[31m-        {[m
[31m-            if (DEBUG_BUILD)[m
[31m-            {[m
[32m+[m[32m        } catch (IOException ioe) {[m
[32m+[m[32m            if (DEBUG_BUILD) {[m
                 println("IOException in parseAnnotations():\n" + ioe.getMessage());[m
             }[m
 [m
             return false;[m
[31m-        }[m
[31m-        finally[m
[31m-        {[m
[32m+[m[32m        } finally {[m
             AnnotationTypeManagerBG.getInstanceSingleton().setChipGeneCount(chipGeneCount);[m
             layoutProgressBarDialog.endProgressBar();[m
         }[m
[36m@@ -294,9 +250,8 @@[m [mpublic final class CorrelationLoader[m
         return true;[m
     }[m
 [m
[31m-    private String cleanString(String string)[m
[31m-    {[m
[32m+[m[32m    private String cleanString(String string) {[m
         // This is apparently marginally faster than replaceAll("[\"\']", " ")[m
         return string.replace('\"', ' ').replace('\'', ' ');[m
     }[m
[31m-}[m
\ No newline at end of file[m
[32m+[m[32m}[m
[1mdiff --git a/src/main/java/org/Kajeka/Files/Parsers/CoreParser.java b/src/main/java/org/Kajeka/Files/Parsers/CoreParser.java[m
[1mindex 0c651d9..49a12d9 100644[m
[1m--- a/src/main/java/org/Kajeka/Files/Parsers/CoreParser.java[m
[1m+++ b/src/main/java/org/Kajeka/Files/Parsers/CoreParser.java[m
[36m@@ -17,16 +17,15 @@[m [mimport static org.Kajeka.Correlation.CorrelationEnvironment.*;[m
 import static org.Kajeka.DebugConsole.ConsoleOutput.*;[m
 [m
 /**[m
[31m-*[m
[31m-*  org.Kajeka.File.CoreParser[m
[31m-*[m
[31m-* @author Full refactoring by Thanos Theo, 2008-2009-2010-2011-2012[m
[31m-* @version 3.0.0.0[m
[31m-*[m
[31m-*/[m
[31m-[m
[31m-public class CoreParser[m
[31m-{[m
[32m+[m[32m *[m
[32m+[m[32m * org.Kajeka.File.CoreParser[m
[32m+[m[32m *[m
[32m+[m[32m * @author Full refactoring by Thanos Theo, 2008-2009-2010-2011-2012[m
[32m+[m[32m * @version 3.0.0.0[m
[32m+[m[32m *[m
[32m+[m[32m */[m
[32m+[m[32mpublic class CoreParser {[m
[32m+[m
     protected NetworkContainer nc = null;[m
     protected LayoutFrame layoutFrame = null;[m
     protected File file = null;[m
[36m@@ -41,28 +40,29 @@[m [mpublic class CoreParser[m
     protected boolean isSuccessful = false;[m
 [m
     /**[m
[31m-    *  String variable to store the simple file name.[m
[31m-    */[m
[32m+[m[32m     * String variable to store the simple file name.[m
[32m+[m[32m     */[m
     protected String simpleFileName = "";[m
 [m
     /**[m
[31m-    *  GraphmlNetworkContainer reference (reference for the grapml network container).[m
[31m-    */[m
[32m+[m[32m     * GraphmlNetworkContainer reference (reference for the grapml network[m
[32m+[m[32m     * container).[m
[32m+[m[32m     */[m
     private GraphmlNetworkContainer gnc = null;[m
 [m
     /**[m
[31m-    *  Variable only used for graphml files.[m
[31m-    */[m
[32m+[m[32m     * Variable only used for graphml files.[m
[32m+[m[32m     */[m
     private HashMap<String, Tuple6<float[], String[], String[], String[], String[], String>> allGraphmlNodesMap = null;[m
 [m
     /**[m
[31m-    *  Variable only used for graphml files.[m
[31m-    */[m
[32m+[m[32m     * Variable only used for graphml files.[m
[32m+[m[32m     */[m
     private HashMap<String, Tuple6<String, Tuple2<float[], ArrayList<Point2D.Float>>, String[], String[], String[], String[]>> allGraphmlEdgesMap = null;[m
 [m
     /**[m
[31m-    *  Variable only used for graphml files.[m
[31m-    */[m
[32m+[m[32m     * Variable only used for graphml files.[m
[32m+[m[32m     */[m
     private ArrayList<GraphmlComponentContainer> alGraphmllPathwayComponentContainersFor3D = null;[m
 [m
     private ArrayList<Integer> nodeIdColumns;[m
[36m@@ -71,12 +71,11 @@[m [mpublic class CoreParser[m
     private float filterWeight;[m
 [m
     /**[m
[31m-    *  The constructor of the CoreParser class.[m
[31m-    */[m
[32m+[m[32m     * The constructor of the CoreParser class.[m
[32m+[m[32m     */[m
     public CoreParser(NetworkContainer nc, LayoutFrame layoutFrame,[m
             java.util.List<Integer> nodeIdColumns, int edgeWeightColumn,[m
[31m-            int edgeTypeColumn, float filterWeight)[m
[31m-    {[m
[32m+[m[32m            int edgeTypeColumn, float filterWeight) {[m
         this.nc = nc;[m
         this.layoutFrame = layoutFrame;[m
         this.nodeIdColumns = new ArrayList<Integer>(nodeIdColumns);[m
[36m@@ -85,38 +84,30 @@[m [mpublic class CoreParser[m
         this.filterWeight = filterWeight;[m
     }[m
 [m
[31m-    public CoreParser(NetworkContainer nc, LayoutFrame layoutFrame)[m
[31m-    {[m
[32m+[m[32m    public CoreParser(NetworkContainer nc, LayoutFrame layoutFrame) {[m
         this(nc, layoutFrame, Arrays.asList(0, 1), 2, 3, 0.0f);[m
     }[m
 [m
[31m-    public boolean init(File file, String fileExtension)[m
[31m-    {[m
[32m+[m[32m    public boolean init(File file, String fileExtension) {[m
         this.file = file;[m
 [m
[31m-        try[m
[31m-        {[m
[31m-            fileReaderCounter  = new BufferedReader( new FileReader(file) );[m
[31m-            fileReaderBuffered = new BufferedReader( new FileReader(file) );[m
[32m+[m[32m        try {[m
[32m+[m[32m            fileReaderCounter = new BufferedReader(new FileReader(file));[m
[32m+[m[32m            fileReaderBuffered = new BufferedReader(new FileReader(file));[m
 [m
[31m-            isSif = fileExtension.equals( SupportedInputFileTypes.SIF.toString() );[m
[32m+[m[32m            isSif = fileExtension.equals(SupportedInputFileTypes.SIF.toString());[m
             simpleFileName = file.getName();[m
 [m
             return true;[m
[31m-        }[m
[31m-        catch (Exception exc)[m
[31m-        {[m
[31m-            try[m
[31m-            {[m
[32m+[m[32m        } catch (Exception exc) {[m
[32m+[m[32m            try {[m
                 fileReaderCounter.close();[m
                 fileReaderBuffered.close();[m
[31m-            }[m
[31m-            catch (IOException ioe)[m
[31m-            {[m
[31m-                if (DEBUG_BUILD) println("IOException while closing streamers in CoreParser.init():\n" + ioe.getMessage());[m
[31m-            }[m
[31m-            finally[m
[31m-            {[m
[32m+[m[32m            } catch (IOException ioe) {[m
[32m+[m[32m                if (DEBUG_BUILD) {[m
[32m+[m[32m                    println("IOException while closing streamers in CoreParser.init():\n" + ioe.getMessage());[m
[32m+[m[32m                }[m
[32m+[m[32m            } finally {[m
 [m
             }[m
 [m
[36m@@ -125,18 +116,14 @@[m [mpublic class CoreParser[m
     }[m
 [m
     private Pattern quotedStringRegex = Pattern.compile("\"([^\"]*)\"|(\\S+)");[m
[31m-    protected void tokenize(String line)[m
[31m-    {[m
[32m+[m
[32m+[m[32m    protected void tokenize(String line) {[m
         tokens = new ArrayList<String>();[m
         Matcher m = quotedStringRegex.matcher(line);[m
[31m-        while (m.find())[m
[31m-        {[m
[31m-            if (m.group(1) != null)[m
[31m-            {[m
[32m+[m[32m        while (m.find()) {[m
[32m+[m[32m            if (m.group(1) != null) {[m
                 tokens.add(m.group(1));[m
[31m-            }[m
[31m-            else[m
[31m-            {[m
[32m+[m[32m            } else {[m
                 tokens.add(m.group(2));[m
             }[m
         }[m
[36m@@ -145,8 +132,7 @@[m [mpublic class CoreParser[m
         numberOfTokens = tokens.size();[m
     }[m
 [m
[31m-    public boolean parse()[m
[31m-    {[m
[32m+[m[32m    public boolean parse() {[m
         int totalLines = 0;[m
         int counter = 0;[m
 [m
[36m@@ -154,57 +140,53 @@[m [mpublic class CoreParser[m
         nc.setOptimized(false);[m
         LayoutProgressBarDialog layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();[m
 [m
[31m-        try[m
[31m-        {[m
[31m-            while ( ( line = fileReaderCounter.readLine() ) != null )[m
[32m+[m[32m        try {[m
[32m+[m[32m            while ((line = fileReaderCounter.readLine()) != null) {[m
                 totalLines++;[m
[32m+[m[32m            }[m
 [m
             layoutProgressBarDialog.prepareProgressBar(totalLines, "Parsing " + simpleFileName + " Graph...");[m
             layoutProgressBarDialog.startProgressBar();[m
 [m
[31m-            while ( ( line = fileReaderBuffered.readLine() ) != null )[m
[31m-            {[m
[32m+[m[32m            while ((line = fileReaderBuffered.readLine()) != null) {[m
                 layoutProgressBarDialog.incrementProgress(++counter);[m
 [m
                 tokenize(line);[m
[31m-                if (line.length() > 0)[m
[31m-                {[m
[31m-                    if ( line.startsWith("//") )[m
[32m+[m[32m                if (line.length() > 0) {[m
[32m+[m[32m                    if (line.startsWith("//")) {[m
                         updateVertexProperties();[m
[31m-                    else[m
[32m+[m[32m                    } else {[m
                         createVertices(counter);[m
[32m+[m[32m                    }[m
                 }[m
             }[m
 [m
[31m-            if ( nc.getIsGraphml() )[m
[32m+[m[32m            if (nc.getIsGraphml()) {[m
                 gnc.initAllGraphmlNodesMap(allGraphmlNodesMap, allGraphmlEdgesMap, alGraphmllPathwayComponentContainersFor3D);[m
[32m+[m[32m            }[m
 [m
[31m-            if (!iscorrelationData)[m
[31m-            {[m
[31m-                AnnotationTypeManagerBG.getInstanceSingleton().setChipGeneCount( nc.getVerticesMap().size() );[m
[32m+[m[32m            if (!iscorrelationData) {[m
[32m+[m[32m                AnnotationTypeManagerBG.getInstanceSingleton().setChipGeneCount(nc.getVerticesMap().size());[m
 [m
[31m-                if (DEBUG_BUILD) println("Got a total of:" + AnnotationTypeManagerBG.getInstanceSingleton().getChipGeneCount());[m
[32m+[m[32m                if (DEBUG_BUILD) {[m
[32m+[m[32m                    println("Got a total of:" + AnnotationTypeManagerBG.getInstanceSingleton().getChipGeneCount());[m
[32m+[m[32m                }[m
             }[m
 [m
             isSuccessful = true;[m
[31m-        }[m
[31m-        catch (IOException ioe)[m
[31m-        {[m
[31m-            if (DEBUG_BUILD) println("IOException in CoreParser.parse():\n" + ioe.getMessage());[m
[31m-        }[m
[31m-        finally[m
[31m-        {[m
[31m-            try[m
[31m-            {[m
[32m+[m[32m        } catch (IOException ioe) {[m
[32m+[m[32m            if (DEBUG_BUILD) {[m
[32m+[m[32m                println("IOException in CoreParser.parse():\n" + ioe.getMessage());[m
[32m+[m[32m            }[m
[32m+[m[32m        } finally {[m
[32m+[m[32m            try {[m
                 fileReaderCounter.close();[m
                 fileReaderBuffered.close();[m
[31m-            }[m
[31m-            catch (IOException ioe)[m
[31m-            {[m
[31m-                if (DEBUG_BUILD) println("IOException while closing streams in CoreParser.parse():\n" + ioe.getMessage());[m
[31m-            }[m
[31m-            finally[m
[31m-            {[m
[32m+[m[32m            } catch (IOException ioe) {[m
[32m+[m[32m                if (DEBUG_BUILD) {[m
[32m+[m[32m                    println("IOException while closing streams in CoreParser.parse():\n" + ioe.getMessage());[m
[32m+[m[32m                }[m
[32m+[m[32m            } finally {[m
                 layoutProgressBarDialog.endProgressBar();[m
             }[m
         }[m
[36m@@ -212,20 +194,17 @@[m [mpublic class CoreParser[m
         return isSuccessful;[m
     }[m
 [m
[31m-    private void updateVertexProperties()[m
[31m-    {[m
[32m+[m[32m    private void updateVertexProperties() {[m
         String property = getNext();[m
         String vertex = "";[m
         String field1 = "", field2 = "", field3 = "", field4 = "", field5 = "", field6 = "", field7 = "";[m
 [m
[31m-        if (property.startsWith("//CORRELATION_DATA"))[m
[31m-        {[m
[32m+[m[32m        if (property.startsWith("//CORRELATION_DATA") || property.startsWith("//EXPRESSION_DATA")) {[m
             field1 = getNext();[m
             field2 = getNext();[m
             field3 = getNext();[m
 [m
[31m-            if (DEBUG_BUILD)[m
[31m-            {[m
[32m+[m[32m            if (DEBUG_BUILD) {[m
                 println("Correlation data file used was:" + field1);[m
             }[m
 [m
[36m@@ -237,8 +216,22 @@[m [mpublic class CoreParser[m
             CORRELATION_DATA_FIRST_COLUMN = Integer.parseInt(field2);[m
 [m
             iscorrelationData = true;[m
[32m+[m[32m            if (property.equals("//EXPRESSION_DATA")) {[m
[32m+[m[32m                // Older files don't have this data, so fill with likely values[m
[32m+[m[32m                CORRELATION_DATA_FIRST_ROW = 1;[m
[32m+[m[32m                CORRELATION_DATA_TRANSPOSE = false;[m
[32m+[m[32m                CURRENT_CORRELATION_THRESHOLD = Float.parseFloat(field3);[m
[32m+[m[32m                CURRENT_SCALE_TRANSFORM = ScaleTransformType.NONE;[m
[32m+[m[32m            } else if (property.equals("//EXPRESSION_DATA_V2")) // 3.0 through 3.2[m
[32m+[m[32m            {[m
[32m+[m[32m                field4 = getNext();[m
[32m+[m[32m                field5 = getNext();[m
 [m
[31m-            if (property.equals("//CORRELATION_DATA_V1")) // 1.0[m
[32m+[m[32m                CORRELATION_DATA_FIRST_ROW = Integer.parseInt(field3);[m
[32m+[m[32m                CORRELATION_DATA_TRANSPOSE = Boolean.parseBoolean(field4);[m
[32m+[m[32m                CURRENT_CORRELATION_THRESHOLD = Float.parseFloat(field5);[m
[32m+[m[32m                CURRENT_SCALE_TRANSFORM = ScaleTransformType.NONE;[m
[32m+[m[32m            } else if (property.equals("//CORRELATION_DATA_V1") || property.equals("//EXPRESSION_DATA_V3")) // 1.0[m
             {[m
                 field4 = getNext();[m
                 field5 = getNext();[m
[36m@@ -248,162 +241,138 @@[m [mpublic class CoreParser[m
                 CORRELATION_DATA_TRANSPOSE = Boolean.parseBoolean(field4);[m
                 CURRENT_CORRELATION_THRESHOLD = Float.parseFloat(field5);[m
                 CURRENT_SCALE_TRANSFORM = Enum.valueOf(ScaleTransformType.class, field6);[m
[31m-            }[m
[31m-            else[m
[31m-            {[m
[32m+[m[32m            } else {[m
                 iscorrelationData = false;[m
             }[m
[31m-        }[m
[31m-        else if ( property.equals("//NODECOORD") )[m
[31m-        {[m
[32m+[m[32m        } else if (property.equals("//NODECOORD")) {[m
             field1 = getNext();[m
             field2 = getNext();[m
             field3 = getNext();[m
             field4 = getNext();[m
 [m
[31m-            if ( field4.isEmpty() )[m
[32m+[m[32m            if (field4.isEmpty()) {[m
                 field4 = "0.0";[m
[32m+[m[32m            }[m
 [m
[31m-            if (DEBUG_BUILD) println("name: " + field1 + " coord1: " + field2 + " coord2: " + field2);[m
[32m+[m[32m            if (DEBUG_BUILD) {[m
[32m+[m[32m                println("name: " + field1 + " coord1: " + field2 + " coord2: " + field2);[m
[32m+[m[32m            }[m
 [m
[31m-            nc.updateVertexLocation( field1, Float.parseFloat(field2), Float.parseFloat(field3), Float.parseFloat(field4) );[m
[32m+[m[32m            nc.updateVertexLocation(field1, Float.parseFloat(field2), Float.parseFloat(field3), Float.parseFloat(field4));[m
 [m
[31m-        }[m
[31m-        else if ( property.equals("//NODEDESC") )[m
[31m-        {[m
[32m+[m[32m        } else if (property.equals("//NODEDESC")) {[m
             vertex = getNext();[m
             field1 = getNext();[m
 [m
[31m-            if ( nc.getVerticesMap().containsKey(vertex) )[m
[32m+[m[32m            if (nc.getVerticesMap().containsKey(vertex)) {[m
                 nc.getVerticesMap().get(vertex).setDescription(field1);[m
[31m-        }[m
[31m-        else if ( property.equals("//NODECLASS") )[m
[31m-        {[m
[32m+[m[32m            }[m
[32m+[m[32m        } else if (property.equals("//NODECLASS")) {[m
             vertex = getNext();[m
             field1 = getNext();[m
             field2 = getNext();[m
 [m
[31m-            if ( nc.getVerticesMap().containsKey(vertex) )[m
[31m-            {[m
[32m+[m[32m            if (nc.getVerticesMap().containsKey(vertex)) {[m
                 // IF CLASS SET PROVIDED ADD THE VERTEX TO THE CLASS SET[m
                 // ELSE IF NO CLASS SET IS PROVIDED ADD TO THE DEFAULT CLASSES ID 0[m
[31m-                LayoutClasses lc = ( (field2.length() > 0) ? nc.getLayoutClassSetsManager().getClassSet(field2) : nc.getLayoutClassSetsManager().getClassSet(0) );[m
[32m+[m[32m                LayoutClasses lc = ((field2.length() > 0) ? nc.getLayoutClassSetsManager().getClassSet(field2) : nc.getLayoutClassSetsManager().getClassSet(0));[m
                 VertexClass vc = lc.createClass(field1);[m
                 lc.setClass(nc.getVerticesMap().get(vertex), vc);[m
 [m
[31m-                if (!iscorrelationData)[m
[31m-                    AnnotationTypeManagerBG.getInstanceSingleton().add( vertex, lc.getClassSetName(), vc.getName() );[m
[32m+[m[32m                if (!iscorrelationData) {[m
[32m+[m[32m                    AnnotationTypeManagerBG.getInstanceSingleton().add(vertex, lc.getClassSetName(), vc.getName());[m
[32m+[m[32m                }[m
             }[m
[31m-        }[m
[31m-        else if ( property.equals("//NODECLASSCOLOR") )[m
[31m-        {[m
[32m+[m[32m        } else if (property.equals("//NODECLASSCOLOR")) {[m
             field1 = getNext();[m
             field2 = getNext();[m
             field3 = getNext();[m
 [m
             LayoutClasses lc = null;[m
 [m
[31m-            if (field3.length() > 0)[m
[31m-            {[m
[32m+[m[32m            if (field3.length() > 0) {[m
                 // IF CLASS SET PROVIDED SET THE COLOR TO THE CLASS SET[m
                 lc = nc.getLayoutClassSetsManager().getClassSet(field2);[m
[31m-                lc.createClass(field1).setColor( Color.decode(field3) );[m
[31m-            }[m
[31m-            else[m
[31m-            {[m
[32m+[m[32m                lc.createClass(field1).setColor(Color.decode(field3));[m
[32m+[m[32m            } else {[m
                 // IF NO CLASS SET IS PROVIDED SET THE COLOR TO THE DEFAULT CLASSES ID 0[m
                 lc = nc.getLayoutClassSetsManager().getClassSet(0);[m
[31m-                lc.createClass(field1).setColor( Color.decode(field2) );[m
[32m+[m[32m                lc.createClass(field1).setColor(Color.decode(field2));[m
             }[m
[31m-        }[m
[31m-        else if ( property.equals("//NODESIZE") )[m
[31m-        {[m
[32m+[m[32m        } else if (property.equals("//NODESIZE")) {[m
             vertex = getNext();[m
             field1 = getNext();[m
 [m
[31m-            if ( nc.getVerticesMap().containsKey(vertex) )[m
[31m-                nc.getVerticesMap().get(vertex).setVertexSize( Float.parseFloat(field1) );[m
[31m-        }[m
[31m-        else if ( property.equals("//NODECOLOR") )[m
[31m-        {[m
[32m+[m[32m            if (nc.getVerticesMap().containsKey(vertex)) {[m
[32m+[m[32m                nc.getVerticesMap().get(vertex).setVertexSize(Float.parseFloat(field1));[m
[32m+[m[32m            }[m
[32m+[m[32m        } else if (property.equals("//NODECOLOR")) {[m
             vertex = getNext();[m
             field1 = getNext();[m
 [m
[31m-            if ( nc.getVerticesMap().containsKey(vertex) )[m
[31m-                nc.getVerticesMap().get(vertex).setVertexColor( Color.decode(field1) );[m
[31m-        }[m
[31m-        else if ( property.equals("//NODESHAPE") )[m
[31m-        {[m
[32m+[m[32m            if (nc.getVerticesMap().containsKey(vertex)) {[m
[32m+[m[32m                nc.getVerticesMap().get(vertex).setVertexColor(Color.decode(field1));[m
[32m+[m[32m            }[m
[32m+[m[32m        } else if (property.equals("//NODESHAPE")) {[m
             vertex = getNext();[m
             field1 = getNext();[m
             field2 = getNext();[m
 [m
[31m-            if ( nc.getVerticesMap().containsKey(vertex) )[m
[31m-            {[m
[32m+[m[32m            if (nc.getVerticesMap().containsKey(vertex)) {[m
                 nc.getVerticesMap().get(vertex).setVertex2DShape(get2DShapeForString(field1));[m
                 nc.getVerticesMap().get(vertex).setVertex3DShape(get3DShapeForString(field2));[m
             }[m
[31m-        }[m
[31m-        else if ( property.equals("//NODEALPHA") )[m
[31m-        {[m
[32m+[m[32m        } else if (property.equals("//NODEALPHA")) {[m
             vertex = getNext();[m
             field1 = getNext();[m
 [m
[31m-            if ( nc.getVerticesMap().containsKey(vertex) )[m
[31m-                nc.getVerticesMap().get(vertex).setVertexTransparencyAlpha( Float.parseFloat(field1) );[m
[31m-        }[m
[31m-        else if ( property.equals("//NODEURL") )[m
[31m-        {[m
[32m+[m[32m            if (nc.getVerticesMap().containsKey(vertex)) {[m
[32m+[m[32m                nc.getVerticesMap().get(vertex).setVertexTransparencyAlpha(Float.parseFloat(field1));[m
[32m+[m[32m            }[m
[32m+[m[32m        } else if (property.equals("//NODEURL")) {[m
             vertex = getNext();[m
             field1 = getNext();[m
 [m
[31m-            if ( nc.getVerticesMap().containsKey(vertex) )[m
[32m+[m[32m            if (nc.getVerticesMap().containsKey(vertex)) {[m
                 nc.getVerticesMap().get(vertex).setVertexURLString(field1);[m
[31m-        }[m
[31m-        else if ( property.equals("//NODETYPE") )[m
[31m-        {[m
[32m+[m[32m            }[m
[32m+[m[32m        } else if (property.equals("//NODETYPE")) {[m
             vertex = getNext();[m
             field1 = getNext();[m
 [m
[31m-            if ( nc.getVerticesMap().containsKey(vertex) )[m
[31m-            {[m
[31m-                if ( field1.equals("IS_MEPN_COMPONENT") )[m
[32m+[m[32m            if (nc.getVerticesMap().containsKey(vertex)) {[m
[32m+[m[32m                if (field1.equals("IS_MEPN_COMPONENT")) {[m
                     nc.getVerticesMap().get(vertex).setmEPNComponent();[m
[31m-                else if ( field1.equals("IS_MEPN_TRANSITION") )[m
[32m+[m[32m                } else if (field1.equals("IS_MEPN_TRANSITION")) {[m
                     nc.getVerticesMap().get(vertex).setmEPNTransition();[m
[32m+[m[32m                }[m
[32m+[m[32m            }[m
[32m+[m[32m        } else if (property.equals("//CURRENTCLASSSET")) {[m
[32m+[m[32m            nc.getLayoutClassSetsManager().switchClassSet(getNext());[m
[32m+[m[32m        } else if (property.equals("//EDGESIZE")) {[m
[32m+[m[32m            DEFAULT_EDGE_SIZE.set(Float.parseFloat(getNext()));[m
[32m+[m[32m        } else if (property.equals("//EDGECOLOR")) {[m
[32m+[m[32m            DEFAULT_EDGE_COLOR.set(Color.decode(getNext()));[m
[32m+[m[32m        } else if (property.equals("//EDGEARROWHEADSIZE")) {[m
[32m+[m[32m            ARROW_HEAD_SIZE.set(Integer.parseInt(getNext()));[m
[32m+[m[32m        } else if (property.equals("//DEFAULTSEARCH")) {[m
[32m+[m[32m            if (DEBUG_BUILD) {[m
[32m+[m[32m                println("Default Search found.");[m
             }[m
[31m-        }[m
[31m-        else if ( property.equals("//CURRENTCLASSSET") )[m
[31m-        {[m
[31m-            nc.getLayoutClassSetsManager().switchClassSet( getNext() );[m
[31m-        }[m
[31m-        else if ( property.equals("//EDGESIZE") )[m
[31m-        {[m
[31m-            DEFAULT_EDGE_SIZE.set( Float.parseFloat( getNext() ) );[m
[31m-        }[m
[31m-        else if ( property.equals("//EDGECOLOR") )[m
[31m-        {[m
[31m-            DEFAULT_EDGE_COLOR.set( Color.decode( getNext() ) );[m
[31m-        }[m
[31m-        else if ( property.equals("//EDGEARROWHEADSIZE") )[m
[31m-        {[m
[31m-            ARROW_HEAD_SIZE.set( Integer.parseInt( getNext() ) );[m
[31m-        }[m
[31m-        else if ( property.equals("//DEFAULTSEARCH") )[m
[31m-        {[m
[31m-            if (DEBUG_BUILD) println("Default Search found.");[m
 [m
             field1 = getNext();[m
             boolean preset = false;[m
[31m-            for (int i = 0; i < PRESET_SEARCH_URL.length; i++)[m
[31m-            {[m
[31m-                if ( field1.equals( PRESET_SEARCH_URL[i].getName() ) )[m
[31m-                {[m
[31m-                    if (DEBUG_BUILD) println("Is a Preset Search.");[m
[32m+[m[32m            for (int i = 0; i < PRESET_SEARCH_URL.length; i++) {[m
[32m+[m[32m                if (field1.equals(PRESET_SEARCH_URL[i].getName())) {[m
[32m+[m[32m                    if (DEBUG_BUILD) {[m
[32m+[m[32m                        println("Is a Preset Search.");[m
[32m+[m[32m                    }[m
 [m
                     SEARCH_URL = PRESET_SEARCH_URL[i];[m
 [m
[31m-                    if (DEBUG_BUILD) println( SEARCH_URL.getUrl() );[m
[32m+[m[32m                    if (DEBUG_BUILD) {[m
[32m+[m[32m                        println(SEARCH_URL.getUrl());[m
[32m+[m[32m                    }[m
 [m
                     preset = true;[m
 [m
[36m@@ -411,20 +380,21 @@[m [mpublic class CoreParser[m
                 }[m
             }[m
 [m
[31m-            if (!preset)[m
[31m-            {[m
[31m-                if (DEBUG_BUILD) println("Is a Custom Search.");[m
[32m+[m[32m            if (!preset) {[m
[32m+[m[32m                if (DEBUG_BUILD) {[m
[32m+[m[32m                    println("Is a Custom Search.");[m
[32m+[m[32m                }[m
 [m
                 SearchURL customSearchURL = new SearchURL(field1);[m
                 SEARCH_URL = customSearchURL;[m
 [m
[31m-                if (DEBUG_BUILD) println( SEARCH_URL.getUrl() );[m
[32m+[m[32m                if (DEBUG_BUILD) {[m
[32m+[m[32m                    println(SEARCH_URL.getUrl());[m
[32m+[m[32m                }[m
 [m
                 CUSTOM_SEARCH = true;[m
             }[m
[31m-        }[m
[31m-        else if ( property.equals("//HAS_GRAPHML_NODE_DATA") )[m
[31m-        {[m
[32m+[m[32m        } else if (property.equals("//HAS_GRAPHML_NODE_DATA")) {[m
             // enable graphml-style parsing[m
 [m
             field1 = getNext();[m
[36m@@ -439,59 +409,52 @@[m [mpublic class CoreParser[m
             gnc.setIsGraphml(true);[m
             gnc.setRangeX(rangeX);[m
             gnc.setRangeY(rangeY);[m
[31m-            gnc.setIsPetriNet( field3.equals("IS_SPN_MEPN_GRAPHML_GRAPH_TYPE") );[m
[32m+[m[32m            gnc.setIsPetriNet(field3.equals("IS_SPN_MEPN_GRAPHML_GRAPH_TYPE"));[m
 [m
             allGraphmlNodesMap = new HashMap<String, Tuple6<float[], String[], String[], String[], String[], String>>();[m
             allGraphmlEdgesMap = new HashMap<String, Tuple6<String, Tuple2<float[], ArrayList<Point2D.Float>>, String[], String[], String[], String[]>>();[m
             alGraphmllPathwayComponentContainersFor3D = new ArrayList<GraphmlComponentContainer>();[m
[31m-        }[m
[31m-        else if ( property.equals("//GRAPHML_NODE_DATA") )[m
[31m-        {[m
[32m+[m[32m        } else if (property.equals("//GRAPHML_NODE_DATA")) {[m
             // parse node key-to-name hashmap[m
 [m
             field1 = getNext();[m
             field2 = getNext();[m
 [m
[31m-            float graphmlCoordX = Float.parseFloat( getNext() );[m
[31m-            float graphmlCoordY = Float.parseFloat( getNext() );[m
[32m+[m[32m            float graphmlCoordX = Float.parseFloat(getNext());[m
[32m+[m[32m            float graphmlCoordY = Float.parseFloat(getNext());[m
 [m
             String nextString = getNext();[m
             // older graphml layout files may not have the Z coord axis, so this check is required before parsing[m
[31m-            float graphmlCoordZ = ( !nextString.isEmpty() ) ? Float.parseFloat(nextString) : 0.0f;[m
[31m-[m
[31m-            Tuple6<float[], String[], String[], String[], String[], String> nodeTuple6 = Tuples.tuple( new float[] { 0.0f, 0.0f, graphmlCoordX, graphmlCoordY, graphmlCoordZ },[m
[31m-                                                                                                       new String[] { "", "", "", "" },[m
[31m-                                                                                                       new String[] { "", "", "", "" },[m
[31m-                                                                                                       new String[] { "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" },[m
[31m-                                                                                                       new String[] { field2 },[m
[31m-                                                                                                       "" );[m
[32m+[m[32m            float graphmlCoordZ = (!nextString.isEmpty()) ? Float.parseFloat(nextString) : 0.0f;[m
[32m+[m
[32m+[m[32m            Tuple6<float[], String[], String[], String[], String[], String> nodeTuple6 = Tuples.tuple(new float[]{0.0f, 0.0f, graphmlCoordX, graphmlCoordY, graphmlCoordZ},[m
[32m+[m[32m                    new String[]{"", "", "", ""},[m
[32m+[m[32m                    new String[]{"", "", "", ""},[m
[32m+[m[32m                    new String[]{"", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},[m
[32m+[m[32m                    new String[]{field2},[m
[32m+[m[32m                    "");[m
             allGraphmlNodesMap.put(field1, nodeTuple6);[m
[31m-        }[m
[31m-        else if ( property.equals("//GRAPHML_EDGE_DATA") )[m
[31m-        {[m
[32m+[m[32m        } else if (property.equals("//GRAPHML_EDGE_DATA")) {[m
             // parse edge key-to-name hashmap[m
 [m
             field1 = getNext();[m
             field2 = getNext();[m
 [m
[31m-            float[] pathValues = new float[] { 0.0f, 0.0f, 0.0f, 0.0f };[m
[32m+[m[32m            float[] pathValues = new float[]{0.0f, 0.0f, 0.0f, 0.0f};[m
             ArrayList<Point2D.Float> allPointValues = new ArrayList<Point2D.Float>();[m
[31m-            while ( !( field3 = getNext() ).isEmpty() )[m
[31m-            {[m
[32m+[m[32m            while (!(field3 = getNext()).isEmpty()) {[m
                 field4 = getNext();[m
[31m-                allPointValues.add( new Point2D.Float( Float.parseFloat(field3), Float.parseFloat(field4) ) );[m
[32m+[m[32m                allPointValues.add(new Point2D.Float(Float.parseFloat(field3), Float.parseFloat(field4)));[m
             }[m
             Tuple2<float[], ArrayList<Point2D.Float>> allPathValues = Tuples.tuple(pathValues, allPointValues);[m
             Tuple6<String, Tuple2<float[], ArrayList<Point2D.Float>>, String[], String[], String[], String[]> edgeTuple6 = Tuples.tuple(field2,[m
[31m-                                                                                                                                        allPathValues,[m
[31m-                                                                                                                                        new String[] { "", "", "" },[m
[31m-                                                                                                                                        new String[] { "", "" },[m
[31m-                                                                                                                                        new String[] { "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" },[m
[31m-                                                                                                                                        new String[] { "", "", "", "" });[m
[32m+[m[32m                    allPathValues,[m
[32m+[m[32m                    new String[]{"", "", ""},[m
[32m+[m[32m                    new String[]{"", ""},[m
[32m+[m[32m                    new String[]{"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},[m
[32m+[m[32m                    new String[]{"", "", "", ""});[m
             allGraphmlEdgesMap.put(field1, edgeTuple6);[m
[31m-        }[m
[31m-        else if ( property.equals("//GRAPHML_COMPONENT_CONTAINER_DATA") )[m
[31m-        {[m
[32m+[m[32m        } else if (property.equals("//GRAPHML_COMPONENT_CONTAINER_DATA")) {[m
             vertex = getNext();[m
             field1 = getNext();[m
             field2 = getNext();[m
[36m@@ -501,36 +464,28 @@[m [mpublic class CoreParser[m
             field6 = getNext();[m
             field7 = getNext();[m
 [m
[31m-            alGraphmllPathwayComponentContainersFor3D.add( new GraphmlComponentContainer("", vertex,[m
[31m-                                                                                  Integer.parseInt(field1),[m
[31m-                                                                                  Float.parseFloat(field2),[m
[31m-                                                                                  new Rectangle2D.Float( Float.parseFloat(field3), Float.parseFloat(field4), Float.parseFloat(field5), Float.parseFloat(field6) ),[m
[31m-                                                                                  Color.decode(field7)[m
[31m-                                                                                 )[m
[31m-                                                  );[m
[32m+[m[32m            alGraphmllPathwayComponentContainersFor3D.add(new GraphmlComponentContainer("", vertex,[m
[32m+[m[32m                    Integer.parseInt(field1),[m
[32m+[m[32m                    Float.parseFloat(field2),[m
[32m+[m[32m                    new Rectangle2D.Float(Float.parseFloat(field3), Float.parseFloat(field4), Float.parseFloat(field5), Float.parseFloat(field6)),[m
[32m+[m[32m                    Color.decode(field7)[m
[32m+[m[32m            )[m
[32m+[m[32m            );[m
         }[m
     }[m
 [m
[31m-    private static <T extends Enum<T>> T getEnumValueForString(Class<T> clazz, HashMap<String, T> map, String field)[m
[31m-    {[m
[32m+[m[32m    private static <T extends Enum<T>> T getEnumValueForString(Class<T> clazz, HashMap<String, T> map, String field) {[m
         T value = map.get(field);[m
[31m-        if (value == null)[m
[31m-        {[m
[31m-            try[m
[31m-            {[m
[32m+[m[32m        if (value == null) {[m
[32m+[m[32m            try {[m
                 // Textual shape description[m
                 value = Enum.valueOf(clazz, field);[m
[31m-            }[m
[31m-            catch (Exception e)[m
[31m-            {[m
[32m+[m[32m            } catch (Exception e) {[m
                 // Index shape description[m
                 int index;[m
[31m-                try[m
[31m-                {[m
[32m+[m[32m                try {[m
                     index = Integer.parseInt(field);[m
[31m-                }[m
[31m-                catch (NumberFormatException nfe)[m
[31m-                {[m
[32m+[m[32m                } catch (NumberFormatException nfe) {[m
                     index = 0;[m
                 }[m
 [m
[36m@@ -544,33 +499,29 @@[m [mpublic class CoreParser[m
     }[m
 [m
     HashMap<String, Shapes2D> shapes2DMap = new HashMap<String, Shapes2D>();[m
[31m-    private Shapes2D get2DShapeForString(String field)[m
[31m-    {[m
[32m+[m
[32m+[m[32m    private Shapes2D get2DShapeForString(String field) {[m
         return getEnumValueForString(Shapes2D.class, shapes2DMap, field);[m
     }[m
 [m
     HashMap<String, Shapes3D> shapes3DMap = new HashMap<String, Shapes3D>();[m
[31m-    private Shapes3D get3DShapeForString(String field)[m
[31m-    {[m
[32m+[m
[32m+[m[32m    private Shapes3D get3DShapeForString(String field) {[m
         return getEnumValueForString(Shapes3D.class, shapes3DMap, field);[m
     }[m
 [m
[31m-    private void createVertices(int lines)[m
[31m-    {[m
[32m+[m[32m    private void createVertices(int lines) {[m
         String vertex1 = "";[m
         String vertex2 = "";[m
         String weightString = "";[m
         String edgeType = "";[m
 [m
[31m-        if (!isSif)[m
[31m-        {[m
[32m+[m[32m        if (!isSif) {[m
             vertex1 = getToken(nodeIdColumns.get(0));[m
             vertex2 = getToken(nodeIdColumns.get(1));[m
             weightString = getToken(edgeWeightColumn);[m
             edgeType = getToken(edgeTypeColumn);[m
[31m-        }[m
[31m-        else[m
[31m-        {[m
[32m+[m[32m        } else {[m
             vertex1 = getNext();[m
             edgeType = getNext();[m
             vertex2 = getNext();[m
[36m@@ -579,63 +530,30 @@[m [mpublic class CoreParser[m
 [m
         float weight = Float.NaN;[m
 [m
[31m-        if (weightString.length() > 0)[m
[31m-        {[m
[31m-            try[m
[31m-            {[m
[31m-                weight = Float.parseFloat( weightString.replace(',', '.') );[m
[31m-            }[m
[31m-            catch (NumberFormatException nfe)[m
[31m-            {[m
[31m-                if (DEBUG_BUILD) println("NumberFormatException in CoreParser.createVertices():\n" + nfe.getMessage());[m
[32m+[m[32m        if (weightString.length() > 0) {[m
[32m+[m[32m            try {[m
[32m+[m[32m                weight = Float.parseFloat(weightString.replace(',', '.'));[m
[32m+[m[32m            } catch (NumberFormatException nfe) {[m
[32m+[m[32m                if (DEBUG_BUILD) {[m
[32m+[m[32m                    println("NumberFormatException in CoreParser.createVertices():\n" + nfe.getMessage());[m
[32m+[m[32m                }[m
             }[m
         }[m
 [m
         // SPN type edge[m
[31m-        if ( !edgeType.isEmpty() && edgeType.startsWith("SPN_") )[m
[31m-        {[m
[32m+[m[32m        if (!edgeType.isEmpty() && edgeType.startsWith("SPN_")) {[m
             String edgeName = "";[m
[31m-            if ( edgeType.contains("SPN_EDGE_VALUE:") )[m
[31m-            {[m
[32m+[m[32m            if (edgeType.contains("SPN_EDGE_VALUE:")) {[m
                 String[] splitEdgeType = edgeType.split("\\s+");[m
[31m-                edgeName = splitEdgeType[0].substring( splitEdgeType[0].indexOf(":") + 1, splitEdgeType[0].length() );[m
[32m+[m[32m                edgeName = splitEdgeType[0].substring(splitEdgeType[0].indexOf(":") + 1, splitEdgeType[0].length());[m
             }[m
 [m
[31m-            nc.addNetworkConnection( vertex1, vertex2, edgeName, edgeType.contains("SPN_IS_TOTAL_INHIBITOR_EDGE"), edgeType.contains("SPN_IS_PARTIAL_INHIBITOR_EDGE"), edgeType.contains("SPN_HAS_DUAL_ARROWHEAD") );[m
[31m-        }[m
[31m-        else[m
[31m-        {[m
[31m-            if (!Float.isNaN(weight))[m
[31m-            {[m
[31m-                if (weight > filterWeight)[m
[31m-                {[m
[31m-                    if (!edgeType.isEmpty())[m
[31m-                    {[m
[31m-                        nc.addNetworkConnection(vertex1, edgeType + lines, weight / 2.0f);[m
[31m-                        nc.addNetworkConnection(edgeType + lines, vertex2, weight / 2.0f);[m
[31m-[m
[31m-                        Vertex vertex = nc.getVerticesMap().get(edgeType + lines);[m
[31m-                        vertex.setVertexSize(vertex.getVertexSize() / 2);[m
[31m-                        vertex.setPseudoVertex();[m
[31m-[m
[31m-                        LayoutClasses lc = nc.getLayoutClassSetsManager().getClassSet(0);[m
[31m-                        VertexClass vc = lc.createClass(edgeType);[m
[31m-                        lc.setClass(nc.getVerticesMap().get(edgeType + lines), vc);[m
[31m-                    }[m
[31m-                    else[m
[31m-                    {[m
[31m-                        nc.addNetworkConnection(vertex1, vertex2, weight);[m
[31m-                    }[m
[31m-                }[m
[31m-[m
[31m-                WEIGHTED_EDGES = true;[m
[31m-            }[m
[31m-            else[m
[31m-            {[m
[31m-                if ( !edgeType.isEmpty() )[m
[31m-                {[m
[31m-                    nc.addNetworkConnection(vertex1, edgeType + lines, 0.0f);[m
[31m-                    nc.addNetworkConnection(edgeType + lines, vertex2, 0.0f);[m
[32m+[m[32m            nc.addNetworkConnection(vertex1, vertex2, edgeName, edgeType.contains("SPN_IS_TOTAL_INHIBITOR_EDGE"), edgeType.contains("SPN_IS_PARTIAL_INHIBITOR_EDGE"), edgeType.contains("SPN_HAS_DUAL_ARROWHEAD"));[m
[32m+[m[32m        } else if (!Float.isNaN(weight)) {[m
[32m+[m[32m            if (weight > filterWeight) {[m
[32m+[m[32m                if (!edgeType.isEmpty()) {[m
[32m+[m[32m                    nc.addNetworkConnection(vertex1, edgeType + lines, weight / 2.0f);[m
[32m+[m[32m                    nc.addNetworkConnection(edgeType + lines, vertex2, weight / 2.0f);[m
 [m
                     Vertex vertex = nc.getVerticesMap().get(edgeType + lines);[m
                     vertex.setVertexSize(vertex.getVertexSize() / 2);[m
[36m@@ -644,32 +562,41 @@[m [mpublic class CoreParser[m
                     LayoutClasses lc = nc.getLayoutClassSetsManager().getClassSet(0);[m
                     VertexClass vc = lc.createClass(edgeType);[m
                     lc.setClass(nc.getVerticesMap().get(edgeType + lines), vc);[m
[31m-                }[m
[31m-                else[m
[31m-                {[m
[31m-                    nc.addNetworkConnection(vertex1, vertex2, 0.0f);[m
[32m+[m[32m                } else {[m
[32m+[m[32m                    nc.addNetworkConnection(vertex1, vertex2, weight);[m
                 }[m
             }[m
[32m+[m
[32m+[m[32m            WEIGHTED_EDGES = true;[m
[32m+[m[32m        } else if (!edgeType.isEmpty()) {[m
[32m+[m[32m            nc.addNetworkConnection(vertex1, edgeType + lines, 0.0f);[m
[32m+[m[32m            nc.addNetworkConnection(edgeType + lines, vertex2, 0.0f);[m
[32m+[m
[32m+[m[32m            Vertex vertex = nc.getVerticesMap().get(edgeType + lines);[m
[32m+[m[32m            vertex.setVertexSize(vertex.getVertexSize() / 2);[m
[32m+[m[32m            vertex.setPseudoVertex();[m
[32m+[m
[32m+[m[32m            LayoutClasses lc = nc.getLayoutClassSetsManager().getClassSet(0);[m
[32m+[m[32m            VertexClass vc = lc.createClass(edgeType);[m
[32m+[m[32m            lc.setClass(nc.getVerticesMap().get(edgeType + lines), vc);[m
[32m+[m[32m        } else {[m
[32m+[m[32m            nc.addNetworkConnection(vertex1, vertex2, 0.0f);[m
         }[m
     }[m
 [m
[31m-    protected String getNext()[m
[31m-    {[m
[31m-        if (currentTokenIndex >= numberOfTokens)[m
[31m-        {[m
[32m+[m[32m    protected String getNext() {[m
[32m+[m[32m        if (currentTokenIndex >= numberOfTokens) {[m
             return "";[m
         }[m
 [m
         return tokens.get(currentTokenIndex++);[m
     }[m
 [m
[31m-    protected String getToken(int index)[m
[31m-    {[m
[31m-        if (index >= numberOfTokens || index < 0)[m
[31m-        {[m
[32m+[m[32m    protected String getToken(int index) {[m
[32m+[m[32m        if (index >= numberOfTokens || index < 0) {[m
             return "";[m
         }[m
 [m
         return tokens.get(index);[m
     }[m
[31m-}[m
\ No newline at end of file[m
[32m+[m[32m}[m

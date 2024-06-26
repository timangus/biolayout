package org.biolayout.ClassViewerUI.Tables.TableModels;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import javax.swing.table.*;
import org.biolayout.Analysis.Utils.*;
import org.apache.commons.collections15.map.MultiKeyMap;

/**
*
* @author Markus Brosch (mb8[at]sanger[dot]ac[dot]uk)
* @author Full refactoring by Thanos Theo, 2008-2009-2010-2011
* @version 3.0.0.0
*
**/

public final class ClassViewerTableModelEnrichment extends AbstractTableModel
{
    /**
    *  Serial version UID variable for the ClassViewerTableModelDetail class.
    */
    public static final long serialVersionUID = 111222333444555789L;

    public static final String[] COLUMN_NAMES = { "Current Class", "Class", "Class Set", "Observed" , "Expected" , "OverRep (Obs/Exp)", "Fisher's P", "Adj. Fisher's P", "Members"};
    private static final Class[]  COLUMN_CLASSES = {
                                                      String.class, // Cluster name
                                                      String.class,  //Term
                                                      String.class,  //Type
                                                      String.class,  //Observed
                                                      String.class,  //Expected
                                                      Double.class,  //OverRep
                                                      Double.class,  //Fishers
                                                      Double.class,  // Fishers Corrected
                                                      Integer.class, //Members
                                                    };

    private String[] clusterName = null;
    private String[] annotationTerm = null;
    private String[] annotationType = null;

    private String[] observed = null;
    private String[] expected = null;
    private String[] expectedTrial = null;

    private Double[] fobs = null;
    private Double[] fexp = null;
    private Double[] overRep = null;
    private Double[] zscore = null;


    private  Double[] relativeEntropy = null;
    private  Double[] fishersPvalue = null;
    private  Double[] adjustedPValue = null;
    private Integer[] clusterMembers = null;
    private  Double[] score = null;
    
    private HashMap<String, Integer> heatmapPosition = null;
    private MultiKeyMap<String, Integer> pValuePosition = null;

    public ClassViewerTableModelEnrichment() {
        heatmapPosition = new HashMap<>();
        pValuePosition = new MultiKeyMap<>();
    }

    private int overallEntropies = 0;

    @Override
    public int getColumnCount()
    {
        if (clusterName == null || clusterName.length == 0)
            return COLUMN_NAMES.length - 1;
        else
            return COLUMN_NAMES.length;
    }

    @Override
    public int getRowCount()
    {
        return (annotationType != null) ? annotationType.length : 0;
    }

    @Override
    public Class<?> getColumnClass(int col)
    {
        if (clusterName == null  || clusterName.length == 0)
            return COLUMN_CLASSES[col + 1];
        else
            return COLUMN_CLASSES[col];
    }

    @Override
    public String getColumnName(int col)
    {
        if (clusterName == null  || clusterName.length == 0)
            return COLUMN_NAMES[col + 1];
        else
            return COLUMN_NAMES[col];
    }

    @Override
    public Object getValueAt(int row, int col)
    {
        if (row > getRowCount() || col > getColumnCount())
            return null;
        if (clusterName == null || clusterName.length == 0)
            col++;
        
        switch(col)
        {
            case(0): return clusterName[row];
            case(1): return annotationTerm[row];
            case(2): return annotationType[row];

            case(3): return observed[row];
            case(4): return expected[row];
            //case(4): return expectedTrial[row];

            //case(5): return fobs[row];
            //case(6): return fexp[row];
            case(5): return overRep[row];
            //case(8): return zscore[row];

            //case(9): return relativeEntropy[row];
            case(6): return fishersPvalue[row];
            case(7): return adjustedPValue[row];

            case(8): return clusterMembers[row];
            //case(13): return score[row];

            default: throw new IllegalArgumentException("column " + col + " doesn't exist!");
        }
    }

    public void setTerm2Entropy(Map<String, String> Observed, Map<String, String> Expected, Map<String, String> ExpectedTrial, Map<String, String> Fobs, Map<String, String> Fexp, Map<String, Double> OverRep, Map<String, String> Zscore, Map<String, Double> Term2Entropy, Map<String, Double> Fishers, Map<String, Integer> Members, String AnnotationType)
    {
        if (Term2Entropy == null || AnnotationType == null)
        {
            annotationTerm  = new String[0];
            annotationType  = new String[0];
            observed        = new String[0];
            expected        = new String[0];
            expectedTrial   = new String[0];
            fobs            = new Double[0];
            fexp            = new Double[0];
            overRep         = new Double[0];
            zscore          = new Double[0];
            relativeEntropy = new Double[0];
            fishersPvalue   = new Double[0];
            clusterMembers  = new Integer[0];
            score           = new Double[0];
            adjustedPValue  = new Double[0];

            return;
        }

        this.setSize( Term2Entropy.size() );

        int i = 0;
        Set<String> terms = Term2Entropy.keySet();
        for (String term : terms)
        {
            annotationTerm[i]  = term;
            annotationType[i]  = AnnotationType;

            observed[i]        = Observed.get(annotationTerm[i]);
            expected[i]        = Expected.get(annotationTerm[i]);
            expectedTrial[i]   = ExpectedTrial.get(annotationTerm[i]);
            fobs[i]            = Double.parseDouble( Fobs.get(annotationTerm[i]) );
            fexp[i]            = Double.parseDouble( Fexp.get(annotationTerm[i]) );
            overRep[i]         = OverRep.get(annotationTerm[i]);
            zscore[i]          = Double.parseDouble( Zscore.get(annotationTerm[i]) );

            relativeEntropy[i] = Term2Entropy.get(annotationTerm[i]);
            fishersPvalue[i]   = Fishers.get(annotationTerm[i]);
            clusterMembers[i]  = Members.get(annotationTerm[i]);
            adjustedPValue[i]  = fishersPvalue[i] * Fishers.size();

            score[i]           = new Double( MathUtil.calcScore( fishersPvalue[i].doubleValue(), clusterMembers[i].intValue(), relativeEntropy[i].doubleValue() ) );

            i++;
        }
    }

    // VERY SPECIFIC USAGE to display over ALL values of ALL term types

    public void setSize(int size)
    {
        clusterName      = new String[size];
        annotationTerm   = new String[size];
        annotationType   = new String[size];
        observed         = new String[size];
        expected         = new String[size];
        expectedTrial    = new String[size];
        fobs             = new Double[size];
        fexp             = new Double[size];
        overRep          = new Double[size];
        zscore           = new Double[size];
        relativeEntropy  = new Double[size];
        fishersPvalue    = new Double[size];
        adjustedPValue   = new Double[size];
        clusterMembers   = new Integer[size];
        score            = new Double[size];
        overallEntropies = 0;
    }

    public void addAnalysisValues(Map<String, String> Observed, Map<String, String> Expected, Map<String, String> ExpectedTrial, Map<String, String> Fobs, Map<String, String> Fexp, Map<String, String> OverRep, Map<String, String> Zscore, Map<String, Double> Term2Entropy, Map<String, Double> Term2FisherPVal, Map<String, Integer> Term2MembersCount, String AnnotationType, String cluster)
    {
        if (Term2Entropy == null || AnnotationType == null || Term2FisherPVal == null) return;

        Set<String> terms = Term2Entropy.keySet();
        for (String term : terms)
        {
            clusterName[overallEntropies]     = cluster;
            annotationTerm[overallEntropies]  = term;
            annotationType[overallEntropies]  = AnnotationType;
            observed[overallEntropies]        = Observed.get(annotationTerm[overallEntropies]);
            expected[overallEntropies]        = Expected.get(annotationTerm[overallEntropies]);
            expectedTrial[overallEntropies]   = ExpectedTrial.get(annotationTerm[overallEntropies]);
            fobs[overallEntropies]            = Double.parseDouble( Fobs.get(annotationTerm[overallEntropies]) );
            fexp[overallEntropies]            = Double.parseDouble( Fexp.get(annotationTerm[overallEntropies]) );
            overRep[overallEntropies]         = Double.parseDouble( OverRep.get(annotationTerm[overallEntropies]) );
            zscore[overallEntropies]          = Double.parseDouble( Zscore.get(annotationTerm[overallEntropies]) );
            
            adjustedPValue[overallEntropies]  = Term2FisherPVal.get(annotationTerm[overallEntropies])*Term2FisherPVal.size();

            relativeEntropy[overallEntropies] = Term2Entropy.get(annotationTerm[overallEntropies]);
            fishersPvalue[overallEntropies]   = Term2FisherPVal.get(annotationTerm[overallEntropies]);
            clusterMembers[overallEntropies]  = Term2MembersCount.get(annotationTerm[overallEntropies]);
            score[overallEntropies]           = new Double( MathUtil.calcScore( fishersPvalue[overallEntropies].doubleValue(), clusterMembers[overallEntropies].intValue(), relativeEntropy[overallEntropies].doubleValue() ) );
            overallEntropies++;
        }
    }
    
    public void setHeatmapData(String heatmapPosition, int modelPosition){
        this.heatmapPosition.put(heatmapPosition, modelPosition);
    }
    
    public void setPValueData(String rowcol, String term, int modelPosition){
        this.pValuePosition.put(rowcol, term, modelPosition);
    }
    
    public int getPValueTableIndex(String key, String term){
        return this.pValuePosition.get(key, term);
    }
    
   public int getHeatmapTableIndex(int heatmapX, int heatmapY){
       String id = heatmapX+" "+heatmapY;
       if (!this.heatmapPosition.containsKey(id)){
           return -1;
       }
       return this.heatmapPosition.get(id).intValue();
   }
   
   public ArrayList<String> generateCSV(){
       
       // TO-DO escape CSV
       ArrayList<String> csvFile = new ArrayList<>();
       String columnTitles = "";
       for (int c = 0; c < this.getColumnCount(); c++) {
            columnTitles += escapeCommas(this.getColumnName(c).toString()) + ",";
       }
       csvFile.add(columnTitles);
       
       for (int r = 0; r < this.getRowCount(); r++) {
           String line = "";
           for (int c = 0; c < this.getColumnCount(); c++) {
               line += escapeCommas(this.getValueAt(r, c).toString()) + ",";
           }
           csvFile.add(line);
       }
       
       return csvFile;
   }
   
   private String escapeCommas(String input){
       return "\""+input+"\"";
   }


}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.biolayout.Analysis;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sebhorsewell
 */
public class EnrichmentData {
    
    public EnrichmentData(){
        
    }
    public String clusterName;
    public Map<String, Map<String, Double>> perType = new HashMap<String, Map<String, Double>>();
    public Map<String, Map<String, Double>> fishers = new HashMap<String, Map<String, Double>>();
    public Map<String, Map<String, Double>> adjustedFishers = new HashMap<String, Map<String, Double>>();
    
        public Map<String, HashMap<String, String>> OverRep = new HashMap<String, HashMap<String, String>>();
        
    public Map<String, HashMap<String, String>> Observed = new HashMap<String, HashMap<String, String>>();
    public Map<String, HashMap<String, String>> Expected = new HashMap<String, HashMap<String, String>>();
    public Map<String, HashMap<String, String>> Fobs = new HashMap<String, HashMap<String, String>>();
    public Map<String, HashMap<String, String>> Fexp = new HashMap<String, HashMap<String, String>>();

    public Map<String, HashMap<String, String>> ExpectedTrial = new HashMap<String, HashMap<String, String>>();
    public Map<String, HashMap<String, String>> Zscore = new HashMap<String, HashMap<String, String>>();
    
    
    

    public Map<String, Map<String, Integer>> numberOfMembers = new HashMap<String, Map<String, Integer>>();
}

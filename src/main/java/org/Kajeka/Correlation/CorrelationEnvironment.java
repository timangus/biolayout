package org.Kajeka.Correlation;

import java.util.HashSet;

/**
*
* @author Thanos Theo, 2008-2009-2010
* @version 3.0.0.0
*
*/

public final class CorrelationEnvironment
{

    /**
    *  Available Correlation types.
    */
    public static enum CorrelationTypes { PEARSON, SPEARMAN }
    public static CorrelationTypes CURRENT_METRIC = CorrelationTypes.PEARSON;

    public static enum TransformType
    {
        RAW,
        LOG_SCALE,
        MEAN_CENTRED,
        UNIT_VARIANCE_SCALED,
        PARETO_SCALED,
    }

    public static enum ScaleTransformType
    {
        NONE,
        LOG2,
        LOG10,
        ANTILOG2,
        ANTILOG10,
    }
    public static ScaleTransformType CURRENT_SCALE_TRANSFORM = ScaleTransformType.NONE;
    
    public static enum NormalisationType
    {
        NONE,
        MINMAX
    }
    public static NormalisationType CURRENT_NORMALISATION = NormalisationType.NONE;

    public static final float DEFAULT_CORRELATION_THRESHOLD = 0.85f;
    public static final float DEFAULT_STORED_CORRELATION_THRESHOLD = 0.70f;
    public static float       CURRENT_CORRELATION_THRESHOLD = DEFAULT_CORRELATION_THRESHOLD;
    public static HashSet<Integer> CURRENT_FILTER_SET = new HashSet<Integer>();
    public static float       STORED_CORRELATION_THRESHOLD = DEFAULT_STORED_CORRELATION_THRESHOLD;
    public static String      CORRELATION_FILE = "";
    public static String      CORRELATION_FILE_PATH = "";
    public static int         CORRELATION_DATA_FIRST_COLUMN = 0;
    public static int         CORRELATION_DATA_FIRST_ROW = 0;
    public static boolean     CORRELATION_DATA_TRANSPOSE = false;

}

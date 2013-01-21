package org.BioLayoutExpress3D.Expression;

/**
*
* @author Thanos Theo, 2008-2009-2010
* @version 3.0.0.0
*
*/

public final class ExpressionEnvironment
{

    /**
    *  Available Correlation types.
    */
    public static enum CorrelationTypes { PEARSON, SPEARMAN }
    public static CorrelationTypes CURRENT_METRIC = CorrelationTypes.PEARSON;
    public static final float DEFAULT_CORRELATION_THRESHOLD = 0.85f;
    public static final float DEFAULT_STORED_CORRELATION_THRESHOLD = 0.70f;
    public static float       CURRENT_CORRELATION_THRESHOLD = DEFAULT_CORRELATION_THRESHOLD;
    public static float       STORED_CORRELATION_THRESHOLD = DEFAULT_STORED_CORRELATION_THRESHOLD;
    public static String      EXPRESSION_FILE = "";
    public static String      EXPRESSION_FILE_PATH = "";
    public static int         EXPRESSION_DATA_FIRST_COLUMN = 0;
    public static int         EXPRESSION_DATA_FIRST_ROW = 0;
    public static boolean     EXPRESSION_DATA_TRANSPOSE = false;

}

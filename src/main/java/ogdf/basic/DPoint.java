package ogdf.basic;

public class DPoint
{

    public double m_x; //!< The x-coordinate.
    public double m_y; //!< The y-coordinate.

    //! Creates a real point (0,0).
    public DPoint()
    {
        this(0.0, 0.0);
    }

    //! Creates a real point (\a x,\a y).
    public DPoint(double x, double y)
    {
        m_x = x;
        m_y = y;
    }

    //! Copy constructor.
    public DPoint(DPoint dp)
    {
        this(dp.m_x, dp.m_y);
    }

    private boolean DIsEqual(double a, double b)
    {
        final double OGDF_GEOM_EPS = 1e-06;
        return (a < (b + OGDF_GEOM_EPS) && a > (b - OGDF_GEOM_EPS));
    }

    //! Relaxed equality operator.
    public boolean equals(DPoint dp)
    {
        return DIsEqual(m_x, dp.m_x) && DIsEqual(m_y, dp.m_y);
    }

    //! Returns the norm of the point.
    public double norm()
    {
        return java.lang.Math.sqrt(m_x * m_x + m_y * m_y);
    }

    // gives the euclidean distance between p and *this
    public double distance(DPoint p)
    {
        double dx = p.m_x - m_x;
        double dy = p.m_y - m_y;
        return java.lang.Math.sqrt((dx * dx) + (dy * dy));
    }

    // adds p to *this
    public DPoint plus(DPoint p)
    {
        return new DPoint(m_x + p.m_x, m_y + p.m_y);
    }

    // subtracts p from *this
    public DPoint minus(DPoint p)
    {
        return new DPoint(m_x - p.m_x, m_y - p.m_y);
    }
}
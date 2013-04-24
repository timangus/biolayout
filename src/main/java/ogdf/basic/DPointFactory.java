package ogdf.basic;

/**
 *
 * @author Tim Angus <tim.angus@roslin.ed.ac.uk>
 */
public enum DPointFactory
{
    INSTANCE;

    public enum Dimensions
    {
        _2,
        _3
    }

    private Dimensions d = Dimensions._2;

    public void setDimensions(Dimensions d)
    {
        this.d = d;
    }

    public DPoint newPoint()
    {
        switch (d)
        {
            default:
            case _2:
                return new DPoint2();

            case _3:
                return new DPoint3();
        }
    }

    public DPoint newPoint(DPoint p)
    {
        switch (d)
        {
            default:
            case _2:
                return new DPoint2((DPoint2)p);

            case _3:
                return new DPoint3((DPoint3)p);
        }
    }
}

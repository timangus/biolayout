package ogdf.basic;

public class GraphAttributes
{

    public Graph constGraph()
    {
        return null;
    }

    public void clearAllBends()
    {
    }

    /*double &x(node v) {
     return m_x[v];
     }*/
    public double x(node v) { return 0.0; }
    public void setX(node v, double value) { }

    public double y(node v) { return 0.0; }
    public void setY(node v, double value) { }

    public double width(node v) { return 0.0; }
    public void setWidth(node v, double value) { }

    public double height(node v) { return 0.0; }
    public void setHeight(node v, double value) { }
}

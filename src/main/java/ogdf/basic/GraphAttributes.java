package ogdf.basic;

public class GraphAttributes
{
    Graph graph;
    NodeArray<Double> m_x;
    NodeArray<Double> m_y;
    NodeArray<Double> m_width;
    NodeArray<Double> m_height;

    public GraphAttributes(Graph graph)
    {
        this.graph = graph;
        m_x = new NodeArray<Double>(graph);
        m_y = new NodeArray<Double>(graph);
        m_width = new NodeArray<Double>(graph);
        m_height = new NodeArray<Double>(graph);
    }

    public Graph constGraph()
    {
        return graph;
    }

    public double x(node v)
    {
        return m_x.get(v);
    }

    public void setX(node v, double value)
    {
        m_x.set(v, value);
    }

    public double y(node v)
    {
        return m_y.get(v);
    }

    public void setY(node v, double value)
    {
        m_y.set(v, value);
    }

    public double width(node v)
    {
        return m_width.get(v);
    }

    public void setWidth(node v, double value)
    {
        m_width.set(v, value);
    }

    public double height(node v)
    {
        return m_height.get(v);
    }

    public void setHeight(node v, double value)
    {
        m_height.set(v, value);
    }
}

package ogdf.basic;

import java.util.*;
import org.BioLayoutExpress3D.Network.*;

public class GraphAttributes
{
    Graph graph;
    NodeArray<Double> m_x;
    NodeArray<Double> m_y;
    NodeArray<Double> m_width;
    NodeArray<Double> m_height;
    Map<Vertex,node> m;

    public GraphAttributes(Graph graph)
    {
        this.graph = graph;
        m_x = new NodeArray<Double>(graph, Factory.DOUBLE);
        m_y = new NodeArray<Double>(graph, Factory.DOUBLE);
        m_width = new NodeArray<Double>(graph, Factory.DOUBLE);
        m_height = new NodeArray<Double>(graph, Factory.DOUBLE);
        m = null;
    }

    public GraphAttributes(NetworkContainer nc)
    {
        this(new Graph());

        m = new HashMap<Vertex,node>();

        for (Vertex vertex : nc.getVertices())
        {
            node n = graph.newNode();
            setX(n, vertex.getX());
            setY(n, vertex.getY());
            setWidth(n, vertex.getVertexSize());
            setHeight(n, vertex.getVertexSize());
            m.put(vertex, n);
        }

        for (Edge edge : nc.getEdges())
        {
            node v = (node)m.get(edge.getFirstVertex());
            node w = (node)m.get(edge.getSecondVertex());

            edge e = graph.newEdge(v, w);
        }
    }

    public void applyTo(NetworkContainer nc)
    {
        for (Vertex vertex : nc.getVertices())
        {
            node n = m.get(vertex);
            vertex.setVertexLocation((float)x(n), (float)y(n), NetworkContainer.CANVAS_Z_SIZE * 0.5f);
        }
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

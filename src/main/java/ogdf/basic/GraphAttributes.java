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
        double xMin = Double.MAX_VALUE;
        double xMax = Double.MIN_VALUE;
        double yMin = Double.MAX_VALUE;
        double yMax = Double.MIN_VALUE;

        for (Iterator<node> i = graph.nodesIterator(); i.hasNext();)
        {
            node v = i.next();

            if (x(v) < xMin)
            {
                xMin = x(v);
            }
            else if (x(v) > xMax)
            {
                xMax = x(v);
            }
            if (y(v) < yMin)
            {
                yMin = y(v);
            }
            else if (y(v) > yMax)
            {
                yMax = y(v);
            }
        }

        double maxDimension = Math.max(xMax - xMin, yMax - yMin);
        double maxTargetDimension = Math.max(NetworkContainer.CANVAS_X_SIZE, NetworkContainer.CANVAS_Y_SIZE);
        double scale = maxTargetDimension / maxDimension;
        double xOffset = -xMin;
        double yOffset = -yMin;

        for (Vertex vertex : nc.getVertices())
        {
            node n = m.get(vertex);
            vertex.setVertexLocation(
                    (float)((x(n) + xOffset) * scale),
                    (float)((y(n) + yOffset) * scale),
                    NetworkContainer.CANVAS_Z_SIZE * 0.5f);
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

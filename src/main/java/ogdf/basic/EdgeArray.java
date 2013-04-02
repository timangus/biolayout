package ogdf.basic;

/*
 * $Revision: 2615 $
 *
 * last checkin:
 *   $Author: gutwenger $
 *   $Date: 2012-07-16 14:23:36 +0200 (Mo, 16. Jul 2012) $
 ***************************************************************/
/**
 * \file \brief Declaration and implementation of EdgeArray class.
 *
 * \author Carsten Gutwenger
 *
 * \par License: This file is part of the Open Graph Drawing Framework (OGDF).
 *
 * \par Copyright (C)<br> See README.txt in the root directory of the OGDF installation for details.
 *
 * \par This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License Version 2 or 3 as published by the Free Software Foundation; see the file LICENSE.txt included in the
 * packaging of this file for details.
 *
 * \par This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * \par You should have received a copy of the GNU General Public License along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * \see http://www.gnu.org/copyleft/gpl.html
 **************************************************************
 */
import java.util.*;

//! Dynamic arrays indexed with edges.
/**
 * Edge arrays represent a mapping from edges to data of type \a T. They adjust their table size automatically when the
 * graph grows.
 *
 * @tparam T is the element type.
 */
public class EdgeArray<T> extends ArrayList<T>
{
    /* EdgeArrayBase */

    /**
     * Pointer to list element in the list of all registered edge arrays which references this array.
     */
    //ListIterator<EdgeArrayBase*> m_it;
    public Graph m_pGraph; //!< The associated graph.

    //! Associates the array with a new graph.
    void reregister(Graph pG)
    {
        if (m_pGraph != null)
        {
            //m_pGraph.unregisterArray(m_it);
        }
        if ((m_pGraph = pG) != null)
        {
            //m_it = pG.registerArray(this);
        }
    }
    /* ~EdgeArrayBase */
    T m_x; //!< The default value for array elements.

    //! Constructs an empty edge array associated with no graph.
    public EdgeArray()
    {
        super();
        m_pGraph = null;
    }

    //! Constructs an edge array associated with \a G.
    public EdgeArray(Graph G)
    {
        super(G.nodeArrayTableSize());
        m_pGraph = G;
    }

    //! Constructs an edge array associated with \a G.
    /**
     * @param G is the associated graph.
     * @param x is the default value for all array elements.
     */
    public EdgeArray(Graph G, T x)
    {
        this(G);
        m_x = x;
    }

    //! Constructs an edge array that is a copy of \a A.
    /**
     * Associates the array with the same graph as \a A and copies all elements.
     */
    public EdgeArray(EdgeArray<T> A)
    {
        this(A.m_pGraph, A.m_x);
    }

    //! Returns true iff the array is associated with a graph.
    boolean valid()
    {
        return m_pGraph != null;
    }

    //! Returns a pointer to the associated graph.
    Graph graphOf()
    {
        return m_pGraph;
    }

    //! Returns a reference to the element with index \a e.
    public T get(edge e)
    {
        assert e != null && e.graphOf() == m_pGraph;
        return super.get(e.index());
    }

    public void set(edge e, T value)
    {
        assert e != null && e.graphOf() == m_pGraph;
        super.set(e.index(), value);
    }

    //! Reinitializes the array. Associates the array with no graph.
    public void init()
    {
        init(null);
    }

    //! Reinitializes the array. Associates the array with \a G.
    public void init(Graph G)
    {
        super.clear();
        reregister(G);
    }

    //! Reinitializes the array. Associates the array with \a G.
    /**
     * @param G is the associated graph.
     * @param x is the default value.
     */
    void init(Graph G, T x)
    {
        super.clear();
        m_x = x;
        reregister(G);
    }

    //! Sets all array elements to \a x.
    public void fill(T x)
    {
        for (T element : this)
        {
            element = x;
        }
    }
}

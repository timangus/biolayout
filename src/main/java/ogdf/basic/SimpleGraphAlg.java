package ogdf.basic;

import ogdf.basic.*;

public class SimpleGraphAlg
{

    public static int connectedComponents(Graph G, NodeArray<Integer> component)
    {
        return 0;
        /*int nComponent = 0;
        component.fill(-1);

        StackPure<node> S;

        node v;
        forall_nodes(v, G) {
            if (component[v] != -1)
            {
                continue;
            }

            S.push(v);
            component[v] = nComponent;

            while (!S.empty())
            {
                node w = S.pop();
                edge e;
                forall_adj_edges(e, w) {
                    node x = e - > opposite(w);
                    if (component[x] == -1)
                    {
                        component[x] = nComponent;
                        S.push(x);
                    }
                }
            }

            ++nComponent;
        }

        return nComponent;*/
    }
}

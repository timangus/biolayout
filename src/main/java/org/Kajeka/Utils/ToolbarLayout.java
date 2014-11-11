package org.Kajeka.Utils;

import java.awt.*;

public class ToolbarLayout extends FlowLayout
{
    public ToolbarLayout()
    {
        super(FlowLayout.LEADING, 0, 0);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent)
    {
        synchronized (parent.getTreeLock())
        {
            Dimension size = super.minimumLayoutSize(parent);

            int nComponents = parent.getComponentCount();
            if (nComponents > 0)
            {
                int firstY = parent.getComponent(0).getY();
                Component last = parent.getComponent(nComponents - 1);
                int lastY = last.getY();

                if (lastY != firstY)
                {
                    size = new Dimension((int) size.getWidth(), lastY + last.getHeight());
                }
            }

            return size;
        }
    }
}

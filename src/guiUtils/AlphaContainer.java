package guiUtils;

import javax.swing.*;
import java.awt.*;

public class AlphaContainer extends JComponent
{
    private JComponent component;

    public AlphaContainer(JComponent component)
    {
        this.component = component;
        setLayout( new BorderLayout() );
        setOpaque( false );
        component.setOpaque( false );
        add( component );
    }

    /**
     *  Paint the background using the background Color of the
     *  contained component
     */
    @Override
    public void paintComponent(Graphics g)
    {
        g.setColor( component.getBackground() );
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
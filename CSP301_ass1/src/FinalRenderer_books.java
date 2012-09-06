import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import prefuse.render.AbstractShapeRenderer;
import prefuse.visual.VisualItem;

class FinalRenderer_books extends AbstractShapeRenderer
{
//protected RectangularShape m_box = new Rectangle2D.Double();
protected Ellipse2D m_box = new Ellipse2D.Double();

protected Shape getRawShape(VisualItem item)
{
m_box.setFrame(item.getX(), item.getY(), (Integer) item.get("degree")*2, 
				(Integer) item.get("degree")*2);
return m_box;
}
}

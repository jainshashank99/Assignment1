
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JPopupMenu;
import prefuse.controls.ControlAdapter;
import prefuse.controls.Control;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class FinalControlListener_edited extends ControlAdapter implements Control {
	public void itemClicked(VisualItem item, MouseEvent e) {
		if (item instanceof NodeItem) {
			String name = ((String) item.get("label"));
			String value;
			int v = (Integer) item.get("value");
			if(v==0){
				value = "Liberal";
			}else{
				value = "Conservative";
			}
			int id = (Integer) item.get("id");
			String source = (String) item.get("source");
			
			JPopupMenu jpub = new JPopupMenu();
			jpub.add("ID: "+ id);
			jpub.add("Name: " + name);
			jpub.add("Type: " + value);
			jpub.add("Source: " + source);
			jpub.show(e.getComponent(), (int) e.getX(), (int) e.getY());
		}
	}
}

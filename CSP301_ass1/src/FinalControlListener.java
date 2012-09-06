import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JPopupMenu;
import prefuse.controls.ControlAdapter;
import prefuse.controls.Control;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class FinalControlListener extends ControlAdapter implements Control {
	public void itemClicked(VisualItem item, MouseEvent e) {
		if (item instanceof NodeItem) {
			String name = ((String) item.get("label"));
			String value = (String) item.get("value");
			if(value.equals("c")){
				value = "Conservative";
			}else if(value.equals("n")){
				value = "Neutral";
			}else{
				value = "Liberal";
			}
			
			
			int id = (Integer) item.get("id");
			JPopupMenu jpub = new JPopupMenu();
			jpub.add("ID: "+ id);
			jpub.add("Name: " + name);
			jpub.add("Type: " + value);
			jpub.show(e.getComponent(), (int) e.getX(), (int) e.getY());
		}
	}
}
package GUI.View;

import Tester.Osoba;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BlockView extends JPanel {

    public BlockView() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void updateBlocks(List<List<Osoba>> blocks) {
        this.removeAll();
        for (int i = 0; i < blocks.size(); i++) {
            JPanel b = new JPanel();
            b.setLayout(new BorderLayout());
            b.setBorder(BorderFactory.createTitledBorder("Block " + i));

            JTextArea area = new JTextArea();
            area.setEditable(false);

            for (Osoba o : blocks.get(i)) {
                area.append(o.toString() + "\n");
            }

            b.add(new JScrollPane(area), BorderLayout.CENTER);
            this.add(b);
        }
        this.revalidate();
        this.repaint();
    }
}

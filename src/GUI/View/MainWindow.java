package GUI.View;

import GUI.Controller.AppController;
import Tester.Osoba;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private final OsobaForm osobaForm;
    private final BlockView blocksPanel;
    private final AppController controller;

    public MainWindow(AppController controller) {
        super("HeapFile GUI");
        this.controller = controller;

        this.osobaForm = new OsobaForm(controller, this::refreshBlocks);
        this.blocksPanel = new BlockView();

        this.setLayout(new BorderLayout());
        this.add(this.osobaForm, BorderLayout.NORTH);
        this.add(new JScrollPane(this.blocksPanel), BorderLayout.CENTER);

        this.setSize(900,700);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

        this.refreshBlocks();
    }

    private void refreshBlocks() {
        this.blocksPanel.updateBlocks(this.controller.loadBlocks());
    }
}

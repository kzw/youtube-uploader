package kzw.youtube.gui;

import java.awt.BorderLayout;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author kz
 */
public class MainPanel extends JPanel {
    public final static JTextArea log=new JTextArea(35,22);
    public MainPanel() throws Exception{
        super(new BorderLayout());
        log.setLineWrap(true);
        log.setWrapStyleWord(true); 
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(true);
        JScrollPane logScrollPane = new JScrollPane(log);

        DataPanel buttonPanel = new DataPanel(); //use FlowLayout
        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
        buttonPanel.addListeners();
    }
    
}

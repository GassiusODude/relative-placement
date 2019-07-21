package relative_placement;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.table.*;
import relative_placement.Results;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
public class ScorerGUI extends JFrame{

    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu("File");
    JMenuItem menuItemLoad = new JMenuItem("Load CSV");
    JMenuItem menuItemExport = new JMenuItem("Export CSV");
    JMenuItem menuItemExit = new JMenuItem("Exit");
    JFileChooser jfc = new JFileChooser();
    JPanel panel = new JPanel();
    JScrollPane scrollPane;
    JTable table;
    Results results = new Results();
    public ScorerGUI(){
        super("Relative Placement Scorer");
        this.setSize(600, 400);

        // ------------------------- setup menu  ----------------------------
        menu.add(menuItemLoad);
        menuItemLoad.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent event){
                try{
                    int retVal = jfc.showOpenDialog(null);
                    if (retVal == 0){
                        File f = jfc.getSelectedFile();
                        
                        // assumes first row is header and token is comma
                        results.load(f.toString());

                        menuItemExport.setEnabled(true);
                    }
                }
                catch (RuntimeException e){
                    JOptionPane.showMessageDialog(null, e.toString(), "Warning",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menuItemExport.setEnabled(false);
        menu.add(menuItemExport);
        menuItemExport.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                int retVal = jfc.showSaveDialog(null);
                if (retVal == 0){
                    File f = jfc.getSelectedFile();
                    
                    // assumes first row is header and token is comma
                    results.export(f.toString());
                }
            }
        });
        menu.add(menuItemExit);
        menuItemExit.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                System.exit(0);
            }
        });
        menuBar.add(menu);

        // ------------------------  setup panel  ---------------------------
        table = new JTable(results);
        table.setFillsViewportHeight(true);

        table.setDefaultRenderer(Object.class, new TableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
                
                JTextField editor = new JTextField();
                editor.setPreferredSize(new Dimension(20, 50));
                editor.setHorizontalAlignment(editor.CENTER);
                if (value != null)
                    editor.setText(value.toString());
                int numContestants = table.getRowCount();
                int numCols = table.getColumnCount();
                int numJudges = numCols - numContestants - 2;
                editor.setForeground(Color.white);
                if (isSelected){
                    editor.setBackground(Color.yellow);
                    editor.setForeground(Color.BLACK);
                }
                else if (column == 0){
                    editor.setBackground(new Color(100, 00, 100));
                }
                else if (column == 1){
                    // leaders
                    editor.setBackground(new Color(0, 0, 100));
                    editor.setForeground(Color.white);
                }
                else if (column == 2){
                    // followers
                    editor.setBackground(new Color(100, 0, 0));
                    editor.setForeground(Color.white);
                }
                else if (column < 2 + numJudges){
                    // judges
                    editor.setBackground(new Color(100, 100, 0));
                }
                else if (column == 2 + numJudges){
                    // head judges
                    editor.setBackground(new Color(0, 100, 100));
                }
                else{
                    //editor.setBackground((row % 2 == 0) ? Color.DARK_GRAY : Color.cyan);
                    if (value == "")
                        editor.setBackground(Color.lightGray);
                    else if (value == "--")
                        editor.setBackground(Color.DARK_GRAY);
                    else
                        editor.setBackground(new Color(0, 100, 0));


                }
                return editor;
            }
        });
        
        scrollPane = new JScrollPane(table);

        // ------------------------  setup JFrame  --------------------------
        this.setJMenuBar(menuBar);
        this.add(scrollPane);

        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent event){
              System.exit(0);
            }
        });
    }

    public static void main(String[] args) 
    { 

        javax.swing.SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                ScorerGUI scoreGUI = new ScorerGUI();
                scoreGUI.show();
            }
        });
    } 
}
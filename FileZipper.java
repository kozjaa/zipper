
package filezipper;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.*;

public class FileZipper extends JFrame
{
    public FileZipper()
    {
        this.setTitle("FileZipper");
        this.setBounds(275, 300, 250, 250);
        this.setJMenuBar(menuBar);
        
        JMenu menuFile = menuBar.add(new JMenu("File"));
        
        
        
        Action addAction = new MyAction("Add", "Add new record to archive", "ctrl D", new ImageIcon("dodaj.png"));
        Action deleteAction = new MyAction("Delete", "Delete selected record/records", "ctrl U", new ImageIcon("usun.png"));
        Action zipAction = new MyAction("ZIP", "Zip", "ctrl Z");
        
        JMenuItem menuAdd = menuFile.add(addAction);
        JMenuItem menuDelete = menuFile.add(deleteAction);
        JMenuItem menuZip = menuFile.add(zipAction);
        
        bAdd = new JButton(addAction);
        bDelete = new JButton(deleteAction);
        bZip = new JButton(zipAction);
        JScrollPane scroll = new JScrollPane(list);
        list.setBorder(BorderFactory.createEtchedBorder());
        GroupLayout layout = new GroupLayout(this.getContentPane());
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addComponent(scroll, 100, 150, Short.MAX_VALUE)
                .addContainerGap(0, Short.MAX_VALUE)
                .addGroup(
                layout.createParallelGroup().addComponent(bAdd).addComponent(bDelete).addComponent(bZip)
                
                )
        
            );
        layout.setVerticalGroup(
            layout.createParallelGroup()
            .addComponent(scroll, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup().addComponent(bAdd).addComponent(bDelete).addGap(5, 40, Short.MAX_VALUE).addComponent(bZip))
            
            );
        
        this.getContentPane().setLayout(layout);
    
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        this.pack();
    
    }
    private DefaultListModel listModel = new DefaultListModel()
    {
        public void addElement(Object obj) 
        {
            aList.add(obj);
            super.addElement(((File)obj).getName());
        }
        
        public Object get(int index)
        {
            return aList.get(index);
        }
        
        public Object remove(int index)
        {
            aList.remove(this);
            return super.remove(index);
        }
        ArrayList aList = new ArrayList();
    };
    private JList list = new JList(listModel);
    private JButton bAdd;
    private JButton bDelete;
    private JButton bZip;
    private JMenuBar menuBar = new JMenuBar();
    private JFileChooser chooser = new JFileChooser();
    
    public static void main(String[] args) 
    {
        
        new FileZipper().setVisible(true);
        
        
        
    }
    
    private class MyAction extends AbstractAction
    {

        public MyAction(String name, String description, String shortcut)
        {
            this.putValue(Action.NAME, name);
            this.putValue(Action.SHORT_DESCRIPTION, description);
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(shortcut));            
        }
        
        public MyAction(String name, String description, String shortcut, Icon icon)
        {
            this(name, description, shortcut);
            this.putValue(Action.SMALL_ICON, icon);
            
        }
        public void actionPerformed(ActionEvent e) 
        {
            if (e.getActionCommand().equals("Add"))
                addRecordToArchive();
            else if(e.getActionCommand().equals("Delete"))
                deleteRecordFromList();
            else if(e.getActionCommand().equals("ZIP"))
                    createZipArchive();
                    
                
        }
        
        private void addRecordToArchive()
        {
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setMultiSelectionEnabled(true);
            int tmp = chooser.showDialog(rootPane, "Add to archive");
            
            if (tmp == JFileChooser.APPROVE_OPTION)
            {
                File [] paths = chooser.getSelectedFiles();
                
                for (int i = 0; i < paths.length; i++)
                    if (!ifRecordRepeat(paths[i].getPath())) 
                    listModel.addElement(paths[i]);
            }
            
        }
        
        private void deleteRecordFromList()
        {
            int[] tmp = list.getSelectedIndices();
            
            for (int i = 0; i < tmp.length; i++ )
                listModel.remove(tmp[i]-i);
            
        }
        
        private void createZipArchive()
        {
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            chooser.setSelectedFile(new File(System.getProperty("user.dir")+File.separator+"myname.zip"));
            int tmp = chooser.showDialog(rootPane, "Compress");
            if (tmp == JFileChooser.APPROVE_OPTION)
            {
                
       
                byte tmpData[] = new byte[BUFFOR];
                try
                {
                    ZipOutputStream zOutS = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(chooser.getSelectedFile()),BUFFOR));
        
                    for (int i = 0; i < listModel.getSize(); i++)
                    {
                        if (!((File)listModel.get(i)).isDirectory())
                            zip(zOutS,(File)listModel.get(i),tmpData, ((File)listModel.get(i)).getPath());
                        else
                        {
                            returnPaths((File)listModel.get(i));
                            
                            for (int j = 0; j < pathList.size(); j++)
                                zip(zOutS, (File)pathList.get(j), tmpData,((File)listModel.get(i)).getPath() );
                            
                            pathList.removeAll(pathList);
                        }
                    }
        
                    zOutS.close();
                }
                catch(IOException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }
        
        private void returnPaths(File pathName)
        {
            String[] fileAndFolderNames = pathName.list();
       
            for (int i = 0; i < fileAndFolderNames.length; i++)
            {
                File p = new File(pathName.getPath(), fileAndFolderNames[i]);
          
                if (p.isFile())
                    pathList.add(p);
               
             
           
                if (p.isDirectory())
                    returnPaths(new File(p.getPath()));
               
            }
         }
        ArrayList pathList = new ArrayList();
        
        private void zip(ZipOutputStream zOutS, File filePath, byte[] tmpData, String basePath) throws FileNotFoundException, IOException
        {
            BufferedInputStream inS = new BufferedInputStream(new FileInputStream(filePath), BUFFOR);

            zOutS.putNextEntry(new ZipEntry(filePath.getPath().substring(basePath.lastIndexOf(File.separator)+1)));

            int counter;
            while ((counter = inS.read(tmpData, 0, BUFFOR)) != -1)
                zOutS.write(tmpData, 0, counter);


            zOutS.closeEntry();

            inS.close();
        }
        
        public static final int BUFFOR = 1024;
        
        private boolean ifRecordRepeat(String testedRecord)
        {
        for (int i = 0; i < listModel.getSize(); i++)
            if (((File)listModel.get(i)).getPath().equals(testedRecord))
                    return true;
        
        return false;
        }
    
    
        
    }
}

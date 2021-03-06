/*
 * File:  PrintUtilities2.java
 *
 * Copyright (C) 2007, Andrew Moe
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Primary   Andrew Moe <moea@uwstout.edu>
 * Contact:  Student Developer, University of Wisconsin-Stout
 *           
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.9  2007/09/09 16:12:48  rmikk
 * Added code to handle the case where no printer can be found. It first tries to
 *    find a printer with a null attribute set.
 *
 * Revision 1.8  2007/08/23 21:06:31  dennis
 * Removed unused imports.
 *
 * Revision 1.7  2007/08/22 15:19:08  rmikk
 * Created a public static get_print_service that included the attribute set
 * The low-level print code has a scale factor of 1
 * The JWindow's do NOT need to be visible.  Removed that code
 * Broke up the printing code and put into several Runnables
 *
 * Revision 1.6  2007/08/16 22:13:11  rmikk
 * Added a few more InvokeLater's so there is time for the Window to draw
 *
 * Revision 1.5  2007/08/12 21:56:46  amoe
 * -Fixed comment typos.
 * -Added gov.anl.ipns.Util.Sys.FinishWindowListener to JWindow when the
 *  component container is being initiated.
 *
 * Revision 1.4  2007/08/02 20:59:46  oakgrovej
 * reintroduced the squish/stretch of the component to fit on the page.
 * It will keep the aspect ratio.
 *
 * Revision 1.3  2007/08/02 15:19:32  oakgrovej
 * Removed the stretching/shrinking of the component to be printed.
 *
 * Revision 1.2  2007/07/26 21:14:22  amoe
 * -In init_component_container(), the container size is set to the same size
 *  as the Component to be printed, not the default.
 *
 * -PrinterPage.print(..) no longer sets the component container to visible.
 *  This in now done in silent_print() and dialog_print().  Also here, the
 *  container is now moved off-screen since it is not necessary to see.  This
 *  is a temporary fix, since making the container not visible will cause it
 *  to make the output component to not show up when printed.
 *
 * Revision 1.1  2007/07/25 22:05:23  amoe
 * Initial commit.
 *
 */
package gov.anl.ipns.Util.Sys;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.JWindow;
import javax.swing.RepaintManager;

/** 
 *  A utility class that lets you use a printer to print
 *  an arbitrary {@link java.awt.Component}.
 */
public class PrintUtilities2 
{
  public static final int DEFAULT_COMPONENT_WIDTH = 500;  
  public static final int DEFAULT_COMPONENT_HEIGHT = 500;
  
  private JWindow comp_container;
  
  //Constructor parameters
  private Component comp;
  private String printer_name;
  private boolean page_orientation;
  private int num_copies;
  
  //Components for silent printing
  private HashPrintRequestAttributeSet aset = 
                                    new HashPrintRequestAttributeSet();
  private PrintService pservice;
  private PrinterPage printer_page;
  private DocFlavor doc_format = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
  private Doc doc;
  private Copies numberOfCopies;
  
  /**
   * Creates a new PrintUtilities object.
   * 
   * @param comp - The {@link java.awt.Component} to be printed out.
   * @param printer_name - The name of the output printer.
   * @param page_orientation - The {@link java.awt.Component}'s orientation on 
   *                           the page.  Use <code>true</code> for the 
   *                           Portrait orientation and <code>false</code> for 
   *                           the Landscape orientation.
   * @param num_copies - The number of copies to be printed out.
   */
  public PrintUtilities2( Component comp, String printer_name, 
      boolean page_orientation, int num_copies) 
  {
    this.comp = comp;
    this.printer_name = printer_name;
    this.page_orientation = page_orientation;
    this.num_copies = num_copies;
    
    validate_size(this.comp);
    init_component_container();
    init_attributes();
  }
  
  /**
   * Creates a new PrintUtilities object.
   * 
   * @param comp - The {@link java.awt.Component} to be printed out.
   * @param printer_name - The name of the output printer.
   * @param aset - This holds all of the requested printing attributes,
   *               such as: the number of copies desired, the output page 
   *               orientation, etc. 
   */
  public PrintUtilities2(Component comp, String printer_name, 
      HashPrintRequestAttributeSet aset)
  {
    this.comp = comp;
    this.printer_name = printer_name;
    this.aset = aset;

    validate_size(this.comp);
    init_component_container();
  }
  
  /**
   * Creates a new PrintUtilities object.  This constructor is mostly intended 
   * to be used when the print dialog is called.  It doesn't set many 
   * attributes or variables since they will be specified in the dialog.
   * 
   * @param comp - The {@link java.awt.Component} to be printed out.
   */
  private PrintUtilities2(Component comp)
  {
    this.comp = comp;
    this.printer_name = "";

    validate_size(this.comp);
    init_component_container();
  }
  
  /**
   * This static method allows a {@link java.awt.Component} to be printed out 
   * from a static context.
   * 
   * @param comp - The {@link java.awt.Component} to be printed out.
   * @param printer_name - The name of the output printer.
   * @param page_orientation - The {@link java.awt.Component}s orientation on
   *                           the page.  Use <code>true</code> for the 
   *                           Portrait orientation and <code>false</code> for 
   *                           the Landscape orientation.
   * @param num_copies - The number of copies to be printed.
   */
  public static void print(Component comp, String printer_name, 
      boolean page_orientation, int num_copies)
  {
    PrintUtilities2 pu = new PrintUtilities2(comp, printer_name, 
        page_orientation, num_copies);
    pu.print();
  }
  
  /**
   * This static method allows a {@link java.awt.Component} to be printed out 
   * from a static context.
   * 
   * @param comp - The {@link java.awt.Component} to be printed out.
   * @param printer_name - The name of the output printer.
   * @param aset - This holds all of the requested printing attributes,
   *               such as: the number of copies desired, the output page 
   *               orientation, etc.
   */
  public static void print(Component comp, String printer_name, 
      HashPrintRequestAttributeSet aset)
  {
    PrintUtilities2 pu = new PrintUtilities2(comp, printer_name, aset);
    pu.print();
  }
  
  /**
   * This static method allows a {@link java.awt.Component} to be printed out 
   * from a static context.  This method will show a printer dialog so that 
   * the user may choose their desired attributes through a GUI.
   * 
   * @param comp - The {@link java.awt.Component} to be printed out.
   */
  public static void print_with_dialog(Component comp)
  {
    PrintUtilities2 pu = new PrintUtilities2(comp);
    pu.print_with_dialog();
  }
  
  /**
   * This method prints the {@link java.awt.Component} specified in the 
   * Constructor.
   */
  public void print()
  {
    init_silent_print();
    silent_print();
  }
  
  /**
   * This method shows a printer dialog so that the user may choose their 
   * desired attributes through a GUI, then prints the 
   * {@link java.awt.Component} specified in the constructor.
   */
  public void print_with_dialog()
  {
    dialog_print();
  }
  
  /**
   * This method sets the name of the output printer.
   * 
   * @param printer_name - The name of the printer.
   */
  public void set_printer_name(String printer_name)
  {
    this.printer_name = printer_name;
  }
  
  /*
   * This method initializes the attributes for the 
   * HashPrintRequestAttributeSet.
   */
  private void init_attributes()
  {
    //setting attributes hash table
    aset = new HashPrintRequestAttributeSet();
    
    //setting page orientation
    if (page_orientation == true) //true for portrait
      aset.add(OrientationRequested.PORTRAIT);
    else if(page_orientation == false)
      aset.add(OrientationRequested.LANDSCAPE);
    
    //setting number of copies to print
    numberOfCopies = new Copies(num_copies);
    aset.add(numberOfCopies);
  }
  
  /*
   * This method initializes the container for the specified Component .
   */
  private void init_component_container()
  {
    //setting up the JComponent container
    comp_container = new JWindow();
    comp_container.addWindowListener(new FinishWindowListener());
    comp_container.setSize(comp.getWidth(),comp.getHeight());
    //jc_container.getContentPane().setLayout( new GridLayout(1,1));
    comp_container.getContentPane().add(this.comp);
    comp_container.pack();
    comp_container.validate();
  }
  
  /*
   * This method initializes the printer service and the SimpleDoc for the 
   * silent printing method.
   */
  private void init_silent_print()
  {          
    //setting up printer service 
    //pservice = get_print_service1(this.printer_name);
    
    printer_page = new PrinterPage(comp);

    try
    {
      doc = new SimpleDoc(printer_page, doc_format, null);
    }
    catch(Throwable u)
    {
      System.err.println("Error creating Doc. ");
      u.printStackTrace();
    }
  }
  
 
  
  
  /*
   * This method silently prints the Component with the specified attributes.
   */
  private void silent_print() 
  {
    pservice = get_print_service1(  printer_name,aset);
    
    if( pservice == null){
       javax.swing.JOptionPane.showMessageDialog( null,"Cannot Find Printer");
       return;
    }
    
   
    DocPrintJob printJob = pservice.createPrintJob();
    Attribute[] array = printJob.getAttributes().toArray();
    
    //TODO: Instead of just hiding the JWindow container off-screen, try to 
    //      find a way to make it invisible and still have the contained 
    //      Component actually printed out.
    /*comp_container.setBounds(
        Toolkit.getDefaultToolkit().getScreenSize().width + 1,
        Toolkit.getDefaultToolkit().getScreenSize().height + 1,
        comp_container.getWidth(),
        comp_container.getHeight());
    comp_container.setVisible(true);
    */
   /* try 
    {    
      printJob.print(doc, aset);       
    } 
    catch (Exception pe) 
    {
      System.err.println( pe.toString() );
      pe.printStackTrace();
    }
    comp_container.dispose();
    */
    if(printJob != null)
        javax.swing.SwingUtilities.invokeLater( new printJb( doc, aset,printJob) );
    else
       javax.swing.JOptionPane.showMessageDialog( null,"Cannot Create PrintJob");
    
  }
  
 
  /*
   * This method brings up the printer dialog and prints the Component.
   */
  private void dialog_print()
  {
    PrinterJob job = PrinterJob.getPrinterJob();
    job.setPrintable(new PrinterPage(comp));
    
    //setting up dialog default printer
    if(printer_name != null && !(printer_name.trim().equals("")))
    {
      try
      {
        job.setPrintService(get_print_service1(printer_name, aset));
      }
      catch(PrinterException pe)
      {
        System.err.println(
              "Could not set dialog default printer \'"+printer_name+"\' .");
        pe.printStackTrace();
      }
    }
    
    //setting up dialog with default attributes
    boolean do_print = job.printDialog(aset);    
    if (do_print) 
    {
      //TODO: Instead of just hiding the JWindow container off-screen, try to 
      //      find a way to make it invisible and still have the contained 
      //      Component actually printed out.      
     /* comp_container.setBounds( 
                      Toolkit.getDefaultToolkit().getScreenSize().width + 1,
                      Toolkit.getDefaultToolkit().getScreenSize().height + 1,
                      comp_container.getWidth(),
                      comp_container.getHeight());
     comp_container.setVisible(true); 
     */
      try
      {
        job.print();
      }
      catch (PrinterException ex) 
      {
        System.err.println("Could not print to printer.");
        ex.printStackTrace();
      }
      comp_container.dispose();
    }
  }
  
  /**
   * This inner class defines how the Component will be displayed on the page.
   */
  private class PrinterPage implements Printable
  {
    private Component comp_to_print;
    
    public PrinterPage(Component comp)
    {
      this.comp_to_print = comp;
      
    }
    
    public int print(Graphics g, PageFormat page_format, int pageIndex) 
                                                     throws PrinterException 
    {
            
      if (pageIndex > 0)
       {
         return(NO_SUCH_PAGE);
       }
       else 
       {
         Graphics2D g2d = (Graphics2D)g;          
         
         Rectangle R = comp_to_print.getBounds();

         double xscale=(double)(page_format.getImageableWidth() )/(R.width);
         double yscale=(double)(page_format.getImageableHeight())/(R.height);
        // xscale = yscale =1;
         if(yscale < xscale) 
            xscale= yscale; 
     
         g2d.translate(page_format.getImageableX(), 
                       page_format.getImageableY());
  
         g2d.scale(xscale ,xscale); 

         double w = R.width*xscale;
         double h = R.height*xscale;
         
         if ( w > page_format.getImageableWidth() ) 
            w = page_format.getImageableWidth();
         if ( h > page_format.getImageableHeight() ) 
            h = page_format.getImageableHeight();         
         
         disableDoubleBuffering(comp_to_print);         
         comp_to_print.paint(g2d);       
         enableDoubleBuffering(comp_to_print);
         
         return(PAGE_EXISTS);
       }
    }
    
    //disables double-buffering for the Component
    public void disableDoubleBuffering(Component c) 
    {
      RepaintManager currentManager = RepaintManager.currentManager(c);
      currentManager.setDoubleBufferingEnabled(false);
    }
    
    //enables double-buffering for the Component
    public void enableDoubleBuffering(Component c) 
    {
      RepaintManager currentManager = RepaintManager.currentManager(c);
      currentManager.setDoubleBufferingEnabled(true);
    }
  }
  
  
 
  
  /*
   * This method retrieves the printer service from the specified priner name and
   * attributes. 
   *  
   * @param printerName  the name of the printer
   * 
   * @param aset  the set of attributes
   * 
   * @return a PrintService for a corresponding printer with the given name
   *             and can satisfy all the attributs that were set
   */
  public static PrintService get_print_service( String printerName,
           HashPrintRequestAttributeSet aset) {
     
     PrintService[] pservices = PrintServiceLookup.lookupPrintServices(
              DocFlavor.SERVICE_FORMATTED.PRINTABLE,aset); 
     
     if( pservices == null)
        return null;
     
     for( int i=0; i<pservices.length;i++){
        
        if(pservices[i] != null &&pservices[i].toString() != null &&
          pservices[i].getName().trim().equals( printerName.trim()))
           return pservices[i];
     }
     
     return null;
  }
  
  //uses aset first.  If there is no printer it uses null
  private  PrintService get_print_service1( String printerName,
           HashPrintRequestAttributeSet aset) {
     
     PrintService P = get_print_service( printerName, aset);
     if( P == null)
        return get_print_service( printerName, null);
     return P;
     
  }
  
  /*
   * This method makes sure that the specified Component's size is greater 
   * than zero.  
   */
  private static void validate_size(Component comp)
  {
    int width = comp.getWidth();
    int height = comp.getHeight();
    
    //Making sure the Component size is at least greater than 0.
    if(width <=0 && height <= 0) 
    {
      
      width = DEFAULT_COMPONENT_WIDTH;
      height = DEFAULT_COMPONENT_HEIGHT;
      comp.setSize(width,height);
    }
    
    //This ensures that the Component will be the specified size.
    comp.setMinimumSize(new Dimension(width, height));
    comp.setPreferredSize(new Dimension(width, height));
  }
    
  /*
  public static void main(String args[]) 
  { 
    String type = "Contour";
    
    IVirtualArray2D va2D = ContourViewComponent.getTestData(51, 51, 3.0, 4.0);
    va2D.setAxisInfo( AxisInfo.X_AXIS, 0f, 10000f, 
           "TestX","TestUnits", AxisInfo.LINEAR );
    va2D.setAxisInfo( AxisInfo.Y_AXIS, 0f, 1500f, 
           "TestY","TestYUnits", AxisInfo.LINEAR );
    va2D.setAxisInfo( AxisInfo.Z_AXIS, 0f, 1f, "Z", "Units",
               AxisInfo.PSEUDO_LOG );
    va2D.setTitle("ScreenDevice Test");

    VirtualArray2D_Displayable va2d_disp = 
                                  new VirtualArray2D_Displayable( va2D, type);
    JComponent jcomp = va2d_disp.getJComponent(false);
    jcomp.setSize(300,600);

    HashPrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
    aset.add(OrientationRequested.PORTRAIT);
    aset.add(new Copies(2));
    
    
    //PrintUtilities2 pu=new PrintUtilities2(
    //                va2d_disp.getJComponent(false),"hp4000_A140",aset);
    //PrintUtilities2 pu = new PrintUtilities2(va2d_disp.getJComponent(false),
    //                                       "hp4000_A140",false, 2);
    
    //pu.print();
    //pu.print_with_dialog();

    //PrintUtilities2.print(va2d_disp.getJComponent(false),"hp4000_A140",aset);
    
    PrintUtilities2.print(jcomp,"hp4000_A140",false,1);
    
    //PrintUtilities2.print_with_dialog(va2d_disp.getJComponent(false));
  }//*/
}
/**
 * Placed the code to print the document on the event queue so it is executed
 * after the JWindow is through painting.
 * 
 * @author Ruth
 *
 */
class printJb implements Runnable{
   Doc doc;
   HashPrintRequestAttributeSet aset;
   DocPrintJob printJob;
   
   public printJb( Doc doc, HashPrintRequestAttributeSet aset,
            DocPrintJob printJob){
      this.doc = doc;
      this.aset = aset;
      this.printJob = printJob;
   }
   
   public void run(){
      try 
      {    
        printJob.print(doc, aset);       
      } 
      catch (Exception pe) 
      {
        System.err.println( pe.toString() );
        pe.printStackTrace();
      } 
      doc = null;
      aset = null;
      printJob = null;
   }
}
/**
 * Placed the code to dispose of the JWindow on the event queue so it is executed
 * after the printing is executed.
 * 
 * @author Ruth
 *
 */
class dspose implements Runnable{
   
   JWindow wind;
   public dspose( JWindow wind ){
      
      this.wind = wind;
   }
   
   public void run(){
      if( wind != null)
      wind.dispose();
      wind = null;
   }
}
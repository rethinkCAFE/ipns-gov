/* 
 * File: VirtualArray2D_Displayable.java 
 *  
 * Copyright (C) 2007     Ruth Mikkelson
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
 * Contact :  Ruth Mikkelson<mikkelsonr@uwstout.edu>
 *            MSCS Department
 *            Menomonie, WI. 54751
 *            (715)-235-8482
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.8  2007/07/20 01:54:41  dennis
 * Fixed typo (double assignment to comp).
 *
 * Revision 1.7  2007/07/18 15:12:27  rmikk
 * Added GPL and public static strings for the view type
 *
 */
package gov.anl.ipns.DisplayDevices;

import java.awt.Color;

import gov.anl.ipns.ViewTools.Components.*;
import gov.anl.ipns.ViewTools.Components.TwoD.*;
import gov.anl.ipns.ViewTools.Components.TwoD.Contour.*;
import gov.anl.ipns.ViewTools.Layouts.*;
import javax.swing.*;
import java.awt.*;
import gov.anl.ipns.ViewTools.Displays.*;

public class VirtualArray2D_Displayable  extends Displayable {

   
   public static final String  TABLE = "Table";
   public static final String  IMAGE = "Image";
   public static final String  CONTOUR = "Contour";
   
   IVirtualArray2D array;
   String Type ;
   Display2D comp;
   java.util.Hashtable XlateAttrNames;
   
   ObjectState Ostate;
   /**
    *  Produces a displayable
    *  
    * @param array  The virtual array of data to view
    * @param Type   The type of view. It must be ImageV2D, TableV2D,and
    *                     ContourV2D so far.
    *                 
    * NOTE:  ComponentSwapper is not implemented
    */
   public VirtualArray2D_Displayable( IVirtualArray2D array, String Type)
                  throws IllegalArgumentException {

      super();
      this.array= array;
      this.Type = Type;
      XlateAttrNames = new java.util.Hashtable();
      if( array == null)
         throw new IllegalArgumentException( " No array of values");
      
      if( Type == null)
         throw new IllegalArgumentException( " No Type for view");
      
      if( ".ImageV2D.TableV2D.ContourV2D.".indexOf("."+Type+".") <  0 )
         throw new IllegalArgumentException( " Improper View Type");
      
      if( Type .equals( IMAGE)) {
         

         comp = new Display2D( array , Display2D.IMAGE, 1 );
         Ostate = comp.getObjectState( true);
         
         XlateAttrNames.put( "ColorModel", "Color Scale" );
         XlateAttrNames.put( "Axes Displayed" , "View Component0.AxisOverlay2D.Axes Displayed");
         XlateAttrNames.put("intensity", "View Component0.Log Scale Slider.Slider Value");
         XlateAttrNames.put("xxx", "View Component0.Axis Control.Unselected Color");
      
      }else if( Type.equals( TABLE)){
         
         comp = new Display2D( array , Display2D.TABLE, 1 );;
         Ostate = comp.getObjectState( true);
         //XlateAttrNames =
      
      }else if( Type.equals( CONTOUR )) {   
         
         comp = new Display2D( array , Display2D.CONTOUR, 1 );;
         Ostate =comp.getObjectState( true);
         //XlateAttrNames =
      }else
         Ostate = new ObjectState();
      
      
      showOstate( Ostate,1);
    
      
   }
   
   public void showOstate(){
       showOstate( Ostate,1);
   }
   public void showOstate(ObjectState Ostate,  int nspaces ){
      
      if( Ostate == null)
         return;
       
       java.util.Enumeration En = Ostate.getKeys();
       Object e = En.nextElement();
       for( ; En.hasMoreElements();  e = En.nextElement()){
          Object st = Ostate.get( e );
          if( st instanceof ObjectState){
             for( int i=0; i<nspaces +2; i++) System.out.print(" ");
             System.out.println("**"+ e+"**" );
             showOstate((ObjectState)st, nspaces+4 );
          }else {
             for( int j=0; j< nspaces ; j++)System.out.print(" ");
             System.out.println( e+":::"+ st);
          }
       }
       
   }
   
   /**
    *  This method sets an attribute of the displayable that pertains
    *  to the overall display, such as a background color.
    *
    *  @param  name     The name of the attribute being set.
    *  @param  value    The value to use for the attribute.
    */
   public void  setViewAttribute(String name, Object value){
      
      if( name == null)
         return;

          
      if( value == null)
         return;
    
      String S =  (String)XlateAttrNames.get(name);
      
      Object DT = Ostate.get( S );
      
      Object Oval = null;
      
      if( ObjectState.INVALID_PATH ==( DT))
         DT = null;
      
      if( DT != null)
      try{
         
          Oval =Util.cvrt( DT.getClass(), value);
          
      }catch( Exception s){
         
         Oval = null;
      }
      
      if( Oval == null)
         Ostate.insert( S, Oval) ;
      
      else if(! Ostate.reset( S, Oval))
         if( !Ostate.insert( S, Oval))
          System.out.println("Could not make the change")  ;
   }
   
   
   
   /**
    *  This method sets an attribute of the displayable that pertains
    *  to a particular portion of the display, such as one particular
    *  line. 
    *
    *  @param  index    An index identifying the part of the display
    *                   that the attribute applies to, such as a 
    *                   specific line number.
    *  @param  name     The name of the attribute being set.
    *  @param  value    The value to use for the attribute.
    */
   public void setLineAttribute(int index,  String name, Object value){
      
      
      if( name == null)
         return;

     
      
      if( value == null)
         return;
      
      String S ;
      if( index >=10){
         
         S = (String) XlateAttrNames.get("Line_"+name+"__"+7);
         int k = S.indexOf("7");
         S = S.substring(0,k)+index+S.substring(k+1);
         
      }else
         S =(String) XlateAttrNames.get("Line_"+name+"__"+index);
      
      Object DT = Ostate.get( S );
 
      if( ObjectState.INVALID_PATH ==( DT))
         DT = null;
      
      Object Oval = null;
      
      if( DT != null)
      try{
         
          Oval =Util.cvrt(DT.getClass(), value);
          
      }catch( Exception s){
         
         Oval = null;
      }
      
      if( Oval == null)
         Ostate.insert( S, value );
      else if(!Ostate.reset( S , value))
         Ostate.insert( S, value );
      
   }

   
   
   
   /**
    *  This method returns a JComponent that can be displayed in a Frame,
    *  printed, or saved to a file.
    *
    *  @param  with_controls   If this is false, any interactive controls
    *                          associated with the view of the data will
    *                          NOT be visible on the JComponent
    *
    *  @return A reference to a JComponent containing the configured 
    *          display.
    */
   public JComponent getJComponent( boolean live){
       
      if( !live )
         Ostate.reset( Display2D.CONTROL_OPTION, Display.CTRL_NONE);
      else
         Ostate.reset(Display2D.CONTROL_OPTION, Display.CTRL_ALL );
      
      comp.setObjectState( Ostate );
      
      
      if( live )
         return comp.getRootPane();
      else
         return (JComponent)comp.getContentPane();
      
     
   }
   
 
   /**
    * @param args
    */
   public static void main( String[] args ) throws Exception{
      String Type = "ImageV2D";
      VirtualArray2D v2d = new VirtualArray2D( 
               new float[][]{
                        {  1,1,1,1,1,1,1,1,1},
                        {  2,2,2,2,2,2,2,2,2 },
                        {  3,3,3,3,3,3,3,3,3},
                        {  4,4,4,4,4,4,4,4,4},
                        {  5,5,5,5,5,5,5,5,5},
                        {  6,6,6,6,6,6,6,6,6}
                        
               });
      VirtualArray2D_Displayable disp =  new VirtualArray2D_Displayable( v2d, "ImageV2D");
      
      disp.setViewAttribute("ColorModel", "Rainbow");
      disp.setViewAttribute("Axes Displayed", new Integer(2));
      disp.setViewAttribute( "intensity", new Integer(95));
      disp.setViewAttribute( "xxx", "Red");
      JFrame jf = new JFrame("test");
      jf.getContentPane().setLayout( new GridLayout(1,1));
      jf.setSize( 400,400);
      jf.getContentPane().add( disp.getJComponent( true ));
      jf.show();
      System.out.println("============================");
      disp.showOstate();

   }

  public void setLineAttribute(int index, String name, String value) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  public void setViewAttribute(String name, String value) throws Exception
  {
    // TODO Auto-generated method stub
    
  }

  
}

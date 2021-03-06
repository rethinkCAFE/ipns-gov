/*
 * File:  ThreeD_GL_Panel.java
 *
 * Copyright (C) 2004, Dennis Mikkelson
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log: ThreeD_GL_Panel.java,v $
 * Revision 1.21  2008/02/18 20:00:14  dennis
 * Added workaround for a bug in some OpenGL implementations that
 * causes the screen to blank after rendering in selection mode.
 *
 * Revision 1.20  2007/08/13 23:50:17  dennis
 * Switched from old JOGL to the JSR231 version of JOGL.
 *
 * Revision 1.19  2005/07/21 13:20:54  dennis
 * Removed dependency on DataSetTools by:
 * 1. Now gets PixelDepthScale property directly from System
 *    properties, instead of from SharedData.getProperty()
 * 2. Now sends messages using gov's  SharedMessages.addmsg()
 *    rather than SharedData.
 *
 * Revision 1.18  2005/05/25 20:28:47  dennis
 * Now calls convenience method WindowShower.show() to show
 * the window, instead of instantiating a WindowShower object
 * and adding it to the event queue.
 *
 * Revision 1.17  2005/03/06 23:32:38  dennis
 * Added a pixel_depth_scale_factor that can be set from
 * IsawProps.dat to allow the user to specify a non-standard
 * scale factor for interpreting pixel depths.  This provides
 * a workaround for the problem of incorrect pixel depths using
 * the ATI proprietary OpenGL driver.  Such cards require setting
 * "PixelDepthScale=256" in IsawProps.dat to get proper depth
 * values.  Other cards, or the Mesa OpenGL driver don't need
 * anything set, or a scale value of 1 can be set.
 *
 * Revision 1.16  2004/09/16 18:40:15  dennis
 * Fixed one javadoc error.
 *
 * Revision 1.15  2004/08/04 23:06:11  dennis
 * No longer includes empty hit records in list of hit records.
 * Now controls swapping the back buffer: setAutoSwapBufferMode(false)
 * so buffers are not swapped when rendering for GL Select mode.
 * Put some more debug prints in if (debug) statements.
 * Removed some redundant calls to set glMatrixMode(), etc.
 *
 * Revision 1.14  2004/08/04 16:04:43  dennis
 * Added main program to test basic functionality.
 *
 * Revision 1.13  2004/08/03 23:40:36  dennis
 * Made all class level variables private.
 * Commented out mouse event handler used for debugging.
 * Renamed getPixelWorldCoordinates() to pickedWorldCoordinates()
 * for consistency.
 * Removed a couple of unused variables.
 *
 * Revision 1.12  2004/07/28 15:48:55  dennis
 * Decreased default light intensity to 0.7, instead of 1.  Made LIGHT0
 * also contribute to the ambient light.
 *
 * Revision 1.11  2004/07/23 13:06:21  dennis
 * Commented out explicit request for double buffer.
 *
 * Revision 1.10  2004/07/16 14:54:14  dennis
 * Now uses HitRecords to keep track of OpenGL selection information.
 * Stubs for methods pickedPoint() and pickID() to get 3D coordinates
 * and the integer name (ie. ID) of a selected object are now
 * implemented.
 *
 * Revision 1.9  2004/07/14 16:41:05  dennis
 * Changed return type on pickedObject() to be Gl_Shape, rather than
 * IThreeD_GL_Object, since only actual shapes can be picked.
 * Added method pickedPoint() that will return the 3D coordinates of
 * a point.
 * These methods are currently just "stubs".
 *
 * Revision 1.8  2004/06/18 19:57:41  dennis
 * Imports newly created subpackage Shapes
 *
 * Revision 1.7  2004/06/17 19:23:14  dennis
 * Code to locate a point in 3D now pushes the projection matrix stack,
 * computes the projection matrix and pops the projection matrix
 * stack when it's done.  This makes sure that the projection matrix
 * used for calculating the 3D coordinates is the same as is currently
 * specified for the 3D display (not one used in the selection process.)
 *
 * Revision 1.6  2004/06/17 15:32:28  dennis
 * Added initial implementation of code to locate 3D world coordinate
 * point given a pixel position.  Currently this only works the second
 * time a point is selected.  Also this currently contains many debug
 * prints.
 *
 * Revision 1.5  2004/06/15 22:14:41  dennis
 * Now uses ported versions of gluLookAt(), gluPerspective() and
 * gluPickMatrix() from the BasicGLU class, instead of the actual
 * GLU functions.  This was needed since the GLU provided on some
 * systems did not work with jogl.
 *
 * Revision 1.4  2004/06/15 16:46:20  dennis
 * Added local implementations of gluLookAt and gluPerspective, since
 * the Mesa GLU crashed with jogl.
 *
 * Revision 1.3  2004/06/04 14:05:58  dennis
 * Added method to control whether a perspective or orthographic
 * projection is used.
 *
 * Revision 1.2  2004/05/30 04:31:07  dennis
 * Commented out calls to canvas.setRenderingThread() which prevented
 * test program from running on Compaq Evo N1000c running Windows XP
 * with ATI Mobility Radeon 7500 graphics.  More tests needed.
 *
 * Revision 1.1  2004/05/28 20:51:18  dennis
 * Initial (test) version of classes for displaying and picking
 * 3D objects using OpenGL from Java, built on the "jogl" system.
 *
 */

package gov.anl.ipns.ViewTools.Panels.GL_ThreeD;

import java.awt.*;
import java.awt.event.*;  
import javax.swing.*;
import java.io.*;
import java.nio.*;
import java.util.*;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.ViewTools.Panels.GL_ThreeD.Shapes.*;
import gov.anl.ipns.ViewTools.Panels.GL_ThreeD.ViewControls.*;
import gov.anl.ipns.Util.Sys.*;

import javax.media.opengl.*;
import com.sun.opengl.util.*;


/**
 *  A ThreeD_GL_Panel object maintains and draws a list of ThreeD_GL_Objects,
 *  such as shapes, lights, transforms and groups of objects. 
 */ 

public class ThreeD_GL_Panel implements Serializable
{
  public  static final String PIXEL_DEPTH_SCALE_PROPERTY = 
                              "PixelDepthScale";

  private static boolean debug = false;

  private static float   pixel_depth_scale_factor = 1;  // needed to workaround
                                                        // quirk with ATI cards
  private Vector         old_list_ids = null;

  private GLCanvas       canvas;

  private Color          background = Color.BLACK;

  private Hashtable      obj_lists     = null;  // Hastable storing, object[]
                                                // lists referenced by name

  private IThreeD_GL_Object picked_object = null;   // last object picked

  private IThreeD_GL_Object all_objects[] = null;   // array of all current 
                                                    // objects
  private volatile boolean obj_lists_valid = false;

  private Vector3D  cop, 
                    vrp, 
                    vuv;
  private float     view_angle = 40;
  private float     near_plane = 0.5f;
  private float     far_plane  = 200;

  private boolean  use_perspective_proj = true;

  private boolean  do_select = false;
  private boolean  do_locate = false;
  private Vector3D last_picked_point = new Vector3D();

  private final int HIT_BUFFER_SIZE = 512;
  private int n_hits = 0;
  private IntBuffer hit_buffer = BufferUtil.newIntBuffer( HIT_BUFFER_SIZE );

  private int cur_x,                              // x,y pixel coords of point
              cur_y;                              // used for picking and 
                                                  // locating objects 
  private float world_coords[] = new float[3];


/* --------------------- Default Constructor ------------------------------ */
/**
 *  Construct a default ThreeD_GL_Panel with an empty list of ThreeD objects.
 */
  public ThreeD_GL_Panel()
  { 
    cop = new Vector3D( 10, 10, 10 );
    vrp = new Vector3D(  0,  0,  0 );
    vuv = new Vector3D(  0,  0,  1 );

    try
    {
      GLCapabilities capabilities = new GLCapabilities();
//      capabilities.setDoubleBuffered( true );
//      capabilities.setStereo( true );

      if ( debug )
        System.out.println("capabilites = " + capabilities );

      canvas = new GLCanvas(capabilities);
//      canvas.setAutoSwapBufferMode(false);
      canvas.addGLEventListener(new Renderer());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    obj_lists = new Hashtable();
    old_list_ids = new Vector();

    if ( debug )
      canvas.addMouseListener( new MouseClickHandler() );

    String depth_scale_prop = 
                            System.getProperty(PIXEL_DEPTH_SCALE_PROPERTY);
    if ( depth_scale_prop != null )
    try
    {  
      depth_scale_prop.trim();
      Double scale_d = new Double( depth_scale_prop );
      float  scale_f = scale_d.floatValue();
      if ( scale_f > 0 )
      {
        pixel_depth_scale_factor = scale_f; 
        SharedMessages.addmsg("Using non-standard " +PIXEL_DEPTH_SCALE_PROPERTY+
                              ": " + pixel_depth_scale_factor + 
                              " from System properties" );
      }
    }
    catch ( NumberFormatException e )
    {
      SharedMessages.addmsg( "Warning: invalid number " + depth_scale_prop + 
                             " in " + PIXEL_DEPTH_SCALE_PROPERTY );
    }
  }


/* ------------------------- SetBackground ---------------------------- */
/**
 *  Set the background color for this object.
 */
  public void SetBackground( Color new_background )
  {
    background = new_background;
  }


/* ------------------------ ChangeOldLists  ------------------------------ */
/**
 *  This method maintains a list of OpenGL display list IDs that are no
 *  longer used.  When a ThreeD_GL_Object that used a display list no longer
 *  needs that list, it should pass the list_id to this method.  Each time
 *  the display is redrawn, the list will be emptied and the display lists
 *  freed.  Since it is synchronized, this method should be safe to call
 *  from any thread.  The OpenGL rendering thread will call this method with
 *  the code GL_Shape.INVALID_LIST_ID to get the list IDs to free.  Other
 *  threads should not call it with the code GL_Shape.INVALID_LIST_ID.
 *  Typically, shapes will call this method from their finalize method,
 *  to free their display list id.
 *
 *  @param code  The integer code for the OpenGL display list.  This should
 *               NOT be the value GL_Shape.INVALID_LIST_ID, unless the calling
 *               code is running in the OpenGL rendering thread and will 
 *               free the list ids.
 */
  synchronized public int[] ChangeOldLists( int code )
  {
    if ( code == GL_Shape.INVALID_LIST_ID )
    {
       int result[] = new int[ old_list_ids.size() ];
       for ( int i = 0; i < result.length; i++ )
         result[i] = ((Integer)old_list_ids.elementAt(i)).intValue();
       old_list_ids.removeAllElements();
       return result;
    } 
    else
    {
       old_list_ids.add( new Integer(code) );
       return null;
    }
  }


/* ----------------------------- setObject ----------------------------- */
/**
 *  Set a named ThreeD object in the list of objects to be 
 *  handled by this panel, if the name is already used, the new object
 *  will replace the old objects.
 *
 *  NOTE: The application must call Draw() to show the new list of objects.
 *
 *  @param  name  String identifer the object being set for this panel.
 *  @param  obj   ThreeD_GL_Object to be set for this panel.
 */
 public void setObject( String name, IThreeD_GL_Object obj )
 {
                                                     // ignore degenerate cases
   if ( name == null || obj == null )
     return;

   IThreeD_GL_Object new_obj[] = new IThreeD_GL_Object[1];
   new_obj[0] = obj;

   setObjects( name, new_obj );
 }


/* ----------------------------- setObjects ----------------------------- */
/**
 *  Set a named list of ThreeD objects in the list of objects to be 
 *  handled by this panel, if the name is already used, the new objects
 *  will replace the old objects.
 *
 *  NOTE: The application must call repaint() or request_painting() to show
 *        the new list of objects.
 *
 *  @param  name  String identifer the array of objects being set for 
 *                this panel.
 *  @param  obj   Array of ThreeD objects to be set for this panel.
 */
 synchronized public void setObjects( String name, IThreeD_GL_Object obj[] )
 {
                                                     // ignore degenerate cases
   if ( name == null || obj == null )  
     return;

   if ( obj.length <= 0 )
   {
     removeObjects( name );
     return;
   }
                                                     // free any objects with
                                                     // this name
   IThreeD_GL_Object old_obj[] = (IThreeD_GL_Object[])obj_lists.get(name);
   if ( old_obj != null )
     for ( int i = 0; i < old_obj.length; i++ )
       old_obj[i].clearList();
                                                     // make new list of 
                                                     // objects for this name
   IThreeD_GL_Object new_obj[] = new IThreeD_GL_Object[ obj.length ];
   for ( int i = 0; i < obj.length; i++ )
     new_obj[i] = obj[i];

   obj_lists.put( name, new_obj );
   obj_lists_valid = false; 
 }


/* ----------------------------- getObjects ----------------------------- */
/**
 *  Get a named list of ThreeD objects from the list of objects to be
 *  handled by this panel.  If the named set of objects is not in the list,
 *  this returns null.
 *
 *  @param  name  String identifer the array of objects being requested from
 *                this panel.
 *
 *  @return  Array of ThreeD objects or null if the named objects don't
 *           exit.
 */
 private IThreeD_GL_Object[] getObjects( String name )
 {                                                // ignore degenerate cases
   if ( name == null )
     return null;

   IThreeD_GL_Object objects[] = (IThreeD_GL_Object[])obj_lists.get(name);
   if ( objects == null || objects.length == 0 )
     return objects; 
                                                  // if there is a valid list
                                                  // return a copy of it
   IThreeD_GL_Object result[] = new IThreeD_GL_Object[ objects.length ];
   System.arraycopy( objects, 0, result, 0, objects.length );
   return result;
 }


/* ----------------------------- getAllObjects ----------------------------- */
/**
 *  Get list of all of the ThreeD objects handled by this panel.  
 *
 *  @return  Array of ThreeD objects or null if no objects have been added
 *           to the panel. 
 */
 private IThreeD_GL_Object[] getAllObjects()
 {
   IThreeD_GL_Object result[] = null;

   if ( !obj_lists_valid )
     build_object_list();

   if ( obj_lists_valid && all_objects != null )
   {
     result = new IThreeD_GL_Object[ all_objects.length ];
     System.arraycopy( all_objects, 0, result, 0, result.length );
     return result;
   }
   else
     return null;   
 }


/* ----------------------------- removeObjects ----------------------------- */
/**
 *  Remove a named list of ThreeD objects from the objects to be handled by
 *  this panel.  If the named list is not present, this method has no
 *  effect.
 *
 *  NOTE: The application must call repaint() or request_painting() to show
 *        the new list of objects.
 *
 *  @param  name  Unique string identifer to be used for the new array
 *                of objects being removed from this panel
 */
 synchronized public void removeObjects( String name )
 {
                                                     // free any objects with
                                                     // this name
   IThreeD_GL_Object old_obj[] = (IThreeD_GL_Object[])obj_lists.get(name);
   if ( old_obj != null )
     for ( int i = 0; i < old_obj.length; i++ )
       old_obj[i].clearList();

   obj_lists.remove( name );
   obj_lists_valid = false; 
 }


/* ----------------------------- removeObjects ---------------------------- */
/**
 *  Remove all of the ThreeD objects from the objects to be handled by
 *  this panel.  
 *
 *  NOTE: The application must call repaint() or request_painting() to 
 *        redraw the panel after removing the objects.
 */
 synchronized public void removeObjects()
 {
   obj_lists.clear();
   obj_lists_valid = false; 
 }


/* --------------------------- getPickHitList ------------------------------ */
/**
 *  Get the OpenGL selection hit list for the specified window coordinates
 *  x,y.  The objects in the list of objects for this panel will be rendered
 *  using a special small viewing volume centered around the specified pixel.
 *  This should only be called from the event handling thread.
 *
 *  @param x  The pixel x (i.e. column) value
 *  @param y  The pixel y (i.e. row) value, in window coordinates.
 */
public HitRecord[] pickHitList( int x, int y )
{
  if ( do_select )             // ignore more requests to do selection
    return new HitRecord[0];   // if currently doing selection

  cur_x = x;
  cur_y = y;

  do_select = true;
  canvas.display();         // this will cause Renderer.display(drawable) to be
                            // called with the correct drawable, GL and thread

  int hits[] = new int[ HIT_BUFFER_SIZE ];
  hit_buffer.get( hits );

  Vector    hit_list = new Vector();
  HitRecord hit_rec;
  int start = 0;
  for ( int i = 0; i < n_hits; i++ )
  {
    hit_rec = new HitRecord( hits, start );
    if ( hit_rec.numNames() > 0 )
      hit_list.add( hit_rec );
    start += hits[ start ] + 3;
  }

  HitRecord hit_recs[] = new HitRecord[ hit_list.size() ];
  for ( int i = 0; i < hit_recs.length; i++ )
    hit_recs[i] = (HitRecord)hit_list.elementAt(i);

  return hit_recs;
}


/* ----------------------------- pickID ----------------------------- */
/*
 *  Return the Pick ID of the object whose projection is closest to
 *  the specified pixel, provided it is within the specified pick radius.
 *  NOTE: Currently the pick_radius parameter is ignored, so picking 
 *  must be "exact".
 *
 *  @param  x            The x coordinate of the specified pixel
 *  @param  y            The y coordinate of the specified pixel
 *  @param  pick_radius  Objects that are further away from the specified
 *                       point than the pick_radius are ignored.
 *
 *  @return  The Pick ID of the first object found that is closest to 
 *           pixel (x,y), provided it is within the pick_radius.  
 *           If no such object is found, this returns INVALID_PICK_ID. 
 */
 public int pickID( int x, int y, int pick_radius )
 {
   HitRecord hitlist[] = pickHitList( x, y );

   if ( debug )
   {
      System.out.println( "pickID() method, length = " + hitlist.length );
      System.out.println( "x,y = " + x + ", " + y );
      for ( int i = 0; i < hitlist.length; i++ )
         System.out.println("hit = " + hitlist[i] );
   }

   if ( hitlist.length <= 0 )
     return GL_Shape.INVALID_PICK_ID;

   int min_distance = Integer.MAX_VALUE;
   for ( int i = 0; i < hitlist.length; i++ )
     if ( hitlist[i].getMin() < min_distance )
       min_distance = hitlist[i].getMin();

   if ( min_distance == Integer.MAX_VALUE )    
     return GL_Shape.INVALID_PICK_ID;

   for ( int i = 0; i < hitlist.length; i++ )
     if ( hitlist[i].getMin() == min_distance )
       return hitlist[i].lastName();

   return GL_Shape.INVALID_PICK_ID;   // this should never be reached, but
 }                                    // the compiler requires it.


/* ---------------------------- pickedObject ----------------------------- */
/*
 *  NOT IMPLEMENTED YET, so it just returns null.
 *  Return a reference to the object that was last picked by a call to
 *  pickID(,,).  The pickID(,,) method must have been previously called and
 *  returned a valid pick ID for this to be valid.  Otherwise it will return
 *  null.
 *
 *  @return  The IThreeD_GL_Object that was last picked by pickID(,,) or null
 *           if the last attempt at picking returned INVALID_PICK_ID.
 *           If no such object is found, this returns INVALID_PICK_ID.
 */
 public GL_Shape pickedObject()
 {
   if ( picked_object instanceof GL_Shape )
     return (GL_Shape)picked_object;
   else
     return null;
 }


/* ---------------------- pickedWorldCoordinates ------------------------ */
/**
 *  Get the world coordinates of the specified point.
 *
 *  @param x  The pixel x (i.e. column) value
 *  @param y  The pixel y (i.e. row) value, in window coordinates.
 */
public float[] pickedWorldCoordinates( int x, int y )
{
  cur_x = x;
  cur_y = y;

  do_locate = true;
  canvas.display();         // this will cause Renderer.display(drawable) to be
                            // called with the correct drawable, GL and thread

  float coords[] = new float[3];
  for ( int i = 0; i < 3; i++ )
    coords[i] = world_coords[i];

  last_picked_point = new Vector3D( coords );
  return coords;
}


/* ---------------------------- pickedPoint ----------------------------- */
/*
 *  Return the point in 3D corresponding to the specified pixel location.
 *
 *  @param   x  The x coordinate of the specified pixel
 *  @param   y  The y coordinate of the specified pixel
 *
 *  @return  A vector corresponding to pixel (x,y)
 */
 public Vector3D pickedPoint( int x, int y )
 {  
   pickedWorldCoordinates( x, y );
   return new Vector3D( last_picked_point );
 }


/* ---------------------------- pickedPoint ----------------------------- */
/*
 *  Return the point in 3D corresponding to the last selected location.
 *
 *  @return  A vector corresponding to the last point in 3D that was selected 
 */
 public Vector3D pickedPoint()
 {
   return new Vector3D( last_picked_point );
 }


/* ---------------------------- setCOP ---------------------------------- */
/**
 *  Set the center of projection (i.e. the viewers position) to use for the
 *  virtual camera.
 *
 *  @param new_cop  The users location.
 */
 public void setCOP( Vector3D new_cop )
 {
   cop = new Vector3D( new_cop );
 }


/* ---------------------------- setVRP ---------------------------------- */
/**
 *  Set the view reference point (i.e. the position the viewer is looking
 *  at) to use for the virtual camera.
 *
 *  @param new_vrp  The users location.
 */
 public void setVRP( Vector3D new_vrp )
 {
   vrp = new Vector3D( new_vrp );
 }


/* ---------------------------- setVUV ---------------------------------- */
/**
 *  Set the view up vector(i.e. the up direction) from the viewer's point
 *  of view, to use for the virtual camera.
 *
 *  @param new_vuv  The users location.
 */

 public void setVUV( Vector3D new_vuv )
 {
   vuv = new Vector3D( new_vuv );
 }


/* ---------------------------- setPerspective ---------------------------- */
/**
 *  Set whether or not to use a perspective projection, instead of an
 *  orthographic projection.
 *
 *  @param  onoff  Flag to indicate whether or not to use perspective
 *                 projection.
 */
  public void setPerspective( boolean onoff )
  {
    use_perspective_proj = onoff;
  }


/* ---------------------------- getDisplayComponent ---------------------- */
/**
 *  Get the actual GLCanvas that this panel draws into.
 *
 *  @return the GL_Canvas for this panel.
 */
 public Component getDisplayComponent()
 {
   return canvas;
 }


/* ------------------------------- Draw ---------------------------------- */
/**
 *  Request that the the panel be cleared and redrawn.  This can be called
 *  from any thread, since it calls repaint() and the drawing will actually be
 *  invoked from the event thread. 
 */
 public void Draw()
 {
   if ( !canvas.isShowing() || !canvas.isValid() )
     return;
   
   float width = canvas.getWidth();
   float height = canvas.getHeight();
   if ( width <= 0 || height <= 0 )
     return;

//   canvas.setRenderingThread( Thread.currentThread() );
//   canvas.repaint();
     canvas.display();
//   canvas.setRenderingThread( null );
 }


/* -------------------------------------------------------------------------
 *
 *  PRIVATE METHODS
 *
 */

/* ------------------------- build_object_list --------------------------- */
/**
 *  Make a single array with references to all of the objects from all of the
 *  named object lists for purposes of depth-sorting, projecting and drawing.
 */
 synchronized private void build_object_list()
 {
   if ( obj_lists_valid )                   // no need to rebuild
     return;

   if ( obj_lists.isEmpty() )               // no more objects, so clean up
   {
     all_objects = null;
     return;
   }

   int n_objects = 0;
   Enumeration e = obj_lists.elements();
   while ( e.hasMoreElements() ) 
     n_objects += ( (IThreeD_GL_Object[])(e.nextElement()) ).length;

   all_objects = new IThreeD_GL_Object[n_objects];
   IThreeD_GL_Object list[];
   int place = 0;
   e = obj_lists.elements();
   while ( e.hasMoreElements() ) 
   {
     list = (IThreeD_GL_Object[])e.nextElement();
     for ( int j = 0; j < list.length; j++ )
     {
       all_objects[ place ] = list[j];
       place++;
     }
   }

   obj_lists_valid = true;
 }


/* -------------------------------------------------------------------------
 *
 *  INTERNAL CLASSES 
 *
 */

  /**
   *  The class Renderer provides the interface to OpenGL through jogl.
   *  It's display() method is called by the system to do the OpenGL drawing.
   */
  public class Renderer implements GLEventListener
  {
    /* ---------------------------- init ----------------------------- */
    /**
     *  Called by the JOGL system when the panel is initialized. 
     *
     *  @param drawable  The GLAutoDrawable for this canvas.
     */
    public void init( GLAutoDrawable drawable )
    {
      GL gl = drawable.getGL();

//    drawable.setGL( new DebugGL( gl ) );

      gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }


    /* ---------------------------- reshape ----------------------------- */
    /**
     *  Called by the JOGL system when the panel is resized, and by the
     *  display method to set up the projection matrix before drawing the
     *  scene.
     *
     *  @param drawable  The GLAutoDrawable for this canvas.
     *  @param x         The x position of the window (typically 0)
     *  @param y         The y position of the window (typically 0)
     *  @param width     The width of the window in pixels
     *  @param height    The height of the window in pixels
     */
    public void reshape( GLAutoDrawable drawable,
                         int x,
                         int y,
                         int width,
                         int height)
    {
      if ( debug )
        System.out.println("reshape called: persp = " + use_perspective_proj +
                           ", select = " + do_select +
                           ", locate = " + do_locate );

      GL  gl  = drawable.getGL();

      gl.glViewport( 0, 0, width, height );
      gl.glMatrixMode(GL.GL_PROJECTION);
      gl.glLoadIdentity();
      
      if ( !canvas.isShowing() || !canvas.isValid() )
      {
        System.out.println("ERROR: reshape called when not visible");
        return;
      }
      
      if ( width <= 0 || height <= 0 )
      {
        System.out.println("ERROR: reshape called with zero size, width ="+
                            width + " height = " + height );
        return;
      }
      
      if ( do_select )
      {
        int viewport[] = { 0, 0, width, height };
        BasicGLU.gluPickMatrix( gl, cur_x, height-cur_y, 1, 1, viewport );
      }

      if ( use_perspective_proj )
        BasicGLU.gluPerspective( gl, 
                                 view_angle, 
                                 width/(float)height, 
                                 near_plane, 
                                 far_plane );
      else
      {
        Vector3D difference_v = new Vector3D( cop );
        difference_v.subtract( vrp );
        float distance = difference_v.length();
        float angle_radians = (float)(Math.PI * view_angle/2 / 180);
        float half_h = (float)Math.tan( angle_radians ) * distance;
        float half_w = half_h * width/(float)height;
        gl.glOrtho( -half_w, half_w, -half_h, half_h, near_plane, far_plane );
      }

      gl.glMatrixMode(GL.GL_MODELVIEW);
    }


    /* ---------------------- displayChanged --------------------------- */
    /**
     *  NOT IMPLEMENTED.  Called by the JOGL system when the panel is 
     *  moved to another display monitor.
     */
    public void displayChanged( GLAutoDrawable drawable,
                                boolean modeChanged,
                                boolean deviceChanged)
    {
    }


    /* --------------------------- display ----------------------------- */
    /**
     *  Called by the JOGL system when the panel is to be redrawn.
     *
     *  @param drawable  The GLAutoDrawable for this canvas.
     */
    synchronized public void display(GLAutoDrawable drawable)
    {
      if ( debug )
        System.out.println("display called: persp = " + use_perspective_proj +
                           ", select = " + do_select +
                           ", locate = " + do_locate );
      GL gl = drawable.getGL();
 
                             // Clean up any old display lists that are no 
                             // longer used, so that OpenGL can free the any 
                             // resources used by the lists.  We do this here, 
                             // to be sure that we have a valid "gl", run from
                             // the correct thread.
      int old_lists[] = ChangeOldLists( GL_Shape.INVALID_LIST_ID );
      if ( old_lists != null )
        for ( int i = 0; i < old_lists.length; i++ )
           gl.glDeleteLists( old_lists[i], 1 );

                             // return quickly if the the region is degenerate
                             // or if the list of objects is empty
      Dimension size = canvas.getSize();
      if ( size.width <= 0 || size.height <= 0 )
      {
        if ( debug )
          System.out.println("ERROR: Drawable has zero area");
        return;
      }
      if ( drawable instanceof GLCanvas && !((GLCanvas)drawable).isShowing() )
      {
        if ( debug )
          System.out.println("ERROR: Drawable not visible yet");
        return;
      }
                                      // if just locating 3D point, get the
      if ( do_locate )                // projection info, unproject, and return
      {
        float depths[] = new float[1];
        gl.glReadPixels( cur_x, size.height-cur_y, 
                         1, 1, 
                         GL.GL_DEPTH_COMPONENT,
                         GL.GL_FLOAT,
                         FloatBuffer.wrap(depths) ); 
        float cur_z = depths[0];
        cur_z *= pixel_depth_scale_factor; 

        int    viewport[] = new int[4];  
        gl.glGetIntegerv( GL.GL_VIEWPORT, viewport, 0 ); 
        double model_view_mat[] = new double[16];
        double projection_mat[] = new double[16];
        gl.glGetDoublev( GL.GL_MODELVIEW_MATRIX, model_view_mat, 0 ); 
        gl.glGetDoublev( GL.GL_PROJECTION_MATRIX, projection_mat, 0 ); 
        double world_x[] = new double[1];
        double world_y[] = new double[1];
        double world_z[] = new double[1];
        BasicGLU.gluUnProject( cur_x, size.height-cur_y, cur_z, 
                               model_view_mat, projection_mat, viewport,
                               world_x, world_y, world_z );
        world_coords[0] = (float)world_x[0];
        world_coords[1] = (float)world_y[0];
        world_coords[2] = (float)world_z[0];

        do_locate = false;
        return;
      }
                                          // At this point we'll clear and draw 
                                          // all of the objects in our list,
                                          // if the list is not empty.
      IThreeD_GL_Object list[] = getAllObjects();

      gl.glClearColor( background.getRed()/256.0f, 
                       background.getGreen()/256.0f,
                       background.getBlue()/256.0f,
                       0.0f);

      if ( list == null || list.length == 0 )
      {
        if ( debug )
          System.out.println("WARNING: Drawing empty list of objects");
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
        gl.glFlush();
        return; 
      }
                                          // Finally we're ready to draw, either
                                          // to actually display, or for 
                                          // OpenGL selection to pick objects.

      if ( do_select )                    // Get ready to do the drawing 
      {                                   // in select mode, not render mode.
        n_hits = 0;
        for ( int i = 0; i < HIT_BUFFER_SIZE; i++ )
          hit_buffer.put( i, 0 );
        hit_buffer.clear();

        gl.glSelectBuffer( HIT_BUFFER_SIZE, hit_buffer );
        gl.glRenderMode( GL.GL_SELECT );
        gl.glInitNames();
      }

      gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
      reshape( drawable, 0, 0, size.width, size.height ); 
      gl.glLoadIdentity();

      if ( !do_select )                    // only need lighting for actual
      {                                    // rendering, not if doing selection
        float white[] = { 0.7f, 0.7f, 0.7f, 1 };
        float l0_position[] = { 0, 0, 0, 1 };
        gl.glLightModeli( GL.GL_LIGHT_MODEL_COLOR_CONTROL,
                          GL.GL_SEPARATE_SPECULAR_COLOR);
        gl.glLightModelfv( GL.GL_LIGHT_MODEL_AMBIENT, white, 0 );
        gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
        gl.glShadeModel(GL.GL_SMOOTH);
        gl.glEnable( GL.GL_LIGHTING );
        gl.glEnable( GL.GL_DEPTH_TEST );

        // light 0 is the "headlight"
        gl.glEnable( GL.GL_LIGHT0 );
        gl.glLightfv( GL.GL_LIGHT0, GL.GL_POSITION, l0_position, 0 );
        gl.glLightfv( GL.GL_LIGHT0, GL.GL_AMBIENT, white, 0 );
        gl.glLightfv( GL.GL_LIGHT0, GL.GL_DIFFUSE, white, 0 );
      }

      float cop_pt[] = cop.get();
      float vrp_pt[] = vrp.get();
      float vuv_pt[] = vuv.get();

      BasicGLU.gluLookAt( gl, cop_pt[0], cop_pt[1], cop_pt[2],
                              vrp_pt[0], vrp_pt[1], vrp_pt[2],
                              vuv_pt[0], vuv_pt[1], vuv_pt[2] );

      for ( int i = 0; i < list.length; i++ )
        if ( list == null && debug )
          System.out.println("LIST IS NULL ");
        else if ( list[i] == null && debug )
          System.out.println("LIST[i] IS NULL, i = " + i );
        else
          list[i].Render( drawable );

      if ( do_select )                            // switch back to render mode
      {                                           // to get the number of hits
        n_hits = gl.glRenderMode( GL.GL_RENDER );
        do_select = false; 
        Draw();                             // This Draw() should NOT be needed
                                            // but it provides a work-around 
                                            // for the scene blacking out when
                                            // using selection with some OpenGL
                                            // implementations.
                                            // (Eg:ATI/VESA on HP xw8240 laptop)
        if ( debug )
          System.out.println("n_hits = " + n_hits );
      }
      else
      {                           // we only get here if we actually did a new
        gl.glFlush();             // drawing (not locate, not select), so now
//      canvas.swapBuffers();     // we can swap the buffers
      }
    }
  }


  /* ------------------------ MouseClickHandler --------------------------- */
  /**
   *  This class listens for mouse clicks and then uses pickHitList() to get
   *  and print a list of the OpenGL names that are "hit" by a ray through
   *  the current x,y pixel locations for debugging purposes.
   */
   public class MouseClickHandler extends MouseAdapter
   {
      public void mouseClicked (MouseEvent e)
      {  
        if ( e.getClickCount() == 1 )                 // zoom out to full view
        {
           int x = e.getX();
           int y = e.getY();

           // Test locate point in 3D ...... 

           float wc[] = pickedWorldCoordinates( x, y );
           System.out.println("World Coordinates " + wc[0] + 
                                              ", " + wc[1] +
                                              ", " + wc[2] );
           // Test object selection ......

           HitRecord hitlist[] = pickHitList( x, y );           
           System.out.println( "MouseClickHandler: length = "+hitlist.length );
           System.out.println( "x,y = " + x + ", " + y );
           for ( int i = 0; i < hitlist.length; i++ )
             System.out.println("hit = " + hitlist[i] );
        }
      }
   }


   /* ------------------------------- main ------------------------------- */
   /**
    *  Basic functionality test for Three
    */
   public static void main( String args[] )
   {                                                 // make a 3D display panel 
                                                     // and put it in a frame
      ThreeD_GL_Panel panel = new ThreeD_GL_Panel();
      JFrame frame = new JFrame( "ThreeD_GL_Panel" );
      frame.setSize(500,500);
      frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      frame.getContentPane().add( panel.getDisplayComponent() );
                     
                                                     // add objects and show 
                                                     // the frame
      panel.setObject( "Cube 1", new Cube( panel, 0, 0, 0, 2 ) );     
      Cube cube2 =  new Cube( panel, 0, 0, 1.5f, 1 );
      cube2.setPickID( 1010101 );
      panel.setObject( "Cube 2", cube2 );     
      WindowShower.show(frame);

      panel.getDisplayComponent().addMouseListener( 
                                  panel.new MouseClickHandler() );

      JFrame c_frame = new JFrame( "View Control" );
      ViewController controller = new AltAzController( 45, 45, 1, 100, 25 );
      c_frame.getContentPane().add( controller );
      controller.addActionListener( new ViewControlListener( panel ) );
      c_frame.setSize(200,200);
      WindowShower.show(c_frame);
   }

}

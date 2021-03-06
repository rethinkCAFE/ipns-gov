/*
 * File:  ViewController.java
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
 * $Log$
 * Revision 1.1  2004/06/18 19:23:09  dennis
 * Moved to ViewControls package
 *
 * Revision 1.2  2004/06/04 14:11:04  dennis
 * Added methods to get/set whether or not the projection is a
 * perspective projection.  Changed methods to set the VRP, COP
 * and VUP to public, from private.
 *
 * Revision 1.1  2004/05/28 20:51:19  dennis
 * Initial (test) version of classes for displaying and picking
 * 3D objects using OpenGL from Java, built on the "jogl" system.
 *
 *
 */

package gov.anl.ipns.ViewTools.Panels.GL_ThreeD.ViewControls;

import java.io.*;

import gov.anl.ipns.MathTools.Geometry.*;
import gov.anl.ipns.ViewTools.UI.*;

/**
 *  A ViewController object controls the observers position for one or more
 *  GL_Panel objects.  Derived classes implement various interfaces to
 *  allow the user to modify the position.
 */ 

abstract public class ViewController extends    ActiveJPanel
                                     implements Serializable,
                                                IViewController 
{
  public static final String VIEW_CHANGED = "View Changed";

  private Vector3D vrp,
                   cop,
                   vuv;

  private boolean  use_perspective = true;


/* ------------------------- Default Constructor -------------------------- */
/**
 *  Construct a default ViewController, initially viewing the origin from
 *  (1,1,1) with a virtual screen height and width of 1. 
 */
  public ViewController()
  { 
    vrp     = new Vector3D( 0, 0, 0 );
    cop     = new Vector3D( 1, 1, 1 );
    vuv     = new Vector3D( 0, 0, 1 );
  }

/* ------------------------------ Constructor ----------------------------- */
/**
 *  Construct a ViewController with the specified COP, VRP and VUV
 */
  public ViewController( Vector3D cop, Vector3D vrp, Vector3D vuv )
  {
    this.vrp = new Vector3D( vrp );
    this.cop = new Vector3D( cop );
    this.vuv = new Vector3D( vuv );
  }

/* -------------------------------- setCOP ------------------------------- */
/**
 *   Set the observer's position (i.e. the Center of Projection ) for this view.
 *
 *   @param  cop  The new vector to use for the observer's position.  THIS MUST
 *                BE DIFFERENT FROM THE VRP WHEN THE apply() METHOD IS CALLED.
 */
 public void setCOP( Vector3D cop )
 {
   this.cop = new Vector3D( cop );
 }

/* -------------------------------- setVRP ------------------------------- */
/**
 *   Set the point the observer is looking at (i.e. the View Reference Point )
 *   for this view.
 *
 *   @param  vrp  The new vector to use for the point the observer is looking
 *                at.  THIS MUST BE DIFFERENT FROM THE COP WHEN THE apply() 
 *                METHOD IS CALLED.
 */
  public void setVRP( Vector3D vrp )
  {
    this.vrp = new Vector3D( vrp );
  }

/* -------------------------------- setVUV ------------------------------- */
/**
 *   Set the direction that is "up" from the observer's point of view
 *   (i.e. the View Up Vector ) for this view.
 *
 *   @param  vuv  The new vector to use for the view up vector.  THIS MUST
 *                NOT BE IN THE SAME DIRECTION AS THE DIFFERENCE (cop-vrp)
 *                WHEN THE apply() METHOD IS CALLED.
 */
  public void setVUV( Vector3D vuv )
  {
    this.vuv = new Vector3D( vuv );
  }

/* -------------------------------- getCOP ------------------------------- */
/**
 *   Get the observer's position (i.e. the Center of Projection ) for this view. *
 *   @return  The the observer's position.  
 */
  public Vector3D getCOP( )
  {
    return new Vector3D( cop );
  }

/* -------------------------------- getVRP ------------------------------- */
/**
 *   Get the point the observer is looking at (i.e. the View Reference Point )
 *   for this view.
 *
 *   @return  The point the observer is looking at.
 */
  public Vector3D getVRP()
  {
    return new Vector3D( vrp );
  }

/* -------------------------------- getVUV ------------------------------- */
/**
 *   Get the direction that is "up" from the observer's point of view
 *   (i.e. the View Up Vector ) for this view.
 *
 *   @return  The the view up vector. 
 */
  public Vector3D getVUV()
  {
    return new Vector3D( vuv );
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
    use_perspective = onoff;
  }

/* ---------------------------- isPerspective ---------------------------- */
/**
 *  Get whether or not to use a perspective projection was selected.
 *
 *  @return  true if a perspective projection should be used and false
 *           if an orthographic projection should be used.
 */
  public boolean isPerspective()
  {
    return use_perspective;
  }


}

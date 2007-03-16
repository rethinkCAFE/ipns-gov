/*
 * File: Region.java
 *
 * Copyright (C) 2003-2004, Mike Miller
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
 * Primary   Mike Miller <millermi@uwstout.edu>
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
 *
 *  $Log$
 *  Revision 1.14  2007/03/16 16:38:12  dennis
 *  Major refactoring of Region concept.  Regions are defined in world
 *  coordinates.  Any information about the mapping from world coordinates
 *  to array (col,row) coordinates is no longer kept by the regions.
 *  The world_to_array transformation is passed in as a parameter to
 *  the methods that require it.  This avoids any problems with keeping
 *  the mapping up to date.  Also, a region can be easily used with different
 *  mappings, say from world to an array of data or from world to pixels
 *  on a display, just by providing the correct mapping when calling the
 *  methods.  All methods for getting and setting mapping information were
 *  removed.  Also, methods specific to regions with an interior (such as box)
 *  were placed in a subclass RegionsWithInterior.
 *
 *  Revision 1.13  2007/03/11 04:37:16  dennis
 *  Added methods to setWorldToArrayTran() and getWorldToArrayTran().
 *
 *  Revision 1.12  2005/01/18 22:59:47  millermi
 *  - Added getWorldBounds() and getImageBounds()
 *
 *  Revision 1.11  2004/07/02 16:40:52  millermi
 *  - Added comments and println message to getRegionUnion() notifying
 *    them that TableRegions may not work with this method.
 *
 *  Revision 1.10  2004/05/20 17:02:26  millermi
 *  - Made method getRegionBounds() public so it may be used by
 *    outside classes.
 *
 *  Revision 1.9  2004/05/11 01:08:08  millermi
 *  - Removed unused variables.
 *
 *  Revision 1.8  2004/03/24 03:05:31  millermi
 *  - convertFloatPoint() now converts image row/column values to
 *    world coord values corresponding to the center of the pixel.
 *
 *  Revision 1.7  2004/03/12 02:00:35  rmikk
 *  Fixed package Names
 *
 *  Revision 1.6  2004/02/15 21:42:01  millermi
 *  - Revised javadocs. Commented out world/image_coords_set
 *    variables since they are not being used.
 *
 *  Revision 1.5  2004/02/14 03:33:02  millermi
 *  - revised getRegionUnion()
 *  - added setImageBounds(), setWorldBounds(), convertFloatPoint(),
 *    equals(), and toString() methods
 *  - Introduced transformation for converting from world to image
 *  - getDefiningPoints() now takes a parameter to determine if
 *    world or image coords are desired.
 *  - added floorImagePoint() to round float image coordinates to
 *    the greatest integer row/column value less than the float.
 *
 *  Revision 1.4  2004/01/07 06:44:53  millermi
 *  - Added static method getRegionUnion() which removes duplicate
 *    points from one or more selections.
 *  - Added protected methods initializeSelectedPoints() and
 *    getRegionBounds(). Each is needed by getRegionUnion()
 *    to calculate a unique set of points.
 *
 *  Revision 1.3  2003/11/18 01:03:29  millermi
 *  - Now implement serializable to allow saving of state.
 *
 *  Revision 1.2  2003/10/22 20:26:09  millermi
 *  - Fixed java doc errors.
 *
 *  Revision 1.1  2003/08/11 23:40:41  millermi
 *  - Initial Version - Used to pass region info from a
 *    ViewComponent to the viewer. WCRegion is an unrelated
 *    class that passes info from the overlay to the
 *    ViewComponent.
 *
 */ 
package gov.anl.ipns.ViewTools.Components.Region;

import java.awt.Point;
import java.util.Vector;

import gov.anl.ipns.Util.Sys.SharedMessages;
import gov.anl.ipns.Util.Numeric.floatPoint2D;
import gov.anl.ipns.ViewTools.Panels.Transforms.CoordBounds;
import gov.anl.ipns.ViewTools.Panels.Transforms.CoordTransform;

/**
 * This class is a base class for all regions in the Region package. A Region is
 * used to pass selected regions (selected using the Selection Overlay) from a
 * view component to the viewer. Given the defining points of a region,
 * subclasses of this class can return all of the points inside the selected
 * region. The defining points are saved in the world coordinate system.
 * Classes that use regions are responsible for constructing and maintaining
 * a mapping from world coordinates to array or pixel coordinates, if they
 * such a discrete coordinate system.
 */ 
public abstract class Region implements java.io.Serializable
{
  protected floatPoint2D[] definingpoints;  // saved in world coords.
  
 /**
  * Constructor - provides basic initialization for all subclasses
  *
  *  @param  dp - world coordinate defining points of the region.
  */ 
  protected Region( floatPoint2D[] dp )
  {
    definingpoints = dp;
  }


 /**
  * Get the world coordinate points used to define the geometric shape.
  *
  * @return a reference to the array of points that define the region.
  */
  public floatPoint2D[] getDefiningPoints()
  {
    return definingpoints;
  }

  
 /**
  * Compare two regions. This will return true only if the two intances
  * are of the same class and have identical defining points. 
  * Be aware that it is difficult to compare
  * float values reliably, so even identical regions will return false if
  * one of the values is off by a small amount. This will reliably return true
  * if the references of the two regions match. 
  *
  *  @param  reg2 The region to be compared.
  *  @return true if equal, false if not.
  */
  public boolean equals1( Region reg2 )
  {
    // if references are the same, the region is the same.
    if( this == reg2 )
      return true;

    // check if instances of the same class
    if( !this.getClass().getName().equals(reg2.getClass().getName()) )
      return false;

    // make sure there exist the same number of defining points
    floatPoint2D[] thisdp = this.definingpoints;
    floatPoint2D[] reg2dp = reg2.definingpoints;
    if( thisdp.length != reg2dp.length )
      return false;

    // check each individual element, be aware that float values are hard
    // to compare reliably.
    for( int i = 0; i < thisdp.length; i++ )
    {
      if( thisdp[i] != reg2dp[i] )
        return false;
    }

    // else, must be same region.
    return true;
  }


 /**
  *  Get a bounding box for the region, in World Coordinates.  The
  *  points of the region will lie in the X-interval [X1,X2] and
  *  in the Y-interval [Y1,Y2], where X1,X2,Y1 and Y2 are the values
  *  returned by the CoordBounds.getX1(), getX2(), getY1(), getY2()
  *  methods.
  * 
  *  @return a CoordBounds object containing the full extent of this
  *          region.
  */
 abstract public CoordBounds getRegionBoundsWC();

  
 /**
  * Get the discrete points that lie within this region, based on the
  * specified mapping from world to array (col,row) coordinates.
  * 
  *  @return array of points included within the region.
  */
  abstract public Point[] getSelectedPoints( CoordTransform world_to_array );
  

 /**
  * Displays the Region type and its defining points.
  */
  abstract public String toString();


 /**
  *  This method gets a rectangular bounding box in array (col,row) coordinates
  *  that contains the region, based on the bounds returned by the
  *  getRegionBoundsWC() method.  The Math.floor and Math.ceil functions are
  *  applied to the results of mapping the world coordinate bounds to array
  *  coordinates, to guarantee that the integer bounds produced contain
  *  the region.
  *
  *  @param world_to_array  The transformation from world coordinates to 
  *                         array coordinates
  *
  *  @return Rectangular bounds containing the region.
  */
  public CoordBounds getRegionBounds( CoordTransform world_to_array )
  {
    CoordBounds bounds = getRegionBoundsWC();

    floatPoint2D min_point = new floatPoint2D( bounds.getX1(), bounds.getY1() );
    floatPoint2D max_point = new floatPoint2D( bounds.getX2(), bounds.getY2() );

    min_point = world_to_array.MapTo( min_point );
    max_point = world_to_array.MapTo( max_point );

    if ( min_point.x > max_point.x )
    {
      float temp = min_point.x;
      min_point.x = max_point.x;
      max_point.x = temp;
    }

    if ( min_point.y > max_point.y )
    {
      float temp = min_point.y;
      min_point.y = max_point.y;
      max_point.y = temp;
    }

    min_point.x = (float)Math.floor( min_point.x );
    min_point.y = (float)Math.floor( min_point.y );

    max_point.x = (float)Math.ceil( max_point.x );
    max_point.y = (float)Math.ceil( max_point.y );

    return new CoordBounds( min_point.x, min_point.y,
                            max_point.x, max_point.y );
  }

  
 /**
  * Since image row/column values are integers, the mapping from world to
  * image coordinates must be converted from image float values to integers.
  * To display properly, the decimal portion of the float values must be
  * truncated instead of rounded. Off-by-one errors will occur if image decimal
  * is >= .5.
  *
  *  @param  imagept Float image row/column values.
  *  @return The corresponding integer image row/column values.
  */
  protected Point floorImagePoint( floatPoint2D imagept )
  {
    int x = (int)Math.floor(imagept.x);
    int y = (int)Math.floor(imagept.y);
    return new Point( x, y );
  }


 /**
  * This method removes duplicate points selected by multiple regions.
  * Calling this method will combine all regions' selected points into
  * one list of points, where each point is unique.
  *
  *  @param  regions The list of regions to be unionized.
  *  @return A list of unique points for all of the regions.
  */
  public static Point[] getRegionUnion( Region[]       regions, 
                                        CoordTransform world_to_array  )
  {
    // Only the TableRegion class has the concept of selected and deselected
    // regions. This means only TableRegions can "subtract". However, the
    // getRegionUnion() is generalized for the other regions, thus the
    // selected and deselected concept is not implemented. If all TableRegions
    // are selected, the TableRegion class behaves just like any other
    // Region class.
    if( regions instanceof TableRegion[] )
      SharedMessages.addmsg( "Note: Using Region.getRegionUnion() with " +
                             "TableRegions will return an incorrect list of " +
			     "points if any of the TableRegions are " +
			     "unselected." );
    
    // this transform will map image bounds to an integer grid from
    // [0,#rows-1] x [0,#col-1]
    CoordTransform image_to_array = new CoordTransform();

    // if no regions are passed in, return no points.
    if( regions.length == 0 )
      return new Point[0];

    // region bounds are in image coordinates.
    CoordBounds regionbounds = regions[0].getRegionBounds( world_to_array );
    float rowmin = regionbounds.getX1();
    float rowmax = regionbounds.getX2();
    float colmin = regionbounds.getY1();
    float colmax = regionbounds.getY2();
    for( int i = 1; i < regions.length; i++ )
    {
      if( regions[i].getRegionBounds( world_to_array ).getX1() < rowmin )
      {
        rowmin = regions[i].getRegionBounds( world_to_array ).getX1();
      }
      if( regions[i].getRegionBounds( world_to_array ).getX2() > rowmax )
      {
        rowmax = regions[i].getRegionBounds( world_to_array ).getX2();
      }
      if( regions[i].getRegionBounds( world_to_array ).getY1() < colmin )
      {
        colmin = regions[i].getRegionBounds( world_to_array ).getY1();
      }
      if( regions[i].getRegionBounds( world_to_array ).getY2() > colmax )
      {
        colmax = regions[i].getRegionBounds( world_to_array ).getY2();
      }
    }

    // create a nice integer-like interval that will nicely map to
    // the array.
    rowmin = (float)Math.floor((double)rowmin);
    colmin = (float)Math.floor((double)colmin);
    rowmax = (float)Math.ceil((double)rowmax);
    colmax = (float)Math.ceil((double)colmax);

    // set image bounds
    image_to_array.setSource( new CoordBounds(rowmin,colmin,rowmax,colmax) );

    // build table to keep track of selected points
    int rows = Math.abs(Math.round(rowmax - rowmin)) + 1;
    int columns = Math.abs(Math.round(colmax - colmin)) + 1;

    // set array bounds
    image_to_array.setDestination( new CoordBounds(0,0,
                                                   (float)(rows-1),
						   (float)(columns-1) ) );
    boolean[][] point_table = new boolean[rows][columns];

    //System.out.println("Row/Column: " + rows + "/" + columns );
    Vector points = new Vector();
    Point[] sel_pts;
    Point temp = new Point();

    // for each region, mark its selected points
    for( int i = 0; i < regions.length; i++ )
    {
      System.out.println("Region = \n" + regions[i] );
      // use initializeSelectedPoints() since getSelectedPoints() may call
      // this method, causing an endless loop.
      // sel_pts = regions[i].initializeSelectedPoints(); 
      sel_pts = new Point[0];
      for( int pt = 0; pt < sel_pts.length; pt++ )
      { 
	// map image points to array points
	temp = image_to_array.MapTo(new floatPoint2D(sel_pts[pt])).toPoint();
        /*
	System.out.println(image_to_array);
	System.out.println("row min/max: " + rowmin + "/" + rowmax );
	System.out.println("col min/max: " + colmin + "/" + colmax );
	System.out.println("Point: " + temp );
	*/
	if( !(point_table[temp.x][temp.y]) )
	{
          //System.out.println("Point: (" + sel_pts[pt].x + "," + 
	  //                                sel_pts[pt].y + ")");
	  points.add( new Point( sel_pts[pt] ) );
	  point_table[temp.x][temp.y] = true;
	}
      }
    }

    // put the vector of points into an array of points
    Point[] unionpoints = new Point[points.size()];
    for( int i = 0; i < points.size(); i++ )
      unionpoints[i] = (Point)points.elementAt(i);
    return unionpoints;
  }
}

/*
 * File: RegionWithInterior.java
 *
 * Copyright (C) 2007, Dennis Mikklelson 
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
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.2  2007/03/25 21:59:04  dennis
 *  Cleaned up getSelectedPoints() method.  Now calls getRegionBuonds()
 *  method to get the range of rows and columns covered, instead of
 *  getting the world coordinate bounds and mapping them to array
 *  coordinates.  Also, now uses an array to accumulate the selected
 *  points, instead of a vector, for efficiency.
 *
 *  Revision 1.1  2007/03/16 16:31:48  dennis
 *  New subclass of Region to deal with regions like box, wedge, circle,
 *  etc. that have an interior, as opposed to regions like line and point
 *  that don't have an interior.  All regions with interior are now handled
 *  uniformly using a method to determine whether or not a point is in
 *  the region, in world coordinates.
 *
 */ 
package gov.anl.ipns.ViewTools.Components.Region;

import java.awt.Point;
import java.util.Vector;

import gov.anl.ipns.Util.Numeric.floatPoint2D;
import gov.anl.ipns.ViewTools.Panels.Transforms.CoordBounds;
import gov.anl.ipns.ViewTools.Panels.Transforms.CoordTransform;

/**
 * This class is a base class for Regions that have an interior, such as
 * boxes, circles, etc.  These regions must have a method, isInsideWC(x,y)
 * to determine whether or not a point, specified in world coordinates,
 * is inside the region.  This allows finding which discrete array (or pixel)
 * elements are inside the region by stepping over all disrete elements in
 * a bounding box for the region, mapping the centers of the discrete elements 
 * to world coordinates, and checking whether or not the centers fall in 
 * the region.
 * such a discrete coordinate system.
 */ 
public abstract class RegionWithInterior extends    Region
                                         implements java.io.Serializable
{
  
 /**
  * Constructor 
  *
  *  @param  dp - world coordinate defining points of the region.
  */ 
  protected RegionWithInterior( floatPoint2D[] dp )
  {
    super(dp);
    definingpoints = dp;
  }


 /**
  *  Check whether or not the specified World Coordinate point is inside 
  *  of the Region.  This method must be overridden in a meaningful way 
  *  by derived classes, such as BoxRegion, that have a non-degenerate
  *  interior.  For such regions, this method is used by the base class
  *  implementation of getSelectedPoints(), so such derived classes should
  *  NOT need to override the getSelectedPoints() method.  On the other 
  *  hand, derived classes like PointRegion or LineRegion that have 
  *  an empty interior MUST override getSelectedPoints().
  *  It is the responsibility of concrete derived classes to determine 
  *  which points are selected, by either overriding isInsideWC() and
  *  getRegionBoundsWC(), OR overriding getSelectedPoints().
  *
  *  @param x   The x-coordinate of the point, in world coordinates.
  *  @param y   The y-coordinate of the point, in world coordinates.
  *  @return true if the point is in the region and false otherwise.
  */
 abstract public boolean isInsideWC( float x, float y );


 /**
  * Get all of the image points inside the region. The use of
  * Point was chosen over floatPoint2D because at this point we are dealing
  * with row/column coordinates, so rounding is acceptable. This method assumes
  * that the input points are in (x,y) where (x = col, y = row ) form.
  *
  *  @return array of points included within the region.
  */
  public Point[] getSelectedPoints( CoordTransform world_to_array )
  {
                                          // get bounding box for the region

    CoordBounds bounds = getRegionBounds( world_to_array );

    int min_x = (int)bounds.getX1();
    int max_x = (int)bounds.getX2();

    int min_y = (int)bounds.getY1();
    int max_y = (int)bounds.getY2();
                                           // Step through box rowwise, saving 
                                           // points that are in the region.

    Point pts[] = new Point[ ( max_x - min_x + 1 ) * ( max_y - min_y + 1 ) ];

    floatPoint2D temp_point = new floatPoint2D();
    int  num_pts = 0;
    for( int row = min_y; row <= max_y; row++ )
    {
      for( int col = min_x; col <= max_x; col++ )
      {
                                      // NOTE: We could avoid making lots of
        temp_point.x = col + 0.5f;    // small temp_point objects, if we added
        temp_point.y = row + 0.5f;    // a method to do the mapping "in place"
        temp_point = world_to_array.MapFrom( temp_point );

        if ( isInsideWC( temp_point.x, temp_point.y ) )
          pts[num_pts++] = new Point(col,row);
      }
    }
                                            // construct final list of points.
    Point[] selected_points = new Point[num_pts];
    System.arraycopy( pts, 0, selected_points, 0, num_pts );

    return selected_points;
  }
  
}

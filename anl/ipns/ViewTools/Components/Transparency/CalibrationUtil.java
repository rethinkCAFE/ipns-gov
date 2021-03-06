/*
 * File: CalibrationUtil.java
 *
 * Copyright (C) 2003, Mike Miller
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
 *  Revision 1.13  2006/07/18 21:56:25  amoe
 *  Fixed javadoc typo.
 *
 *  Revision 1.12  2005/10/14 15:16:02  dennis
 *    "Fixed" a bug in the subDivide(), that caused an infinite loop
 *  when a very small interval was subdivided.  (Eg. an interval from
 *  0.9999999 to 1.0000001).  Due to rounding errors in floating point
 *  arithmetic, the calculated step size between subdivision points is so
 *  small that when adding it to the starting point, the value does not
 *  change.
 *    This was "Fixed" by limiting the iteration to a reasonable number of
 *  steps.  If that number of steps is exceeded, the target interval is
 *  extended to a larger interval, and the larger interval is subdivided.
 *  The subDivideLog() method should also be checked to verify that it
 *  is robust enough to handle this case.
 *    This is a partial fix to the problem of the SelectedGraphView
 *  "hanging" on data that is essentially (but not quite) constant.
 *  Unfortunately, the axis calibration code still has a small problem with
 *  scaling such data.  In particular, the interval from 0.9999999 to
 *  1.0000001 gets represented with values on the order of 1000000E-6.
 *  However, at least it now works.
 *
 *  Revision 1.11  2005/01/24 22:29:34  millermi
 *  - Fixed bug that altered the xmin and xmax values in method calls.
 *  - Realigned code for easier reading.
 *
 *  Revision 1.10  2004/07/29 16:44:40  robertsonj
 *  added javadocs
 *
 *  Revision 1.9  2004/07/28 19:33:59  robertsonj
 *  added functionality to find the greatest lower bound and the least upper
 *  bound from the number 1*10^x, 2*10^x, 5*10^x, 1.2*10^x and 1.5*10^x
 *  These functiions are used to find nice logarithmic numbers to use on the
 *  axis overlay
 *
 *  Revision 1.8  2004/05/11 01:40:13  millermi
 *  - Removed unused variables.
 *
 *  Revision 1.7  2004/03/12 02:49:42  millermi
 *  - Changed package, fixed imports.
 *
 *  Revision 1.6  2003/10/16 05:00:08  millermi
 *  - Fixed java docs errors.
 *
 *  Revision 1.5  2003/08/01 21:52:18  serumb
 *  Commented out print line.
 *
 *  Revision 1.4  2003/07/05 19:45:57  dennis
 *  - Added method subDivideLog() to provide geometric steps for
 *    logarithmically dividing an axis. (Mike Miller)
 *
 *  Revision 1.3  2003/05/22 17:48:44  dennis
 *  Adjusted starting position returned by subDivid() to start
 *  at xmin when applicable. (Mike Miller)
 *
 *  Revision 1.2  2003/05/16 14:57:09  dennis
 *  Added acknowledgement of NSF funding.
 *
 */

package gov.anl.ipns.ViewTools.Components.Transparency;

import gov.anl.ipns.Util.Numeric.Format;
import gov.anl.ipns.Util.Sys.SharedMessages;

/**
 * This class bundles calibration functions together. Pass in an interval,
 * this class can subdivide the interval and allow for consistent number 
 * formatting of the interval values. 
 */
public class CalibrationUtil 
{
  private float xmin;
  private float xmax;
  private int sig_dig;  	// significant digits
  private int baseE;		// the exponent all numbers will be based from
  private String format;
  private boolean isTwoSided;	// true if the data is two sided, (+/-) data
  
 /**
  * Constructor - Initializes interval to be manipulated. Significant digits
  * and format are specified to allow this class to display numbers in a
  * consistent way.
  *
  *  @param  interval_min
  *  @param  interval_max
  *  @param  signif_dig
  *  @param  formattee
  */ 
  public CalibrationUtil( float interval_min, float interval_max, 
			  int signif_dig, String formattee )
  {
    xmin = interval_min;
    xmax = interval_max;
    
    if( xmax < xmin )
    {
      float temp = xmax;
      xmax = xmin;
      xmin = temp;
    }
    if( xmax == xmin )
    {
      SharedMessages.addmsg("ERROR in CalibrationUtil -- endpoints of "+
        	      "interval are equal. Interval has been adjusted.");
      xmax = xmax + 1;
    }
    
    sig_dig = signif_dig;
    baseE = 0;
    
    // if either of the bounds triggers engineer format, all numbers
    // will be represented in engineer format
    if( formattee == Format.AUTO && 
          ( Math.abs(xmax) >= 10000 || Math.abs(xmin) < .001 ) )
      formattee = Format.ENGINEER;	
    format = formattee;
    isTwoSided = true;
  }
  
 /**
  * Convenience constructor - Allows only interval to be specified. Based
  * on the interval, either engineering or decimal formatting will be used.
  * All numbers will be rounded to a precision of 4.
  *
  *  @param  interval_min
  *  @param  interval_max
  */
  public CalibrationUtil( float interval_min, float interval_max )
  {
    this( interval_min, interval_max, 4, Format.AUTO );
  }   
  

 /* ------------------------------- subDivide ------------------------*
  * Method taken and modified from Subdivide.c by Dennis Mikkelson
  */
 /** 
  * Given an interval [a,b] find a "rounded" step size "step" and a
  * "rounded" starting point "start" in [a,b], so that start+k*step
  * for k = 0,1,... gives a reasonable subdivision of [a,b].
  * However, a and b are not changed.
  *
  *  @return array containing step, start, and numsteps
  */   
  public float[] subDivide()
  {
    final float MAX_STEPS = 1000;

    float s_diff = 0;
    int   i_power = 0;
    float start = 0;
    float step = 0;
    float[] values = new float[3];
       
    s_diff = xmax - xmin;
    
 /* Now express the length of the interval in the form  s_diff * 10^ipower
    where s_diff is in the interval [1., 10.) */
    i_power = 0;
    while ( s_diff >= 10.0 )
    {
      s_diff = s_diff / 10.0f;
      i_power = i_power + 1;
    }
    while ( s_diff < 1.0 )
    {
      s_diff = s_diff * 10.0f;
      i_power = i_power - 1;
    }

    baseE = i_power - 1;
 /* Now choose step size to give a reasonable number of subdivisions
    over an interval of length b-a. */

    if ( s_diff <= 1.2 )
      step = .1f * (float)Math.pow(10.0, i_power );
    else if ( s_diff <= 2.0 )
      step = .2f * (float)Math.pow( 10.0, i_power );
    else if ( s_diff <= 2.5 )
      step = .25f * (float)Math.pow( 10.0, i_power );
    else if ( s_diff <= 5.0 )
      step = .5f * (float)Math.pow( 10.0, i_power );
    else
      step = 1.0f * (float)Math.pow( 10.0, i_power );

 /* Now find the first grid point in the specified interval. */

    start = xmin;
    if( start%step == 0 )
      ; // keep start = xmin
    else if ( start >= 0.0 )
      start = start - ((start%step ) - step );
    else
    {
      start = -start;
      start = start - (start%step);
      start = -start;
    }

  // return the number of steps
  // NOTE: This can fail due to rounding errors in the floating point
  //       calculation.  If step is so small that it is the "noise level"
  //       of floating point arithmetic, it can happen that sum+step is
  //       no different than sum.  To avoid this, we break out of the loop
  //       when too many iterations have occurred and try to subdivide a
  //       larger interval.
    float sum = start; 
    int numstep = 0;	 
    while( sum <= xmax && numstep < MAX_STEPS )
    {
      sum = sum + step;
      numstep++;
    }

    //System.out.println("Step = " + step );
    //System.out.println("Degree = " + i_power );
    //System.out.println("Start = " + start );
    //System.out.println("NumStep = " + numstep );
      
    if ( numstep < MAX_STEPS )
    {
      values[0] = step;
      values[1] = start;
      values[2] = numstep;
    }
    else
    {
      CalibrationUtil try_again = new CalibrationUtil( xmin/5, xmax*5 ); 
      return try_again.subDivide();
    }
    
    return values;
  } /* subDivide */
  

 /**
  * This method will roughly divide the interval using geometric steps.
  * If xmin is within one degree of xmax, values could be negative and 
  * one of three intervals will be used:
  *	       Interval I:   1, 2, 5, 10
  *	       Interval II:  1, 1.5, 2, 3, 5
  *	       Interval III: 1, 1.25, 1. 5, 2
  * If xmin is not within one degree of xmax, powers of Interval I are used.
  * If data is two-sided, the absolute max becomes xmax. In this case,
  * all values will be zero or positive.
  *
  *  @return array of steps for the interval
  */
  public float[] subDivideLog()
  {
    float[] values = new float[0];
    // Create temporary min and max values so original interval is not altered.
    float temp_min = xmin;
    float temp_max = xmax;
    
    //***********************************************************************
    // if xmin is within one power of xmax, use specific log scaling.
    // positive xmin is within one power if xmax/10 < xmin, while
    // negative xmin is within one power if xmax*10 < xmin.
    if( (temp_min >= temp_max/10 && temp_min > 0) ||
            (temp_min >= temp_max*10 && temp_min < 0) )
    { 
      float diff = Math.abs(temp_max - temp_min);
      int diffpower = 0;
      while( diff >= 10 )
      { 
	diff = diff / 10f;
	diffpower++;
	// this is to try to correct rounding errors
	if( diff/10 < 10 )
          diff = diff + .1f;
      }
      while ( diff < 1.0 )
      {
	diff = diff * 10.0f;
	diffpower--;
	// this is to try to correct rounding errors
	if( diff*10 > 1 )
          diff = diff + .001f;
      } 
      //System.out.println("diff/diffpower =" + diff + "/" + diffpower );
      
      // find degree of xmin
      float start_min = Math.abs(temp_min);
      int xmin_power = 0;
      while( start_min >= 10 )
      {
	start_min = start_min / 10f;
	xmin_power++;
      }
      while ( start_min < 1.0 )
      {
	start_min = start_min * 10.0f;
	xmin_power--;
      }
      
      // find degree of xmax
      float end_max = Math.abs(temp_max);
      int xmax_power = 0;
      while( end_max >= 10 )
      {
	end_max = end_max / 10f;
	xmax_power++;
      }
      while ( end_max < 1.0 )
      {
	end_max = end_max * 10.0f;
	xmax_power--;
      }

      // put temp just "above" xmin
      if( temp_min > 0 )
	start_min = (float)Math.round( .4 + (double)(start_min *
				       Math.pow(10,Math.abs(diffpower) ) ) );
      else
	start_min = -(float)Math.round( -.4 + (double)(start_min *
					Math.pow(10,Math.abs(diffpower)) ) );
      start_min = start_min * (float)Math.pow(10, (double)(xmin_power - 
						 Math.abs(diffpower)) );

      // put temp just "below" xmin
      if( temp_max > 0 )
	end_max = (float)Math.round( -.4 + (double)(end_max *
				      Math.pow(10,Math.abs(diffpower) ) ) );
      else
	end_max = -(float)Math.round( .4 + (double)(end_max *
					Math.pow(10,Math.abs(diffpower)) ) );
      end_max = end_max * (float)Math.pow(10, (double)(xmax_power - 
						 Math.abs(diffpower)) );

      // if diff between [1,2], use approximate geometric steps of 2
      if ( diff <= 2.0 )
      { 
	// this is to prevent the last two steps from repeating.
	if( end_max != ( start_min + ( 1.0f * 
			    (float)Math.pow( 10, (double)(diffpower) ) ) ) )
	{
	  values = new float[6];
	  values[5] = end_max;
	}
	else
          values = new float[5];

	values[0] = start_min;
        values[1] = start_min + ( 0.1f * 
        			(float)Math.pow( 10, (double)(diffpower) ) );
        values[2] = start_min + ( 0.2f * 
        			(float)Math.pow( 10, (double)(diffpower) ) );
        values[3] = start_min + ( 0.5f * 
        			(float)Math.pow( 10, (double)(diffpower) ) );
        values[4] = start_min + ( 1.0f * 
        			(float)Math.pow( 10, (double)(diffpower) ) );
      }
      // if diff between [2,5], use approximate geometric steps of 1.25
      else if ( diff <= 5.0 )
      {
	// this is to prevent the last two steps from repeating.
	if( end_max != ( start_min + ( 2f * 
			    (float)Math.pow( 10, (double)(diffpower) ) ) ) )
	{
	  values = new float[6];
	  values[5] = end_max;
	}
	else
          values = new float[5];

        values[0] = start_min;
        values[1] = start_min + ( 1f * 
        			(float)Math.pow( 10, (double)(diffpower) ) );
        values[2] = start_min + ( 1.25f * 
        			(float)Math.pow( 10, (double)(diffpower) ) );
        values[3] = start_min + ( 1.5f * 
        			(float)Math.pow( 10, (double)(diffpower) ) );
        values[4] = start_min + ( 2f * 
        			(float)Math.pow( 10, (double)(diffpower) ) );
      }
      // if diff between [5,10], use approximate geometric steps of 1.5
      else
      { 
	// this is to prevent the last two steps from repeating.
	if( end_max != ( start_min + ( 5f * 
			 (float)Math.pow( 10, (double)(diffpower) ) ) ) )
	{
	  values = new float[7];
	  values[6] = end_max;
	}
	else
          values = new float[6];
     
        values[0] = start_min;
        values[1] = start_min + ( 1f * 
        			(float)Math.pow( 10, (double)(diffpower) ) );
        values[2] = start_min + ( 1.5f * 
        			(float)Math.pow( 10, (double)(diffpower) ) );
        values[3] = start_min + ( 2f * 
        			(float)Math.pow( 10, (double)(diffpower) ) );
        values[4] = start_min + ( 3f * 
        			(float)Math.pow( 10, (double)(diffpower) ) );
        values[5] = start_min + ( 5f * 
        			(float)Math.pow( 10, (double)(diffpower) ) );
      }  
    
      // this will round off the values to "nice" numbers
      for( int i = 0; i < values.length; i++ )
	values[i] = (float)Format.round( (double)values[i], 
        				     Math.abs(diffpower) + 3 );  
    } // end if xmin > xmax/10
    // else, use ...,.1,.2,.5,1,2,5,10,20,50,... scaling method.
    else
    {
      boolean zeroflag = false;
      if( temp_min < 0 )
      {
	// if not two sided, everything negative goes to zero.
	if( isTwoSided )
	{
          if( temp_max < -temp_min )
            temp_max = -temp_min; 
        }
	temp_min = 0;
	// if two sided, make interval symmetric
      }
      if( temp_min == 0 )
      {
	zeroflag = true;
	if( temp_max > 1000000 )
	      temp_min = 1;
	else
	      temp_min = temp_max / 10000000;
      }
      
      // find degree of xmax
      float end_max = Math.abs(temp_max);
      int xmax_power = 0;
      while( end_max >= 10 )
      {
	end_max = end_max / 10f;
	xmax_power++;
      }
      while ( end_max < 1.0 )
      {
	end_max = end_max * 10.0f;
	xmax_power--;
      }
    
      // find degree of xmin
      float start_min = Math.abs(temp_min);
      int xmin_power = 0;
      while( start_min >= 10 )
      {
	start_min = start_min / 10f;
	xmin_power++;
      }
      while ( start_min < 1.0 )
      {
	start_min = start_min * 10.0f;
	xmin_power--;
      }
    
      int step_index = 0;
      float[] steps = {1f,2f,5f};
      int step_power = xmin_power;
    
      start_min = start_min - .00001f; // try to eleviate rounding errors
      // set the value of the first step
      if( start_min <= 1 )     // start value is 1
	step_index = 0;
      else if( start_min <= 2 ) // start value is 2
	step_index = 1;
      else if( start_min <= 5 ) // start value is 5
	step_index = 2;
      else		       // start value is 10
      {
	step_index = 0;
	step_power++;
      }

      int maxcounter = 0;
      // find maximum ending value
      while( maxcounter < 3 && temp_max >= (steps[maxcounter] * 
	 Math.pow(10,(double)xmax_power )))
	maxcounter = maxcounter + 1; 

      // number of steps this interval is divided into.
      int numsteps = (int)(3 * (xmax_power - step_power) + 
			       maxcounter - step_index);

      int value_step = 0;
      // is zero was originally part of the interval, include it.
      if( zeroflag )
      { 
	numsteps = numsteps + step_index;
	step_index = 0;
        values = new float[++numsteps];
        values[0] = 0;
        value_step++;
      }
      else
        values = new float[numsteps];

      // fill in the array with the steps.
      for( int step = step_index; 
	       step < (3 * (xmax_power - step_power) + maxcounter); step++ )
      {
	values[value_step]= steps[step%3] * (float)Math.pow( 10, 
				(double)( Math.floor(step/3) + step_power) );
	value_step++; 
      }
    } // end else (inteval greater than one degree)

    return values;
  }
 
 /**
  * This method is used to find the Least Upperbound of a number.  This leastupper bound is 
  * considered to be a "nice" logrithmic number.  It uses one of five sets of numbers to find
  * the bound.  These sets are:
  * 1*10^x, 2*10^x, 5*10^x, 1.2*10^x, 1.5*10^x
  *
  *  @return  the least upper bound.
  */
  public float leastUpperBound()
  {
    // Create temporary min value so original interval is not altered.
    float temp_min = xmin;
    
    //find the least upperbound from the values 1*10^x, 2*10^x or 5*10^x
    if(temp_min <=0)
    {
      temp_min = 1;
    }
    int x = 0;

    while(1 * Math.pow(10, x) < (xmax))
      x++;
    int ones = (int)Math.pow(10, x);
    
    x=0;
    while(1.2 * Math.pow(10,x) < xmax)
      x++;
    float onetwo = (float)1.2 * (int)Math.pow(10,x);
    
    x = 0;
    while(1.5 * Math.pow(10,  x) < xmax)
      x++;
    float onefive = (float)1.5 * (int)Math.pow(10, x);
    
    x = 0;
    while(2* Math.pow(10, x) < (xmax))
      x++;
    int twos = 2 * (int)Math.pow(10 ,x);
    
    x = 0;
    while(5 * Math.pow(10, x) < (xmax))
      x++;
    
    int fives = 5 * (int)Math.pow(10, x);
    float lub = 0;
    if(ones < onetwo){
      lub = ones;
    }else{
      lub = onetwo;
    }
    if(onefive < lub){
      lub = onefive;
    }
    if(twos < lub){
      lub = twos;
    }
    if(fives < lub){
      lub = fives;
    }

    return lub;
  }
 
 /**
  * This method is used to find the Greatest Lower Bound of a number.  This leastupper bound is 
  * considered to be a "nice" logrithmic number.  It uses one of five sets of numbers to find
  * the bound.  These sets are:
  * 1*10^x, 2*10^x, 5*10^x, 1.2*10^x, 1.5*10^x
  *
  *  @return  the Greatest Lower bound.
  */
  public float greatestLowerBound()
  {
    // Create temporary min value so original interval is not altered.
    float temp_min = xmin;
    if(temp_min < 1)
      temp_min = 1;
	   
    int x = 0;
    while(1 * Math.pow(10, x) <= temp_min)
      x++;
    int lones = 1 * (int)Math.pow(10, x-1);
    
    x = 0;
    while(1.2 * (int)Math.pow(10, x) <= temp_min)
      x++;
    float lonetwo = (float)1.2 * (int)Math.pow(10, x-1);
    
    x = 0;
    while(1.5 * Math.pow(10, x) <= temp_min)
      x++;
    float lonefive = (float)1.5 * (int)Math.pow(10, x-1);
    
    x = 0;
    while(2 * Math.pow(10, x) <= temp_min)
      x++;
    int ltwos = 2 * (int)Math.pow(10, x-1);
    
    x = 0;
    while(5 * Math.pow(10, x) <= temp_min)
      x++;
    int lfives = 5 * (int)Math.pow(10, x-1);
    
    x = 0;
    float glb = 0;
    if(lones > lonetwo){
      glb = lones;
    }else{
      glb = lonetwo;
    }
    if(lonefive > glb){
      glb = lonefive;
    }
    if(ltwos > glb){
      glb = ltwos;
    }

    if(lfives > glb){
      glb = lfives;
    }

    return glb;
  }
  
 /**
  * This method find the psuedo power difference between two numbers by finding how many times
  * the lowernumber can be multiplied by ten befor it gets larger than the larger number.
  *  @param glb
  *  @param lub
  *  @return  the power difference.
  */
  public int powerdiff(float glb, float lub)
  {
    int powerdiff = 0;
    if(glb != 0)
    {
      while ((glb * Math.pow(10, powerdiff)) <= lub)
	powerdiff++;
    }
    return powerdiff;
  }
 
 /**
  * This method finds a starting point, step interval, and number of steps between a start value
  * and an end value
  * @param glb
  * @param lub
  *
  *  @return  float array of the values above.
  */
  public float[] subDivideLogLarge(float glb, float lub)
  {
    float[] values = new float[3];
    values[2] = (float)powerdiff(glb, lub);
    values[0] = glb;
    values[1] = glb;
 
    return values;
  }

 /*
    public float maxError(int axis){
       return ((float)Math.pow((xmax/xmin),(1/(float)axis)));
  }
  public float getError(float value, int axis){
       float r = 0;
       float power = (1/(float)axis);
       float number = xmax/value;
       r = (float)Math.pow(number,power);
       return r;
       
  }
  
  public boolean isNice(float error, float value, float number){
       //System.out.println("checking nice number");
       if((number/error <= value && (number*error >= value)) ){
	       return true;
       }else{
	       return false;
       }
       
  }*/
  
 /**
  * This method takes in a number and sets it to the power set in either
  * subDivide() or subDivideLog(). The default is E0.
  *
  *  @param  num
  *  @return formatted String number
  */	
  public String standardize( float num )
  {	 
    if( Math.abs(num) < 10000 && Math.abs(num) >= .001 
	&& format == Format.AUTO )		
      return Format.choiceFormat( (double)num, format );    
    if( format == Format.DECIMAL )
      return Format.choiceFormat( (double)num, format ); 
    
    if( format == Format.ENGINEER || format == Format.AUTO )
    {
      baseE = (int)(baseE/3);
      baseE = baseE*3;
    }
    return Format.setE( (double)num, baseE, sig_dig);
  }
  
 /**
  * Returns a String description of what type of formatting is used for
  * formatting the numbers.
  *
  *  @return format
  */ 
  public String getFormat()
  {
    if( format == Format.AUTO )
      return "Automatic (Decimal or Engineering) Format";
    if( format == Format.DECIMAL )
      return "Decimal Format";
    if( format == Format.SCIENTIFIC )
      return "Scientific Format";
    return "Engineering Format";
  }
  
 /**
  * This method is used by the subDivideLog() to determine whether negative
  * values are important or not. Set two sided to true if negative values are
  * of interest.
  *
  *  @param  includeNeg
  */
  public void setTwoSided( boolean includeNeg )
  {
    isTwoSided = includeNeg;
  }
  
 /*
  * Main Test Program
  */ 
  public static void main( String argv[] )
  {
    CalibrationUtil testcal = new CalibrationUtil( 0f, 49000f );
    //testcal.setTwoSided(false);
    float[] testvalue = testcal.subDivideLog();
    for( int i = 0; i < testvalue.length; i++ )
      System.out.println("Value: " + testvalue[i]);
    /*
    float[] testvalue = testcal.subDivide();
    System.out.println("TestStep = " + testvalue[0] );
    System.out.println("TestStart = " + testvalue[1] );  
    
    testcal = new CalibrationUtil( 50000f, 432000f, 3, Format.DECIMAL );
    
    testvalue = testcal.subDivide();
    
    System.out.println("Standardize 4007000: " + 
			 testcal.standardize( 4007000 ) );
    System.out.println("Standardize 400700: " + 
			 testcal.standardize( 400700 ) );
    System.out.println("Standardize 40070: " + 
			 testcal.standardize( 40070 ) );
    
    System.out.println("TestStep = " + testvalue[0] );
    System.out.println("TestStart = " + testvalue[1] );  
    
    testcal = new CalibrationUtil( .001f, .1f );
    
    testvalue = testcal.subDivide();
    
    System.out.println("TestStep = " + testvalue[0] );
    System.out.println("TestStart = " + testvalue[1] );  
    
    System.out.println("Format = " + testcal.getFormat() );  */     
  }
}
   
   
  

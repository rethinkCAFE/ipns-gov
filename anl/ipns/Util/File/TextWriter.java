/*
 * File:  TextWriter.java
 *
 * Copyright (C) 2003 Chris Bouzek
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
 *           Chris Bouzek <coldfusion78@yahoo.com>
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
 * Revision 1.4  2004/05/11 23:29:04  bouzekc
 * Created private constructor, as this class should not be instantiated.
 *
 * Revision 1.3  2004/03/11 22:31:22  millermi
 * - Changed package, added import for SharedMessages.
 * - Removed code that wrote out an error file since it was
 *   dependent on SharedData.
 *
 * Revision 1.2  2004/01/24 21:00:28  bouzekc
 * Removed unused variables and imports.
 *
 * Revision 1.1  2003/07/29 20:56:06  bouzekc
 * Added to CVS.
 *
 */
package gov.anl.ipns.Util.File;

import gov.anl.ipns.Util.Sys.SharedMessages;

import java.io.*;


/**
 * This is a class for utility printing Throwable stack traces, ASCII files,
 * and other text file things.
 */
public class TextWriter {
  //~ Constructors -------------------------------------------------------------

  /**
   * Do not instantiate
   */
  private TextWriter(  ) {}

  //~ Methods ------------------------------------------------------------------

  /**
   * Utility to write to an ASCII file using a FileWriter.  Handles the
   * possible exceptions in a generic manner.
   *
   * @param file2Write The File to write to.
   * @param text2Write The String to write to the file.
   */
  public static void writeASCII( File file2Write, String text2Write ) {
    FileWriter fw = null;

    try {
      fw = new FileWriter( file2Write );
      fw.write( text2Write );
    } catch( IOException e ) {
      e.printStackTrace(  );
      SharedMessages.addmsg( "Error saving file: " + file2Write.toString(  ) );
    } finally {
      if( fw != null ) {
        try {
          fw.close(  );
        } catch( IOException e ) {
          //let it drop on the floor
        }
      }
    }
  }

  /**
   * Utility to write to an ASCII file using a FileWriter.  Handles the
   * possible exceptions in a generic manner.
   *
   * @param file2Write The file name to write to.
   * @param text2Write The String to write to the file.
   */
  public static void writeASCII( String file2Write, String text2Write ) {
    TextWriter.writeASCII( new File( file2Write ), text2Write );
  }

  /**
   * Utility to print the stack trace of a throwable to a file.  Uses the
   * printStackTrace( PrintWriter ) method.
   *
   * @param file2Write The File to write to.
   * @param thrown The Throwable item you wish to print the stack trace for.
   */
  public static void writeStackTrace( File file2Write, Throwable thrown ) {
    PrintWriter pw = null;

    try {
      pw = new PrintWriter( new FileWriter( file2Write ) );
      thrown.printStackTrace( pw );
    } catch( IOException e2 ) {
      SharedMessages.addmsg( "Error writing stack trace and back trace to ." +
        file2Write.toString(  ) + ".  Please see console." );
      e2.printStackTrace(  );
    } finally {
      if( pw != null ) {
        pw.close(  );
      }
    }
  }

  /**
   * Utility to print the stack trace of a throwable to a file.  Uses the
   * printStackTrace( PrintWriter ) method.
   *
   * @param file2Write The file name to write to.
   * @param thrown The Throwable item you wish to print the stack trace for.
   */
  public static void writeStackTrace( String file2Write, Throwable thrown ) {
    TextWriter.writeStackTrace( new File( file2Write ), thrown );
  }
}

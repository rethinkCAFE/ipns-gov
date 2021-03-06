/*
 * File: TextFileReader.java
 *
 * Copyright (C) 2001-2002, Dennis Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.15  2005/06/27 02:11:10  dennis
 *  Added convenience method to skip comment lines beginning with a
 *  specified character.
 *
 *  Revision 1.14  2004/03/11 22:17:04  millermi
 *  - Changed package names and replaced SharedData with
 *    SharedMessages class.
 *
 *  Revision 1.13  2004/01/24 20:59:01  bouzekc
 *  Removed unused variables in main().
 *
 *  Revision 1.12  2003/02/19 23:17:58  dennis
 *  Changed read_line() to fill look_ahead "buffer" with next character
 *  in file.  This fixes a bug where eof() was not detected when reading
 *  a sequence of entire lines.
 *
 *  Revision 1.11  2003/02/18 00:31:50  dennis
 *  Now uses null character (char)0, as default value for look_ahead
 *  buffer.  However, the null character is not returned by any of the
 *  read routines.  This avoids an extra space at the beginning of lines.
 *  Renamed the original "robust" eof() method, that checked for any
 *  remaining non-blank characters in the file to end_of_data().  The
 *  eof() file now just checks for the end of file mark in the look_ahead
 *  buffer.
 *  The read_String(n_to_read) method now sets a mark and uses the
 *  "pre_mark_ch" variable to correctly "unread" fixed length values that
 *  were read with the fixed length read routines.
 *
 *  Revision 1.10  2002/11/27 23:23:49  pfpeterson
 *  standardized header
 *
 *  Revision 1.9  2002/08/05 19:00:32  pfpeterson
 *  Added reading of fixed format text files. Most of the original
 *  methods were cloned with a new parameter specifying how many
 *  characters to read. Also updated the documentation across the
 *  entire file.
 *
 *  Revision 1.8  2002/08/02 15:28:40  dennis
 *  skip_blanks now sets mark with larger buffer size limit.
 *
 *  Revision 1.7  2002/07/16 14:38:08  dennis
 *  Now maintain its own one character buffer that is used
 *  to reset the whitespace character terminating a non-blank
 *  string, in addition to the stream "mark" that allows
 *  un-reading the last non-blank string that was read.
 *
 *  Revision 1.6  2002/07/15 21:42:40  dennis
 *  The whitespace character that terminates a sequence of non-blank
 *  characters is now put back in the file when the non-blank sequence
 *  is read.
 *
 *  Revision 1.5  2002/06/07 16:19:53  dennis
 *  Now has additional constructors that accept an existing InputStream
 *  or an existing Reader, so that this TextFileReader can be "wrapped"
 *  around streams that are opened elsewhere (eg. an InputStream from
 *  a network connection).
 *
 *  Revision 1.4  2002/03/29 17:34:04  dennis
 *  Added eof() method.
 *  Removed unneeded try{}catch{} blocks in methods that throw
 *  exceptions.
 *  Methods that read numeric values now explicitly throw a
 *  NumberFormatException as well as an IOException
 *  Constructor now tries to close any resources that might have
 *  been allocated in an unsuccessful construction.
 *
 *  Revision 1.3  2002/03/27 22:54:29  pfpeterson
 *  All exceptions that are caught or thrown are now IOExceptions.
 *
 */

package gov.anl.ipns.Util.File;
import java.io.*;

/**
 *   This class supports reading of Strings, floats, ints etc. from an
 *   ordinary text file.  In addition to methods to read each of the 
 *   basic data types from the file, there is an "unread" method that 
 *   restores the last non-blank item that was read.  Error handling and
 *   end of file detection are done using exceptions.
 */
public class TextFileReader 
{
  public  static final String EOF = "End of file"; // string used to construct 
                                                   // the EOF exception 
  public  static final String EOL = "End of line"; // string used to construct
                                                   // the end-of-line exception

  private int                 look_ahead = 0;      // One char buffer
  private int                 pre_mark_ch;         // Saved buffer before mark

  public  static final int    BUFFER_SIZE = 200;   // Maximum number of chars 
                                                   // in a String or one line
  private BufferedReader      in       = null;
  private InputStreamReader   reader   = null;
  private boolean             close_in = true;     // Flag indicating whether
                                                   // it is our responsibility
                                                   // to close the Reader "in". 

  /* -------------------------- Constructor -------------------------- */
  /**
   *  Construct a TextFileReader to read from the specified file.  The
   *  constructor will throw an exception if the file can't be opened.  The
   *  other methods of this class should not be used if the file can't be
   *  opened.
   *
   *  @param file_name  The fully qualified file name.
   */
  public TextFileReader( String file_name ) throws IOException
  {
    try
    {
      reader = new FileReader( file_name );
      in     = new BufferedReader( reader );
      in.mark( BUFFER_SIZE );
      pre_mark_ch = look_ahead;
    }
    catch ( IOException e )
    {
      close();
      throw e;
    }
  }


  /* -------------------------- Constructor -------------------------- */
  /**
   *  Construct a TextFileReader to read from the specified byte stream. 
   *  NOTE: If the close method is invoked the readers constructed by this
   *        object will be closed, but the InputStream passed to this
   *        constructor will NOT be closed. 
   *
   *  @param stream  The byte stream from which to read data. 
   */
  public TextFileReader( InputStream stream ) throws IOException
  {
    try
    {
      reader = new InputStreamReader( stream );
      in     = new BufferedReader( reader );
      in.mark( BUFFER_SIZE );
      pre_mark_ch = look_ahead;
    }
    catch ( IOException e )
    {
      close();
      throw e;
    }
  }


  /* -------------------------- Constructor -------------------------- */
  /**
   *  Construct a TextFileReader to read from the specified character stream.
   *  NOTE: If the close method is invoked any reader constructed by this
   *        object will be closed, but the Reader passed to this constructor 
   *        will NOT be closed. 
   *
   *  @param stream  The character stream from which to read data.
   */
  public TextFileReader( Reader stream ) throws IOException
  {
    try
    {
      if ( stream instanceof BufferedReader )
      {
        in = (BufferedReader)stream;
        close_in = false;      // if someone else passes us the stream, we 
      }                        // won't close it.
      else
        in = new BufferedReader( stream );

      in.mark( BUFFER_SIZE );
      pre_mark_ch = look_ahead;
    }
    catch ( IOException e )
    {
      close();
      throw e;
    }
  }


  /* ---------------------------- eof -------------------------------- */
  /**
   *  Check for the end of file.  This eof() method just checks the look 
   *  ahead character.  If it is -1, the end of file has been reached.
   *  NOTE: This may be misleading, since there may just be whitespace 
   *        remaining in the file.
   *
   *  @return true if the last character has been read from the file.
   */
  public boolean eof()
  {
    if ( look_ahead < 0 )
      return true;

    return false;
  }


  /* ------------------------- end_of_data ------------------------ */
  /**
   *  Check for the end of non-blank characters in the file.  This 
   *  end_of_data() method will skip any blanks starting at the current 
   *  position in the file and will advance to the first non-blank character 
   *  remaining in the file, if there is one. If there is a non-blank character
   *  remaining in the file, end_of_data() will return false and that non-blank
   *  character will be the next character read from the file.  If no non-blank
   *  characters remain, end_of_data() returns true.  
   *  NOTE: This changes the current postion in the file.
   *
   *  @return true if no non-blank characters remain past the current position 
   *               in the file.   
   */
  public boolean end_of_data()
  {
    try
    {
      skip_blanks();
    }
    catch ( IOException e )
    {
      return true;
    }
    return false;
  }


  /* -------------------------- read_line ---------------------------- */
  /**
   *  Read one line from the file, starting at the current position in the
   *  file.  
   *
   *  @return The remaining characters on the current line of the file if
   *          there is one.
   *
   *  @throws IOException with the message TextFileReader.EOF, if the end
   *          of file has been reached.
   */
  public String read_line() throws IOException
  {
    if ( look_ahead == '\n' )
    {
      look_ahead = in.read();
      return new String("");
    }
    else
    {
      in.mark( BUFFER_SIZE );
      pre_mark_ch = look_ahead;
      String s = in.readLine();
      if ( s == null )
        throw new IOException( EOF );

      if ( look_ahead != 0 )                       // don't keep null chars
        s = "" + (char)look_ahead + s;
 
      look_ahead = in.read();                      // set look_ahead to next 
                                                   // character, or -1 if eof() 
      return s;
    }
  }
 

  /* -------------------------- skip_blanks ---------------------------- */
  /**
   *  Skip whitespace characters in the file, starting at the current 
   *  position, stopping at the first non-blank character.  The first
   *  non-blank character encountered is placed back in the file and 
   *  will be the next character read. 
   *
   *  @throws IOException with the message TextFileReader.EOF, if the end
   *          of file has been reached.
   */
  public void skip_blanks() throws IOException
  {
    in.mark(2);                           // prepare to reset to this position
    pre_mark_ch = look_ahead;             // at start of a non-blank string

    if ( look_ahead == 0 )
      look_ahead = (int)' ';

    while ( Character.isWhitespace( (char)look_ahead ) )
    {
      getc();
      in.mark(2);
      pre_mark_ch = look_ahead; 
    }

    if ( look_ahead < 0 )                                    // -1 is EOF
      throw new IOException( EOF );
    else
      in.mark( BUFFER_SIZE );
  }


  /* -------------------------- read_String ---------------------------- */
  /**
   *  Read a sequence of non-whitespace characters from the file, starting at 
   *  the current position in the file.  If the current position in the
   *  file is a whitespace character, whitespace characters will be skipped
   *  until the first non-whitespace character is encountered.  After reading
   *  the squences of non-whitespace characters, the following whitespace 
   *  character is read and NOT putback in the stream. 
   *
   *  @return The first non-blank sequence of characters encountered, 
   *          starting from the current position.
   *
   *  @throws IOException with the message TextFileReader.EOF, if the end
   *          of file has been reached.
   */
  public String read_String() throws IOException
  {
    byte buffer[] = new byte[BUFFER_SIZE];
    int  n = 0;

    skip_blanks();

    while ( !Character.isWhitespace( (char)look_ahead ) && 
             look_ahead >= 0                            &&     // -1 is EOF
             n  < BUFFER_SIZE                            )
    {
      if ( look_ahead != 0 )                              // ignore null chars
      {
        buffer[n] = (byte)look_ahead;
        n++;
      }
      getc();
    }
       
    if ( n > 0 )
      return new String( buffer, 0, n );
    else
      throw new IOException( EOF );
  }


  /* -------------------------- read_String ---------------------------- */
    /**
     * Read a sequence of characters from the file, starting at the
     * current position in the file. This is intended for use with
     * fixed format files.
     *
     * @param n_char The number of characters to read in.
     *
     * @return A String consisting of the specified number of characters.
     *
     * @throws IOException with the message TextFileReader.EOF, if the
     *         end of the file has been reached or TextFileReader.EOL
     *         if the end of the line has been reached.
     */
    public String read_String(int n_char) throws IOException{
        byte buffer[]=new byte[n_char];
        int chr=0;

        in.mark( BUFFER_SIZE );
        pre_mark_ch = look_ahead;

        for( int i=0 ; i<n_char ; i++ ){
            chr = getc();
            if(chr=='\n'){
                throw new IOException( EOL );
            }else if(chr>=0){
                buffer[i]=(byte)chr;
            }else{
                throw new IOException(EOF);
            }
        }
        return new String(buffer,0,n_char);
    }


  /* -------------------------- read_int ---------------------------- */
  /**
   *  Read a sequence of non-whitespace characters from the file, starting at
   *  the current position in the file and construct an int value from the
   *  the characters, if possible.
   *
   *  @return The int value represented by the next sequence of non-blank
   *          characters in the file.
   *
   *  @throws IOException with the message TextFileReader.EOF, if the end
   *          of file has been reached.
   *
   *  @throws NumberFormatException if the characters don't represent
   *          an int.
   */
  public int read_int() throws IOException, NumberFormatException
  {
    String s = read_String();
    int val = (new Integer( s )).intValue();
    return val;
  }


  /* -------------------------- read_int ---------------------------- */
  /**
   *  Read a sequence of characters from the file, starting at the
   *  current position in the file and construct an int value from the
   *  the characters, if possible.
   *
   *  @param n_char The number of characters to read in.
   *
   *  @return The int value represented by the next sequence of
   *          characters in the file.
   *
   *  @throws IOException with the message TextFileReader.EOF, if the
   *          end of the file has been reached or TextFileReader.EOL
   *          if the end of the line has been reached.
   *
   *  @throws NumberFormatException if the characters don't represent
   *          an int.
   */
  public int read_int(int n_char) throws IOException, NumberFormatException{
    String s = read_String(n_char);
    s        = s.trim();
    int val = (new Integer( s )).intValue();
    return val;
  }


  /* -------------------------- read_float ---------------------------- */
  /**
   *  Read a sequence of non-whitespace characters from the file, starting at
   *  the current position in the file and construct a float value from the
   *  the characters, if possible. 
   *
   *  @return The float value represented by the next sequence of non-blank
   *          characters in the file.
   *
   *  @throws IOException with the message TextFileReader.EOF, if the end
   *          of file has been reached.
   *  @throws NumberFormatException if the characters don't represent
   *          a float.
   */
  public float read_float() throws IOException, NumberFormatException
  {
    String s = read_String();
    float val = (new Float( s )).floatValue();
    return val;
  }


  /* -------------------------- read_float ---------------------------- */
  /**
   *  Read a sequence of characters from the file, starting at the
   *  current position in the file and construct a float value from
   *  the the characters, if possible.
   *
   *  @param n_char The number of characters to read in.
   *
   *  @return The float value represented by the next sequence of
   *          characters in the file.
   *
   *  @throws IOException with the message TextFileReader.EOF, if the
   *          end of the file has been reached or TextFileReader.EOL
   *          if the end of the line has been reached.
   *
   *  @throws NumberFormatException, if the characters don't represent
   *          a float.
   */
  public float read_float(int n_char) throws IOException, NumberFormatException{
    String s = read_String(n_char);
    s        = s.trim();
    float val = (new Float( s )).floatValue();
    return val;
  }


  /* -------------------------- read_double ---------------------------- */
  /**
   *  Read a sequence of non-whitespace characters from the file, starting at
   *  the current position in the file and construct a double value from the
   *  the characters, if possible.
   *
   *  @return The double value represented by the next sequence of non-blank
   *          characters in the file.
   *
   *  @throws IOException with the message TextFileReader.EOF, if the end
   *          of file has been reached.
   *
   *  @throws NumberFormatException if the characters don't represent
   *          a double.
   */
  public double read_double() throws IOException, NumberFormatException
  {
    String s = read_String();
    double val = (new Double( s )).doubleValue();
    return val;
  }


  /* -------------------------- read_double ---------------------------- */
  /**
   *  Read a sequence of characters from the file, starting at the
   *  current position in the file and construct a double value from
   *  the the characters, if possible.
   *
   *  @param n_char The number of characters to read in.
   *
   *  @return The double value represented by the next sequence of
   *          characters in the file.
   *
   *  @throws IOException with the message TextFileReader.EOF, if the
   *          end of the file has been reached or TextFileReader.EOL
   *          if the end of the line has been reached.
   *
   *  @throws NumberFormatException if the characters don't represent
   *          a double.
   */
  public double read_double(int n_char) throws IOException,
                                               NumberFormatException{
    String s = read_String(n_char);
    s        = s.trim();
    double val = (new Double( s )).doubleValue();
    return val;
  }


  /* -------------------------- read_boolean ---------------------------- */
  /**
   *  Read a sequence of non-whitespace characters from the file, starting at
   *  the current position in the file and construct a boolean value from the
   *  the characters.
   *
   *  @return The boolean value represented by the next sequence of non-blank
   *          characters in the file.  The value is "true" if the sequence of
   *          non-blank characters matches "true" ignoring case and is false
   *          otherwise.
   *
   *  @throws IOException with the message TextFileReader.EOF, if the end
   *          of file has been reached.
   */
  public boolean read_boolean() throws IOException
  {
    String s = read_String();
    boolean val = (new Boolean( s )).booleanValue();
    return val;
  }

  /**
   *  Read a sequence of characters from the file, starting at the
   *  current position in the file and construct a boolean value from
   *  the the characters.
   *
   *  @param n_char The number of characters to read in.
   *
   *  @return The boolean value represented by the next sequence of
   *          characters in the file.  The value is "true" if the
   *          sequence of non-blank characters matches "true" ignoring
   *          case and is false otherwise.
   *
   *  @throws IOException with the message TextFileReader.EOF, if the
   *          end of the file has been reached or TextFileReader.EOL
   *          if the end of the line has been reached.
   */
  public boolean read_boolean(int n_char) throws IOException
  {
    String s = read_String(n_char);
    s        = s.trim();
    boolean val = (new Boolean( s )).booleanValue();
    return val;
  }


  /* ---------------------------- read_char ------------------------------ */
  /**
   *  Read the next character from the file, including blanks.
   *
   *  @return The next character in the file.
   *
   *  @throws IOException with the message TextFileReader.EOF, if the end
   *          of file has been reached.
   */
  public char read_char() throws IOException
  {
    in.mark(2);
    pre_mark_ch = look_ahead;

    return (char)(getc());
  }


  /* ----------------------------- unread ------------------------------ */
  /**
   *  Put the last float, double, int, boolean, char or String read from 
   *  the file, back into the file so that it can be read again.  
   *
   *  @throws IOException if something goes wrong when trying to reset the
   *          BufferedReader.
   */
  public void unread() throws IOException
  {
    in.reset();
    look_ahead = pre_mark_ch; 
  }


  /* ----------------------------- close ------------------------------ */
  /**
   *  Close any stream or file that was opened when this object was constructed
   *  NOTE: If this was constructed using an existing InputStream or Reader
   *        object the stream or reader passed to the constructor will NOT
   *        be closed.
   *
   *  @throws IOException if something goes wrong when trying to close the
   *          BufferedReader or FileInputStream.
   */
  public void close() throws IOException
  {
    if ( reader != null )
      reader.close();

    if ( in != null && close_in ) 
      in.close();
  }


  /* ---------------------- SkipLinesStartingWith ------------------------ */
  /**
   *  Skip lines that begin with a certaing string, such as "#"
   * 
   *  @param  skip_string  The tag at the start of the line, indicating
   *                       that it should be skipped.
   */
  public void SkipLinesStartingWith( String skip_string ) throws IOException
  {
    while ( read_line().startsWith( skip_string ) )
      ;  // empty loop

    unread();
  }

   
  /* ------------------------------ getc ---------------------------------- */
  /*
   *  Get the next "logical" character from the file, which is currently stored
   *  in the one character look_ahead "buffer".  Put the next actual character
   *  from the file into the look_ahead buffer.
   */
  private int getc() throws IOException
  {
    int temp;

    if ( look_ahead == 0 )          // no real look_ahead value, so 
      temp = in.read();             // get an actual character from the file
    else
      temp = look_ahead;            // return the look_ahead value

    look_ahead = in.read();         // and look ahead to the next character
    return temp;
  }


  /* --------------------------  main  ---------------------------------- */
  /*
   *  Main program for testing purposes only.
   */
  public static void main( String args[] )
  {
    String         line  = "";
    float          f_num = 0;
    int            i_num = 0;
    char           ch    = 0;
    boolean        b     = false;
    TextFileReader f     = null;

    try
    {
      f = new TextFileReader( "lines.dat" );

      while ( !f.eof() )
      {
        line = f.read_line();
        System.out.println(line);
      }
    }
    catch ( Exception e )
    {
      System.out.println("1: EXCEPTION: " + e );
    }

    try
    {
                     // Construct the TextFileReader using one of:
                     //   -- file name
                     //   -- byte stream
                     //   -- character stream

      // f = new TextFileReader("my_file.dat");
         f = new TextFileReader( new FileInputStream("my_file.dat") );
      // f = new TextFileReader( new FileReader("my_file.dat") );

      line = f.read_line();
      System.out.println("First line is " + line );
      f.unread();
      line = f.read_line();
      System.out.println("First line again is " + line );

      b = f.read_boolean();
      System.out.println("boolean val: " + b );
      f.unread();
      b = f.read_boolean();
      System.out.println("boolean val again: " + b );
      f.read_line();
    
      f_num = f.read_float();
      System.out.println("float value is " + f_num );
      f.unread();
      f_num = f.read_float();
      System.out.println("float value again is " + f_num );

      i_num = f.read_int();
      System.out.println("int value is " + i_num );
      f.unread();
      i_num = f.read_int();
      System.out.println("int value again is " + i_num );

      f.skip_blanks();
      ch = f.read_char();
      System.out.println("char value is " + ch );
      f.unread();
      ch = f.read_char();
      System.out.println("char value again is " + ch );

      f.skip_blanks();
      ch = f.read_char();
      System.out.println("char value is " + ch );
      f.unread();
      ch = f.read_char();
      System.out.println("char value again is " + ch );
 
      line = f.read_line();
      System.out.println("rest of line is <" + line + ">" );
      //f.close();
    }
    catch ( Exception e )
    {
      System.out.println("1: EXCEPTION: " + e );
    }

    try
    {
      f_num = f.read_float();
      System.out.println("A:Read : " + f_num );

      f_num = f.read_float();
      System.out.println("A:Read : " + f_num );
   
      f.read_line();
      f_num = f.read_float();
      System.out.println("A:Read : " + f_num );
    }
    catch ( Exception e )
    {
      System.out.println("2: EXCEPTION: " + e );
    }

    try{
        line = f.read_line();
        System.out.println("read line is <" + line + ">" );
        //f.read_char();
        String str=f.read_String(8);
        System.out.println("FORMATTED: "+str);
        f.unread();
        str=f.read_String(8);
        System.out.println("FORMATTED AGAIN: "+str);
        f.unread();
        int i=f.read_int(2);
        System.out.println("FORMATTED YET AGAIN: "+i);
        double d=f.read_double(5);
        System.out.println(" "+d);
        f.read_line();
        float fl=f.read_float(4);
        System.out.println(" "+fl);
    }catch( Exception e ){
        System.out.println("3: EXCEPTION: " + e );
    }

    while ( f != null && !f.end_of_data() )
    {
      try
      {
        f_num = f.read_float();
        System.out.println("B:Read : " + f_num );
      }
      catch ( Exception e )
      {
        System.out.println("4: EXCEPTION: " + e );
      }
    }
  }
} 

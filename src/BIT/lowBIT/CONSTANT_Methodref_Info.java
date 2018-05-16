/* CONSTANT_Methodref_Info.java
 * Part of BIT -- Bytecode Instrumenting Tool
 *
 * Copyright (c) 1997, The Regents of the University of Colorado. All
 * Rights Reserved.
 * 
 * Permission to use and copy this software and its documentation for
 * NON-COMMERCIAL purposes and without fee is hereby granted provided
 * that this copyright notice appears in all copies. If you wish to use
 * or wish to have others use BIT for commercial purposes please contact,
 * Stephen V. O'Neil, Director, Office of Technology Transfer at the
 * University of Colorado at Boulder (303) 492-5647.
 *  
 * By downloading BIT, the User agrees and acknowledges that in no event
 * will the Regents of the University of Colorado be liable for any
 * damages including lost profits, lost savings or other indirect,
 * incidental, special or consequential damages arising out of the use or
 * inability to use the BIT software.
 * 
 * BIT was invented by Han Bok Lee at the University of Colorado in
 * Boulder, Colorado.
 */


package BIT.lowBIT;

import java.io.*;

public class CONSTANT_Methodref_Info extends Cp_Info {
  // data member
  // index of the class into the constant_pool table
  public short class_index;
  
  // index of name and descriptor of the method
  public short name_and_type_index;
  
  // constructor
  public CONSTANT_Methodref_Info(DataInputStream iStream, byte tag)
    throws IOException {
    this.tag = tag;
    class_index = (short) iStream.readUnsignedShort();
    name_and_type_index = (short) iStream.readUnsignedShort();
  }

  public CONSTANT_Methodref_Info(short class_index, short name_and_type_index) {
    this.tag = Constant.CONSTANT_Methodref;
    this.class_index = class_index;
    this.name_and_type_index = name_and_type_index;
  }

  public void write(DataOutputStream oStream)
    throws IOException {
      oStream.writeByte((int) tag);
      oStream.writeShort((int) class_index);
      oStream.writeShort((int) name_and_type_index);
  }

  public boolean equals(Object obj) {
    if (obj instanceof CONSTANT_Methodref_Info) {
      return ((class_index == ((CONSTANT_Methodref_Info)obj).class_index) &&
              (name_and_type_index == ((CONSTANT_Methodref_Info)obj).name_and_type_index));
    }
    return false;
  }
  public int size() { return 5; }
}


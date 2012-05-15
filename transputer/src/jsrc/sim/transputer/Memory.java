/*
 *  Copyright 2012 Jose Sebastian Reguera Candal
 *
 *  This file is part of Tremor.
 *
 *  Tremor is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Tremor is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Tremor.  If not, see <http://www.gnu.org/licenses/>. 
 */
package jsrc.sim.transputer;

public class Memory {

	private short start;
	
	private byte[] contents;
		
	public Memory(short start, int size) {
		this.start = start;
		this.contents = new byte[size];
	}

	public byte getByteMem(short address) {
		return contents[(address + start) & 0xFFFF];
	}

	public void setByteMem(short address, byte value) {
		contents[address + start] = value;
	}
	
	public short getMem(short address) {
		final byte lo = contents[address + start];
		final byte hi = contents[address + start + 1];
		return (short) ((lo & 0xFF) | (hi << 8));
	}

	public void setMem(short address, short value) {
		contents[address + start] = (byte) (value & 0xFF);
		contents[address + start + 1] = (byte) (value >>> 8);		
	}
	
	// Testing
	void initMem(short start, byte[] contents) {
		System.arraycopy(contents, 0, this.contents, start - this.start, contents.length);
	}

}

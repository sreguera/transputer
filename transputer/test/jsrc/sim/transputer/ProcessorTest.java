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

import static org.junit.Assert.*;

import org.junit.Test;

public class ProcessorTest {

	@Test
	public void ldc() {
		byte[] instructions = {0x43}; // ldc 3
		assertEquals(3, new Processor().testExecute(instructions));
	}

	@Test
	public void ldc4() {
		byte[] instructions = {0x21, 0x22, 0x23, 0x44};
		// pfix 1; pfix 2; pfix 3; ldc 4
		assertEquals(0x1234, new Processor().testExecute(instructions));
	}

	@Test
	public void ldc4neg() {
		byte[] instructions = {0x28, 0x20, 0x20, 0x40};
		// pfix 8; pfix 0; pfix 0; ldc 0
		assertEquals(-32768, new Processor().testExecute(instructions));
	}
	
	@Test
	public void eqcEq() {
		byte[] instructions = {0x43, (byte) 0xC3}; // ldc 3; eqc 3
		assertEquals(1, new Processor().testExecute(instructions));
	}
	
	@Test
	public void eqcNeq() {
		byte[] instructions = {0x43, (byte) 0xC2}; // ldc 3; eqc 2
		assertEquals(0, new Processor().testExecute(instructions));
	}

	@Test
	public void rev() {
		byte[] instructions = {0x43, 0x42, (byte) 0xF0}; // ldc 3; ldc 2; rev
		assertEquals(3, new Processor().testExecute(instructions));
	}

	@Test
	public void sum() {
		byte[] instructions = {
				0x43, 0x42, // ldc 3; ldc 2
				0x25, (byte) 0xF2 // pfix 5; opr 2 == sum
		}; 
		assertEquals(5, new Processor().testExecute(instructions));		
	}

	@Test
	public void diff() {
		byte[] instructions = {
				0x43, 0x42, // ldc 3; ldc 2
				(byte) 0xF4 // opr 4 == diff
		}; 
		assertEquals(1, new Processor().testExecute(instructions));		
	}

}

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

public class LinkTest {

	@Test
	public void test() {
		Memory m1 = new Memory((short) 0, 100);
		Link l1 = new Link(m1);
		
		Memory m2 = new Memory((short) 0, 100);
		Link l2 = new Link(m2);
		
		l1.setPeer(l2);
		l2.setPeer(l1);
		
		byte[] data = {1, 2, 3, 4, 5, 6, 7, 8};
		byte[] zeros = {0, 0, 0, 0, 0, 0, 0, 0};
		
		m1.initMem((short) 0, data);
		m2.initMem((short) 0, zeros);
		
		l1.start();
		l2.start();
		
		l1.send((short) 123, (short) 0, (short) data.length);
		l2.receive((short) 456, (short) 0, (short) data.length);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int i = 0; i < data.length; i++) {
			assertEquals(m2.getByteMem((short) i), data[i]);
		}
	}

}

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

public class Link {

	private Memory memory;
	
	private Link peer;
	
	class Spec {
		public short process;
		public short address;
		public short length;
	}

	private Spec inputSpec;
	
	private Spec outputSpec;
	
	private Boolean receivedAck;
	private Boolean receivedData;
	private byte data;
	
	public Link(Memory memory) {
		this.inputSpec = new Spec();
		this.outputSpec = new Spec();
		this.receivedAck = new Boolean(false);
		this.receivedData = new Boolean(false);
		this.memory = memory;
	}
	
	public void setPeer(Link peer) {
		this.peer = peer;
	}
	
	public void start() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				receiveTask();
			}
		}).start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				sendTask();
			}
		}).start();
	}
	
	public synchronized void send(short process, short address, short length) {
		outputSpec.process = process;
		outputSpec.address = address;
		outputSpec.length = length;
		notifyAll(); //XXX outputSpec.notify();
	}
	
	public synchronized void receive(short process, short address, short length) {
		inputSpec.process = process;
		inputSpec.address = address;
		inputSpec.length = length;
		notifyAll(); //XXX inputSpec.notify();
	}
	
	private synchronized void sendTask() {
		try {
			while (true) {
				while (outputSpec.length == 0) {
					wait(); //XXX outputSpec.wait();
				}
				while (outputSpec.length > 0) {
					peer.receiveData(memory.getByteMem(outputSpec.address));
					outputSpec.address += 1;
					outputSpec.length -= 1;
					while (!receivedAck) {
						wait(); //XXX receivedAck.wait();
					}
					receivedAck = false;
				}
			}
		} catch (InterruptedException e) {
			
		}
	}
	
	private synchronized void receiveTask() {
		try {
			while (true) {
				while (inputSpec.length == 0) {
					wait(); //XXX inputSpec.wait();
				}
				while (inputSpec.length > 0) {
					while (!receivedData) {
						wait(); //XXX receivedData.wait();
					}
					receivedData = false;
					memory.setByteMem(inputSpec.address, data);
					inputSpec.address += 1;
					inputSpec.length -= 1;
					peer.receiveAck();
				}
			}
		} catch (InterruptedException e) {
			
		}
	}
	
	private synchronized void receiveAck() {
		receivedAck = true;
		notifyAll(); //XXX receivedAck.notify();
	}
	
	private synchronized void receiveData(byte data) {
		this.data = data;
		receivedData = true;
		notifyAll(); //XXX receivedData.notify();
	}
}

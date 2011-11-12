package jsrc.sim.transputer;

public class Main {

	public static void main(String[] args) {
		System.out.println("Hello");
		Processor p = new Processor();
		p.reset();
		p.step();
	}
}

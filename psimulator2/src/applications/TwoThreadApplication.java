/*
 * Erstellt am 13.3.2012.
 */
package applications;

import device.Device;

/**
 * Toto je trida pro aplikace, ktera nejen ze posloucha pozadavky za site, ale taky sama neco dela. K tomu, aby mohla
 * delat obe veci najednou ma 2 vlakna, jedno klasicky pres SmartRunable, ktery se probouzi a zase uspava podle
 * pozadavku ze site a druhy uplne normalni javovsky vlakno, ktery neco udela a pak se ukonci. Takhle funguje napr. ping
 * nebo traceroute.
 *
 * @author Tomas Pitrinec
 */
public abstract class TwoThreadApplication extends Application implements Runnable {

	private Thread myThread;
	/**
	 * Jestli se ma bezet nebo uz skoncit. Potomci si ho sami musi volat!
	 */
	protected boolean die = false;

	public TwoThreadApplication(String name, Device device) {
		super(name, device);
		myThread = new Thread(this);
	}

	/**
	 * Prepisuju metodu zdedenou po aplikaci, aby se v ni spustilo to moje vlakno.
	 */
	@Override
	public void start() {
		super.start();
		myThread.start();
	}

	@Override
	public void exit() {
		die = true;
		super.exit();
	}

	@Override
	public void kill() {
		die = true;
		super.kill();
	}
}

/*
 * created 26.3.2012
 */

package applications;

import commands.ApplicationNotifiable;
import dataStructures.IcmpPacket;
import dataStructures.IpPacket;
import dataStructures.ipAddresses.IpAddress;
import device.Device;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import logging.Logger;
import logging.LoggingCategory;
import psimulator2.Psimulator;
import utils.Util;
import utils.Wakeable;

/**
 *
 * @author Stanislav Rehak <rehaksta@fit.cvut.cz>
 */
public abstract class TracerouteApplication extends TwoThreadApplication implements Wakeable {

	protected IpAddress target;
	protected final ApplicationNotifiable command;
	protected int payload = 56; // default linux size (without header)
	protected int maxTTL = 30; // traceroute -m
	protected int firstTTL = 1; // traceroute -f
	protected int timeout = 5_000; // traceroute -w
	protected int queryPerTTL = 3; // traceroute -q
	protected Method method = Method.ICMP; // na linuxu je defaultne UDP
	protected transient boolean targetReached = false;
	protected transient int targetTTL = -1;

	protected Map<Integer, Double> timeForTTL = new HashMap<>();

	protected List<List<Record>> records;

	protected int printedTTL = 0;

	/**
	 * Key - seq <br />
	 * Value - timestamp in ms
	 */
	protected Map<Integer, Timestamp> timestamps = new HashMap<>();

	private transient boolean zavolanoBudikem = false;

	public TracerouteApplication(Device device, ApplicationNotifiable command) {
		super("traceroute", device);
		this.command = command;
	}

	public enum Method {
		UDP, // will be implement later
		ICMP,
	}

	// metody pro predavani informaci z jinejch vlaken -------------------------------------------------------------------

	@Override
	public void wake() {
		if (isRunning()) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "Byl jsem probuzen budikem a jdu zavolat svuj worker.", null);
			zavolanoBudikem = true;
			worker.wake();
		} else {
			Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "Byl jsem probuzen budikem ale nevolam svuj worker, protoze mam bejt mrtvej.", null);
		}
	}

	//pak je tady jeste receivePacket, ktera je podedena od Application



	// metody na vyrizovani sitovejch pozadavku: --------------------------------------------------------------------------

	/**
	 * Dela totez co jinde u ruznejch modulu, tzn kontroluje buffery a vyrizuje je. Poprve je spustena k vyrizeni prvniho pozadavku nejakym jinym vlaknem.
	 */
	@Override
	public void doMyWork() {
//		TODO

		Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "Spustena metoda doMyWork vlaknem "+Util.threadName(), null);
		if (buffer.isEmpty()) {
//			Logger.log(this, Logger.WARNING, LoggingCategory.TRACEROUTE_APPLICATION, "doMyWork(): spusteno, ale buffer je prazdny!!!  vlaknem "+Util.threadName(), null);
			if (!timestamps.containsKey(1)) { // nebyl jeste odeslan 1. paket
				return;
			}
		}


		IcmpPacket packet;

		while (!buffer.isEmpty()) {
			IpPacket p = buffer.remove(0);
			double arrivalTime = (double)System.nanoTime()/1_000_000;

			// zkouseni, jestli je ten paket spravnej:
			if (! (p.data instanceof IcmpPacket)) {
				Logger.log(this, Logger.WARNING, LoggingCategory.TRACEROUTE_APPLICATION, "Dropping packet: TracerouteApplication recieved non ICMP packet", p);
				continue;
			}

			packet = (IcmpPacket) p.data;

			Timestamp t = timestamps.get(packet.seq);
			// TODO: duplikace

			if (t == null) {
				Logger.log(this, Logger.WARNING, LoggingCategory.TRACEROUTE_APPLICATION, "Dropping packet: TracerouteApplication doesn't expect such a PING reply "
						+ "(IcmpPacket with this seq=" + packet.seq + " was never send OR it was served in a past)", p);
				continue;
			}

			double delay = arrivalTime - t.timestamp;

			// vsechno v poradku, paket se zpracuje:
			if (delay <= timeout) { // ok, paket dorazil vcas
				Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "Dorazil mi nejaky ping.", packet);

				if (p.src.equals(target) && packet.type == IcmpPacket.Type.REPLY && packet.code == IcmpPacket.Code.ZERO) {
					// ok, dopingnul jsem se do cile -> uz dalsi ICMP request neposilat
					targetReached = true;
					targetTTL = t.ttl;
				}

//				try {
				Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "Pridavam paket: ttl="+t.ttl+", seq="+packet.seq+", seznamu c."+(t.ttl-1), null);
				records.get(t.ttl - 1).add(new Record(delay, packet, p.src)); // pridavam paket do seznamu indexovanyho dle TTL-1 (indexy zacinaj od 0)
//				} catch (IndexOutOfBoundsException e) {
//					Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "Paket s TTL: "+t.ttl, null);
//					Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "timestamp map: "+timestamps, null);
//
//					e.printStackTrace();
//				}
			} else {
				Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "Dorazil mi nejaky ping, ale vyprsel timeout.", packet);

				records.get(t.ttl - 1).add(new Record(null, packet, p.src)); // dam tam null, aby se to jednodusejc vypisovalo
			}

			// tak, tady mam vsechny dosud prijate pakety v records




			//==========================================================================================================


			// tady bych mel checknout, jestli uz vyprsel cas pro danou tridu, abych ji uz mohl vypsat

			if (printedTTL >= t.ttl) {
				Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "Pakety s TTL odesilatele = " + t.ttl + " uz byly vypsany.", packet);
				continue;
			}

			processTTL(t.ttl);
		}

		// tady projed vserchny nevypsany TTL, zda je uz nemam nahodou vypsat
		checkRecords();

		if(zavolanoBudikem){ // TODO: domyslet
			exit();
		}
	}

	private void checkRecords() {
		if (!isRunning()) {
			return;
		}

		Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "checkRecords() zavolano.", null);

		for (int ttl = printedTTL + 1; ttl <= maxTTL; ttl++) {
			processTTL(ttl);
		}
	}

	private void processTTL(int ttl) {
		// vyprsel cas pro danou TTL radu?
//		Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "processTTL() ttl="+ttl, null);

		if (printedTTL >= ttl) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "Uz vypsano. ttl="+ttl + ", printedTTL="+printedTTL, null);
			return;
		}

		Double now = (double)System.nanoTime()/1_000_000;
		Double timeTTL = timeForTTL.get(ttl);

		if (timeTTL == null) {
//			Logger.log(this, Logger.WARNING, LoggingCategory.TRACEROUTE_APPLICATION, "Toto by nikdy nemelo nastat,ttl="+ttl+" printedTTL="+printedTTL+" vlakno: "+Util.threadName(), null);
			return;
		}

		if (now - timeTTL > (double) timeout || records.get(ttl - 1).size() == queryPerTTL) { //		ANO - vypsat ji

			Record temp = null;
			if (records.get(ttl - 1).size() > 0) {
				temp = records.get(ttl - 1).get(0);
			}
			String address;
			if (temp == null) {
				address = "*";
			} else {
				address = temp.sender.toString();
			}

			Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "Pocet zaznamu v seznamu c."+(ttl-1) + ", size:"+records.get(ttl - 1).size(), null);

			lineBeginning(ttl, address);
			int count = 0;
			for (Record record : records.get(ttl - 1)) { // indexovano od 0, ale TTL zacinaj na 1
				printPacket(record);
				count++;
			}
			for (int i = count; i < queryPerTTL; i++) {
				printTimeout();
			}
			lineEnding();

			printedTTL = ttl;
			Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "Vypisuju radek s ttl="+ttl, null);
			if (targetReached && targetTTL == printedTTL) {
				Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "volam metodu exit, protoze jsem dosahl cile.", null);
				exit();
			}
		}
	}

	// metody na delani vlastni prace: ------------------------------------------------------------------------------------

	/**
	 * Tahleta metoda bezi ve vlastnim javovskym vlakne ty aplikace. Posila pingy.
	 */
	@Override
	public void run() {
		Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "Spustena metoda run vlaknem "+Util.threadName(), null);
		int seq = 1;
		int ttl = firstTTL;

		while ((ttl <= maxTTL && !targetReached) && isRunning()) {	// bezi se jen dokud je running

			timeForTTL.put(ttl, (double)System.nanoTime()/1_000_000); // musi tu byt kvuli tomu, ze uz pak prijde paket

			for (int attempt = 0; attempt < queryPerTTL; attempt++) {
				Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, getName() + " posilam ping seq=" + seq + ", ttl="+ ttl, null);
				timestamps.put(seq, new Timestamp((double)System.nanoTime()/1_000_000, ttl));
				transportLayer.icmphandler.sendRequest(target, ttl, seq, port, payload);
				seq++;

				Util.sleep(100); // TODO: pak smazat?
			}

			timeForTTL.put(ttl, (double)System.nanoTime()/1_000_000); // spravne pocitani casu

			ttl++;
			Psimulator.getPsimulator().budik.registerWake(this, timeout); // vypsat hlasku o pripadnych nedoslych paketech - bude se vypisovat po celym radku!

		}
		Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "Konci metoda run. Opustena vlaknem "+Util.threadName(), null);
	}

	// abstraktni metody, ktery je potreba doimplementovat v konkretnich traceroutech: ----------------------------------------

	/**
	 * Message at start.
	 */
	protected abstract void startMessage();
	/**
	 * Prints beginning of each line: ' 1  192.168.1.254 (192.168.1.254)  '.
	 * @param ttl
	 */
	protected abstract void lineBeginning(int ttl, String address);
	protected abstract void printPacket(Record record);
	/**
	 * Prints end of line for one TTL.
	 */
	protected abstract void lineEnding();
	/**
	 * Prints string when 1 packet not arrived.
	 */
	protected abstract void printTimeout();

	// metody spousteny pri startovani a ukoncovani aplikace: -------------------------------------------------------------

	@Override
	protected synchronized void atExit() {
		//
	}

	@Override
	protected void atKill(){
		command.applicationFinished();
	}


	@Override
	protected void atStart() {
		//kontrola cilovy adresy:
		if (target == null) {
			Logger.log(this, Logger.WARNING, LoggingCategory.TRACEROUTE_APPLICATION, name+" has no target! Exiting..", null);
			kill();
		}

		//kontrola, jestli mam port:
		if (getPort() == null) {
			Logger.log(this, Logger.WARNING, LoggingCategory.TRACEROUTE_APPLICATION, name+" has no port assigned! Exiting..", null);
			kill();
		}

		Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, getName()+" atStart()", null);

		records = new ArrayList<>();
		for (int i = 0; i < maxTTL-firstTTL+1; i++) {
			records.add(new ArrayList<Record>());
		}
		Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "Vytvoreno tolik TTL seznamu: "+records.size(), null);
//		Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "TTLka a casy odeslani: "+timeForTTL.toString(), null);
		vypisMapu();

		startMessage();
	}

	public void setSize(int size) {
		this.payload = size;
	}

	public void setTarget(IpAddress target) {
		this.target = target;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setFirstTTL(int firstTTL) {
		this.firstTTL = firstTTL;
	}

	public void setMaxTTL(int maxTTL) {
		this.maxTTL = maxTTL;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public void setQueryPerTTL(int queryPerTTL) {
		this.queryPerTTL = queryPerTTL;
	}

	@Override
	public String toString(){
		return name+" "+PID;
	}

	public class Record {
		public final Double delay;
		public final IcmpPacket packet;
		public final IpAddress sender;

		public Record(Double arrival, IcmpPacket packet, IpAddress sender) {
			this.delay = arrival;
			this.packet = packet;
			this.sender = sender;
		}
	}

	public class Timestamp {
		public final double timestamp;
		public final int ttl;

		public Timestamp(double timestamp, int ttl) {
			this.timestamp = timestamp;
			this.ttl = ttl;
		}
	}

	private void vypisMapu() {
		for (Integer i : timestamps.keySet()) {
			Logger.log(this, Logger.DEBUG, LoggingCategory.TRACEROUTE_APPLICATION, "seq="+i+", ttl="+timestamps.get(i).ttl, null);
		}
	}
}

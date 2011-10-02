package spider.prototype.services.communication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import spider.prototype.services.communication.addresses.Address;

/***
 * Provides functionality for communication, like sending data, broadcasting
 * data, etc. This class is a facade, which forwards the requests to the
 * specific mappers (e.g. TCPMapper), which are able to establish a real
 * communication (e.g. open a socket for TCP communication)
 * 
 * @author Thomas
 * 
 */
public class CommunicationService {

	/***
	 * The available mappers (e.g. a mapper for TCP, a mapper for bluetooth,
	 * etc)
	 */
	private HashMap<NetworkType, AbstractMapper> mappers = new HashMap<NetworkType, AbstractMapper>();

	private List<Address> knownNeighbours = new ArrayList<Address>();

	/***
	 * Adds the given mapper to the communication service and calls the method
	 * {@link AbstractMapper#startListening()} of it.
	 * 
	 * @param mapper
	 *            The mapper to add
	 */
	public void addMapper(AbstractMapper mapper) {
		addMapper(mapper, true);
	}

	/***
	 * 
	 * Adds the given mapper to the communication service
	 * 
	 * @param mapper
	 *            The mapper to add
	 * @param startService
	 *            If true, the method {@link AbstractMapper#startListening()}
	 *            will be called
	 */
	public void addMapper(final AbstractMapper mapper, boolean startService) {

		if (!mappers.containsKey(mapper.getNetworkType())) {
			mappers.put(mapper.getNetworkType(), mapper);
			if (startService) {
				// start listening service in own thread
				new Thread(new Runnable() {

					@Override
					public void run() {
						mapper.startListening();
					}
				}).start();
			}
		}
	}

	/***
	 * Shuts down every registered mapper
	 */
	public void shutdown() {

		for (AbstractMapper mapper : mappers.values()) {
			try {
				mapper.stopListening();
			} catch (Exception e) {
				e.printStackTrace();
				// TODO [Protokollierung]
			}
		}
	}

	/***
	 * Sends the given data to the given receiver
	 * 
	 * @param receiver
	 *            The receiver
	 * @param data
	 *            The data to send
	 */
	public boolean sendAt(Address receiver, byte[] data) {

		return sendAt(receiver, data, false);
	}

	/**
	 * 
	 * @param receiver
	 * @param data
	 * @param sendSenderAddress
	 */
	public boolean sendAt(Address receiver, byte[] data,
			boolean sendSenderAddress) {
		try {
			// check if a mapper for the receiver's network type has been
			// registered
			if (mappers.containsKey(receiver.getNetworkType())) {
				// send the data
				return mappers.get(receiver.getNetworkType()).sendAt(receiver,
						data, sendSenderAddress);
			} else {
				throw new IllegalArgumentException(
						"No CommunicationService registered for NetworkType="
								+ receiver.getNetworkType().toString());
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			// TODO [Protokollierung]
		}

		return false;
	}

	/***
	 * 
	 * @return A list of addresses for this device. Each mapper provides its own
	 *         address (IP address for TCP, PAN-ID for ZigBee, etc)
	 */
	public List<Address> getAddresses() {

		List<Address> addresses = new LinkedList<Address>();
		for (AbstractMapper mapper : mappers.values()) {
			addresses.add(mapper.getAddress());
		}
		return addresses;
	}

	public synchronized void clearKnownNeighbours() {
		knownNeighbours.clear();
	}
	
	public synchronized List<Address> getKnownNeighbours() {
		List<Address> copy = new ArrayList<Address>();
		copy.addAll(knownNeighbours);
		return copy;
	}
	
	public synchronized void addNeighbour(Address address) {
		knownNeighbours.add(address);
	}

	public synchronized void discoverNeighbours() {

		knownNeighbours.clear();
		for (AbstractMapper mapper : mappers.values()) {
			for (Address addr : mapper.discoverNeighbours()) {
				knownNeighbours.add(addr);
			}
		}
	}

	/***
	 * Broadcasts the given data (for each registered mapper the
	 * {@link AbstractMapper#broadcast(byte[])} method will be called)
	 * 
	 * @param data
	 *            The data to broadcast
	 */
	public void broadcast(byte[] data) {

		for(Address addr : knownNeighbours) {
			if(mappers.containsKey(addr.getNetworkType())) {
				mappers.get(addr.getNetworkType()).sendAt(addr, data, true);
			}
		}
	}
}

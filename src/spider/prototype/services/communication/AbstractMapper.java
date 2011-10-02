package spider.prototype.services.communication;

import java.util.List;

import spider.prototype.services.communication.addresses.Address;
import spider.prototype.utils.BinaryTransformer;

/***
 * Abstract mapper. Implement a mapper for each network type available on your
 * device.
 * 
 * @author Thomas
 * 
 */
public abstract class AbstractMapper {
	
	/***
	 * The network type of the mapper
	 */
	private NetworkType networkType;
	
	/***
	 * Initializes the mapper
	 * 
	 * @param networkType
	 *            The network type of the mapper
	 */
	public AbstractMapper(NetworkType networkType) {
		this.networkType = networkType;
	}

	/***
	 * 
	 * @return The network type of the mapper
	 */
	public NetworkType getNetworkType() {
		return networkType;
	}
	
	protected abstract List<Address> discoverNeighbours();

	/***
	 * 
	 * Sends the given data to the given receiver
	 * 
	 * @param receiver
	 *            The receiver
	 * @param data
	 *            The data to send
	 * @param addSenderAddress
	 *            If set to true, the sender's address will be added to the data
	 *            array
	 */
	public boolean sendAt(Address receiver, byte[] data,
			boolean addSenderAddress) {

		byte[] dataToSend;
		if (addSenderAddress) {
			// source address
			byte[] address = getAddress().getAddressAsBytes();

			// add source address to data
			dataToSend = new byte[data.length + address.length];

			int i = 0;
			// add tag
			dataToSend[i++] = data[0];
			// add new length
			int oldLength = BinaryTransformer.toInt(new byte[] { data[1],
					data[2], data[3], data[4] });
			byte[] newLength = BinaryTransformer.toByta(oldLength
					+ address.length);
			for (int j = 0; j < newLength.length; j++, i++) {
				dataToSend[i] = newLength[j];
			}
			// add source-address
			for (int j = 0; j < address.length; j++, i++) {
				dataToSend[i] = address[j];
			}
			// add remaining
			for (int j = 5; j < data.length; j++, i++) {
				dataToSend[i] = data[j];
			}
		} else {
			dataToSend = data;
		}

		return sendAt(receiver, dataToSend);
	}

	protected abstract boolean sendAt(Address receiver, byte[] data);

	/***
	 * Tells the mapper to start listening for incoming messages
	 */
	public abstract void startListening();

	/***
	 * Tells the mapper to stop listening
	 */
	public abstract void stopListening();

	/***
	 * 
	 * @return The address of the mapper for this device
	 */
	public abstract Address getAddress();	
}

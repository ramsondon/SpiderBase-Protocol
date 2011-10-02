package spider.prototype.utils;

import java.util.ArrayList;
import java.util.List;

import spider.prototype.services.communication.ProtocolTag;
import spider.prototype.services.communication.addresses.Address;
import spider.prototype.services.yellowpage.ServiceDescription;

/***
 * 
 * Use this class for buiding packages
 * 
 * @author Thomas
 * 
 */
public class PacketBuilder {

	/***
	 * 
	 * Builds a service request packet
	 * 
	 * @param serviceDesc
	 * @param hops
	 * @param maxHops
	 * @return
	 */
	public static byte[] buildServiceRequest(ServiceDescription serviceDesc,
			int hops, int maxHops) {

		// x bytes = source-address -> will be added in the broadcast method of
		// the mapper
		// 1 byte = service description
		// 1 byte = hops
		// 1 byte = max hops

		return buildPacket(ProtocolTag.ServiceRequest, new byte[] {
				ServiceDescription.convert(serviceDesc),
				BinaryTransformer.toByte(hops),
				BinaryTransformer.toByte(maxHops) });
	}

	/***
	 * 
	 * Builds a service request response packet
	 * 
	 * @param serviceDescription
	 * @param pageAddresses
	 *            Key = Address; Value = The service-id (The name of the
	 *            service)
	 * @return
	 */
	public static byte[] buildServiceRequestResponse(
			ServiceDescription serviceDescription,
			List<KeyValuePair<Address, Integer>> pageAddressToServiceId) {

		// 1 byte = service description
		// 1 byte = countOfAddresses
		// (1 byte (network type flag) + address_length) * countOfAddresses
		// 4 byte service-id * countOfAddresses

		ArrayList<Byte> data = new ArrayList<Byte>();

		// service description
		data.add(ServiceDescription.convert(serviceDescription));

		// count of addresses
		data.add(BinaryTransformer.toByte(pageAddressToServiceId.size()));

		// addresses
		for (KeyValuePair<Address, Integer> entry : pageAddressToServiceId) {
			byte[] byteAddr = entry.getKey().getAddressAsBytes();

			for (int i = 0; i < byteAddr.length; i++) {
				data.add(byteAddr[i]);
			}
		}

		// service ids
		for (KeyValuePair<Address, Integer> entry : pageAddressToServiceId) {
			byte[] byteServiceId = BinaryTransformer.toByta(entry.getValue());

			for (int i = 0; i < byteServiceId.length; i++) {
				data.add(byteServiceId[i]);
			}
		}

		// transform to byte array
		byte[] dataArray = new byte[data.size()];
		for (int i = 0; i < data.size(); i++) {
			dataArray[i] = (byte) data.get(i);
		}

		return buildPacket(ProtocolTag.ServiceRequestResponse, dataArray);
	}

	/**
	 * 
	 * @param serviceDescription
	 * @param serviceId
	 * @return
	 */
	public static byte[] buildDataRequest(
			ServiceDescription serviceDescription, int serviceId) {

		// x bytes = source address -> will be added in the send method of the
		// mapper
		// 1 byte = service description
		// 1 byte = service id

		return buildPacket(ProtocolTag.DataRequest, new byte[] {
				ServiceDescription.convert(serviceDescription),
				BinaryTransformer.toByte(serviceId) });
	}

	/**
	 * 
	 * @param serviceDescription
	 * @param serviceId
	 * @param value
	 * @return
	 */
	public static byte[] buildDataResponse(
			ServiceDescription serviceDescription, int serviceId, byte[] value) {

		byte[] data = new byte[2 + value.length];
		data[0] = ServiceDescription.convert(serviceDescription);
		data[1] = BinaryTransformer.toByte(serviceId);

		for (int i = 0; i < value.length; i++) {
			data[i + 2] = value[i];
		}

		return buildPacket(ProtocolTag.DataResponse, data);
	}

	/**
	 * 
	 * @param serviceDescription
	 * @param serviceId
	 * @param value
	 * @return
	 */
	public static byte[] buildUseRequest(ServiceDescription serviceDescription,
			int serviceId, byte[] value) {

		byte[] data = new byte[2 + value.length];
		data[0] = ServiceDescription.convert(serviceDescription);
		data[1] = BinaryTransformer.toByte(serviceId);

		for (int i = 0; i < value.length; i++) {
			data[i + 2] = value[i];
		}

		return buildPacket(ProtocolTag.UseRequest, data);
	}
	
	public static byte[] buildServiceObservationRequest(ServiceDescription serviceDescription,
			int serviceId, int intervalInMilliseconds, byte[] value) {
		
		// 1 byte: service-description
		// 1 byte: service id
		// 4 bytes: interval
		// x bytes: value
		
		int index = 0;
		byte[] data = new byte[6 + value.length];
		data[index++] = ServiceDescription.convert(serviceDescription);
		data[index++] = BinaryTransformer.toByte(serviceId);
		byte[] interval = BinaryTransformer.toByta(intervalInMilliseconds);
		data[index++] = interval[0];
		data[index++] = interval[1];
		data[index++] = interval[2];
		data[index++] = interval[3];

		
		
		for (int i = 0; i < value.length; i++) {
			data[index++] = value[i];
		}

		return buildPacket(ProtocolTag.ServiceObservationRequest, data);
	}

	/***
	 * 
	 * Builds a packet
	 * 
	 * @param tag
	 * @param data
	 * @return
	 */
	public static byte[] buildPacket(ProtocolTag tag, byte[] data) {

		// 1 byte for tag
		// 4 bytes for length
		// x bytes for data

		byte[] packet = new byte[5 + data.length];

		// tag
		packet[0] = ProtocolTag.conv(tag);

		// length
		byte[] length = BinaryTransformer.toByta(data.length);
		packet[1] = length[0];
		packet[2] = length[1];
		packet[3] = length[2];
		packet[4] = length[3];

		// value
		for (int i = 0; i < data.length; i++) {
			packet[i + 5] = data[i];
		}

		return packet;
	}

}

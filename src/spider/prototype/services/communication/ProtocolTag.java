package spider.prototype.services.communication;

/***
 * 
 * ServiceRequest: 				0x20;
 * ServiceRequestResponse: 		0x21;
 * DataRequest: 				0x40;
 * UseRequest: 					0x41;
 * DataResponse: 				0x50;
 * ServiceObservationRequest: 	0x60;
 * everything else is handled as invalid protocol tag
 * 
 * @author Thomas
 *
 */
public enum ProtocolTag {

	ServiceRequest, ServiceRequestResponse, DataRequest, UseRequest, DataResponse, ServiceObservationRequest, Invalid;

	/***
	 * 
	 * @param tag The ProtocolTag to convert
	 * @return The byte that represents the ProtocolTag
	 */
	public static byte conv(ProtocolTag tag) {

		switch (tag) {
		case ServiceRequest:
			return 0x20;
		case ServiceRequestResponse:
			return 0x21;
		case DataRequest:
			return 0x40;
		case UseRequest:
			return 0x41;
		case DataResponse:
			return 0x50;
		case ServiceObservationRequest:
			return 0x60;
		case Invalid:
		default:
			return 0x01;
		}

	}

	/***
	 * 
	 * @param ProtocolTagFlag A byte that represents the ProtocolTag
	 * @return The ProtocolTag for the given ProtocolTagFlag
	 */
	public static ProtocolTag conv(byte ProtocolTagFlag) {

		if (ProtocolTagFlag == 0x20) {
			return ServiceRequest;
		} else if (ProtocolTagFlag == 0x21) {
			return ServiceRequestResponse;
		} else if(ProtocolTagFlag == 0x40) {
			return DataRequest;
		} else if(ProtocolTagFlag == 0x41) {
			return UseRequest;
		} else if(ProtocolTagFlag == 0x50) {
			return DataResponse;
		} else if(ProtocolTagFlag == 0x60) {
			return ServiceObservationRequest;
		}
		return Invalid;
	}
}

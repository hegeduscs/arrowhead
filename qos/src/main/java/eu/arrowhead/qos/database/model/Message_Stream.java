package eu.arrowhead.qos.database.model;

import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "message_stream", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"code"})})
@XmlRootElement
public class Message_Stream {

	@Column(name = "message_stream_id")
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int message_stream_id;

	@NotNull
	@ManyToOne(cascade = CascadeType.ALL)
	private ArrowheadService service;

	@NotNull
	@ManyToOne(cascade = CascadeType.ALL)
	private ArrowheadSystem consumer;

	@NotNull
	@ManyToOne(cascade = CascadeType.ALL)
	private ArrowheadSystem provider;

	@OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
	private QoS_Resource_Reservation qualityOfService;

	@Column(name = "configuration")
	@ElementCollection
	@LazyCollection(LazyCollectionOption.FALSE)
	private Map<String, String> configuration;

	@Column(name = "type")
	private String type;

	@Column(name = "code")
	private String code;

	protected Message_Stream() {

	}

	public Message_Stream(ArrowheadService service, ArrowheadSystem consumer,
						  ArrowheadSystem provider,
						  Map<String, String> qualityOfService,
						  Map<String, String> configuration, String type) {
		this.service = service;
		this.consumer = consumer;
		this.provider = provider;
		this.qualityOfService = new QoS_Resource_Reservation("UP", qualityOfService);
		this.configuration = configuration;
		this.type = type;
		getCode();
	}

	/**
	 * Get Message Stream ID
	 *
	 * @return Returns Integer with ID.
	 */
	public int getMessage_stream_id() {
		return message_stream_id;
	}

	/**
	 * Set Message Stream ID.
	 *
	 * @param message_stream_id Integer with Message Stream ID.
	 */
	public void setMessage_stream_id(int message_stream_id) {
		this.message_stream_id = message_stream_id;
	}

	/**
	 * Get Service.
	 *
	 * @return Returns Service.
	 */
	public ArrowheadService getService() {
		return service;
	}

	/**
	 * Set Arrowhead Service.
	 *
	 * @param service Arrowhead Service.
	 */
	public void setService(ArrowheadService service) {
		this.service = service;
		getCode();
	}

	/**
	 * Get Consumer Arrowhead System
	 *
	 * @return Arrowhead System.
	 */
	public ArrowheadSystem getConsumer() {
		return consumer;
	}

	/**
	 * Set Consumer Arrowhead System.
	 *
	 * @param consumer Arrowhead System.
	 */
	public void setConsumer(ArrowheadSystem consumer) {
		this.consumer = consumer;
		getCode();
	}

	/**
	 * Get Provider Arrowhead System.
	 *
	 * @return Arrowhead System.
	 */
	public ArrowheadSystem getProvider() {
		return provider;
	}

	/**
	 * Set Provider Arrowhead System.
	 *
	 * @param provider Arrowhead System
	 */
	public void setProvider(ArrowheadSystem provider) {
		this.provider = provider;
		getCode();
	}

	/**
	 * Get QoS Reservation
	 *
	 * @return Returns QoS_Resource_Reservation
	 */
	public QoS_Resource_Reservation getQualityOfService() {
		return qualityOfService;
	}

	/**
	 * Set QoS Reservation
	 *
	 * @param qualityOfService QoS Reservation
	 */
	public void setQualityOfService(QoS_Resource_Reservation qualityOfService) {
		this.qualityOfService = qualityOfService;
	}

	/**
	 * Get Message Configuration.
	 *
	 * @return Returns Map with the configuration.
	 */
	public Map<String, String> getConfiguration() {
		return configuration;
	}

	/**
	 * Set Configuration.
	 *
	 * @param configuration Map with configuration.
	 */
	public void setConfiguration(Map<String, String> configuration) {
		this.configuration = configuration;
	}

	/**
	 * This method generates the code that also identifies the message stream.
	 *
	 * @return Returns String with the code.
	 */
	public String getCode() {
		this.code = provider.getSystemGroup() + "/" + provider.getSystemName() + "," + consumer.
			getSystemGroup() + "/"
			+ consumer.getSystemName() + "," + service.getServiceGroup() + "/" + service.
			getServiceDefinition();
		return code;
	}

	/**
	 * Get Message Stream Type (ex. fttse).
	 *
	 * @return Returns a message stream type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Set message stream type (ex. type)
	 *
	 * @param type Set String type.
	 */
	public void setType(String type) {
		this.type = type;
	}

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadSystem;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Message used to delete a monitor rule.
 *
 * @author Renato Ayres
 */
@XmlRootElement
public class RemoveMonitorRule {

    private ArrowheadSystem provider;
    private ArrowheadSystem consumer;

    /**
     * Creates a new instance with no parameters initialized.
     */
    public RemoveMonitorRule() {
    }

    /**
     * Creates a new instance with the given service provider, and service
     * consumer.
     *
     * @param provider the service provider
     * @param consumer the service consumer
     */
    public RemoveMonitorRule(ArrowheadSystem provider, ArrowheadSystem consumer) {
        this.provider = provider;
        this.consumer = consumer;
    }

    /**
     * Gets the service provider
     *
     * @return the service provider
     */
    public ArrowheadSystem getProvider() {
        return provider;
    }

    /**
     * Sets the service provider
     *
     * @param provider the service provider
     */
    public void setProvider(ArrowheadSystem provider) {
        this.provider = provider;
    }

    /**
     * Gets the service consumer
     *
     * @return the service consumer
     */
    public ArrowheadSystem getConsumer() {
        return consumer;
    }

    /**
     * Sets the service consumer
     *
     * @param consumer the service consumer
     */
    public void setConsumer(ArrowheadSystem consumer) {
        this.consumer = consumer;
    }
}

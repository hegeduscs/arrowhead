/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.database.qos;

import eu.arrowhead.common.database.ArrowheadSystem;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "deployed_system", uniqueConstraints = {@UniqueConstraint(columnNames = {"arrowhead_system_id"})})
public class DeployedSystem {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
  @JoinColumn(name = "arrowhead_system_id")
  private ArrowheadSystem system;

  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
  @JoinColumn(name = "network_device_id")
  private NetworkDevice networkDevice;

  public DeployedSystem() {
  }

  public DeployedSystem(ArrowheadSystem system, NetworkDevice networkDevice) {
    this.system = system;
    this.networkDevice = networkDevice;
  }

  @XmlTransient
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public ArrowheadSystem getSystem() {
    return system;
  }

  public void setSystem(ArrowheadSystem system) {
    this.system = system;
  }

  public NetworkDevice getNetworkDevice() {
    return networkDevice;
  }

  public void setNetworkDevice(NetworkDevice networkDevice) {
    this.networkDevice = networkDevice;
  }

}

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

package eu.arrowhead.common.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.arrowhead.common.database.ArrowheadService;

public class ServiceQueryForm {

  private ArrowheadService service;
  private boolean pingProviders;
  private boolean metadataSearch;
  private int version = 1;

  public ServiceQueryForm() {
  }

  public ServiceQueryForm(ArrowheadService service, boolean pingProviders, boolean metadataSearch) {
    this.service = service;
    this.pingProviders = pingProviders;
    this.metadataSearch = metadataSearch;
  }

  public ServiceQueryForm(ServiceRequestForm srf) {
    this.service = srf.getRequestedService();
    this.pingProviders = srf.getOrchestrationFlags().get("pingProvider");
    this.metadataSearch = srf.getOrchestrationFlags().get("metadataSearch");
  }

  public ArrowheadService getService() {
    return service;
  }

  public void setService(ArrowheadService service) {
    this.service = service;
  }

  public boolean isPingProviders() {
    return pingProviders;
  }

  public void setPingProviders(boolean pingProviders) {
    this.pingProviders = pingProviders;
  }

  public boolean isMetadataSearch() {
    return metadataSearch;
  }

  public void setMetadataSearch(boolean metadataSearch) {
    this.metadataSearch = metadataSearch;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  @JsonIgnore
  public boolean isValid() {
    return service != null && service.isValid();
  }

}

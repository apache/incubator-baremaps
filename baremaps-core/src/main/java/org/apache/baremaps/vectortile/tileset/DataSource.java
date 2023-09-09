/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.baremaps.vectortile.tileset;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataSource {

  @JsonProperty("dataSourceClassName")
  private String dataSourceClassName;

  @JsonProperty("tilejson")
  private String jdbcUrl;

  @JsonProperty("username")
  private String username;

  @JsonProperty("password")
  private String password;

  @JsonProperty("autoCommit")
  private Boolean autoCommit;

  @JsonProperty("connectionTimeout")
  private Integer connectionTimeout;

  @JsonProperty("idleTimeout")
  private Integer idleTimeout;

  @JsonProperty("keepAliveTime")
  private Integer keepAliveTime;

  @JsonProperty("maxLifetime")
  private Integer maxLifetime;

  @JsonProperty("minimumIdle")
  private Integer minimumIdle;

  @JsonProperty("maximumPoolSize")
  private Integer maximumPoolSize;

  @JsonProperty("poolName")
  private String poolName;

  @JsonProperty("readOnly")
  private Boolean readOnly;

  public DataSource() {}


  public String getDataSourceClassName() {
    return dataSourceClassName;
  }

  public void setDataSourceClassName(String dataSourceClassName) {
    this.dataSourceClassName = dataSourceClassName;
  }

  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public void setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Boolean getAutoCommit() {
    return autoCommit;
  }

  public void setAutoCommit(Boolean autoCommit) {
    this.autoCommit = autoCommit;
  }

  public Integer getConnectionTimeout() {
    return connectionTimeout;
  }

  public void setConnectionTimeout(Integer connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  public Integer getIdleTimeout() {
    return idleTimeout;
  }

  public void setIdleTimeout(Integer idleTimeout) {
    this.idleTimeout = idleTimeout;
  }

  public Integer getKeepAliveTime() {
    return keepAliveTime;
  }

  public void setKeepAliveTime(Integer keepAliveTime) {
    this.keepAliveTime = keepAliveTime;
  }

  public Integer getMaxLifetime() {
    return maxLifetime;
  }

  public void setMaxLifetime(Integer maxLifetime) {
    this.maxLifetime = maxLifetime;
  }

  public Integer getMinimumIdle() {
    return minimumIdle;
  }

  public void setMinimumIdle(Integer minimumIdle) {
    this.minimumIdle = minimumIdle;
  }

  public Integer getMaximumPoolSize() {
    return maximumPoolSize;
  }

  public void setMaximumPoolSize(Integer maximumPoolSize) {
    this.maximumPoolSize = maximumPoolSize;
  }

  public String getPoolName() {
    return poolName;
  }

  public void setPoolName(String poolName) {
    this.poolName = poolName;
  }

  public Boolean getReadOnly() {
    return readOnly;
  }

  public void setReadOnly(Boolean readOnly) {
    this.readOnly = readOnly;
  }
}

package org.modelseeed.rast;

import java.util.List;

public class AnnotationRASTResultEvent {
  protected List<String> parameters;
  protected String id;
  protected String hostname;
  protected String tool_name;
  protected Double execution_time;
  protected Double execute_time;

  public List<String> getParameters() {
    return parameters;
  }

  public void setParameters(List<String> parameters) {
    this.parameters = parameters;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getTool_name() {
    return tool_name;
  }

  public void setTool_name(String tool_name) {
    this.tool_name = tool_name;
  }

  public Double getExecution_time() {
    return execution_time;
  }

  public void setExecution_time(Double execution_time) {
    this.execution_time = execution_time;
  }

  public Double getExecute_time() {
    return execute_time;
  }

  public void setExecute_time(Double execute_time) {
    this.execute_time = execute_time;
  }
}

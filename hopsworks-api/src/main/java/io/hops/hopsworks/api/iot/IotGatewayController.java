package io.hops.hopsworks.api.iot;

import com.google.gson.Gson;
import io.hops.hopsworks.common.dao.iot.IotGatewayConfiguration;
import io.hops.hopsworks.common.dao.iot.IotGatewayFacade;
import io.hops.hopsworks.common.dao.iot.IotGatewayState;
import io.hops.hopsworks.common.dao.iot.IotGateways;
import io.hops.hopsworks.common.dao.project.Project;
import io.hops.hopsworks.exceptions.GatewayException;
import io.hops.hopsworks.restutils.RESTCodes;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class IotGatewayController {
  
  @EJB
  private IotGatewayFacade iotGatewayFacade;
  
  private static final Logger LOGGER = Logger.getLogger(IotGatewayController.class.getName());
  
  public IotGateways getGateway(Project project, Integer id) throws GatewayException {
    IotGateways gateway = iotGatewayFacade.findByProjectAndId(project, id);
    if (gateway == null) {
      throw new GatewayException(RESTCodes.GatewayErrorCode.GATEWAY_NOT_FOUND, Level.FINEST, "gatewayId:" + id);
    }
    return gateway;
  }
  
  public IotGateways putGateway(Project project, IotGatewayConfiguration config) {
    if (project == null || config == null) {
      throw new IllegalArgumentException("Arguments cannot be null.");
    }
  
    IotGateways gateway = iotGatewayFacade.findByProjectAndId(project, config.getGatewayId());
  
    if (gateway == null) {
      gateway = new IotGateways(config, project, IotGatewayState.ACTIVE);
    } else if (gateway.getState() == IotGatewayState.INACTIVE_BLOCKED) {
      gateway.setState(IotGatewayState.BLOCKED);
    }
    
    gateway = iotGatewayFacade.putIotGateway(gateway);
    return gateway;
  }
  
  public List<IotDevice> getNodesOfGateway(Integer gatewayId, Project project)
    throws URISyntaxException, IOException {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    IotGateways gateway = iotGatewayFacade.findByProjectAndId(project, gatewayId);
    URI uri = new URIBuilder()
      .setScheme("http")
      .setHost(gateway.getHostname())
      .setPort(gateway.getPort())
      .setPath("/gateway/nodes")
      .build();
    HttpGet httpGet = new HttpGet(uri);
    CloseableHttpResponse response = httpClient.execute(httpGet);
    return responseToDevices(response, gatewayId);
  }
  
  private List<IotDevice> responseToDevices(CloseableHttpResponse response, Integer gatewayId)
    throws IOException {
    StringWriter writer = new StringWriter();
    IOUtils.copy(response.getEntity().getContent(), writer);
    String json = writer.toString();
    
    Gson gson = new Gson();
    IotDevice[] array = gson.fromJson(json, IotDevice[].class);
    List<IotDevice> list = Arrays.asList(array);
    list.forEach(d -> d.setGatewayId(gatewayId));
    return list;
  }
  
  //TODO: future work - implement in terms of an endpoint that doesn't return all nodes
  public IotDevice getNodeById(Integer gatewayId, String nodeId, Project project)
    throws URISyntaxException, IOException {
    return getNodesOfGateway(gatewayId, project)
      .stream()
      .filter(d -> d.getEndpoint().equals(nodeId))
      .findAny()
      .orElseThrow(IllegalArgumentException::new);
  }
  
  public void actionBlockingNode(Integer gatewayId, String nodeId, Project project, Boolean block)
    throws URISyntaxException, IOException {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    IotGateways gateway = iotGatewayFacade.findByProjectAndId(project, gatewayId);
    URI uri = new URIBuilder()
      .setScheme("http")
      .setHost(gateway.getHostname())
      .setPort(gateway.getPort())
      .setPath("/gateway/nodes/" + nodeId + "/blocked")
      .build();
    HttpRequestBase method;
    if (block) {
      method = new HttpPost(uri);
    } else {
      method = new HttpDelete(uri);
    }
    CloseableHttpResponse response = httpClient.execute(method);
  }
}
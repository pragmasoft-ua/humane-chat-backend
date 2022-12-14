package ua.com.pragmasoft.k1te.router.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import ua.com.pragmasoft.k1te.shared.KiteException;
import ua.com.pragmasoft.k1te.shared.NotFoundException;
import ua.com.pragmasoft.k1te.shared.RoutingException;

public class KiteRouter implements Router {

  public static final String CONNECTOR_ID = "k1te";

  public static final String ATTR_FROM = "k1te.member.from";
  public static final String ATTR_TO = "k1te.member.to";

  private final Map<String, Connector> connectors = new HashMap<>(8);
  private final Channels channels;

  /**
   * @param channels
   */
  public KiteRouter(Channels channels) {
    this.channels = channels;
  }

  @Override
  public synchronized Router registerConnector(Connector connector) {
    this.connectors.put(connector.id(), connector);
    return this;
  }

  @Override
  public String id() {
    return CONNECTOR_ID;
  }

  @Override
  public void dispatch(RoutingContext ctx) throws KiteException {
    if (null == ctx.originConnection) {
      throw new RoutingException("unknown origin");
    }
    if (null == ctx.from) {
      ctx.from = this.channels.find(ctx.originConnection);
    }
    if (null == ctx.to) {
      ctx.to = this.channels.find(ctx.from.getChannelName(), ctx.from.getPeerMemberId());
    }
    if (null == ctx.destinationConnection) {
      ctx.destinationConnection = ctx.to.getConnectionUri();
    }
    Connector connector = requiredConnector(this.connectorId(ctx.destinationConnection));
    connector.dispatch(ctx);
    if (null == ctx.response) {
      throw new RoutingException("missing response from connector " + connector.id());
    }
  }

  private synchronized Connector requiredConnector(String connectorId) throws NotFoundException {
    return Optional.ofNullable(this.connectors.get(connectorId))
        .orElseThrow(() -> new NotFoundException("No connector with id " + connectorId));
  }

}

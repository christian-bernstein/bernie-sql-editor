package de.christianbernstein.bernie.ses.events;

import de.christianbernstein.bernie.sdk.discovery.websocket.SocketIdentifyingAttachment;
import lombok.Data;

/**
 * @author Christian Bernstein
 */
@Data
public class WebsocketConnectionCloseEvent {

    private SocketIdentifyingAttachment si;

}

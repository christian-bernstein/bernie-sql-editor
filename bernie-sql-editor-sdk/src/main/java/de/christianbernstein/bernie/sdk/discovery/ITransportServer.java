package de.christianbernstein.bernie.sdk.discovery;

import de.christianbernstein.bernie.sdk.discovery.websocket.SocketShutdownReason;
import lombok.NonNull;

// todo make better
public interface ITransportServer {

    ITransportServer shutdown(@NonNull final SocketShutdownReason reason) throws InterruptedException;

}

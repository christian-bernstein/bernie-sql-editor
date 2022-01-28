package de.christianbernstein.bernie.shared.discovery;

import de.christianbernstein.bernie.shared.discovery.websocket.SocketShutdownReason;
import lombok.NonNull;

// todo make better
public interface ITransportServer {

    ITransportServer shutdown(@NonNull final SocketShutdownReason reason) throws InterruptedException;

}

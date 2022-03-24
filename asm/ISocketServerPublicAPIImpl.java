import de.christianbernstein.bernie.shared.tailwind.IProteus;
import de.christianbernstein.bernie.shared.tailwind.IPublicAPI;
import de.christianbernstein.bernie.shared.discovery.websocket.server.ISocketServerPublicAPI;
import de.christianbernstein.bernie.shared.tailwind.Bridge;
import de.christianbernstein.bernie.shared.discovery.websocket.server.OnStopSocketContext;
import de.christianbernstein.bernie.shared.discovery.websocket.server.OnErrorSocketContext;
import de.christianbernstein.bernie.shared.discovery.websocket.server.OnOpenSocketContext;
import de.christianbernstein.bernie.shared.discovery.websocket.server.OnMessageSocketContext;

@SuppressWarnings("unchecked")
public class ISocketServerPublicAPIImpl implements ISocketServerPublicAPI {
  private IProteus<?> proteus;

  public ISocketServerPublicAPIImpl(IProteus proteus) {
    this.proteus = proteus;
  }

  public final IPublicAPI<ISocketServerPublicAPI> proteus(
      IProteus<ISocketServerPublicAPI> proteus) {
    this.proteus = proteus;
    return this;
  }

  public final IProteus<ISocketServerPublicAPI> proteus() {
    return (IProteus<ISocketServerPublicAPI>) this.proteus;
  }

  @Bridge(value = "on-start", async = false)
  public void onStart() {
    this.proteus().internal().sync("on-start", (ctx, gate) -> ctx.yield(gate.sync()));
  }

  @Bridge(value = "on-stop", async = false)
  public void onStop(OnStopSocketContext param0) {
    this.proteus().internal().sync("on-stop", (ctx, gate) -> ctx.yield(gate.sync(param0)));
  }

  @Bridge(value = "on-error", async = false)
  public void onError(OnErrorSocketContext param0) {
    this.proteus().internal().sync("on-error", (ctx, gate) -> ctx.yield(gate.sync(param0)));
  }

  @Bridge(value = "on-open", async = false)
  public void onOpen(OnOpenSocketContext param0) {
    this.proteus().internal().sync("on-open", (ctx, gate) -> ctx.yield(gate.sync(param0)));
  }

  @Bridge(value = "on-message", async = false)
  public void onMessage(OnMessageSocketContext param0) {
    this.proteus().internal().sync("on-message", (ctx, gate) -> ctx.yield(gate.sync(param0)));
  }
}

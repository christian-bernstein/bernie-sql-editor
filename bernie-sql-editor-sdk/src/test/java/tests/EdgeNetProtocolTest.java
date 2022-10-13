package tests;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import tests.ENPSpecification.IIMux;
import tests.ENPTestImplementation.IMux;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Christian Bernstein
 */
public class EdgeNetProtocolTest {

    public static void main(String[] args) {

        final IIMux<?, byte[]> mux = new IMux();

        mux.select("main");

        mux.pub("Hello world".getBytes(StandardCharsets.UTF_8));

    }
}

@UtilityClass
@SuppressWarnings({"UnusedReturnValue", "unused"})
class ENPSpecification {

    interface IIMux<This extends IIMux<?, ?>, BysDataType> extends IENPDataBus<BysDataType> {
        This select(@NonNull String id);
        This unselect();
        This addOutput(@NonNull String id, @NonNull IENPDataBus<BysDataType> bus);
        This removeOutput(@NonNull String id);
        This setFallbackBus(@Nullable IENPDataBus<BysDataType> fallbackBus);
        IENPDataBus<BysDataType> getActiveBus();
    }

    interface IENPSubLane<Type> {
        void sub(@NonNull Consumer<Type> handler);
    }

    interface IENPPubLane<Type> {
        void pub(Type message);
    }

    interface IENPDataBus<Type> extends IENPSubLane<Type>, IENPPubLane<Type> { }

    interface IENPEndpoint<This extends IENPEndpoint<?>> extends IENPPubLane<IENPPacket> {
        This setIMux(@NonNull IIMux<?, byte[]> iMux);
    }

    @Data
    @Builder
    abstract class IENPPacket {
        private String type;
        private String payload;
    }
}

@UtilityClass
class ENPTestImplementation {

    class IMux implements IIMux<IMux, byte[]> {

        private final Map<String, ENPSpecification.@NonNull IENPDataBus<byte[]>> busses = new HashMap<>();

        @Nullable
        private ENPSpecification.IENPDataBus<byte[]> fallbackBus;

        @Nullable
        private String activeBus;

        @Override
        public IMux select(@NonNull String id) {
            this.activeBus = id;
            return this;
        }

        @Override
        public IMux unselect() {
            this.activeBus = null;
            return this;
        }

        @Override
        public IMux addOutput(@NonNull String id, ENPSpecification.@NonNull IENPDataBus<byte[]> bus) {
            this.busses.putIfAbsent(id, bus);
            return this;
        }

        @Override
        public IMux removeOutput(@NonNull String id) {
            this.busses.remove(id);
            return this;
        }

        @Override
        public IMux setFallbackBus(@Nullable ENPSpecification.IENPDataBus<byte[]> fallbackBus) {
            this.fallbackBus = fallbackBus;
            return this;
        }

        @Override
        public ENPSpecification.IENPDataBus<byte[]> getActiveBus() {
            return this.busses.getOrDefault(this.activeBus, this.fallbackBus);
        }

        @Override
        public void sub(@NonNull Consumer<byte[]> handler) {
        }

        @Override
        public void pub(byte[] message) {
            final ENPSpecification.IENPDataBus<byte[]> bus = this.busses.getOrDefault(this.activeBus, this.fallbackBus);
            if (bus != null) {
                bus.pub(message);
            }
        }
    }
}

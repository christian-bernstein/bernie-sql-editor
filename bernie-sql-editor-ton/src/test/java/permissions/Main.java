package permissions;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Christian Bernstein
 */
public class Main {

    @SuppressWarnings("CommentedOutCode")
    public static void main(String[] args) {

        final Map<String, Byte> register = Map.of(
                "a", (byte) 0x1,
                "b", (byte) 0x2,
                "c", (byte) 0x3
        );

        final List<String> assigned = IntStream.range(0, 7)
                .mapToObj(operand -> "a")
                .collect(Collectors.toList());


        final Byte[] arr = assigned.stream().map(register::get).toArray(Byte[]::new);
        final byte[] bytes = new byte[arr.length];
        for(int i = 0; i < arr.length; i++){
            bytes[i] = arr[i];
        }

        System.out.println("initial bytes: " + Arrays.toString(bytes));

        final ByteBuffer wrapped = ByteBuffer.wrap(bytes); // big-endian by default

        System.out.println("remaining: " + wrapped.remaining());
        final double hash = wrapped.getDouble();

        final ByteBuffer dBuf = ByteBuffer.allocate(register.size());
        dBuf.putDouble(hash);
        byte[] dBytes = dBuf.array(); // { 0, 1 }

        System.out.println(Arrays.toString(dBytes));


    }
}

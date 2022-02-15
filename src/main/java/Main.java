import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings("unused")
public class Main {

    interface Alu {
        Function<Integer, Integer> not = a -> a ^= 1;
        BiFunction<Integer, Integer, Integer> and = (a, b) -> a & b;
        BiFunction<Integer, Integer, Integer> nand = and.andThen(not);
        BiFunction<Integer, Integer, Integer> or = (a, b) -> a | b;
        BiFunction<Integer, Integer, Integer> nor = or.andThen(not);
        BiFunction<Integer, Integer, Integer> xor = (a, b) -> a ^ b;
        BiFunction<Integer, Integer, Integer> xnor = xor.andThen(not);
    }

    public static void main(String[] args) {
        System.out.println(Alu.nand.andThen(Alu.not).apply(0, 0));
    }
}

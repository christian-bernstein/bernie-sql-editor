import java.math.RoundingMode
import java.text.DecimalFormat

class Main2 {

    static void main(String[] args) {

        final Random r = new Random()

        final double[] arr = ((Closure<Double> f, int lat) -> {
            final double[] arr = new double[lat]
            for (i in 0..<lat) {
                arr[i] = f(i + 1)
            }
            return arr
        })((int x) -> {
            return x ** 2
        }, 10)

        print(arr)

    }
}

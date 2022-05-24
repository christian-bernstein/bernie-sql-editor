package document;

import de.christianbernstein.bernie.sdk.document.Document;

/**
 * @author Christian Bernstein
 */
public class Main1 {

    public static void main(String[] args) {

        // <objektname>.<methodenname>(..).<methodenname>(..).<methodenname>(..);

        new Document()
                .put("key", "value")
                .ifPresent("key", value -> System.out.printf("Saved key's value: %s%n", value))
                .remove("key");

    }
}

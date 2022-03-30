package de.christianbernstein.bernie.ses.processors;

import de.christianbernstein.bernie.ses.bin.Console;
import de.christianbernstein.bernie.ses.bin.ConsoleCommandRegisterRequest;
import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.annotations.CommandClass;
import de.christianbernstein.bernie.shared.reflection.JavaReflectiveAnnotationAPI;
import lombok.experimental.UtilityClass;

/**
 * @author Christian Bernstein
 */
@UtilityClass
public class CommandClassJPAProcessor {

    @JavaReflectiveAnnotationAPI.JRP(type = CommandClass.class, phases = Constants.commandClassJRAPhase)
    public final JavaReflectiveAnnotationAPI.Processors.IAnnotationAtClassProcessor commandClassJPAProcessor = (annotation, at, meta, instance) -> {
        final CommandClass command = (CommandClass) annotation;
        for (final String gInstance : command.gloriaInstances()){
            Console.requests.add(ConsoleCommandRegisterRequest.builder()
                    .autoInstanceInvoking(command.autoInstanceInvoking())
                    .target(at)
                    .gloriaInstance(gInstance)
                    .build());
        }
    };
}

package resource;

import de.christianbernstein.bernie.ses.bin.Constants;
import de.christianbernstein.bernie.ses.bin.TonConfiguration;
import de.christianbernstein.bernie.ses.bin.TonMode;
import de.christianbernstein.bernie.shared.db.H2RepositoryConfiguration;
import de.christianbernstein.bernie.shared.db.HBM2DDLMode;
import de.christianbernstein.bernie.shared.misc.Resource;
import resource.models.Student;

/**
 * @author Christian Bernstein
 */
public class Main {

    public static void main(String[] args) {
        // new Resource<Student>("C://dev/test_student.yaml")
        //         .init(true, Student::new)
        //         .enableAutoFileUpdate()
        //         .$(res -> System.out.println(res.load(Resource.LoadTrigger.UNDEFINED)))
        //         .$(Resource::stop);

        new Resource<TonConfiguration>("C://dev/conf.yaml")
                .init(true, () -> TonConfiguration.builder()
                        .workingDirectory("hello world!")
                        .internalDatabaseConfiguration(H2RepositoryConfiguration.builder()
                                .hbm2DDLMode(HBM2DDLMode.UPDATE)
                                .databaseDir("./db/")
                                .database("ton")
                                .username("root")
                                .password("root")
                                .build())
                        .jraPhaseOrder(new String[][]{
                                {Constants.constructJRAPhase},
                                {Constants.useTonJRAPhase},
                                {Constants.threadedJRAPhase},
                                {Constants.registerEventClassJRAPhase},
                                {Constants.moduleJRAPhase},
                                {Constants.flowJRAPhase},
                                {Constants.cdnJRAPhase},
                                {Constants.autoEcexJRAPhase}
                        })
                        .mode(TonMode.DEBUG)
                        .tonEngineID("hello world")
                        .build())
                .registerOnLoad(System.out::println)
                .enableAutoFileUpdate()
                .$(res -> System.out.println(res.load(Resource.LoadTrigger.UNDEFINED)));
    }
}

package de.christianbernstein.bernie.modules.net;

import de.christianbernstein.bernie.shared.misc.FileLocation;
import de.christianbernstein.bernie.shared.misc.FileLocationType;
import lombok.*;

/**
 * @author Christian Bernstein
 */
@SuppressWarnings("SpellCheckingInspection")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetModuleConfigShard {

    @Builder.Default
    private boolean syncOpen = true;

    @Builder.Default
    private boolean syncClose = true;

    @Builder.Default
    private boolean ssl = false;

    @Builder.Default
    private String sslCertificateDir = "{root_dir}/ssl/";

    @Builder.Default
    private String sslProviderType = "letsencrypt";

    /**
     * "The Online Certificate Status Protocol stapling,
     *  formally known as the TLS Certificate Status Request extension,
     *  is a standard for checking the revocation status of X.509 digital certificates."
     *
     * extracted from: https://en.wikipedia.org/wiki/OCSP_stapling
     */
    @Builder.Default
    private String sslContext = "TLS";

    @Builder.Default
    private String sslRelativeCertificatePath = "cert.pem";

    @Builder.Default
    private String sslRelativePrivateKeyPath = "privkey.pem";

    @Builder.Default
    private String sslKeyStore = "JKS";

    @Builder.Default
    private String sslKeyManagerFactory = "SunX509";

    @Builder.Default
    private String sslKeyFactory = "RSA";

    @Builder.Default
    private String sslCertificateFactory = "X.509";

    @Builder.Default
    private FileLocation sslPrivateKeyFile = FileLocation.of("privkey.pem", FileLocationType.FS_RELATIVE);
}

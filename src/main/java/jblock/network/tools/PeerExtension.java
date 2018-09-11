package jblock.network.tools;

import akka.actor.*;

/**
 * @author daiyongbing
 * @since 2018-09-05
 */
public class PeerExtension extends AbstractExtensionId<PeerExtensionImpl> implements ExtensionIdProvider {
    //This will be the identifier of our CountExtension
    public final static PeerExtension CountExtensionProvider = new PeerExtension();
    public PeerExtension() {}

    //The lookup method is required by ExtensionIdProvider,
    // so we return ourselves here, this allows us
    // to configure our extension to be loaded when
    // the ActorSystem starts up
    @Override
    public PeerExtension lookup() {
        return PeerExtension.CountExtensionProvider; //The public static final
    }

    //This method will be called by Akka
    // to instantiate our Extension
    @Override
    public PeerExtensionImpl createExtension(ExtendedActorSystem system) {
        return new PeerExtensionImpl();
    }

    /**
     * Java API: retrieve the Count extension for the given system.
     */
    @Override
    public PeerExtensionImpl get(ActorSystem system){
        return super.get(system);
    }
}

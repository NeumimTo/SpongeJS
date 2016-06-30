package cz.neumimto.spongejs;

/**
 * Created by NeumimTo on 30.6.16.
 */
public class ListenerNotCreatedException extends RuntimeException {
    public ListenerNotCreatedException() {
        super("From some reasons dynamic listener could not have been created, check your scripts there might be some compile erros.");
    }
}

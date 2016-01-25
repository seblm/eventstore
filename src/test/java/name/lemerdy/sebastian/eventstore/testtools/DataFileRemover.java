package name.lemerdy.sebastian.eventstore.testtools;

import org.junit.rules.ExternalResource;

import java.io.File;

public class DataFileRemover extends ExternalResource {

    @Override
    protected void after() {
        File file = new File(".eventstore");
        if (!file.exists()) {
            return;
        }
        if (!file.delete()) {
            throw new IllegalStateException("unable to delete data file");
        }
    }

}

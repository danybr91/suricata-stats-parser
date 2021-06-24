package tools.statplotter.parser;

import java.io.Closeable;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;

public interface FileParser extends Closeable {
    Collection<StatTimeSerie> readStats() throws IOException, ParseException;
    void reset() throws IOException;
}

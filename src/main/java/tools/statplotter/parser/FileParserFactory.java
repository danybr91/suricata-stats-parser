package tools.statplotter.parser;

import java.io.InputStream;

public final class FileParserFactory {
    public static FileParser getParser(InputStream stream, String fileType) throws IllegalArgumentException{
        if ("log".equals(fileType)) {
            return new StatFileParser(stream);
        }
        throw new IllegalArgumentException("Solo se acepta ficheros json o log.");
    }
}
